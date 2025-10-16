package io.github.manojohnsons.financeapi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manojohnsons.financeapi.application.dto.LoginRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.LoginResponseDTO;
import io.github.manojohnsons.financeapi.application.dto.UserRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.UserResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public UserResponseDTO register(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email()))
            throw new EmailAlreadyExistsException("The specified e-mail already exists.");

        var encryptedPassword = passwordEncoder.encode(dto.password());
        var newUser = new User(dto.name(), dto.email(), encryptedPassword);
        var savedUser = userRepository.save(newUser);

        return UserResponseDTO.fromEntity(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        return new LoginResponseDTO(token);
    }
}
