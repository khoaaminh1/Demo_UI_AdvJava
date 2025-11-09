# Tổng quan kiến trúc và cấu trúc dự án

Dự án là ứng dụng Spring Boot theo mô hình MVC, render giao diện bằng Thymeleaf (server-side), lưu dữ liệu trên MongoDB, bảo mật bằng Spring Security (login + xác thực email + 2FA qua email), và gửi email qua SMTP (Gmail).

Mục tiêu của tài liệu: giúp người mới vào có thể hiểu 100% cấu trúc, luồng xử lý, các API và cách cấu hình kết nối (MongoDB, SMTP), từ đó có thể đọc code và phát triển tiếp.

---

## 1) Cấu trúc thư mục chính và vai trò

- src/main/java/com/example/pftui
  - PftUiApplication.java: Điểm khởi động ứng dụng (Spring Boot main class).

- config/
  - AppConfig.java: Bean cấu hình chung (nếu có thêm bean dùng chung, đặt tại đây).
  - DataInitializer.java: Seed dữ liệu ban đầu (nếu cần) khi ứng dụng khởi động.
  - SecurityConfig.java: Cấu hình Spring Security (trang login, phân quyền, AuthenticationManager, passwordEncoder, v.v.).

- controller/
  - HomeController.java: Điều hướng trang chủ "/".
  - AuthController.java: Đăng ký, đăng nhập, xác minh email, 2FA, logout.
  - DashboardController.java: Trang tổng quan sau khi đăng nhập "/dashboard".
  - SettingsController.java: Đổi mật khẩu, bật/tắt 2FA cho người dùng hiện tại.
  - AccountController.java: Quản lý tài khoản chi tiêu (xem danh sách, xóa, đổi trạng thái,...).
  - BudgetController.java: Quản lý ngân sách.
  - CategoryController.java: Quản lý danh mục (chi/thu).
  - TransactionController.java: Quản lý giao dịch (thêm, sửa, xóa,... tùy logic hiện có).

- model/
  - Các entity (MongoDB document): User, Account, Transaction, Budget, Category, enum liên quan (AccountType, CategoryType, AccountStatus, ...), và DashboardVM (View Model cho dashboard).

- repository/
  - Các interface Spring Data MongoDB: UserRepository, AccountRepository, TransactionRepository, BudgetRepository, CategoryRepository.
  - Vai trò: định nghĩa method truy vấn, Spring Data tự sinh code truy vấn MongoDB.

- service/
  - UserService.java: Nghiệp vụ người dùng: đăng ký, mã xác minh email, 2FA, xác minh mã, cập nhật lần đăng nhập, đổi mật khẩu; triển khai UserDetailsService cho Security.
  - EmailService.java: Gửi email HTML (mã xác minh đăng ký, mã 2FA) qua JavaMailSender.
  - AccountService.java, BudgetService.java, CategoryService.java, TransactionService.java, DashboardService.java: Nghiệp vụ cho từng domain tương ứng.

- security/
  - CustomUserDetails.java: Adapter chuyển User -> UserDetails cho Spring Security.
  - CustomAuthenticationFailureHandler.java: Tùy biến xử lý khi đăng nhập thất bại (ví dụ hiển thị lỗi,...).

- src/main/resources
  - templates/: Thymeleaf templates (login.html, register.html, login-2fa.html, dashboard.html, accounts.html, budgets.html, categories.html, settings.html, transactions.html, fragments/...)
  - static/: Tài nguyên tĩnh (css/app.css, js/app.js, ảnh nếu có).
  - application.properties: Cấu hình ứng dụng (MongoDB, Thymeleaf, logging, mail/SMTP,...).

- src/test/java/...: Test khởi động ứng dụng (PftUiApplicationTests.java).

---

## 2) Các endpoint (API theo MVC) và mục đích

Lưu ý: Ứng dụng render HTML bằng Thymeleaf; form trên giao diện submit trực tiếp tới các controller (không phải REST JSON tách rời). Dưới đây là các tuyến chính (rút trích từ annotation):

- HomeController
  - GET "/": Trang chủ/chuyển hướng (thường tới login hoặc dashboard tùy cấu hình).

