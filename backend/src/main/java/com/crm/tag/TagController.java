package com.crm.tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Gerenciamento de tags")
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    @Operation(summary = "Listar todas as tags")
    public ResponseEntity<List<com.crm.tag.Tag>> findAll(@RequestParam(required = false) String search) {
        if (search != null) return ResponseEntity.ok(tagRepository.findByNameContainingIgnoreCase(search));
        return ResponseEntity.ok(tagRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar tag")
    public ResponseEntity<com.crm.tag.Tag> create(@RequestBody com.crm.tag.Tag tag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagRepository.save(tag));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tag")
    public ResponseEntity<com.crm.tag.Tag> update(@PathVariable UUID id, @RequestBody com.crm.tag.Tag updated) {
        return tagRepository.findById(id).map(tag -> {
            tag.setName(updated.getName());
            tag.setColor(updated.getColor());
            return ResponseEntity.ok(tagRepository.save(tag));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tag")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
