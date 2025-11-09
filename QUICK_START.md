# Quick Start Guide - Personal Finance Tracker

## 🚀 Chạy ứng dụng trong 3 bước

### Bước 1: Start MongoDB
```bash
# Kiểm tra MongoDB đang chạy
mongosh

# Nếu chưa chạy, start MongoDB service
# macOS:
brew services start mongodb-community

# Linux:
sudo systemctl start mongod

# Windows:
net start MongoDB
```

### Bước 2: Chạy ứng dụng
```bash
cd /Users/quangvinh3020/Demo_UI_AdvJava
mvn spring-boot:run
```

### Bước 3: Truy cập
```
URL: http://localhost:8080
Email: user@pft.com
Password: user123
```

---

## 📱 Tính năng đã hoàn thành

### ✅ Dashboard
- Xem tổng quan tài chính
- Biểu đồ thu/chi theo category
- Biểu đồ cash flow 6 tháng
- Recent transactions
- Budget progress

**Truy cập:** http://localhost:8080/dashboard

### ✅ Transactions
- Thêm transaction mới
- Xem danh sách transactions
- Xóa transaction
- Filter theo category/account (backend ready)

**Truy cập:** http://localhost:8080/transactions

**Test thêm transaction:**
1. Chọn Date
2. Nhập Merchant (vd: "Starbucks")
3. Nhập Amount (vd: 5.50)
4. Chọn Category (vd: "Food & Dining")
5. Chọn Account (vd: "Checking Account")
6. Nhập Note (optional)
7. Click "Add Transaction"

**Test xóa transaction:**
1. Click nút trash icon ở cột Actions
2. Confirm trong dialog
3. Transaction sẽ bị xóa

---

## 🎯 Modules Status

| Module | Status | URL |
|--------|--------|-----|
| Login/Register | ✅ Working | `/login`, `/register` |
| Dashboard | ✅ Working | `/dashboard` |
| Transactions | ✅ Working | `/transactions` |
| Accounts | ⏳ Pending | `/accounts` |
| Categories | ⏳ Pending | `/categories` |
| Budgets | ⏳ Pending | `/budgets` |

---

## 👤 Test Accounts

### User với sample data
```
Email: user@pft.com
Password: user123

Có sẵn:
- 3 accounts (Checking, Savings, Credit Card)
- 6 tháng transactions
- 5 budgets
- 14 system categories
```

### Admin account
```
Email: admin@pft.com
Password: admin123

Admin privileges (future use)
```

---

## 🗄️ Database

### MongoDB Collections
```
pft_db
├── users           (2 users)
├── accounts        (3 accounts cho test user)
├── categories      (14 system categories)
├── transactions    (30+ transactions)
└── budgets         (5 budgets)
```

### Xem data trong MongoDB
```bash
mongosh
use pft_db

# Xem users
db.users.find().pretty()

# Xem transactions
db.transactions.find().limit(5).pretty()

# Xem categories
db.categories.find().pretty()

# Đếm documents
db.transactions.countDocuments()
```

---

