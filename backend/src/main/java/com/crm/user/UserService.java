package com.crm.user;

import com.crm.user.dto.UpdateUserRequest;
import com.crm.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.role() != null) user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getAvatarUrl(), user.getPhone(), user.isActive(), user.getCreatedAt()
        );
    }
}
