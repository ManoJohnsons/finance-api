package io.github.manojohnsons.financeapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.service.impl.TokenServiceImpl;
import io.jsonwebtoken.Jwts;

public class TokenServiceImplTest {

    private TokenService tokenService;

    private final String jwtSecret = "minha-longa-e-segura-chave-secreta-my-long-and-secure-secret-key";

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl(jwtSecret, 2L, "Finance-API", "-03:00");
    }

    @Test
    @DisplayName("Should generate a valid JWT token for the user")
    void shouldGenerateValidJwtTokenForUser() {
        // Arrange (Organizar)
        var user = new User("Fulana Silva", "fulana32@email.com", "senha123");

        // Act (Agir)
        String token = tokenService.generateToken(user);

        // Assert (Verificar)
        assertThat(token).isNotNull().isNotBlank();

        // An advanced assertion (Uma verificação avançada)
        // Decode the token to ensure that the 'subject' is correct.
        // Decodificando o token para garantir que o 'subject' está correto.
        SecretKey keyFromService = (SecretKey) ReflectionTestUtils.getField(tokenService, "secretKey");

        var claims = Jwts.parser()
                .verifyWith(keyFromService)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(user.getEmail());
    }
}
