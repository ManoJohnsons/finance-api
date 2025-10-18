package io.github.manojohnsons.financeapi.service;

import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;

public interface EmailService {
    
    void sendSummaryEmail(User user, DashboardResponseDTO summary);
}
