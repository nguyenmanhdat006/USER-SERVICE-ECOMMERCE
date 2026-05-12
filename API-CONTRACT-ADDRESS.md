# Address API Contract

Service: User Service - E-commerce

Base path: `/api/users/me/addresses`

Version: Current implementation does not expose an explicit `/v1` prefix. If versioning is added later, this contract should be updated accordingly.

## 1. Overview

This contract defines all address-related endpoints in the User Service.

All address endpoints are protected and work only for the authenticated current user.

Important response rule:

- The API does not return raw service payloads directly.
- Every response is wrapped by `ApiResponse<T>`.
- Successful data is always inside the `data` field.
- Error responses are also wrapped, with `success = false`.

## 2. Common Response Wrapper

### ApiResponse<T>

| Field | Type | Nullable | Description |
| --- | --- | --- | --- |
| success | boolean | no | `true` for success, `false` for error |
| message | string | yes | Human-readable message |
| data | object, array, map, or null | yes | Wrapped response payload |
| timestamp | string(datetime) | no | Server time when the response was created |

### Success response shape

```json
{
  "success": true,
  "message": "Optional message",
  "data": {},
  "timestamp": "2026-05-12T10:15:30"
}
```

### Error response shape

```json
{
  "success": false,
  "message": "Invalid input",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

## 3. Authentication / Authorization

- All endpoints require JWT Bearer token.
- Header required: `Authorization: Bearer <access_token>`
- No endpoint in this module is public.
- The service always scopes data to the current authenticated user.
- A user cannot read, update, delete, or set default for another user's address.

## 4. Address Entity / DTO Model

### AddressResponse

| Field | Type | Nullable | Notes |
| --- | --- | --- | --- |
| id | string (UUID) | no | Address ID |
| fullName | string | no | Receiver full name |
| phone | string | no | Vietnamese phone number |
| addressLine1 | string | no | Main address line |
| addressLine2 | string | yes | Secondary address line |
| city | string | no | City / province |
| district | string | yes | District / county |
| ward | string | yes | Ward / commune |
| postalCode | string | yes | Postal code |
| country | string | yes | Default is `Vietnam` when not set |
| isDefault | boolean | no | Current default address flag |
| addressType | enum | no | `SHIPPING`, `BILLING`, or `BOTH` |
| createdAt | string(datetime) | no | Creation time |
| updatedAt | string(datetime) | yes | Last update time |

### CreateAddressRequest

This DTO is used for both create and update.

| Field | Type | Required | Validation / Rule |
| --- | --- | --- | --- |
| fullName | string | yes | required, max 255 chars |
| phone | string | yes | required, must match Vietnamese phone format: `^(\\+84|0)[0-9]{9}$` |
| addressLine1 | string | yes | required, max 500 chars |
| addressLine2 | string | no | max 500 chars |
| city | string | yes | required, max 100 chars |
| district | string | no | max 100 chars |
| ward | string | no | max 100 chars |
| postalCode | string | no | max 20 chars |
| country | string | no | max 100 chars |
| isDefault | boolean | no | if `true`, current user's other default address will be unset |
| addressType | enum | no | `SHIPPING`, `BILLING`, `BOTH`; default is `SHIPPING` |

## 5. Enum Values

### addressType

- `SHIPPING`
- `BILLING`
- `BOTH`

## 6. Endpoints

### 6.1 Get all addresses

Method: `GET`

Path: `/api/users/me/addresses`

Auth: Bearer token required

Query params: none

Request body: none

Business rules:

- Returns only the current user's addresses.
- Addresses are ordered by `isDefault DESC`, then `createdAt DESC`.

Success response:

```json
{
  "success": true,
  "data": [
    {
      "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
      "fullName": "Nguyen Van A",
      "phone": "0912345678",
      "addressLine1": "123 Nguyen Trai",
      "addressLine2": "Apartment 12B",
      "city": "Ho Chi Minh",
      "district": "District 1",
      "ward": "Ben Nghe",
      "postalCode": "700000",
      "country": "Vietnam",
      "isDefault": true,
      "addressType": "SHIPPING",
      "createdAt": "2026-05-12T08:00:00",
      "updatedAt": "2026-05-12T08:00:00"
    }
  ],
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.2 Get address by ID

Method: `GET`

Path: `/api/users/me/addresses/{id}`

Auth: Bearer token required

Path params:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| id | UUID | yes | Address ID |

Request body: none

Business rules:

- Returns 404 if the address does not exist.
- Returns 404 if the address exists but does not belong to the current user.

Success response:

```json
{
  "success": true,
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "addressLine2": null,
    "city": "Ho Chi Minh",
    "district": "District 1",
    "ward": "Ben Nghe",
    "postalCode": null,
    "country": "Vietnam",
    "isDefault": false,
    "addressType": "SHIPPING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T08:10:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.3 Get default address

Method: `GET`

Path: `/api/users/me/addresses/default`

Auth: Bearer token required

Request body: none

Business rules:

- Returns the current user's default address.
- Returns 404 if no default address exists.

Success response:

```json
{
  "success": true,
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "addressLine2": null,
    "city": "Ho Chi Minh",
    "district": "District 1",
    "ward": "Ben Nghe",
    "postalCode": null,
    "country": "Vietnam",
    "isDefault": true,
    "addressType": "SHIPPING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T08:00:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.4 Create address

Method: `POST`

Path: `/api/users/me/addresses`

Auth: Bearer token required

Headers:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request body:

```json
{
  "fullName": "Nguyen Van A",
  "phone": "0912345678",
  "addressLine1": "123 Nguyen Trai",
  "addressLine2": "Apartment 12B",
  "city": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "postalCode": "700000",
  "country": "Vietnam",
  "isDefault": true,
  "addressType": "SHIPPING"
}
```

Business rules:

- `fullName`, `phone`, `addressLine1`, and `city` are required.
- `phone` must follow the Vietnamese phone regex defined by validation.
- If this is the user's first address, it becomes default automatically.
- If `isDefault = true`, all other default addresses of the current user will be unset first.
- If `country` is omitted in persistence, entity default is `Vietnam`.
- If `addressType` is omitted, entity default is `SHIPPING`.

Success response:

Status: `201 Created`

```json
{
  "success": true,
  "message": "Address created successfully",
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "addressLine2": "Apartment 12B",
    "city": "Ho Chi Minh",
    "district": "District 1",
    "ward": "Ben Nghe",
    "postalCode": "700000",
    "country": "Vietnam",
    "isDefault": true,
    "addressType": "SHIPPING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T08:00:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

Validation error response example:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "phone": "Phone number must be valid Vietnamese phone number",
    "city": "City is required"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.5 Update address

Method: `PUT`

Path: `/api/users/me/addresses/{id}`

Auth: Bearer token required

Headers:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Path params:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| id | UUID | yes | Address ID |

Request body: same as CreateAddressRequest

Business rules:

- Only provided fields are updated logically in the service.
- If `isDefault = true`, all other default addresses of the current user will be unset first.
- Returns 404 if address not found or not owned by the current user.

Success response:

```json
{
  "success": true,
  "message": "Address updated successfully",
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van B",
    "phone": "0912345679",
    "addressLine1": "456 Le Loi",
    "addressLine2": null,
    "city": "Ho Chi Minh",
    "district": "District 3",
    "ward": "Ward 6",
    "postalCode": null,
    "country": "Vietnam",
    "isDefault": false,
    "addressType": "BILLING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T09:00:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.6 Set default address

Method: `PUT`

Path: `/api/users/me/addresses/{id}/default`

Auth: Bearer token required

Path params:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| id | UUID | yes | Address ID |

Request body: none

Business rules:

- The selected address becomes the only default address for the current user.
- All other default addresses are unset first.
- Returns 404 if the address does not exist or does not belong to the current user.

Success response:

```json
{
  "success": true,
  "message": "Default address updated",
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "addressLine2": null,
    "city": "Ho Chi Minh",
    "district": "District 1",
    "ward": "Ben Nghe",
    "postalCode": null,
    "country": "Vietnam",
    "isDefault": true,
    "addressType": "SHIPPING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T09:30:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

---

### 6.7 Delete address

Method: `DELETE`

Path: `/api/users/me/addresses/{id}`

Auth: Bearer token required

Path params:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| id | UUID | yes | Address ID |

Request body: none

Business rules:

- Only the current user's address can be deleted.
- If the deleted address is the default one, the service automatically assigns another remaining address as default when available.
- Returns 404 if the address does not exist or does not belong to the current user.

Success response:

```json
{
  "success": true,
  "message": "Address deleted successfully",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

## 7. Error Responses

### 7.1 Resource not found

Status: `404 Not Found`

Examples:

```json
{
  "success": false,
  "message": "Address not found with id: 8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

Or when the address belongs to another user:

```json
{
  "success": false,
  "message": "Address not found",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

### 7.2 Validation failed

Status: `400 Bad Request`

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fullName": "Full name is required",
    "phone": "Phone number must be valid Vietnamese phone number"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

### 7.3 Unauthorized

Status: `401 Unauthorized`

```json
{
  "success": false,
  "message": "Authentication required",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

### 7.4 Forbidden

Status: `403 Forbidden`

```json
{
  "success": false,
  "message": "Access denied",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

### 7.5 Unexpected error

Status: `500 Internal Server Error`

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "data": null,
  "timestamp": "2026-05-12T10:15:30"
}
```

## 8. Business Rules

- A user can manage only their own addresses.
- Address ordering for listing is default first, newest first.
- A user should normally have only one default address.
- Creating or updating an address with `isDefault = true` clears other defaults for that user.
- Deleting the default address will automatically promote another remaining address if one exists.
- `addressLine1`, `fullName`, `phone`, and `city` are mandatory.
- Phone validation uses Vietnamese format only.
- `addressType` defaults to `SHIPPING` when not provided.
- `country` defaults to `Vietnam` when not provided.

## 9. Example Flows

### Create a default shipping address

Request:

```http
POST /api/users/me/addresses
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "fullName": "Nguyen Van A",
  "phone": "0912345678",
  "addressLine1": "123 Nguyen Trai",
  "city": "Ho Chi Minh",
  "isDefault": true,
  "addressType": "SHIPPING"
}
```

Response:

```json
{
  "success": true,
  "message": "Address created successfully",
  "data": {
    "id": "8f5fd4ac-1a2c-4d7d-8bb5-6b8d15f6a101",
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "addressLine1": "123 Nguyen Trai",
    "addressLine2": null,
    "city": "Ho Chi Minh",
    "district": null,
    "ward": null,
    "postalCode": null,
    "country": "Vietnam",
    "isDefault": true,
    "addressType": "SHIPPING",
    "createdAt": "2026-05-12T08:00:00",
    "updatedAt": "2026-05-12T08:00:00"
  },
  "timestamp": "2026-05-12T10:15:30"
}
```

## 10. Notes for Frontend / Mobile / Other Services

- Do not read address payloads directly from the service layer. Always expect the wrapper response.
- For validation errors, use the `data` map to map field messages into form errors.
- For default-address selection, the backend is the source of truth.
- If multiple addresses are shown in a list, rely on the order returned by the API.