## 🔧 Development Commands

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn spring-boot:run
```

### Package
```bash
mvn clean package
```

### Run JAR
```bash
java -jar target/pft-ui-0.0.1-SNAPSHOT.jar
```

---

## 📊 Test Scenarios

### Scenario 1: Xem Dashboard
1. Login với `user@pft.com`
2. Tự động redirect đến Dashboard
3. ✅ Thấy MTD Income/Expense
4. ✅ Thấy pie chart categories
5. ✅ Thấy line chart cash flow
6. ✅ Thấy recent transactions
7. ✅ Thấy budget progress bars

### Scenario 2: Thêm Transaction
1. Click "Transactions" trong sidebar
2. Fill form add transaction
3. Click "Add Transaction"
4. ✅ Thấy success message
5. ✅ Transaction xuất hiện trong table
6. Quay lại Dashboard
7. ✅ Dashboard update với data mới

### Scenario 3: Xóa Transaction
1. Vào Transactions page
2. Click delete button
3. Confirm dialog
4. ✅ Transaction bị xóa
5. ✅ Success message hiển thị
6. Quay lại Dashboard
7. ✅ Dashboard update

### Scenario 4: Logout/Login
1. Click Logout
2. Redirect về login page
3. Login lại
4. ✅ Data vẫn còn (persisted)
5. ✅ Dashboard hiển thị đúng

---

## [object Object]eshooting

### Lỗi: MongoDB connection refused
```
Giải pháp:
1. Check MongoDB đang chạy: mongosh
2. Start MongoDB service
3. Restart ứng dụng
```

### Lỗi: Port 8080 already in use
```
Giải pháp:
1. Kill process: lsof -ti:8080 | xargs kill -9
2. Hoặc đổi port trong application.properties
```

### Lỗi: Build failed
```
Giải pháp:
1. mvn clean
2. mvn install
3. mvn spring-boot:run
```

### Lỗi: Cannot login
```
Giải pháp:
1. Check DataInitializer đã chạy
2. Check MongoDB có users collection
3. Reset password nếu cần
```

---

## 📁 Project Structure

```
Demo_UI_AdvJava/
├── src/main/
│   ├── java/com/example/pftui/
│   │   ├── config/          # Configuration classes
│   │   │   ├── SecurityConfig.java
│   │   │   ├── DataInitializer.java
│   │   │   └── AppConfig.java
│   │   ├── controller/      # Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── TransactionController.java
│   │   │   └── HomeController.java
│   │   ├── model/           # Domain models
│   │   │   ├── User.java
│   │   │   ├── Account.java
│   │   │   ├── Category.java
│   │   │   ├── Transaction.java
│   │   │   ├── Budget.java
│   │   │   └── ...
│   │   ├── repository/      # MongoDB repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── AccountRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── BudgetRepository.java
│   │   ├── service/         # Business logic
│   │   │   ├── UserService.java
│   │   │   ├── DashboardService.java
│   │   │   ├── TransactionService.java
│   │   │   ├── AccountService.java
│   │   │   └── CategoryService.java
│   │   ├── security/        # Security classes
│   │   │   └── CustomUserDetails.java
│   │   └── PftUiApplication.java
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       │   ├── login.html
│       │   ├── register.html
│       │   ├── dashboard.html
│       │   ├── transactions.html
│       │   └── fragments/
│       ├── static/          # Static resources
│       │   ├── css/app.css
│       │   └── js/app.js
│       └── application.properties
├── pom.xml
└── Documentation files
```

---

## 🎨 UI Features

### Dark Theme
- Modern dark color scheme
- Easy on the eyes
- Professional look

### Responsive Design
- Works on desktop
- Works on tablet
- Works on mobile

### Icons
- Remix Icons library
- Consistent iconography
- Beautiful visuals

### Charts
- Chart.js integration
- Interactive charts
- Real-time data

---

## [object Object] Endpoints

### Authentication
```
POST /register      - Register new user
POST /login         - Login
GET  /logout        - Logout
```

### Dashboard
```
GET /dashboard      - View dashboard
```

### Transactions
```
GET  /transactions              - List transactions
POST /transactions              - Create transaction
POST /transactions/{id}/delete  - Delete transaction
```

---

## 🔐 Security

### Features
- ✅ BCrypt password encryption
- ✅ Session management
- ✅ CSRF protection
- ✅ User ownership verification
- ✅ Input validation
- ✅ SQL injection prevention

### Best Practices
- Passwords never stored in plain text
- User data isolated by userId
- All endpoints protected
- Secure by default

---

## 💡 Tips & Tricks

### Tip 1: Xem logs
```bash
# Xem logs trong terminal khi chạy
mvn spring-boot:run

# Logs sẽ hiển thị:
# - Database initialization
# - User creation
# - Sample data seeding
# - HTTP requests
```

### Tip 2: Reset database
```bash
mongosh
use pft_db
db.dropDatabase()

# Restart app để re-initialize
```

### Tip 3: Add more sample data
```
Modify DataInitializer.java
Add more transactions/accounts/budgets
Restart app
```

### Tip 4: Debug mode
```
Set logging.level.com.example.pftui=DEBUG
in application.properties
```

---

## 🎯 Next Development Steps

### For Developers

1. **Complete Accounts Module**
   - Update AccountController
   - Update accounts.html
   - Add CRUD operations

2. **Complete Categories Module**
   - Update CategoryController
   - Update categories.html
   - Add CRUD operations

3. **Complete Budgets Module**
   - Create BudgetService
   - Update BudgetController
   - Update budgets.html
   - Add CRUD operations

4. **Add Advanced Features**
   - Transaction filtering
   - Search functionality
   - Data export
   - Reports

---

## 📞 Support

### Documentation
- `DATABASE_IMPLEMENTATION.md` - Dashboard setup
- `TRANSACTIONS_DATABASE.md` - Transactions details
- `IMPLEMENTATION_SUMMARY.md` - Overall progress
- `QUICK_START.md` - This file

### Common Questions

**Q: Làm sao thêm user mới?**
A: Truy cập `/register` và điền form

**Q: Làm sao reset password?**
A: Hiện tại chưa có, cần implement forgot password feature

**Q: Làm sao export data?**
A: Chưa implement, trong roadmap

**Q: Có mobile app không?**
A: Chưa có, nhưng web responsive

---

## ✨ Enjoy!

Ứng dụng đã sẵn sàng để:
- ✅ Demo
- ✅ Testing
- ✅ Development
- ✅ Learning

**Have fun tracking your finances! 💰[object Object]

