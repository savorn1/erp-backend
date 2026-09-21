package com.example.erp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// One-time data migration for the Lead/Opportunity merge into a single
// pipeline (NEW..QUALIFIED..NEEDS_ANALYSIS..QUOTATION..NEGOTIATION..WON/LOST).
// Runs once at startup, after Hibernate's own ddl-auto=update has already
// added Lead's new deal_name/amount/probability/expected_close_date/
// customer_id/closed_at columns and Quotation's new lead_id column (schema
// update happens during EntityManagerFactory startup, strictly before any
// ApplicationRunner bean executes — no extra ordering needed for that part).
//
// Uses raw JDBC, never JPA/Hibernate: once LeadStatus's enum values changed,
// any @Enumerated(STRING) read of a Lead row still holding an old string
// (CONTACTED/PROPOSAL/CONVERTED) throws immediately — so LeadRepository
// can't even be used to read the very rows this migration needs to fix.
// Guarded by a marker row so it only ever runs once.
@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
public class LeadOpportunityMergeMigration implements ApplicationRunner {

    private static final String MARKER = "merge_lead_opportunity_v1";

    private final JdbcTemplate jdbc;

    private final Map<Long, Long> opportunityIdToLeadId = new HashMap<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS schema_migrations (name VARCHAR(255) PRIMARY KEY, executed_at TIMESTAMP)");
        Integer already = jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations WHERE name = ?", Integer.class, MARKER);
        if (already != null && already > 0) {
            return;
        }

        // Hibernate auto-generates a CHECK constraint for @Enumerated(STRING)
        // columns at table-creation time (Hibernate 6+), reflecting whatever
        // enum values existed back then. ddl-auto=update never touches an
        // already-existing column's constraints, so the old LeadStatus/
        // LeadActivityType value lists are still enforced at the DB level
        // and reject every new value below unless dropped first. The ORM
        // layer already fully validates these (only a real Java enum
        // constant can ever be assigned), so losing this DB-level check is
        // pure defense-in-depth, not a real validation gap.
        jdbc.execute("ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_status_check");
        jdbc.execute("ALTER TABLE lead_activities DROP CONSTRAINT IF EXISTS lead_activities_type_check");

        if (tableExists("opportunities")) {
            migrateOpportunities();
            migrateOpportunityActivities();
            repointQuotations();
        }
        migrateRemainingLeadStatuses();

        jdbc.update("INSERT INTO schema_migrations (name, executed_at) VALUES (?, ?)", MARKER, Timestamp.valueOf(LocalDateTime.now()));
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)", Boolean.class, tableName);
        return Boolean.TRUE.equals(exists);
    }

    private static String mapStage(String stage) {
        return switch (stage) {
            case "QUALIFICATION" -> "QUALIFIED";
            case "NEEDS_ANALYSIS" -> "NEEDS_ANALYSIS";
            case "PROPOSAL" -> "QUOTATION";
            case "NEGOTIATION" -> "NEGOTIATION";
            case "CLOSED_WON" -> "WON";
            case "CLOSED_LOST" -> "LOST";
            default -> "QUALIFIED";
        };
    }

    // For each Opportunity: merge its deal fields onto its linked Lead, or —
    // for a direct-created/upsell Opportunity with no source Lead — synthesize
    // a new Lead row from it. Either way, records the mapping so activities
    // and quotations can be repointed afterward.
    private void migrateOpportunities() {
        List<Map<String, Object>> opportunities = jdbc.queryForList("SELECT * FROM opportunities");
        for (Map<String, Object> o : opportunities) {
            Long opportunityId = ((Number) o.get("id")).longValue();
            Long leadId = o.get("lead_id") == null ? null : ((Number) o.get("lead_id")).longValue();
            String mappedStatus = mapStage((String) o.get("stage"));

            if (leadId != null) {
                jdbc.update(
                        "UPDATE leads SET status = ?, deal_name = ?, amount = ?, probability = ?, "
                                + "expected_close_date = ?, customer_id = ?, closed_at = ?, "
                                + "assigned_to_user_id = COALESCE(?, assigned_to_user_id), "
                                + "next_follow_up_date = COALESCE(?, next_follow_up_date) "
                                + "WHERE id = ?",
                        mappedStatus, o.get("name"), o.get("amount"), o.get("probability"),
                        o.get("expected_close_date"), o.get("customer_id"), o.get("closed_at"),
                        o.get("assigned_to_user_id"), o.get("next_follow_up_date"),
                        leadId);
                opportunityIdToLeadId.put(opportunityId, leadId);
            } else {
                Long newLeadId = jdbc.queryForObject(
                        "INSERT INTO leads (company_id, contact_name, source, status, deal_name, amount, probability, "
                                + "expected_close_date, customer_id, closed_at, assigned_to_user_id, next_follow_up_date, created_by) "
                                + "VALUES (?, ?, 'OTHER', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                        Long.class,
                        o.get("company_id"), o.get("name"), mappedStatus, o.get("name"), o.get("amount"), o.get("probability"),
                        o.get("expected_close_date"), o.get("customer_id"), o.get("closed_at"),
                        o.get("assigned_to_user_id"), o.get("next_follow_up_date"), o.get("created_by"));
                opportunityIdToLeadId.put(opportunityId, newLeadId);
            }
        }
    }

    private void migrateOpportunityActivities() {
        if (opportunityIdToLeadId.isEmpty() || !tableExists("opportunity_activities")) {
            return;
        }
        List<Map<String, Object>> activities = jdbc.queryForList("SELECT * FROM opportunity_activities");
        for (Map<String, Object> a : activities) {
            Long opportunityId = ((Number) a.get("opportunity_id")).longValue();
            Long leadId = opportunityIdToLeadId.get(opportunityId);
            if (leadId == null) {
                continue;
            }
            String type = "STAGE_CHANGE".equals(a.get("type")) ? "STATUS_CHANGE" : (String) a.get("type");
            jdbc.update(
                    "INSERT INTO lead_activities (lead_id, type, description, created_by, created_at) VALUES (?, ?, ?, ?, ?)",
                    leadId, type, a.get("description"), a.get("created_by"), a.get("created_at"));
        }
    }

    private void repointQuotations() {
        if (opportunityIdToLeadId.isEmpty()) {
            return;
        }
        List<Map<String, Object>> quotations = jdbc.queryForList(
                "SELECT id, opportunity_id FROM quotations WHERE opportunity_id IS NOT NULL");
        for (Map<String, Object> q : quotations) {
            Long opportunityId = ((Number) q.get("opportunity_id")).longValue();
            Long leadId = opportunityIdToLeadId.get(opportunityId);
            if (leadId == null) {
                continue;
            }
            jdbc.update("UPDATE quotations SET lead_id = ? WHERE id = ?", leadId, q.get("id"));
        }
    }

    // Catches leads whose old-style status was never overwritten by a linked
    // Opportunity above — e.g. a lead still sitting at CONTACTED/PROPOSAL, or
    // (defensively) a CONVERTED lead whose opportunity row is somehow missing.
    private void migrateRemainingLeadStatuses() {
        jdbc.update("UPDATE leads SET status = 'NEW' WHERE status = 'CONTACTED'");
        jdbc.update("UPDATE leads SET status = 'QUOTATION' WHERE status = 'PROPOSAL'");
        jdbc.update("UPDATE leads SET status = 'QUALIFIED' WHERE status = 'CONVERTED'");
    }
}
