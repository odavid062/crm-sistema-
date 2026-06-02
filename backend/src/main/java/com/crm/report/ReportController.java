package com.crm.report;

import com.crm.contact.ContactRepository;
import com.crm.contact.ContactStatus;
import com.crm.deal.DealRepository;
import com.crm.deal.DealStatus;
import com.crm.payment.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Dashboard e relatórios do CRM")
public class ReportController {

    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Dados do dashboard principal")
    public ResponseEntity<Map<String, Object>> dashboard() {
        long totalContacts = contactRepository.count();
        long leads = contactRepository.countByStatus(ContactStatus.LEAD);
        long customers = contactRepository.countByStatus(ContactStatus.CUSTOMER);
        long openDeals = dealRepository.countByStatus(DealStatus.OPEN);
        long wonDeals = dealRepository.countByStatus(DealStatus.WON);
        long lostDeals = dealRepository.countByStatus(DealStatus.LOST);
        BigDecimal wonRevenue = dealRepository.sumValueByStatus(DealStatus.WON);
        BigDecimal openRevenue = dealRepository.sumValueByStatus(DealStatus.OPEN);
        BigDecimal receivedPayments = paymentRepository.sumByStatus("RECEIVED");
        BigDecimal pendingPayments = paymentRepository.sumByStatus("PENDING");

        return ResponseEntity.ok(Map.of(
                "contacts", Map.of(
                        "total", totalContacts,
                        "leads", leads,
                        "customers", customers
                ),
                "deals", Map.of(
                        "open", openDeals,
                        "won", wonDeals,
                        "lost", lostDeals,
                        "wonRevenue", wonRevenue != null ? wonRevenue : BigDecimal.ZERO,
                        "openRevenue", openRevenue != null ? openRevenue : BigDecimal.ZERO
                ),
                "payments", Map.of(
                        "received", receivedPayments != null ? receivedPayments : BigDecimal.ZERO,
                        "pending", pendingPayments != null ? pendingPayments : BigDecimal.ZERO
                )
        ));
    }

    @GetMapping("/funnel")
    @Operation(summary = "Dados do funil de conversão")
    public ResponseEntity<Map<String, Object>> funnel() {
        return ResponseEntity.ok(Map.of(
                "lead", contactRepository.countByStatus(ContactStatus.LEAD),
                "prospect", contactRepository.countByStatus(ContactStatus.PROSPECT),
                "customer", contactRepository.countByStatus(ContactStatus.CUSTOMER)
        ));
    }
}
