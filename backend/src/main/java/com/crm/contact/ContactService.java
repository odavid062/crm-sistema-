package com.crm.contact;

import com.crm.company.CompanyRepository;
import com.crm.contact.dto.ContactRequest;
import com.crm.contact.dto.ContactResponse;
import com.crm.event.CrmEvent;
import com.crm.tag.Tag;
import com.crm.tag.TagRepository;
import com.crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher events;

    public Page<ContactResponse> search(String search, ContactStatus status, UUID ownerId, Pageable pageable) {
        return contactRepository.search(search, status, ownerId, pageable).map(this::toResponse);
    }

    public ContactResponse findById(UUID id) {
        return toResponse(contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contato não encontrado")));
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        Contact contact = Contact.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .whatsapp(request.whatsapp())
                .cpf(request.cpf())
                .birthDate(request.birthDate())
                .position(request.position())
                .department(request.department())
                .status(request.status() != null ? request.status() : ContactStatus.LEAD)
                .source(request.source())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .country(request.country() != null ? request.country() : "Brasil")
                .notes(request.notes())
                .avatarUrl(request.avatarUrl())
                .build();

        if (request.companyId() != null) {
            contact.setCompany(companyRepository.findById(request.companyId()).orElse(null));
        }
        if (request.ownerId() != null) {
            contact.setOwner(userRepository.findById(request.ownerId()).orElse(null));
        }
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.tagIds()));
            contact.setTags(tags);
        }
        Contact saved = contactRepository.save(contact);
        ContactResponse response = toResponse(saved);
        events.publishEvent(new CrmEvent("contact.created", saved.getTenantId(), response));
        return response;
    }

    @Transactional
    public ContactResponse update(UUID id, ContactRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
        ContactStatus oldStatus = contact.getStatus();
        if (request.name() != null) contact.setName(request.name());
        if (request.email() != null) contact.setEmail(request.email());
        if (request.phone() != null) contact.setPhone(request.phone());
        if (request.whatsapp() != null) contact.setWhatsapp(request.whatsapp());
        if (request.status() != null) contact.setStatus(request.status());
        if (request.source() != null) contact.setSource(request.source());
        if (request.notes() != null) contact.setNotes(request.notes());
        if (request.companyId() != null) {
            contact.setCompany(companyRepository.findById(request.companyId()).orElse(null));
        }
        if (request.ownerId() != null) {
            contact.setOwner(userRepository.findById(request.ownerId()).orElse(null));
        }
        if (request.tagIds() != null) {
            contact.setTags(new HashSet<>(tagRepository.findAllById(request.tagIds())));
        }
        Contact saved = contactRepository.save(contact);
        ContactResponse response = toResponse(saved);
        events.publishEvent(new CrmEvent("contact.updated", saved.getTenantId(), response));
        if (request.status() != null && oldStatus != saved.getStatus()) {
            events.publishEvent(new CrmEvent("contact.status_changed", saved.getTenantId(),
                    java.util.Map.of("contactId", saved.getId(), "from", oldStatus.name(), "to", saved.getStatus().name(), "contact", response)));
        }
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        contactRepository.deleteById(id);
    }

    public ContactResponse toResponse(Contact c) {
        Set<ContactResponse.TagDto> tags = c.getTags().stream()
                .map(t -> new ContactResponse.TagDto(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());
        return new ContactResponse(
                c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getWhatsapp(),
                c.getCpf(), c.getBirthDate(), c.getPosition(), c.getDepartment(), c.getStatus(),
                c.getSource(), c.getAddress(), c.getCity(), c.getState(), c.getCountry(),
                c.getNotes(), c.getAvatarUrl(),
                c.getCompany() != null ? c.getCompany().getId() : null,
                c.getCompany() != null ? c.getCompany().getName() : null,
                c.getOwner() != null ? c.getOwner().getId() : null,
                c.getOwner() != null ? c.getOwner().getName() : null,
                tags, c.getCreatedAt()
        );
    }
}
