package com.crm.user;

import com.crm.user.dto.UpdateUserRequest;
import com.crm.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
