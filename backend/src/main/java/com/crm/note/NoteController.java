package com.crm.note;

import com.crm.company.CompanyRepository;
import com.crm.contact.ContactRepository;
import com.crm.deal.DealRepository;
import com.crm.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Notas", description = "Notas de contatos, empresas e deals")
public class NoteController {

    private final NoteRepository noteRepository;
    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/contact/{id}")
    @Operation(summary = "Notas de um contato")
    public ResponseEntity<List<Note>> byContact(@PathVariable UUID id) {
        return ResponseEntity.ok(noteRepository.findByContactIdOrderByCreatedAtDesc(id));
    }

    @GetMapping("/deal/{id}")
    @Operation(summary = "Notas de um deal")
    public ResponseEntity<List<Note>> byDeal(@PathVariable UUID id) {
        return ResponseEntity.ok(noteRepository.findByDealIdOrderByCreatedAtDesc(id));
    }

    @PostMapping
    @Operation(summary = "Criar nota")
    public ResponseEntity<Note> create(@RequestBody Map<String, String> body) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Note note = Note.builder()
                .content(body.get("content"))
                .createdBy(user)
                .build();
        if (body.containsKey("contactId")) {
            contactRepository.findById(UUID.fromString(body.get("contactId"))).ifPresent(note::setContact);
        }
        if (body.containsKey("dealId")) {
            dealRepository.findById(UUID.fromString(body.get("dealId"))).ifPresent(note::setDeal);
        }
        if (body.containsKey("companyId")) {
            companyRepository.findById(UUID.fromString(body.get("companyId"))).ifPresent(note::setCompany);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(noteRepository.save(note));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir nota")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        noteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