- AuthController
  - GET "/login": Trang đăng nhập. Hỗ trợ query param error, logout để hiển thị thông báo.
  - GET "/register": Trang đăng ký.
  - POST "/register": Xử lý đăng ký; tạo User (chưa kích hoạt), phát sinh mã xác minh, gửi email, hiển thị form nhập mã.
  - GET "/verify": Trang nhập mã xác minh (tham số email). Hiển thị thông báo nếu tài khoản chưa xác minh.
  - POST "/verify": Xác minh mã; nếu đúng và còn hạn -> kích hoạt tài khoản -> chuyển về login.
  - POST "/resend-code": Gửi lại mã xác minh qua email nếu tài khoản chưa kích hoạt.
  - GET "/login-2fa": Trang nhập mã 2FA (tham số email).
  - POST "/login/2fa": Xác minh mã 2FA; nếu đúng -> tự tạo Authentication và chuyển tới "/dashboard".
  - GET "/logout-success": Chuyển hướng về "/login?logout".

- DashboardController
  - GET "/dashboard": Trang tổng quan sau khi đăng nhập.

- SettingsController
  - GET "/settings": Trang cài đặt tài khoản.
  - POST "/settings/change-password": Đổi mật khẩu (kiểm tra mật khẩu hiện tại, mã hóa mật khẩu mới).
  - POST "/settings/2fa/enable": Bật 2FA cho tài khoản hiện tại.
  - POST "/settings/2fa/disable": Tắt 2FA cho tài khoản hiện tại.

- AccountController
  - POST "/accounts/{id}/delete": Xóa tài khoản chi tiêu theo id.
  - POST "/accounts/{id}/toggle-status": Đổi trạng thái hoạt động của tài khoản.
  - Lưu ý: Các GET để hiển thị danh sách/screen tương ứng sẽ map trong controller (xem code chi tiết nếu cần).

- BudgetController
  - POST "/budgets/{id}/delete": Xóa ngân sách theo id.

- CategoryController
  - POST "/categories/{id}/delete": Xóa danh mục theo id.

- TransactionController
  - POST "/transactions/{id}/delete": Xóa giao dịch theo id.
  - POST "/transactions/{id}/update": Cập nhật giao dịch theo id.

Các biểu mẫu (form) trong Thymeleaf tương ứng submit đến các URL trên; controller xử lý nghiệp vụ qua service, cập nhật DB thông qua repository rồi trả về view.

---

## 3) Luồng đăng ký, xác minh email và đăng nhập 2FA

- Đăng ký (POST /register)
  1) Kiểm tra email đã tồn tại chưa. Nếu tồn tại nhưng chưa kích hoạt -> sinh & gửi lại mã xác minh.
  2) Nếu email mới -> tạo User với password đã mã hóa (PasswordEncoder), sinh mã xác minh 6 chữ số với hạn 10 phút, lưu DB.
  3) Gọi EmailService.sendVerificationCode(email, code) để gửi email HTML chứa mã.
  4) Hiển thị form nhập mã xác minh trên cùng trang register.

- Xác minh email (POST /verify)
  1) Tải User theo email; kiểm tra mã khớp và còn hạn không.
  2) Nếu hợp lệ -> set enabled=true, xóa mã/expiry -> lưu DB -> chuyển hướng về login.

- Đăng nhập + 2FA (cấu hình trong SecurityConfig):
  1) Người dùng nhập email/mật khẩu tại /login.
  2) Nếu tài khoản có bật 2FA: hệ thống phát mã 2FA (UserService.generateAndSend2faCode), gửi email (EmailService.send2faCode) và chuyển tới /login-2fa.
  3) Người dùng nhập mã 2FA tại /login-2fa (POST /login/2fa). Nếu mã hợp lệ và còn hạn -> tạo Authentication (UsernamePasswordAuthenticationToken) và đặt vào SecurityContextHolder -> chuyển tới /dashboard.

---

## 4) Tầng Service và Repository

- Service chịu trách nhiệm xử lý nghiệp vụ, ví dụ UserService:
  - registerUser: tạo user, mã hóa mật khẩu, sinh & gửi mã xác minh.
  - generateAndSendVerificationCode / generateAndSend2faCode: sinh mã 6 số, set hạn 10 phút, lưu & gửi email.
  - verifyUser / verify2faCode: kiểm tra mã và hạn, bật tài khoản/cho phép đăng nhập.
  - changePassword: đối chiếu mật khẩu hiện tại bằng PasswordEncoder.matches, mã hóa mật khẩu mới và lưu lại.
  - updateLastLogin: cập nhật thời điểm đăng nhập cuối.

- Repository là Spring Data MongoDB (interface) để thao tác DB:
  - Ví dụ: UserRepository có findByEmail, existsByEmail... Spring Data tự hiện thực dựa trên tên hàm.

---

## 5) Kết nối Backend và View (Thymeleaf)

