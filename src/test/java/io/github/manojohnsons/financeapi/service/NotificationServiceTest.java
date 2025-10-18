package io.github.manojohnsons.financeapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Should search all users and send an summary e-mail for each one")
    void shouldSendSummaryEmailToAllUsers() {
        // Arrange (Organizar)
        var user1 = new User("Usuário Um", "one@email.com", "senha123");
        ReflectionTestUtils.setField(user1, "id", 1L);
        var user2 = new User("Usuário Dois", "two@email.com", "senha123");
        ReflectionTestUtils.setField(user2, "id", 2L);
        var userList = List.of(user1, user2);
        var dummyDashboardDTO = new DashboardResponseDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(userRepository.findAll()).thenReturn(userList);

        when(dashboardService.generateMonthlySummary(anyLong(), anyInt(), anyInt())).thenReturn(dummyDashboardDTO);

        // Act (Agir)
        notificationService.sendMonthlySummary();

        // Assert (Verificar)
        verify(userRepository, times(1)).findAll();

        verify(dashboardService, times(2)).generateMonthlySummary(anyLong(), anyInt(), anyInt());

        verify(emailService, times(2)).sendSummaryEmail(any(User.class), any(DashboardResponseDTO.class));
    }

    @Test
    @DisplayName("Should do nothing when there are no registered users")
    void shouldDoNothingWhenNoUsersExist() {
        // Arrange (Organizar)
        when(userRepository.findAll()).thenReturn(List.of());

        // Act (Agir)
        notificationService.sendMonthlySummary();

        // Assert (Verificar)
        verify(userRepository, times(1)).findAll();

        verify(dashboardService, never()).generateMonthlySummary(anyLong(), anyInt(), anyInt());
        verify(emailService, never()).sendSummaryEmail(any(User.class), any(DashboardResponseDTO.class));
    }

    @Test
    @DisplayName("Should continue processing anothers users even if one summary fails to generate")
    void shouldContinueProcessingUserWhenDashboardServiceFailsForOne() {
        // Arrange (Organizar)
        var user1 = new User("Usuário Um", "one@email.com", "senha123");
        ReflectionTestUtils.setField(user1, "id", 1L);
        var user2 = new User("Usuário Dois", "two@email.com", "senha123");
        ReflectionTestUtils.setField(user2, "id", 2L);
        var userList = List.of(user1, user2);

        var dummyDashboardDTO = new DashboardResponseDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(userRepository.findAll()).thenReturn(userList);

        when(dashboardService.generateMonthlySummary(eq(1L), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Simulated error when trying to generate a summary."));
        when(dashboardService.generateMonthlySummary(eq(2L), anyInt(), anyInt()))
                .thenReturn(dummyDashboardDTO);

        // Act
        notificationService.sendMonthlySummary();

        // Assert
        verify(emailService, times(1)).sendSummaryEmail(user2, dummyDashboardDTO);
        verify(emailService, never()).sendSummaryEmail(eq(user1), any(DashboardResponseDTO.class));
        verify(dashboardService, times(2)).generateMonthlySummary(anyLong(), anyInt(), anyInt());
    }
}
