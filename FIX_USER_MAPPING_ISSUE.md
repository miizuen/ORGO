# Fix: User Mapping Issue in Checkout

## Problem
When users tried to place an order, they received the error:
```
Không tìm thấy thông tin người dùng
```

## Root Cause
The `IUserProfileRepository.findByAccountId()` method was incorrectly named. The `UserProfile` entity has a `@OneToOne` relationship with `Account`:

```java
@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinColumn(name = "id_tai_khoan")
private Account account;
```

Spring Data JPA requires the method name to follow the relationship path: `findByAccount_Id()` instead of `findByAccountId()`.

## Solution
Updated the following files:

### 1. IUserProfileRepository.java
Changed method signature:
```java
// Before
Optional<UserProfile> findByAccountId(Integer accountId);

// After
Optional<UserProfile> findByAccount_Id(Integer accountId);
```

### 2. CheckoutService.java
Updated method call in `saveOrder()`:
```java
// Before
Integer userProfileId = userProfileRepository.findByAccountId(accountId)
        .map(com.example.orgo_project.entity.UserProfile::getId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

// After
Integer userProfileId = userProfileRepository.findByAccount_Id(accountId)
        .map(com.example.orgo_project.entity.UserProfile::getId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
```

### 3. UserProfileService.java
Updated method call:
```java
// Before
return userProfileRepository.findByAccountId(accountId);

// After
return userProfileRepository.findByAccount_Id(accountId);
```

### 4. OrderService.java
Updated method call in `getCompatibleUserIds()`:
```java
// Before
userProfileRepository.findByAccountId(accountId).ifPresent(profile -> {

// After
userProfileRepository.findByAccount_Id(accountId).ifPresent(profile -> {
```

## How It Works Now
1. User logs in with their account (TaiKhoan)
2. When placing an order, the system:
   - Gets the `accountId` from the logged-in user
   - Uses `findByAccount_Id(accountId)` to find the corresponding `UserProfile` (NguoiDung)
   - Extracts the `id_nguoi_dung` from the UserProfile
   - Saves the order with the correct `userId` (id_nguoi_dung)

## Testing
After restarting the application, users should be able to:
1. Add products to cart
2. Go to checkout page
3. Complete the order without seeing "Không tìm thấy thông tin người dùng" error

## Database Mapping
```
TaiKhoan (Account)
├── id_tai_khoan (PK)
└── ...

NguoiDung (UserProfile)
├── id_nguoi_dung (PK)
├── id_tai_khoan (FK → TaiKhoan.id_tai_khoan)
└── ...

DonHang (CustomerOrder)
├── id_don_hang (PK)
├── id_nguoi_dung (FK → NguoiDung.id_nguoi_dung) ✅ CORRECT
└── ...
```
