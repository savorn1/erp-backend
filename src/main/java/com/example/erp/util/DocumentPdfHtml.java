package com.example.erp.util;

import com.example.erp.entity.Company;

import java.util.Arrays;
import java.util.stream.Collectors;

// Shared page shell for the small self-contained XHTML documents rendered
// to PDF for emailing an Invoice/Quotation/PurchaseOrder (see
// PdfRenderService). Each caller only builds its own line-items table and
// totals block — everything else (company header, css, party block) is
// identical across the three document types.
public final class DocumentPdfHtml {

    private static final String CSS = """
            body { font-family: Helvetica, Arial, sans-serif; font-size: 12px; color: #1f2937; margin: 32px; }
            .header { display: flex; justify-content: space-between; margin-bottom: 24px; }
            .company-name { font-size: 18px; font-weight: bold; }
            .company-address { color: #6b7280; margin-top: 4px; }
            .doc-title { font-size: 20px; font-weight: bold; text-align: right; }
            .meta { text-align: right; color: #6b7280; margin-top: 4px; }
            .party-label { font-size: 11px; text-transform: uppercase; color: #6b7280; margin-top: 16px; margin-bottom: 4px; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; }
            th, td { border-bottom: 1px solid #e5e7eb; padding: 6px 8px; text-align: left; font-size: 11px; }
            th { background: #f9fafb; text-transform: uppercase; color: #6b7280; }
            .num { text-align: right; }
            .totals { width: 260px; margin-left: auto; margin-top: 16px; }
            .totals div { display: flex; justify-content: space-between; padding: 3px 0; }
            .totals .grand { font-weight: bold; border-top: 1px solid #1f2937; padding-top: 6px; margin-top: 6px; }
            """;

    private DocumentPdfHtml() {
    }

    public static String render(Company company, String documentTitle, String documentNumber, String metaHtml,
                                 String partyLabel, String partyHtml, String linesTableHtml, String totalsHtml) {
        String companyAddress = joinNonBlank(", ", company.getAddressLine1(), company.getAddressLine2(),
                company.getCity(), company.getState(), company.getPostalCode(), company.getCountry());

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>").append(CSS).append("</style></head><body>");
        html.append("<div class=\"header\">");
        html.append("<div><div class=\"company-name\">").append(escape(company.getName())).append("</div>");
        html.append("<div class=\"company-address\">").append(escape(companyAddress)).append("</div></div>");
        html.append("<div><div class=\"doc-title\">").append(escape(documentTitle)).append("</div>");
        html.append("<div class=\"meta\">").append(escape(documentNumber)).append("<br/>").append(metaHtml).append("</div></div>");
        html.append("</div>");
        html.append("<div class=\"party-label\">").append(escape(partyLabel)).append("</div>");
        html.append("<div>").append(partyHtml).append("</div>");
        html.append(linesTableHtml);
        html.append(totalsHtml);
        html.append("</body></html>");
        return html.toString();
    }

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String joinNonBlank(String separator, String... parts) {
        return Arrays.stream(parts).filter(p -> p != null && !p.isBlank()).collect(Collectors.joining(separator));
    }
}
