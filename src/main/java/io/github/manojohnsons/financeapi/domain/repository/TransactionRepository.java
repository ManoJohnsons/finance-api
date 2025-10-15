package io.github.manojohnsons.financeapi.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.manojohnsons.financeapi.domain.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Search all transactions from a specific user in a given month and year.
     * Uses a custom JPQL query to filter by date.
     * 
     * @param userId The user ID.
     * @param year   The desired year.
     * @param month  The desired month (1 to 12).
     * @return A list of transactions that matches the criteria.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year,
            @Param("month") int month);

    /**
     * Search all transactions associated with a specific category.
     * Used in the business logic to disassociate transactions before deleting a
     * category.
     * 
     * @param categoryId The category ID.
     * @return A list of transactions
     */
    List<Transaction> findByCategoryId(Long categoryId);
}