- Ứng dụng không tách frontend-backend kiểu SPA; thay vào đó dùng Thymeleaf render HTML trên server.
- Controller trả về tên template (ví dụ "login"), Spring sẽ render resources/templates/login.html, truyền dữ liệu qua Model.
- Form trong template gửi request trực tiếp đến endpoint (GET/POST) của Controller.
- Tài nguyên tĩnh (CSS/JS) phục vụ từ resources/static.

---

## 6) Cấu hình cơ sở dữ liệu (MongoDB)

Trong file application.properties:
- spring.data.mongodb.host=localhost
- spring.data.mongodb.port=27017
- spring.data.mongodb.database=pft_db

Yêu cầu cài đặt và chạy MongoDB local trên cổng 27017 trước khi khởi chạy ứng dụng. Có thể chuyển sang chuỗi kết nối đầy đủ (spring.data.mongodb.uri) nếu dùng cloud hoặc có xác thực.

---

## 7) Gửi email qua SMTP (Gmail)

Cấu hình trong application.properties:
- spring.mail.host=smtp.gmail.com
- spring.mail.port=587
- spring.mail.username=...@gmail.com
- spring.mail.password=... (App Password, không phải mật khẩu đăng nhập Gmail thường)
- spring.mail.properties.mail.smtp.auth=true
- spring.mail.properties.mail.smtp.starttls.enable=true

Cách hoạt động trong code (EmailService.java):
- Inject JavaMailSender.
- Tạo MimeMessage và MimeMessageHelper, bật nội dung HTML.
- helper.setFrom("...", "Personal Finance Tracker") để set tên hiển thị.
- Gọi mailSender.send(mimeMessage) để gửi.
- Hai hàm tiện ích: sendVerificationCode(to, code) và send2faCode(to, code) dựng sẵn nội dung email (HTML) qua buildEmailTemplate(...).

Lưu ý bảo mật:
- Không commit thẳng mật khẩu vào source thật. Dùng biến môi trường hoặc Secret Manager. Với Gmail, tạo App Password (2FA của Google) và gán qua biến môi trường/spring.config.import.

---

## 8) Bảo mật (Spring Security)

- SecurityConfig cấu hình:
  - Trang login, logout, đường dẫn public/protected, AuthenticationManager, PasswordEncoder (BCrypt).
  - Tích hợp UserService (UserDetailsService) để load người dùng theo email.
- CustomUserDetails chuyển entity User -> đối tượng UserDetails.
- CustomAuthenticationFailureHandler (nếu được gắn) để tùy biến hành vi khi đăng nhập thất bại.

---

## 9) Cách chạy dự án

Yêu cầu:
- JDK 17 (hoặc phiên bản phù hợp với pom.xml).
- Maven.
- MongoDB đang chạy ở localhost:27017.
- Cấu hình SMTP hợp lệ (Gmail App Password) nếu muốn dùng chức năng gửi email/2FA.

Lệnh chạy:
- mvn spring-boot:run
  hoặc
- mvn clean package && java -jar target/pft-ui-0.0.1-SNAPSHOT.jar

Truy cập:
- http://localhost:8080

---

## 10) Hướng phát triển/tuỳ biến nhanh

- Thay đổi kết nối MongoDB: sửa application.properties hoặc dùng spring.data.mongodb.uri.
- Đổi SMTP: sửa khối spring.mail.* theo nhà cung cấp SMTP (host/port/tls/auth/user/pass).
- Bật/tắt 2FA mặc định: điều chỉnh luồng trong UserService/SecurityConfig.
- Thêm API REST thuần JSON: tạo @RestController mới, trả về ResponseEntity thay vì view.
- Thêm trường dữ liệu: bổ sung vào model + repository + cập nhật form Thymeleaf + service + controller tương ứng.

---

## 11) Sơ đồ luồng ngắn gọn

- Đăng ký -> Sinh mã -> Gửi email -> Người dùng nhập mã -> Kích hoạt tài khoản.
- Đăng nhập -> Nếu 2FA bật: sinh mã -> Gửi email -> Nhập mã -> Vào dashboard.
- CRUD Tài khoản/Danh mục/Ngân sách/Giao dịch -> Controller nhận form -> Service xử lý -> Repository lưu -> Render lại view.

---

Tài liệu này bám sát mã nguồn hiện có (controller/service/repository/templates) để mô tả chính xác vai trò và cách kết nối giữa các phần. Đọc theo thứ tự 1 -> 11 sẽ giúp hiểu rõ kiến trúc và có thể phát triển tính năng mới một cách nhất quán.
