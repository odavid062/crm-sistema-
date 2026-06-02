package com.crm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Auto-cadastro (self-signup): cria uma nova empresa (tenant) e o usuário ADMIN dela.
 */
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        String companyName
) {}
