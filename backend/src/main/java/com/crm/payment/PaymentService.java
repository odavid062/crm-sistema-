package com.crm.payment;

import com.crm.contact.Contact;
import com.crm.contact.ContactRepository;
import com.crm.deal.DealRepository;
import com.crm.event.CrmEvent;
import com.crm.payment.dto.CreatePaymentRequest;
import com.crm.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final AsaasClient asaasClient;
    private final ApplicationEventPublisher events;

    public Page<Payment> findAll(String status, UUID contactId, Pageable pageable) {
        if (contactId != null) return paymentRepository.findByContactIdOrderByCreatedAtDesc(contactId, pageable);
        if (status != null) return paymentRepository.findByStatusOrderByDueDateAsc(status, pageable);
        return paymentRepository.findAll(pageable);
    }

    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Contact contact = contactRepository.findById(request.contactId())
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));

        String asaasCustomerId = request.asaasCustomerId();
        if (asaasCustomerId == null) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("name", contact.getName());
            customerData.put("email", contact.getEmail());
            customerData.put("phone", contact.getPhone());
            if (contact.getCpf() != null) customerData.put("cpfCnpj", contact.getCpf().replaceAll("[^0-9]", ""));
            Map<String, Object> customerResult = asaasClient.createCustomer(customerData);
            if (customerResult != null) asaasCustomerId = (String) customerResult.get("id");
        }

        Map<String, Object> chargeData = new HashMap<>();
        chargeData.put("customer", asaasCustomerId);
        chargeData.put("billingType", request.billingType());
        chargeData.put("value", request.value());
        chargeData.put("dueDate", request.dueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        chargeData.put("description", request.description());
        if (request.externalReference() != null) chargeData.put("externalReference", request.externalReference());

        Map<String, Object> result = asaasClient.createCharge(chargeData);

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Payment payment = Payment.builder()
                .contact(contact)
                .description(request.description())
                .value(request.value())
                .billingType(request.billingType())
                .dueDate(request.dueDate())
                .externalReference(request.externalReference())
                .createdBy(currentUser)
                .build();

        if (result != null) {
            payment.setAsaasId((String) result.get("id"));
            payment.setStatus((String) result.get("status"));
            payment.setInvoiceUrl((String) result.get("invoiceUrl"));
            payment.setBankSlipUrl((String) result.get("bankSlipUrl"));
            payment.setNossoNumero((String) result.get("nossoNumero"));
            payment.setInvoiceNumber((String) result.get("invoiceNumber"));
        }

        if (request.dealId() != null) {
            dealRepository.findById(request.dealId()).ifPresent(payment::setDeal);
        }

        Payment saved = paymentRepository.save(payment);
        events.publishEvent(new CrmEvent("payment.created", saved.getTenantId(), saved));
        return saved;
    }

    @Transactional
    public Payment processWebhook(Map<String, Object> payload) {
        Map<String, Object> paymentData = (Map<String, Object>) payload.get("payment");
        if (paymentData == null) return null;

        String asaasId = (String) paymentData.get("id");
        return paymentRepository.findByAsaasId(asaasId).map(payment -> {
            String oldStatus = payment.getStatus();
            String newStatus = (String) paymentData.get("status");
            payment.setStatus(newStatus);
            String paymentDateStr = (String) paymentData.get("paymentDate");
            if (paymentDateStr != null) {
                payment.setPaymentDate(java.time.LocalDate.parse(paymentDateStr));
            }
            Payment saved = paymentRepository.save(payment);
            // Eventos de transição de status, com tenant da própria fatura.
            if (!java.util.Objects.equals(oldStatus, newStatus)) {
                if ("RECEIVED".equals(newStatus) || "CONFIRMED".equals(newStatus)) {
                    events.publishEvent(new CrmEvent("payment.received", saved.getTenantId(), saved));
                } else if ("OVERDUE".equals(newStatus)) {
                    events.publishEvent(new CrmEvent("payment.overdue", saved.getTenantId(), saved));
                } else if ("REFUNDED".equals(newStatus)) {
                    events.publishEvent(new CrmEvent("payment.refunded", saved.getTenantId(), saved));
                }
            }
            return saved;
        }).orElse(null);
    }

    public Map<String, Object> getPixQrCode(UUID id) {
        Payment payment = findById(id);
        if (payment.getAsaasId() == null) throw new RuntimeException("Pagamento não tem ID Asaas");
        return asaasClient.getPixQrCode(payment.getAsaasId());
    }

    @Transactional
    public void cancelPayment(UUID id) {
        Payment payment = findById(id);
        if (payment.getAsaasId() != null) {
            asaasClient.cancelPayment(payment.getAsaasId());
        }
        payment.setStatus("CANCELLED");
        paymentRepository.save(payment);
    }
}
