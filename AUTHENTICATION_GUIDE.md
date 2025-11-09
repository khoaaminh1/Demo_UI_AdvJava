# Hướng dẫn Authentication với MongoDB

## Tổng quan
Backend đã được tích hợp đầy đủ với:
- ✅ MongoDB để lưu trữ user
- ✅ Spring Security để xác thực
- ✅ BCrypt để mã hóa password
- ✅ Session-based authentication
- ✅ Login/Logout/Register functionality

## Cài đặt MongoDB

### 1. Cài đặt MongoDB trên macOS
```bash
# Sử dụng Homebrew
brew tap mongodb/brew
brew install mongodb-community

# Khởi động MongoDB
brew services start mongodb-community
```

### 2. Kiểm tra MongoDB đang chạy
```bash
# Kết nối MongoDB shell
mongosh

# Hoặc kiểm tra service
brew services list | grep mongodb
```

## Chạy ứng dụng

### 1. Build project
```bash
mvn clean install -DskipTests
```

### 2. Chạy application
```bash
mvn spring-boot:run
```

### 3. Truy cập ứng dụng
- URL: http://localhost:8080
- Tự động redirect đến trang login

## Tài khoản mặc định

Hệ thống tự động tạo 2 tài khoản khi khởi động:

### Admin Account
- **Email:** admin@pft.com
- **Password:** admin123
- **Role:** ADMIN

### Test User Account
- **Email:** user@pft.com
- **Password:** user123
- **Role:** USER

## Chức năng

### 1. Đăng ký (Register)
- Truy cập: http://localhost:8080/register
- Nhập: Full Name, Email, Password
- Password tự động được mã hóa bằng BCrypt
- Sau khi đăng ký thành công, redirect về trang login

### 2. Đăng nhập (Login)
- Truy cập: http://localhost:8080/login
- Nhập: Email, Password
- Sau khi login thành công:
  - Session được tạo
  - Last login time được cập nhật
  - Redirect đến Dashboard
  - Hiển thị tên user trên topbar

### 3. Dashboard
- Hiển thị tên user đã đăng nhập
- Có nút Logout
- Tất cả các trang khác yêu cầu authentication

### 4. Đăng xuất (Logout)
- Click nút "Logout" trên Dashboard
- Session bị xóa
- Redirect về trang login

## Cấu trúc Database

### Collection: users
```json
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "password": "$2a$10$...", // BCrypt hash
  "fullName": "John Doe",
  "role": "USER",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "lastLogin": "2024-01-01T12:00:00"
}
```

## Security Configuration

### Protected Routes
Tất cả routes yêu cầu authentication trừ:
- `/login`
- `/register`
- `/css/**`
- `/js/**`
- `/static/**`

### Session Management
- Session timeout: 30 phút (default)
- Cookie name: JSESSIONID
- Session được lưu trong memory (có thể config Redis sau)

## Troubleshooting

### Lỗi: MongoDB connection refused
```bash
# Kiểm tra MongoDB đang chạy
brew services list

# Khởi động MongoDB
brew services start mongodb-community
```

### Lỗi: Port 8080 already in use
```bash
# Tìm process đang dùng port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Lỗi: Cannot find bean 'authenticationManager'
- Đảm bảo đã build lại project: `mvn clean install`
- Restart application

## Mở rộng

### 1. Thêm Remember Me
Trong `SecurityConfig.java`:
```java
.rememberMe(remember -> remember
    .key("uniqueAndSecret")
    .tokenValiditySeconds(86400) // 1 day
)
```

### 2. Thêm Email Verification
- Tạo VerificationToken entity
- Gửi email với token
- Verify token khi user click link

### 3. Thêm Password Reset
- Tạo PasswordResetToken entity
- Gửi email reset link
- Cho phép user đổi password

### 4. Thêm OAuth2 Login
- Google Login
- Facebook Login
- GitHub Login

## Testing

### Test với curl
```bash
# Register
curl -X POST http://localhost:8080/register \
  -d "email=test@test.com&password=test123&fullName=Test User"

# Login (lấy cookie)
curl -X POST http://localhost:8080/login \
  -d "username=test@test.com&password=test123" \
  -c cookies.txt

# Access protected route
curl http://localhost:8080/dashboard \
  -b cookies.txt
```

## MongoDB Queries hữu ích

```javascript
// Xem tất cả users
db.users.find().pretty()

// Tìm user theo email
db.users.findOne({email: "admin@pft.com"})

// Đếm số users
db.users.countDocuments()

// Xóa user
db.users.deleteOne({email: "test@test.com"})

// Update user
db.users.updateOne(
  {email: "admin@pft.com"},
  {$set: {fullName: "Super Admin"}}
)
```

## Notes

- CSRF đã được disable cho development (enable lại trong production)
- Password phải có ít nhất 6 ký tự
- Email phải unique trong database
- Session-based authentication (không dùng JWT)
- Thymeleaf template engine với Spring Security integration

