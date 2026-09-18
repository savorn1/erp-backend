package com.example.erp.repository;

import com.example.erp.entity.Ticket;
import com.example.erp.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByStatusIn(List<TicketStatus> statuses);
}
