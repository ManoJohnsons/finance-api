package io.github.manojohnsons.financeapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.BudgetCategoryDTO;
import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.service.impl.EmailServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    private final String testSubject = "Seu Resumo Financeiro Mensal - Finance API";
    private final String testSignature = "Equipe Finance API";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, testSubject, testSignature);
    }

    @Test
    @DisplayName("Should build and send the e-mail with the email correctly")
    void shouldBuildAndSendSummaryEmailCorrectly() {
        // Arrange (Organizar)
        var user = new User("Usuário de Teste", "test@email.com", "senhaTeste123");
        ReflectionTestUtils.setField(user, "id", 1L);

        var budget = new BudgetCategoryDTO("Alimentação", "#FF0000", new BigDecimal("800"), new BigDecimal("600"),
                new BigDecimal("75.00"));
        var summaryDTO = new DashboardResponseDTO(new BigDecimal("5000"), new BigDecimal("1500"),
                new BigDecimal("3500"), List.of(budget));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act (Agir)
        emailService.sendSummaryEmail(user, summaryDTO);

        // Assert (Verificar)
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly("test@email.com");
        assertThat(sentMessage.getSubject()).isEqualTo(testSubject);
        assertThat(sentMessage.getText()).contains("Olá Usuário de Teste,");

        assertThat(sentMessage.getText()).contains("Total de Receitas:");
        assertThat(sentMessage.getText()).contains("R$");
        assertThat(sentMessage.getText()).contains("5.000,00");

        assertThat(sentMessage.getText()).contains("Total de Despesas:");
        assertThat(sentMessage.getText()).contains("R$");
        assertThat(sentMessage.getText()).contains("1.500,00");

        assertThat(sentMessage.getText()).contains("Saldo Final:");
        assertThat(sentMessage.getText()).contains("R$");
        assertThat(sentMessage.getText()).contains("3.500,00");

        assertThat(sentMessage.getText()).contains("Alimentação:");
        assertThat(sentMessage.getText()).contains("Gastou");
        assertThat(sentMessage.getText()).contains("R$");
        assertThat(sentMessage.getText()).contains("600,00");
        assertThat(sentMessage.getText()).contains("de");
        assertThat(sentMessage.getText()).contains("R$");
        assertThat(sentMessage.getText()).contains("800,00");
        assertThat(sentMessage.getText()).contains("(75.00%)");
        assertThat(sentMessage.getText()).endsWith(testSignature);
    }

    @Test
    @DisplayName("Should propagate MailException when sending fails")
    void shouldPropagateMailExceptionWhenSendFails() {
        // Arrange (Organizar)
        var user = new User("Usuário de Teste", "test@email.com", "senhaTeste123");
        var summaryDTO = new DashboardResponseDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        doThrow(new MailSendException("Erro SMTP simulado")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert (Agir e Verificar)
        assertThrows(MailSendException.class, () -> {
            emailService.sendSummaryEmail(user, summaryDTO);
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
