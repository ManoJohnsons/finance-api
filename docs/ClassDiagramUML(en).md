# UML Class Diagram

```mermaid
classDiagram
    TransactionController --|> TransactionService : use
    TransactionService --|> TransactionRepository : use
    TransactionRepository --|> Transaction : use
    class TransactionController {
        %% @RestController annotation
        - TransactionService service
        + insertTransaction(TransactionRequestDTO dto)
        + getAllTransactions(TransactionFilter filter)
        + getTransactionById(Long id)
        + updateTransaction(TransactionRequestDTO dto, Long id)
        + deleteTransactionById(Long id)
    }
    class TransactionService {
        %% @Service annotation
        - TransactionRepository repository
        + insertTransaction(TransactionRequestDTO dto)
        + getAllTransactions(TransactionFilter filter)
        + getTransactionById(Long id)
        + updateTransaction(TrasactionRequestDTO dto, Long id)
        + deleteTransactionById(Long id)
    }
    class TransactionRepository {
        <<Repository>>
        %% JPA methods will be defined here. If a custom method is required, it can be added in this interface.
    }
    class Transaction {
        %% @Entity annotation
        - Long id
        - String description
        - BigDecimal value
        - LocalDate date
        %% Type ("EXPENSE" or "INCOME" will be a enum)
        - Type type
        - User user
        - Category category
    }
    class TransactionRequestDTO {
        - String description
        - BigDecimal value
        - LocalDate date
        - Type type
        - Long categoryId
    }
    class TransactionResponseDTO {
        - Long id
        - String description
        - BigDecimal value
        - LocalDate date
        - Type type
        - CategorySummaryDTO category
    }

    CategoryController --|> CategoryService : use
    CategoryService --|> CategoryRepository : use
    CategoryRepository --|> Category : use
    class CategoryController {
        %% @RestController annotation
        - CategoryService service
        + insertCategory(CategoryRequestDTO dto)
        + getAllCategories()
        + updateCategory(CategoryRequestDTO dto, Long id)
        + deleteCategoryById(Long id)
    }
    class CategoryService {
        %% @Service annotation
        - CategoryRepository repository
        + insertCategory(CategoryRequestDTO dto)
        + getAllCategories()
        + updateCategory(CategoryRequestDTO dto, Long id)
        + deleteCategoryById(Long id)
    }
    class CategoryRepository {
        <<Repository>>
        %% JPA methods will be defined here. If a custom method is required, it can be added in this interface.
    }
    class Category {
        %% @Entity annotation
        - Long id
        - String name
        - String hexColor
        - String icon
        - BigDecimal monthlyGoal
        - boolean isActive
        - User user
    }
    class CategoryRequestDTO {
        - String name
        - String hexColor
        - String icon
        - BigDecimal monthlyGoal
        - boolean isActive
    }
    class CategoryResponseDTO {
        - Long id
        - String name
        - String hexColor
        - String icon
        - BigDecimal monthlyGoal
        - boolean isActive
    }
    class CategorySummaryDTO {
        - Long id
        - String name
        - String hexColor
        - String icon
    }

    AuthenticationController --|> AuthenticationService : use
    AuthenticationService --|> UserRepository : use
    UserRepository --|> User : use
    class AuthenticationController {
        %% @RestController annotation
        - AuthenticationService service
        + registerUser(UserRequestDTO dto)
        + login(LoginRequestDTO dto)
    }
    class AuthenticationService {
        %% @Service annotation
        - UserRepository repository
        + registerUser(UserRequestDTO dto)
        + login(LoginRequestDTO dto)
    }
    class UserRepository {
        <<Repository>>
        %% JPA methods will be defined here. If a custom method is required, it can be added in this interface.
    }
    class User {
        %% @Entity annotation
        - Long id
        - String name
        - String password
        - String email
    }
    class UserRequestDTO {
        - String name
        - String password
        - String email
    }
    class UserResponseDTO {
        - Long id
        - String name
        - String email
    }
    class LoginRequestDTO {
        - String email
        - String password
    }
    class LoginResponseDTO {
        - String tokenJwt
    }

    DashboardController --|> DashboardService : use
    DashboardService --|> TransactionRepository : use
    DashboardService --|> CategoryRepository : use
    class DashboardController {
        %% @RestController annotation
        - DashboardService service
        + getMonthlySummary(int month, int year)
    }
    class DashboardService {
        %% @Service annotation
        - TransactionRepository transactionRepo
        - CategoryRepository categoryRepo
        + generateMonthlySummary(Long userId, int month, int year)
    }
    class DashboardResponseDTO {
        - BigDecimal totalRevenue
        - BigDecimal totalExpenses
        - BigDecimal finalBalance
        - List~BudgetCategoryDTO~ budgets
    }
    class BudgetCategoryDTO {
        - String categoryName
        - String categoryColor
        - BigDecimal monthlyGoal
        - BigDecimal totalSpent
        - BigDecimal percentagemSpent
    }

    class NotificationService {
        %% @Service annotation
        + sendMonthlySummaryByEmail()
    }
```
