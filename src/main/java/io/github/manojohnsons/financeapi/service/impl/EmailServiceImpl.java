package io.github.manojohnsons.financeapi.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.github.manojohnsons.financeapi.application.dto.BudgetCategoryDTO;
import io.github.manojohnsons.financeapi.application.dto.DashboardResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private final JavaMailSender mailSender;
    private final String emailSubject;
    private final String emailSignature;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${api.email.summary.subject}") String emailSubject,
            @Value("${api.email.signature}") String emailSignature) {
        this.mailSender = mailSender;
        this.emailSubject = emailSubject;
        this.emailSignature = emailSignature;
    }

    @Override
    public void sendSummaryEmail(User user, DashboardResponseDTO summary) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());

        message.setSubject(this.emailSubject);

        String text = buildEmailText(user, summary);
        message.setText(text);

        mailSender.send(message);
    }

    private String buildEmailText(User user, DashboardResponseDTO summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá ").append(user.getName()).append(",\n\n");
        sb.append("Aqui está o seu resumo financeiro do último mês:\n\n");

        sb.append("- Total de Receitas: ").append(formatCurrency(summary.totalIncome())).append("\n");
        sb.append("- Total de Despesas: ").append(formatCurrency(summary.totalExpense())).append("\n");
        sb.append("- Saldo Final: ").append(formatCurrency(summary.finalBalance())).append("\n\n");

        if (summary.budgets() != null && !summary.budgets().isEmpty()) {
            sb.append("Progresso dos seus orçamentos:\n");
            for (BudgetCategoryDTO budget : summary.budgets()) {
                sb.append("- ").append(budget.categoryName()).append(": ");
                sb.append("Gastou ").append(formatCurrency(budget.totalSpent()));
                sb.append(" de ").append(formatCurrency(budget.monthlyGoal()));
                sb.append("  (").append(budget.percentageSpent().setScale(2)).append("%)\n");
            }
        }

        sb.append("\nAtenciosamente,\n").append(this.emailSignature);
        return sb.toString();
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return formatCurrency(BigDecimal.ZERO);
        }

        return CURRENCY_FORMATTER.format(value);
    }
}
