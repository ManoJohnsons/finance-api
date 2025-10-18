package io.github.manojohnsons.financeapi.service.impl;

import org.springframework.stereotype.Service;

import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    // private final JavaMailSender emailSender;

    @Override
    public void sendSummaryEmail(User user, DashboardResponseDTO summary) {
        System.out.println("Simulando envio de e-mail para: " + user.getEmail());
    }
}
