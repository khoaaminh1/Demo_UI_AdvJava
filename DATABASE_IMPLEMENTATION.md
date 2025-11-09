# Database Implementation for Dashboard

## Tổng quan
Đã triển khai database layer cho trang Dashboard sử dụng MongoDB thay vì fake data.

## Những gì đã thực hiện

### 1. Cập nhật Models với MongoDB Annotations
Đã thêm MongoDB annotations cho các model:

#### Account.java
- Thêm `@Document(collection = "accounts")`
- Đổi ID từ `UUID` sang `String` (MongoDB ObjectId)
- Thêm field `userId` để link với User
- Thêm `createdAt`, `updatedAt` timestamps
- Đánh dấu `currentBalance` là `@Transient` (tính toán runtime)

#### Category.java
- Thêm `@Document(collection = "categories")`
- Đổi ID từ `UUID` sang `String`
- Thêm field `userId` (null cho system categories)
- Thêm field `isSystem` để phân biệt system vs user categories
- Thêm timestamps

#### Transaction.java
- Thêm `@Document(collection = "transactions")`
- Đổi ID từ `UUID` sang `String`
- Thêm `userId`, `accountId`, `categoryId` (references)
- Đánh dấu `account` và `category` là `@Transient` (populate khi cần)
- Thêm timestamps

#### Budget.java
- Thêm `@Document(collection = "budgets")`
- Đổi ID từ `UUID` sang `String`
- Thêm `userId`, `categoryId` (references)
- Đánh dấu `category` là `@Transient`
- Thêm timestamps

#### AccountType.java
- Thêm các enum: `CHECKING`, `SAVINGS`, `CREDIT_CARD`

### 2. Tạo Repository Interfaces

#### AccountRepository
```java
- findByUserId(String userId)
- findByUserIdAndStatus(String userId, AccountStatus status)
- countByUserId(String userId)
```

#### CategoryRepository
```java
- findByUserId(String userId)
- findByUserIdOrIsSystemTrue(String userId)
- findByUserIdAndType(String userId, CategoryType type)
- findByIsSystemTrue()
- countByUserId(String userId)
```

#### TransactionRepository
```java
- findByUserIdOrderByDateDesc(String userId)
- findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end)
- findByUserIdAndCategoryId(String userId, String categoryId)
- findByUserIdAndAccountId(String userId, String accountId)
- Custom query với @Query annotation
```

#### BudgetRepository
```java
- findByUserId(String userId)
- findByUserIdAndMonthAndYear(String userId, int month, int year)
- findByUserIdAndCategoryIdAndMonthAndYear(...)
```

### 3. Tạo DashboardService

Service layer xử lý logic nghiệp vụ cho dashboard:

**Chức năng chính:**
- `buildDashboard(String userId)`: Tạo DashboardVM với data từ database
- Tính toán MTD (Month-to-Date) income/expense
- Tạo category breakdown cho pie chart
- Tạo cash flow trend cho 6 tháng gần nhất
- Lấy recent transactions (10 giao dịch gần nhất)
- Tính toán budget usage với phần trăm đã chi tiêu

**Tối ưu hóa:**
- Batch loading accounts và categories để giảm database queries
- Populate transient fields khi cần thiết
- Sử dụng Map để lookup nhanh

### 4. Cập nhật DashboardController

- Thay thế `FakeDataService` bằng `DashboardService`
- Lấy userId từ authenticated user
- Gọi `dashboardService.buildDashboard(userId)` để lấy data thật

### 5. Cập nhật DataInitializer

Tự động seed data khi khởi động ứng dụng:

**System Categories:**
- Income: Salary, Freelance, Investment, Gift, Other Income
- Expense: Food & Dining, Shopping, Transportation, Entertainment, Bills & Utilities, Healthcare, Education, Travel, Other Expense

**Sample Data cho Test User (user@pft.com):**
- 3 accounts: Checking, Savings, Credit Card
- Transactions cho 6 tháng gần nhất
- Budgets cho tháng hiện tại

## Cấu trúc Database

### Collections
1. **users** - User accounts
2. **accounts** - Financial accounts (checking, savings, etc.)
3. **categories** - Transaction categories (system + user-defined)
4. **transactions** - Financial transactions
5. **budgets** - Monthly budgets per category

### Relationships
- User → Accounts (1:N)
- User → Categories (1:N, plus system categories)
- User → Transactions (1:N)
- User → Budgets (1:N)
- Transaction → Account (N:1)
- Transaction → Category (N:1)
- Budget → Category (N:1)

## Test Accounts

### Admin User
- Email: `admin@pft.com`
- Password: `admin123`

### Test User (có sample data)
- Email: `user@pft.com`
- Password: `user123`

## Cách chạy

1. Đảm bảo MongoDB đang chạy:
```bash
mongosh
```

2. Chạy ứng dụng:
```bash
mvn spring-boot:run
```

3. Truy cập: http://localhost:8080

4. Login với `user@pft.com` / `user123`

5. Xem Dashboard với data thật từ MongoDB

## Lưu ý

- Các controller khác (Accounts, Categories, Transactions, Budgets) tạm thời bị disable vì còn dùng FakeDataService
- Chỉ Dashboard đã được implement với database
- Để enable các trang khác, cần implement Service layer tương tự cho từng module

## Next Steps

Để hoàn thiện toàn bộ ứng dụng, cần:
1. Tạo AccountService và cập nhật AccountController
2. Tạo CategoryService và cập nhật CategoryController
3. Tạo TransactionService và cập nhật TransactionController
4. Tạo BudgetService và cập nhật BudgetController
5. Implement CRUD operations cho từng module
6. Thêm validation và error handling
7. Implement pagination cho danh sách dài

