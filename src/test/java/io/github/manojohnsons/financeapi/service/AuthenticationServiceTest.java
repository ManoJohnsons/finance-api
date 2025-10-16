package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.LoginRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.LoginResponseDTO;
import io.github.manojohnsons.financeapi.application.dto.UserRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.UserResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.EmailAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    TokenService tokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange (Organizar)
        var requestDTO = UserRequestDTO.builder()
                .name("Fulana Santos")
                .email("fulana32@email.com")
                .password("senha123")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encrypted_password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(userToSave, "id", 1L);
            return userToSave;
        });

        // Act (Agir)
        UserResponseDTO response = authenticationService.register(requestDTO);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(requestDTO.name());
        assertThat(response.email()).isEqualTo(requestDTO.email());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange (Organizar)
        var requestDTO = UserRequestDTO.builder()
                .name("Ciclano Souza")
                .email("ciclano0706@email.com")
                .password("senha123")
                .build();

        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        // Act & Assert (Agir e Verificar)
        var exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            authenticationService.register(requestDTO);
        });
        assertThat(exception.getMessage()).isEqualTo("The specified e-mail already exists.");
    }

    @Test
    @DisplayName("Should login successfully and return a JWT token")
    void shouldLoginSuccessfullyAndReturnToken() {
        // Arrange (Organizar)
        var loginRequest = new LoginRequestDTO("fulana32@email.com", "senha123");
        var user = new User("Fulana Santos", "fulana32@email.com", "encrypted_password");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        when(tokenService.generateToken(any(User.class))).thenReturn("mock.jwt.token");

        // Act (Agir)
        LoginResponseDTO response = authenticationService.login(loginRequest);

        // Assert (Verificar)
        assertThat(response).isNotNull();
        assertThat(response.tokenJwt()).isEqualTo("mock.jwt.token");
    }
}
