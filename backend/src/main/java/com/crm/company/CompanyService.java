package com.crm.company;

import com.crm.company.dto.CompanyRequest;
import com.crm.company.dto.CompanyResponse;
import com.crm.tag.Tag;
import com.crm.tag.TagRepository;
import com.crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public Page<CompanyResponse> search(String search, String industry, Pageable pageable) {
        return companyRepository.search(search, industry, pageable).map(this::toResponse);
    }

    public CompanyResponse findById(UUID id) {
        return toResponse(companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada")));
    }

    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        Company company = Company.builder()
                .name(request.name())
                .cnpj(request.cnpj())
                .email(request.email())
                .phone(request.phone())
                .website(request.website())
                .industry(request.industry())
                .size(request.size())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .country(request.country() != null ? request.country() : "Brasil")
                .notes(request.notes())
                .build();
        if (request.ownerId() != null) {
            company.setOwner(userRepository.findById(request.ownerId()).orElse(null));
        }
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            company.setTags(new HashSet<>(tagRepository.findAllById(request.tagIds())));
        }
        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponse update(UUID id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
        if (request.name() != null) company.setName(request.name());
        if (request.email() != null) company.setEmail(request.email());
        if (request.phone() != null) company.setPhone(request.phone());
        if (request.industry() != null) company.setIndustry(request.industry());
        if (request.notes() != null) company.setNotes(request.notes());
        if (request.tagIds() != null) {
            company.setTags(new HashSet<>(tagRepository.findAllById(request.tagIds())));
        }
        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public void delete(UUID id) {
        companyRepository.deleteById(id);
    }

    public CompanyResponse toResponse(Company c) {
        Set<CompanyResponse.TagDto> tags = c.getTags().stream()
                .map(t -> new CompanyResponse.TagDto(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());
        return new CompanyResponse(
                c.getId(), c.getName(), c.getCnpj(), c.getEmail(), c.getPhone(),
                c.getWebsite(), c.getIndustry(), c.getSize(), c.getAddress(), c.getCity(),
                c.getState(), c.getCountry(), c.getNotes(),
                c.getOwner() != null ? c.getOwner().getId() : null,
                c.getOwner() != null ? c.getOwner().getName() : null,
                tags, c.getCreatedAt()
        );
    }
}
