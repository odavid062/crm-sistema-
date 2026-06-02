package com.crm.contact;

import com.crm.contact.dto.ContactRequest;
import com.crm.contact.dto.ContactResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Tag(name = "Contatos", description = "Gerenciamento de contatos e leads")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    @Operation(summary = "Listar contatos com filtros e paginação")
    public ResponseEntity<Page<ContactResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(required = false) UUID ownerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(contactService.search(search, status, ownerId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar contato por ID")
    public ResponseEntity<ContactResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contactService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar contato")
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar contato")
    public ResponseEntity<ContactResponse> update(@PathVariable UUID id, @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir contato")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
