package io.github.manojohnsons.financeapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manojohnsons.financeapi.application.dto.TransactionRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.TransactionResponseDTO;
import io.github.manojohnsons.financeapi.application.dto.TransactionUpdateRequestDTO;
import io.github.manojohnsons.financeapi.domain.model.Category;
import io.github.manojohnsons.financeapi.domain.model.Transaction;
import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO dto, Long userId) {
        var user = findUserById(userId);
        var category = findCategoryIfPresent(dto.categoryId(), userId);

        var newTransaction = new Transaction(
                dto.description(),
                dto.amount(),
                dto.date(),
                dto.type(),
                user,
                category);

        var savedTransaction = transactionRepository.save(newTransaction);

        return TransactionResponseDTO.fromEntity(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> findAllByUserIdAndDate(Long userId, int year, int month) {
        var transactions = transactionRepository.findByUserIdAndYearAndMonth(userId, year, month);

        return transactions.stream()
                .map(TransactionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO findById(Long transactionId, Long userId) {
        var transaction = findTransactionByIdAndUser(transactionId, userId);

        return TransactionResponseDTO.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponseDTO update(Long transactionId, TransactionUpdateRequestDTO dto, Long userId) {
        var transaction = findTransactionByIdAndUser(transactionId, userId);
        var category = findCategoryIfPresent(dto.categoryId(), userId);

        transaction.setDescription(dto.description());
        transaction.setAmount(dto.amount());
        transaction.setDate(dto.date());
        transaction.changeCategory(category);

        return TransactionResponseDTO.fromEntity(transaction);
    }

    @Transactional
    public void delete(Long transactionId, Long userId) {
        var transaction = findTransactionByIdAndUser(transactionId, userId);

        transactionRepository.delete(transaction);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource User not found."));
    }

    private Category findCategoryIfPresent(Long categoryId, Long userId) {
        if (categoryId == null)
            return null;

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource Category not found."));
    }

    private Transaction findTransactionByIdAndUser(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource Transaction not found."));
    }
}
