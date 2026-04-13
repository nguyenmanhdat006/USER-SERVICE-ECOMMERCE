# USER & AUTH SERVICE

User Management và Authentication Service sử dụng Spring Boot 3 và Keycloak.

## Tính năng

### Authentication
- Đăng ký tài khoản
- Đăng nhập (JWT token)
- Refresh token
- Logout
- Forgot password (TODO)
- Reset password (TODO)

### User Management
-  Xem thông tin user
-  Cập nhật thông tin user
-  Quản lý địa chỉ (CRUD)
-  Đặt địa chỉ mặc định
-  Upload avatar (TODO)

### Admin Features
-  Xem tất cả users
-  Xem chi tiết user
-  Xóa user

##  Tech Stack

- **Spring Boot** 3.2.2
- **Java** 17
- **PostgreSQL** 15+
- **Keycloak** 23.0
- **Spring Security** với OAuth2 Resource Server
- **Spring Data JPA**
- **MapStruct** cho DTO mapping
- **Swagger/OpenAPI** cho API documentation
- **Lombok**

## Cài đặt & Chạy

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Keycloak đã setup (xem KEYCLOAK-SETUP.md)

### Bước 1: Start Keycloak & PostgreSQL

```bash
docker-compose -f docker-compose-keycloak.yml up -d
```

### Bước 2: Cấu hình Keycloak

Làm theo hướng dẫn trong file `KEYCLOAK-SETUP.md`

**Quan trọng:** Copy Client Secret từ Keycloak và paste vào `application.yml`:

```yaml
keycloak:
  client-secret: YOUR_CLIENT_SECRET_HERE
```

### Bước 3: Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Hoặc chạy trực tiếp từ IDE (IntelliJ IDEA, VS Code)

##  API Endpoints

### Authentication Endpoints (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout |
| POST | `/api/auth/forgot-password` | Quên mật khẩu |
| POST | `/api/auth/reset-password` | Reset mật khẩu |

### User Endpoints (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Lấy thông tin user hiện tại |
| PUT | `/api/users/me` | Cập nhật thông tin user |

### Address Endpoints (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me/addresses` | Lấy tất cả địa chỉ |
| GET | `/api/users/me/addresses/{id}` | Lấy địa chỉ theo ID |
| GET | `/api/users/me/addresses/default` | Lấy địa chỉ mặc định |
| POST | `/api/users/me/addresses` | Tạo địa chỉ mới |
| PUT | `/api/users/me/addresses/{id}` | Cập nhật địa chỉ |
| PUT | `/api/users/me/addresses/{id}/default` | Đặt địa chỉ mặc định |
| DELETE | `/api/users/me/addresses/{id}` | Xóa địa chỉ |

### Admin Endpoints (Admin Role)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Lấy tất cả users |
| GET | `/api/users/{id}` | Lấy user theo ID |
| DELETE | `/api/users/{id}` | Xóa user |

## API Documentation

Swagger UI: http://localhost:8081/swagger-ui.html

## Testing

### 1. Register User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "fullName": "Test User",
    "phone": "0912345678"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 300,
    "tokenType": "Bearer",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "email": "test@example.com",
      "fullName": "Test User",
      ...
    }
  }
}
```

### 3. Get Current User (với token)

```bash
TOKEN="YOUR_ACCESS_TOKEN_HERE"

curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Create Address

```bash
curl -X POST http://localhost:8081/api/users/me/addresses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "city": "Ho Chi Minh",
    "district": "Quan 1",
    "ward": "Phuong 1",
    "isDefault": true
  }'
```

## Security

### JWT Token Format

Access Token chứa các claims:
- `sub`: Keycloak User ID
- `email`: Email
- `name`: Full name
- `realm_access.roles`: Danh sách roles
- `exp`: Expiration time

### Roles

- `CUSTOMER`: User thông thường (default)
- `ADMIN`: Quản trị viên
- `SELLER`: Người bán (optional)
- `SUPPORT`: Nhân viên hỗ trợ (optional)

### Role-based Access Control

```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminOnlyEndpoint() {
    // Only accessible by ADMIN
}
```

## Database Schema
### users
- id (UUID, PK)
- keycloak_id (String, unique)
- email (String, unique)
- phone (String)
- full_name (String)
- avatar_url (String)
- date_of_birth (Date)
- gender (Enum)
- status (Enum)
- email_verified (Boolean)
- phone_verified (Boolean)
- created_at (Timestamp)
- updated_at (Timestamp)

### addresses
- id (UUID, PK)
- user_id (UUID, FK)
- full_name (String)
- phone (String)
- address_line1 (String)
- address_line2 (String)
- city (String)
- district (String)
- ward (String)
- postal_code (String)
- country (String)
- is_default (Boolean)
- address_type (Enum)
- created_at (Timestamp)
- updated_at (Timestamp)

### user_preferences
- id (UUID, PK)
- user_id (UUID, FK, unique)
- language (String)
- currency (String)
- email_notifications (Boolean)
- sms_notifications (Boolean)
- push_notifications (Boolean)
- newsletter_subscribed (Boolean)
- created_at (Timestamp)
- updated_at (Timestamp)
