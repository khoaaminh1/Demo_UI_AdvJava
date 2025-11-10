# Giai đoạn 1: Build ứng dụng (sử dụng Maven)
# Sử dụng image chính thức của Maven với JDK 17
FROM maven:3.8.5-openjdk-17 AS build

# Đặt thư mục làm việc
WORKDIR /app

# Copy file cấu hình build
COPY pom.xml .
COPY .mvn .mvn

# Tải dependencies (giúp build nhanh hơn ở các lần sau)
RUN mvn dependency:go-offline

# Copy toàn bộ code
COPY src ./src

# Build ứng dụng, bỏ qua test để build nhanh hơn
RUN mvn package -DskipTests


# Giai đoạn 2: Chạy ứng dụng (sử dụng JRE - nhẹ hơn)
# Sử dụng image Java 17 JRE (chỉ để chạy, không cần build)
FROM eclipse-temurin:17-jre-jammy

# Đặt thư mục làm việc
WORKDIR /app

# Copy file .jar đã được build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar

# Expose cổng 8080 (cổng Spring Boot đang chạy)
EXPOSE 8080

# Lệnh để khởi động ứng dụng khi container chạy
ENTRYPOINT ["java", "-jar", "app.jar"]