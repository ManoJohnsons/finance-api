package io.github.manojohnsons.financeapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.manojohnsons.financeapi.application.dto.CategoryRequestDTO;
import io.github.manojohnsons.financeapi.application.dto.CategoryResponseDTO;
import io.github.manojohnsons.financeapi.domain.model.Category;
import io.github.manojohnsons.financeapi.domain.repository.CategoryRepository;
import io.github.manojohnsons.financeapi.domain.repository.TransactionRepository;
import io.github.manojohnsons.financeapi.domain.repository.UserRepository;
import io.github.manojohnsons.financeapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource User not found."));

        var newCategory = new Category(
                dto.name(),
                dto.hexColor(),
                dto.icon(),
                user);

        if (dto.monthlyGoal() != null)
            newCategory.setMonthlyGoal(dto.monthlyGoal());

        var savedCategory = categoryRepository.save(newCategory);

        return CategoryResponseDTO.fromEntity(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAllByUserId(Long userId) {
        var categories = categoryRepository.findByUserId(userId);

        return categories.stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public CategoryResponseDTO update(Long categoryId, CategoryRequestDTO dto, Long userId) {
        var category = findCategoryByIdAndUser(categoryId, userId);

        category.setName(dto.name());
        category.setHexColor(dto.hexColor());
        category.setIcon(dto.icon());
        category.setMonthlyGoal(dto.monthlyGoal());

        return CategoryResponseDTO.fromEntity(category);
    }

    @Transactional
    public void delete(Long categoryId, Long userId) {
        var category = findCategoryByIdAndUser(categoryId, userId);
        var transactions = transactionRepository.findByCategoryId(categoryId);

        transactions.forEach(transaction -> transaction.disassociateCategory());

        transactionRepository.saveAll(transactions);

        categoryRepository.delete(category);
    }

    private Category findCategoryByIdAndUser(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource Category not found."));
    }
}
