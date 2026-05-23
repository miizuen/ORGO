# 🌿 ORGO – KẾ HOẠCH TRIỂN KHAI HỆ THỐNG TMĐT THỰC PHẨM HỮU CƠ

**Nhóm:** SNSD | **GVHD:** Th.S Nguyễn Ngọc Huyền Trân | **Lớp:** 225ECM01 | **Thời gian:** 10 ngày | **Stack:** Spring Boot + Thymeleaf + MySQL

---

## 👥 THÀNH VIÊN & VAI TRÒ

| Thành viên | MSSV | Vai trò chính | Module chủ lực | Màu nhận diện |
|---|---|---|---|---|
| Nguyễn Trí Trường | 23115053122146 | Lead Auth + Payout support | Auth / JWT / OTP / Phân quyền | 🔵 Xanh dương |
| Nguyễn Văn Lũy | 23115053122117 | Lead Payout + Transaction | Payout / Wallet / Transaction | 🟠 Cam |
| Bạch Ngọc Mỹ Duyên | 23115053122109 | Product + Search + Review FE | Product / Review / Search-Filter | 🟢 Xanh lá |
| Nguyễn Hà Vi | 23115053122148 | Cart + Checkout + Order | Cart / Checkout / Order | 🟡 Vàng |
| Võ Đức Phú | 23115053122130 | Content + Dashboard + Admin | Blog / Dashboard / Admin UI | 🟣 Tím |

---

## 📦 MODULE PHÂN CHIA

| Module | Mô tả | Người phụ trách | UC count | Priority |
|---|---|---|---|---|
| 🔐 Auth | Đăng ký/nhập, OTP email, JWT, phân quyền RBAC | Trường (lead) | 4 UC | 🔴 Cao |
| 🛍️ Product | CRUD sản phẩm, duyệt sản phẩm, chứng nhận hữu cơ | Duyên (lead) | 3 UC | 🔴 Cao |
| 📝 Content/Blog | Soạn bài, duyệt bài, gắn sản phẩm, thống kê | Phú (lead) | 4 UC | 🟡 TB |
| 🛒 Cart + Checkout | Giỏ hàng, đặt hàng, mã giảm giá, thanh toán | Vi (lead) | 4 UC | 🔴 Cao |
| 📦 Order | Quản lý đơn hàng, hoàn trả, tracking | Vi (lead) | 4 UC | 🔴 Cao |
| ⭐ Review | Đánh giá sản phẩm, reply đánh giá | Duyên (support) | 2 UC | 🟡 TB |
| 🔍 Search/Filter | Tìm kiếm, lọc, sắp xếp sản phẩm | Duyên (lead) | 3 UC | 🟡 TB |
| 📊 Dashboard | Dashboard Admin/Seller/Expert, thống kê | Phú (lead) | 3 UC | 🟡 TB |
| 💰 Payout (⚡CRITICAL) | Rút tiền Seller/Expert, đối soát Admin, wallet | Lũy+Trường (lead) | 4 UC | 🔴 CRITICAL |

---

## ⚙️ TECH STACK

| Layer | Technology |
|---|---|
| Frontend | Thymeleaf 3.x, Bootstrap 5, jQuery, Fetch API / jQuery AJAX (partial) |
| Backend | Spring Boot 3.x, Spring Security + JWT, Spring Data JPA |
| Database | MySQL 8.0, Redis (session/OTP cache), Hibernate ORM |
| Email/OTP | JavaMailSender + Gmail SMTP, 6-digit OTP, 5-min TTL |
| Auth | JWT RS256, Refresh Token, Role-based (RBAC) Spring Security |
| Payout | VietQR mock / Manual bank transfer, Transaction ledger |
| Tools | Docker Compose, Git (feature branch), Postman, Swagger UI |
| Monitoring | Spring Actuator, log4j2, daily stand-up review |

---

## 📊 USECASE COUNT PER PERSON

| Thành viên | Usecase (lead) | UC support | View tasks | BE tasks | Tổng tasks |
|---|---|---|---|---|---|
| Trường | Auth (4 UC) + Payout FE | Payout BE support | 6 | 8 | 14 |
| Lũy | Payout (4 UC) + Wallet | Auth BE support | 5 | 9 | 14 |
| Duyên | Product(3)+Review(2)+Search(3) | Order support | 7 | 7 | 14 |
| Vi | Cart(4)+Order(4) | Product FE support | 7 | 7 | 14 |
| Phú | Blog(4)+Dashboard(3) | Admin UI support | 7 | 7 | 14 |

---

## 📝 TASK BREAKDOWN – CHI TIẾT TỪNG TASK (76 TASKS / 5 NGƯỜI)

### 🔐 Auth

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Dependency | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | T001 | Setup Spring Security + JWT filter chain | Trường | BE | Cấu hình SecurityFilterChain, JwtAuthFilter, JwtUtil, ignore public endpoints | Spring Boot project | JWT filter hoạt động, public/private route phân biệt | 6 | 1 | – | ✅ |
| 2 | T002 | POST /auth/register + gửi OTP email | Trường | BE | Tạo User entity, RegisterRequest DTO, validate email unique, generate 6-digit OTP, store Redis (TTL=5min), send email | RegisterRequest{email,password,fullName} | Email OTP gửi đến user, Redis lưu OTP | 5 | 1 | AUTH-01 | ✅ |
| 3 | T003 | POST /auth/verify-otp + activate account | Trường | BE | Nhận {email,otp}, check Redis, nếu match → set user.status=ACTIVE, xóa Redis key, return JWT+RefreshToken | {email, otp} | JWT access token + refresh token, account ACTIVE | 3 | 2 | AUTH-02 | ✅ |
| 4 | T004 | POST /auth/login + POST /auth/refresh | Trường | BE | Login qua AuthenticationManager, return JWT+RefreshToken. Refresh: validate refreshToken, issue new accessToken. Logout: blacklist token Redis | {email,password}/{refreshToken} | JWT pair, refresh rotation | 4 | 2 | AUTH-01 | ✅ |
| 5 | T005 | POST /auth/forgot-password + /auth/reset-password | Trường | BE | Forgot: generate reset-token (UUID), store Redis (TTL=15min), gửi link email. Reset: validate token, hash new password BCrypt | {email}/{token,newPassword} | Email link reset, password updated | 4 | 2 | AUTH-04 | ✅ |
| 6 | T006 | FE: Trang Register + OTP verify flow | Trường | View | Thymeleaf: template đăng ký, trang nhập OTP 6 ô, countdown 5min, resend OTP, auto-focus next input | Form data | UI register + OTP verify hoàn chỉnh | 6 | 2 | AUTH-02,AUTH-03 | ✅ |
| 7 | T007 | FE: Trang Login + Forgot password + Reset | Trường | View | Login với remember-me, redirect theo role. Forgot: nhập email. Reset: form nhập pass mới | Credentials/reset token | Login redirect + password reset UI | 5 | 3 | AUTH-04 | ✅ |
| 8 | T008 | RBAC: @PreAuthorize + Role guard FE | Trường | View+BE | BE: @PreAuthorize trên controllers. View: Thymeleaf sec:authorize ẩn/hiện theo role | Role enum: USER/SELLER/EXPERT/ADMIN | Route protection hoàn chỉnh 4 role | 4 | 3 | AUTH-01 | ✅ |

### 💰 Payout

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Dependency | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 9 | T009 | Thiết kế DB schema: wallet + payout_request + transaction_log | Lũy | BE | CREATE TABLE wallet, payout_request, transaction_log với đầy đủ columns và constraints | DB design doc | 3 tables migration Liquibase/Flyway | 5 | 1 | – | ❌ |
| 10 | T010 | GET /api/wallet/me – xem số dư ví | Lũy | BE | WalletController → WalletService.getMyWallet → Return {balance, lockedAmount, availableBalance, currency} | JWT token | WalletResponse DTO | 3 | 2 | PAY-01 | ❌ |
| 11 | T011 | POST /api/payout/request – tạo yêu cầu rút tiền | Lũy | BE | Validate amount, lock tiền (pessimistic lock), create payout_request PENDING, insert log LOCK, notify Admin | PayoutRequestDto | PayoutRequest created + tiền locked | 6 | 2 | PAY-01 | ❌ |
| 12 | T012 | GET /api/admin/payout – list tất cả payout requests | Lũy | BE | Admin only. Pageable + filter by status/date/role. JOIN wallet+user | ?status=PENDING&page=0&size=20 | Danh sách payout requests + pagination | 4 | 3 | PAY-03 | ❌ |
| 13 | T013 | PUT /api/admin/payout/{id}/approve – Admin duyệt | Lũy | BE | @Transactional: check status=PENDING → APPROVED → mock payment → PAID. wallet.balance-=amount, locked-=amount. Log DEBIT. Notify | {id, adminNote} | Status PAID, ví đã trừ, transaction logged | 7 | 3 | PAY-03 | ❌ |
| 14 | T014 | PUT /api/admin/payout/{id}/reject – Admin từ chối | Lũy | BE | @Transactional: Set REJECTED, unlock tiền (lockedAmount-=amount), log UNLOCK, notify user với lý do | {id, reason} | Status REJECTED, tiền unlock trả về | 4 | 4 | PAY-04 | ❌ |
| 15 | T015 | GET /api/payout/history – lịch sử rút tiền | Lũy | BE | Lấy tất cả payout_request của userId, filter by status, sort DESC, pagination | JWT (userId) | List<PayoutHistoryDto> | 3 | 4 | PAY-02 | ❌ |
| 16 | T016 | GET /api/admin/payout/report – đối soát tổng | Lũy | BE | Aggregate tổng PAID/PENDING/REJECTED. Group by role, date range. Cache Redis | ?from=&to=&role= | Báo cáo đối soát tổng hợp | 4 | 4 | PAY-01 | ❌ |
| 17 | T017 | FE: Màn hình ví điện tử (Wallet page) | Trường | View | Balance card, lockedAmount, availableBalance. Lịch sử giao dịch timeline. Nút Rút tiền → modal | WalletResponse + transaction list | Wallet UI đẹp, realtime balance | 6 | 3 | PAY-02 | ❌ |
| 18 | T018 | FE: Modal tạo yêu cầu rút tiền | Trường | View | Form: số tiền (max=available), chọn ngân hàng, số TK, tên TK. Preview + loading + toast thành công | PayoutRequestDto | Payout request submitted UI + balance updated | 5 | 4 | PAY-09 | ❌ |
| 19 | T019 | FE: Admin – Trang đối soát & duyệt payout | Trường | View | Table payout requests, filter tabs, nút Duyệt/Từ chối với dialog, pagination, export CSV | Page<PayoutAdminView> | Admin payout management UI hoàn chỉnh | 7 | 4 | PAY-04,PAY-05 | ✅ |
| 20 | T020 | BE: CREDIT wallet khi Seller bán được hàng | Lũy | BE | WalletService.credit: atomic UPDATE balance+=amount. Log CREDIT. @TransactionalEventListener(OrderDeliveredEvent) | userId, amount, refId | Wallet credited + log | 5 | 5 | PAY-01 | ❌ |

### 🛍️ Product

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 21 | T021 | DB schema: Product, Category, ProductVariant, OrganicCert | Duyên | BE | 4 bảng migration đầy đủ columns | ERD design | 4 tables migration | 4 | 1 | ✅ |
| 22 | T022 | CRUD Product (Seller) | Duyên | BE | POST/GET/PUT/DELETE /api/seller/products. Upload ảnh multipart | ProductDto | Product CRUD APIs | 6 | 2 | ✅ |
| 23 | T023 | Admin duyệt/từ chối sản phẩm | Duyên | BE | PUT approve/reject, GET danh sách chờ duyệt | {id, action, note} | Product status updated | 4 | 2 | ✅ |
| 24 | T024 | Public – xem sản phẩm, chi tiết, featured | Duyên | BE | GET /api/products (pageable, filter). GET /api/products/{id} + variants + cert. GET featured/category | Query params | Product list/detail response | 4 | 3 | ✅ |
| 25 | T025 | FE: Trang danh sách sản phẩm (Shop page) | Duyên | View | Grid 4 cols, ProductCard, sidebar filter, skeleton loading, pagination | Product list API | Shop page UI hoàn chỉnh | 7 | 3 | ✅ |
| 26 | T026 | FE: Trang chi tiết sản phẩm | Duyên | View | Image gallery, variant selector, Add to cart, tab Mô tả/Thành phần/Chứng nhận/Đánh giá, related products | Product detail API | Product detail page | 6 | 4 | ✅ |
| 27 | T027 | FE: Seller – Quản lý sản phẩm + Thêm/Sửa form | Duyên | View | Table sản phẩm, stepper 3 bước, drag & drop ảnh, preview | Product CRUD UI | Seller product management UI | 7 | 4 | ✅ |
| 28 | T028 | FE: Admin duyệt sản phẩm UI | Duyên | View | List sản phẩm chờ duyệt, preview chi tiết, nút Duyệt/Từ chối, filter | Admin product approval API | Admin product approval UI | 4 | 5 | ✅ |

### 🔍 Search

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 29 | T029 | GET /api/products/search + filter + sort | Duyên | BE | Params: q, categoryId, minPrice, maxPrice, minRating, hasOrganic, sort. Spring Data JPA Specification | Query params | Kết quả tìm kiếm phân trang | 5 | 3 | ✅ |
| 30 | T030 | FE: Search bar + live suggestion | Duyên | View | Debounce 300ms, dropdown top 5, Enter redirect, highlight keyword | Search API | Search bar UI + suggestions | 5 | 4 | ✅ |
| 31 | T031 | FE: Trang kết quả tìm kiếm + filter panel | Duyên | View | URL-driven filter, sidebar category/giá/rating/hữu cơ, sort dropdown, empty state | Search + filter API | Search results page | 5 | 5 | ✅ |

### 🛒 Cart

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 32 | T032 | DB schema + Cart management APIs | Vi | BE | CartItem schema. GET/POST/PUT/DELETE /api/cart. Check stock | CartItemDto | Cart CRUD APIs | 5 | 2 | ❌ |
| 33 | T033 | POST /api/orders/checkout – đặt hàng | Vi | BE | Validate cart, check stock (SELECT FOR UPDATE), apply coupon, create Order, decrease stock, clear cart, notify Seller. @Transactional | CheckoutRequest | Order created | 7 | 3 | ❌ |
| 34 | T034 | Coupon/Discount code validation | Vi | BE | Discount entity. POST /api/coupons/validate → trả về discount amount hoặc error. @Scheduled xóa hết hạn | {code, amount} | Discount validation response | 4 | 3 | ❌ |
| 35 | T035 | FE: Trang giỏ hàng | Vi | View | CartItem list, qty input +/-, tổng tiền realtime, coupon input + AJAX validate, checkout button | Cart API | Cart page UI | 6 | 3 | ❌ |
| 36 | T036 | FE: Trang checkout (đặt hàng) | Vi | View | Stepper: 1.Địa chỉ → 2.Thanh toán → 3.Review + confirm. Order summary sidebar sticky | Checkout API | Checkout page UI | 7 | 4 | ❌ |
| 37 | T037 | FE: Trang đặt hàng thành công | Vi | View | Animation confetti, order ID, tóm tắt, nút Xem đơn hàng + Tiếp tục mua sắm | Order ID | Success UI | 2 | 4 | ❌ |

### 📦 Order

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 38 | T038 | Order management APIs (User) | Vi | BE | GET list/detail, PUT cancel (chỉ PENDING), POST return-request | JWT, orderId | Order APIs | 5 | 3 | ❌ |
| 39 | T039 | Order management APIs (Seller) | Vi | BE | GET orders, PUT confirm/ship/deliver → trigger credit wallet. GET stats | JWT (sellerId) | Seller order workflow APIs | 5 | 4 | ❌ |
| 40 | T040 | Admin order overview + return request APIs | Vi | BE | GET all orders, GET return-requests, PUT approve/reject return | Pageable + filters | Admin order APIs | 4 | 4 | ❌ |
| 41 | T041 | FE: Lịch sử đơn hàng (User) | Vi | View | Tabs Tất cả/Chờ xác nhận/Đang giao/Đã giao/Đã hủy. OrderCard, pagination | Order list API | Order history UI | 5 | 4 | ❌ |
| 42 | T042 | FE: Chi tiết đơn hàng + tracking timeline | Vi | View | Thông tin người mua, items table, tracking timeline vertical stepper, return request modal | Order detail API | Order detail + tracking UI | 5 | 5 | ❌ |
| 43 | T043 | FE: Seller order management UI | Vi | View | Table filter status, action buttons step-by-step, modal xem chi tiết, thống kê mini | Seller order API | Seller order UI | 5 | 5 | ❌ |

### ⭐ Review

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 44 | T044 | CRUD Review + Reply APIs | Duyên | BE | POST review (chỉ cho phép nếu order DELIVERED và chưa review). GET pageable, sort. PUT Seller reply. GET rating-summary | ReviewDto | Review + Reply APIs | 5 | 4 | ✅ |
| 45 | T045 | FE: Form đánh giá + Review list | Duyên | View | Star rating interactive, textarea, upload ảnh (max 3). ReviewList: avatar, tên, rating, comment, reply. Rating summary bar chart | Review API | Review UI | 5 | 5 | ✅ |

### 📝 Content/Blog

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 46 | T046 | DB schema: Article, ArticleProductLink, ArticleStats | Phú | BE | 3 tables migration | ERD | 3 tables migration | 3 | 1 | ❌ |
| 47 | T047 | Expert CRUD bài viết APIs | Phú | BE | POST/GET/PUT/DELETE /api/expert/articles. POST submit DRAFT→PENDING. POST link-product | ArticleDto | Expert article CRUD APIs | 5 | 2 | ❌ |
| 48 | T048 | Admin duyệt bài viết APIs | Phú | BE | GET pending list/detail, PUT approve (published_at=now, notify), PUT reject (ghi note, notify) | {id, action, note} | Article approval APIs | 4 | 2 | ❌ |
| 49 | T049 | Public – xem bài viết, tăng view, thống kê Expert | Phú | BE | GET articles (pageable), GET /{id} + tăng view, GET stats, GET featured, GET experts | Query params | Article public APIs | 4 | 3 | ❌ |
| 50 | T050 | FE: Trang danh sách bài viết (Blog/Recipe) | Phú | View | Filter tabs Tất cả/Blog/Recipe/FAQ, ArticleCard, featured hero banner, pagination | Article list API | Blog listing UI | 5 | 3 | ❌ |
| 51 | T051 | FE: Trang chi tiết bài viết | Phú | View | Rich content render, TOC sidebar, related products widget, related articles, Expert profile card, share buttons, view count | Article detail API | Article detail UI | 6 | 4 | ❌ |
| 52 | T052 | FE: Expert – Dashboard + soạn bài viết | Phú | View | Stats cards, Quill.js Rich Text Editor, form soạn bài, gắn sản phẩm popup, Lưu nháp/Gửi duyệt | Article CRUD API | Expert editor UI | 8 | 4 | ❌ |
| 53 | T053 | FE: Admin duyệt bài viết UI | Phú | View | List chờ duyệt, preview trong side panel, nút Duyệt/Từ chối, thống kê theo trạng thái | Admin article API | Admin content approval UI | 4 | 5 | ❌ |

### 📊 Dashboard

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 54 | T054 | Admin dashboard stats API | Phú | BE | {totalUsers, totalSellers, totalExperts, totalOrders, totalRevenue, pendingItems, revenueByDay[7], topProducts[5], ordersByStatus}. Cache Redis 5min | JWT (Admin) | Dashboard stats DTO | 5 | 4 | ❌ |
| 55 | T055 | Seller + Expert dashboard stats APIs | Phú | BE | Seller: {totalProducts, totalOrders, revenue, ordersByStatus, revenueByDay[7]}. Expert: {totalArticles, totalViews, pendingArticles} | JWT | Seller/Expert stats DTOs | 4 | 4 | ❌ |
| 56 | T056 | FE: Admin dashboard UI | Phú | View | KPI cards (4 cols), Revenue chart Chart.js (7 ngày), Order status pie, Top products table, Pending approvals quick-access | Dashboard APIs | Admin dashboard UI | 7 | 5 | ✅ |
| 57 | T057 | FE: Seller dashboard + Expert dashboard | Phú | View | Seller: revenue chart, order status donut, recent orders, top products. Expert: views chart, article status, wallet card | Seller/Expert APIs | Seller+Expert dashboard UI | 6 | 6 | ❌ |

### 👑 Admin

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 58 | T058 | Quản lý user APIs (Admin) | Phú | BE | GET users (pageable, filter role/status), PUT ban/unban, GET user details, GET/PUT Seller/Expert approval | userId, action | User/Seller/Expert management APIs | 5 | 5 | ❌ |
| 59 | T059 | FE: Admin user management + role approval | Phú | View | Users table, search+filter, ban/unban, tabs Users/Sellers/Experts, approval list, modal chi tiết | Admin user API | Admin user management UI | 6 | 6 | ❌ |

### 👤 Profile

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 60 | T060 | User profile + address management APIs | Trường | BE | GET/PUT /api/users/me. Avatar upload. Address: GET/POST/PUT/DELETE. Default address logic | ProfileDto | Profile + Address APIs | 4 | 5 | ✅ |
| 61 | T061 | FE: Trang hồ sơ cá nhân | Trường | View | Avatar upload preview, form chỉnh sửa, address list, modal thêm/sửa, tab sidebar | Profile + Address API | Profile page UI | 5 | 5 | ✅ |

### 🔼 Upgrade

| # | Task ID | Task Name | Assignee | Type | Mô tả | Input | Output | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| 62 | T062 | Đăng ký trở thành Seller/Expert APIs | Trường | BE | POST /api/users/upgrade/seller + /expert. Tạo pending request, notify Admin | UpgradeRequestDto | Upgrade request APIs | 4 | 5 | ✅ |
| 63 | T063 | FE: Form đăng ký Seller/Expert | Trường | View | Multi-step form: Seller (3 bước), Expert (2 bước). Progress indicator, file upload drag&drop | Upgrade API | Upgrade registration UI | 4 | 6 | ✅ |

### 🌱 Seed + ⚙️ Infra + 🔧 Testing

| # | Task ID | Task Name | Assignee | Type | Mô tả | Est (h) | Day | Status |
|---|---|---|---|---|---|---|---|---|
| 64 | T064 | Tạo Seed Data SQL + DataLoader | Lũy | BE | data-seed.sql: 5 Users, 3 Sellers, 3 Experts, 10 Products, 5 Orders, 5 Reviews, 3 Articles, Wallets, 2 PayoutRequests | 4 | 1 | ❌ |
| 65 | T065 | Docker Compose setup + API Gateway config | Lũy | BE | docker-compose.yml: mysql:8.0, redis:7, spring-backend. .env, Makefile, CORS config | 3 | 1 | ❌ |
| 66 | T066 | Swagger UI + Postman collection | Lũy | BE | springdoc-openapi-ui, @Tag annotations, import Postman từ swagger json, auth pre-request script | 3 | 2 | ❌ |
| 67 | T067 | FE: Fetch API / jQuery AJAX interceptor + Thymeleaf Router | Trường | View | Authorization header interceptor, 401 → refresh → retry, error pages, toast notification | 3 | 1 | ❌ |
| 68 | T068 | FE: Design system + component library | Phú | View | Bootstrap 5 theme (green palette organic), shared fragments: navbar, sidebar, footer, modal, table, badge, skeleton, toast, pagination, breadcrumb, emptyState | 4 | 1 | ❌ |
| 69 | T069 | Integration test: Auth flow E2E | Trường | View+BE | Register → OTP verify → Login → Access protected → Logout → Refresh token | 3 | 7 | ❌ |
| 70 | T070 | Integration test: Payout flow E2E | Lũy | View+BE | Seller bán hàng → wallet credited → tạo payout request → Admin duyệt → PAID → balance updated. Test reject + concurrent | 4 | 7 | ❌ |
| 71 | T071 | Integration test: Order + Checkout flow E2E | Vi | View+BE | Cart → Coupon → Checkout → Order → Confirm → Ship → Deliver → Review. Test cancel/return | 3 | 7 | ❌ |
| 72 | T072 | Integration test: Content + Admin approval E2E | Phú | View+BE | Expert soạn bài → submit → Admin duyệt → bài hiển thị public. Test reject, verify product link | 3 | 7 | ❌ |
| 73 | T073 | Bug fix + Polish UI | Duyên | View+BE | Fix bugs từ integration test. Responsive mobile (375px). Loading states, error handling, 404/500 pages | 4 | 8 | ❌ |
| 74 | T074 | Demo prep + Final QA | Lũy | View+BE | Full user journey demo script. Reset + reload seed data. Ghi chú demo | 4 | 9 | ❌ |
| 75 | T075 | Performance check + final deployment | Trường | View+BE | mvn package, kiểm tra jar size, Spring Actuator /health, Docker compose production build, smoke test | 3 | 9 | ❌ |
| 76 | T076 | Viết báo cáo đóng góp + README | Phú | View+BE | README.md: setup guide, env vars, seed instructions. Bảng đóng góp. API doc link. Screenshot UI | 3 | 10 | ❌ |

---

## 📅 THỜI KHÓA BIỂU 10 NGÀY

### Ngày 1 – 🎯 Setup & Foundation

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔐 Auth BE: Setup Spring Security + JWT filter. FE: Fetch API/jQuery AJAX interceptor + Thymeleaf Router. ENV config | JWT filter ✅ Thymeleaf routing ✅ |
| Lũy | 💰 Payout: Thiết kế DB schema (wallet/payout_request/transaction_log). Seed data SQL. Docker Compose setup | DB schema migrated ✅ Docker up ✅ |
| Duyên | 🛍️ Product: DB schema 4 tables migration. Cài đặt project structure BE | Product schema ✅ |
| Vi | 🛒 Cart: Setup CartItem schema. Khởi tạo FE CartContext/Spring MVC Model | Cart schema ✅ Spring MVC ✅ |
| Phú | 📝 Content: DB schema 3 tables. FE: Design system + Shared components | Content schema ✅ Components ✅ |

### Ngày 2 – 🎯 Core Auth + Product APIs

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔐 BE: Register API + OTP email gửi Redis. Login + Refresh token. FE: Register page + OTP form | Register+Login API ✅ OTP gửi được ✅ |
| Lũy | 💰 Payout BE: GET /wallet/me, POST /payout/request (lock tiền). Infra: Swagger UI + Postman | Wallet API ✅ Payout request ✅ |
| Duyên | 🛍️ Product BE: CRUD (Seller), Admin approve/reject, Upload ảnh endpoint | Product CRUD APIs ✅ |
| Vi | 🛒 Cart BE: Full CRUD cart API. Check stock logic | Cart CRUD APIs ✅ |
| Phú | 📝 Content BE: Expert CRUD bài viết. Submit duyệt. Admin approve/reject article | Article CRUD + approval APIs ✅ |

### Ngày 3 – 🎯 Auth Complete + Checkout + Search

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔐 BE: Forgot/Reset password. FE: Login + Forgot password UI. RBAC Thymeleaf sec:authorize | Auth hoàn chỉnh ✅ RBAC routes ✅ |
| Lũy | 💰 Payout BE: Admin list payout + approve. GET public articles + view stats. FE: Wallet page | Payout admin APIs ✅ Wallet UI ✅ |
| Duyên | 🛍️ FE: Shop page (grid, filter sidebar, skeleton). BE: Search + filter + sort API | Shop page ✅ Search API ✅ |
| Vi | 🛒 BE: Checkout API + coupon validation. FE: Cart page với coupon input | Checkout API ✅ Cart UI ✅ |
| Phú | 📝 FE: Blog listing page. BE: Dashboard Admin stats API. BE: Public articles API | Blog listing ✅ Dashboard APIs ✅ |

### Ngày 4 – 🎯 Full Feature Development

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 💰 FE: Modal rút tiền (form bank info, preview xác nhận). Profile API BE + Profile page FE | Payout modal ✅ Profile page ✅ |
| Lũy | 💰 FE: Admin payout management page (table, filter, approve/reject dialog). BE: Payout reject + history APIs | Admin payout UI ✅ Reject/history ✅ |
| Duyên | 🛍️ FE: Product detail page (gallery, variant selector, tabs). Seller product management table+form | Product detail ✅ Seller product mgmt ✅ |
| Vi | 🛒 FE: Checkout stepper (địa chỉ→thanh toán→review). Success page. BE: Order management APIs (User) | Checkout UI ✅ Order APIs ✅ |
| Phú | 📝 FE: Article detail page (TOC, related products). Expert editor (Quill + gắn sản phẩm). BE: Seller stats API | Article detail ✅ Expert editor ✅ |

### Ngày 5 – 🎯 Admin + Orders + Reviews

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔐 Upgrade API (Seller/Expert request). FE: Multi-step upgrade form. BE: Profile + address APIs hoàn chỉnh | Upgrade forms ✅ Profile complete ✅ |
| Lũy | 💰 BE: Wallet credit khi đơn delivered (event-driven). Payout report API. BE: Admin user management APIs | Wallet credit ✅ Payout report ✅ |
| Duyên | 🛍️ FE: Admin product approval UI. BE: Review CRUD + reply API. FE: Review form + list UI | Admin product UI ✅ Review APIs+UI ✅ |
| Vi | 📦 FE: Order history (tabs). Order detail + tracking timeline. FE: Seller order management table | Order history+detail ✅ Seller orders ✅ |
| Phú | 📝 FE: Admin article approval UI. FE: Admin user management + Seller/Expert approval. BE: Expert stats | Admin approval UIs ✅ |

### Ngày 6 – 🎯 Dashboard + Search Complete

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 📊 Review tổng thể Auth flow. Fix bugs. Hỗ trợ team connect FE-BE | Auth fully tested ✅ |
| Lũy | 📊 BE: Dashboard Admin + Seller + Expert stats APIs. FE: Seller + Expert dashboard UI | All dashboards ✅ |
| Duyên | 🔍 FE: Search bar + live suggestion. Search results page với URL-driven filter. Fix Product UI bugs | Search complete ✅ |
| Vi | 📦 BE: Seller order workflow (confirm/ship/deliver). Admin order overview APIs. FE: polish | Seller order flow ✅ |
| Phú | 📊 FE: Admin dashboard (KPI cards, charts Chart.js, pending list). Design system polish | Admin dashboard ✅ |

### Ngày 7 – 🎯 Integration Testing

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔧 Integration test Auth flow E2E. Fix Auth bugs. Smoke test tất cả Auth endpoints | Auth E2E tested ✅ |
| Lũy | 🔧 Integration test Payout flow E2E (full cycle). Test concurrent lock. Demo prep payout flow | Payout E2E tested ✅ |
| Duyên | 🔧 Integration test Order+Product. Bug fix UI responsive (375px mobile) | Order+Product tested ✅ |
| Vi | 🔧 Integration test Cart+Checkout+Order E2E. Test coupon edge cases. Fix bugs | Cart+Order E2E tested ✅ |
| Phú | 🔧 Integration test Content + Admin approval. Test article + product approval chains | Content E2E tested ✅ |

### Ngày 8 – 🎯 Bug Fix + Polish

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔧 Fix bugs từ test ngày 7. Production build test. Kiểm tra jar size FE | Build production ✅ |
| Lũy | 🔧 Fix payout edge cases (số dư âm, double submit). Optimize DB queries. Redis cache | Payout bulletproof ✅ |
| Duyên | 🔧 Bug fix UI: loading states, error handling, empty states. Review flow polish | UI polished ✅ |
| Vi | 🔧 Fix order flow bugs. Test return/refund. Error pages (404/500) | Order flow solid ✅ |
| Phú | 🔧 Fix content + dashboard bugs. Export CSV payout. Responsive dashboard charts | Dashboard responsive ✅ |

### Ngày 9 – 🎯 Demo Prep + Final QA

| Thành viên | Công việc | Deliverable |
|---|---|---|
| Trường | 🔧 Performance check + Docker production build. Smoke test all endpoints. Final deploy | Production ready ✅ |
| Lũy | 🔧 Demo script: full payout journey. Reset + reload seed data. Ghi chú demo | Demo script ✅ |
| Duyên | 🔧 Final QA Product + Review + Search. Screenshot UI cho báo cáo | Final QA ✅ |
| Vi | 🔧 Final QA Cart + Order. Test full user journey. Fix minor UI issues | User journey ✅ |
| Phú | 📝 README.md + báo cáo đóng góp. API doc. Screenshot giao diện | Docs hoàn chỉnh ✅ |

### Ngày 10 – 🎯 Demo Day 🎉

| Thành viên | Nội dung Present | Deliverable |
|---|---|---|
| Trường | Auth system (OTP demo live). Payout FE (wallet + rút tiền). Profile + upgrade flow | Auth demo ✅ |
| Lũy | Payout system (CRITICAL demo). Admin đối soát. Transaction history. Báo cáo doanh thu | Payout demo ✅ |
| Duyên | Product flow (Seller thêm → Admin duyệt → Public). Review system. Search | Product demo ✅ |
| Vi | User mua hàng full journey (Browse → Cart → Checkout → Order → Track → Review) | Shopping demo ✅ |
| Phú | Expert soạn bài → Admin duyệt → Đăng bài. Admin dashboard tổng quan. Payout admin | Content+Admin demo ✅ |

---

## 💰 PAYOUT SYSTEM – THIẾT KẾ CHI TIẾT

**Người phụ trách:** Lũy (Lead BE) + Trường (Lead FE) | **Priority:** 🔴 CRITICAL | **Estimate:** 47h tổng

### 1. DATABASE SCHEMA – 3 TABLES CHÍNH

#### TABLE: wallet

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| user_id | BIGINT FK UNIQUE | references users.id |
| balance | DECIMAL(15,2) DEFAULT 0.00 | Tổng số dư |
| locked_amount | DECIMAL(15,2) DEFAULT 0.00 | Đang chờ rút |
| currency | VARCHAR(3) DEFAULT 'VND' | |
| updated_at | TIMESTAMP | |

> `availableBalance = balance - locked_amount`. Constraint: `CHECK(balance >= locked_amount)`

#### TABLE: payout_request

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| wallet_id | BIGINT FK | |
| requester_id | BIGINT FK | references users.id |
| amount | DECIMAL(15,2) | |
| bank_name | VARCHAR(100) | |
| account_no | VARCHAR(30) | |
| account_name | VARCHAR(100) | |
| status | ENUM | 'PENDING','LOCKED','APPROVED','PAID','REJECTED' |
| requested_at | TIMESTAMP DEFAULT NOW() | |
| processed_at | TIMESTAMP NULL | |
| admin_id | BIGINT NULL FK | |
| admin_note | TEXT NULL | |

#### TABLE: transaction_log

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| wallet_id | BIGINT FK | |
| type | ENUM | 'CREDIT','DEBIT','LOCK','UNLOCK' |
| amount | DECIMAL(15,2) | |
| balance_after | DECIMAL(15,2) | |
| ref_id | BIGINT NULL | order_id hoặc payout_id |
| description | VARCHAR(255) | |
| created_at | TIMESTAMP DEFAULT NOW() | |

### 2. API ENDPOINTS – PAYOUT MODULE

| Method | Endpoint | Role | Request | Response | Logic | Assignee |
|---|---|---|---|---|---|---|
| GET | /api/wallet/me | SELLER, EXPERT | Bearer JWT | {balance, lockedAmount, availableBalance, currency, transactions[]} | WalletService.getMyWallet. Cache Redis 30s | Lũy |
| POST | /api/payout/request | SELLER, EXPERT | {amount, bankName, accountNo, accountName} | {payoutRequestId, status:'PENDING', lockedAmount} | Validate → Lock tiền → Create PENDING → Log LOCK → Notify Admin | Lũy |
| GET | /api/payout/history | SELLER, EXPERT | ?page=0&size=10&status= | Page<PayoutHistoryDto> | findByWallet_UserId pageable, sort requestedAt DESC | Lũy |
| GET | /api/admin/payout | ADMIN | ?status=PENDING&page=0&size=20&role=SELLER | Page<PayoutAdminView> | JOIN payout_request+wallet+user. Filter by status/role/date | Lũy |
| PUT | /api/admin/payout/{id}/approve | ADMIN | {adminNote} | {status:'PAID', processedAt, adminId} | @Transactional: APPROVED→mock payment→PAID. wallet.balance-=amount, locked-=amount. Log DEBIT. Notify | Lũy |
| PUT | /api/admin/payout/{id}/reject | ADMIN | {reason} | {status:'REJECTED', reason} | @Transactional: REJECTED. lockedAmount-=amount (unlock). Log UNLOCK. Notify | Lũy |
| GET | /api/admin/payout/report | ADMIN | ?from=&to=&role= | {totalPaid, totalPending, totalRejected, byRole, byDay[]} | Aggregate queries. Cache Redis 1min | Lũy |
| POST (internal) | /api/wallet/credit | SYSTEM | {userId, amount, refId, description} | {newBalance} | @TransactionalEventListener(OrderDeliveredEvent). UPDATE balance+=amount. Log CREDIT | Lũy |

### 3. PAYOUT FLOW – STATE MACHINE

| Step | Tên bước | FE Action | BE Action | DB Change | Business Rules |
|---|---|---|---|---|---|
| 1 | Tạo yêu cầu rút tiền | Nhập số tiền, chọn ngân hàng, số TK. Preview → Gửi yêu cầu | Validate → Lock tiền → Create PENDING → Log LOCK → Notify Admin | wallet.lockedAmount += amount; payout_request.status = PENDING | Không thể rút quá available. Chỉ 1 request PENDING per wallet |
| 2 | Admin nhận thông báo | Admin thấy badge số đỏ, vào trang đối soát, thấy PENDING | Notification stored. Admin filter PENDING, xem thông tin ngân hàng | Không thay đổi | Admin thấy đầy đủ: tên, role, số tiền, ngân hàng |
| 3 | Admin duyệt (APPROVE) | Nhấn Duyệt, nhập admin note, confirm dialog | @Transactional: APPROVED→mock payment→PAID. wallet.balance-=amount, locked-=amount. Log DEBIT. Notify | wallet.balance -= amount; wallet.lockedAmount -= amount; status = PAID | Idempotency: check status=PENDING trước. Rollback nếu fail |
| 4 | Admin từ chối (REJECT) | Nhấn Từ chối, nhập lý do, confirm | @Transactional: REJECTED. lockedAmount -= amount. Log UNLOCK. Notify với lý do | wallet.lockedAmount -= amount; status = REJECTED | Tiền trả lại available. User nhận thông báo với lý do |
| 5 | Xem kết quả | Badge status update realtime (polling 30s). History hiện PAID/REJECTED với note | GET /payout/history trả về trạng thái mới | Không thay đổi | PAID: đã chuyển khoản (mock). REJECTED: có thể tạo request mới |
| 6 | Wallet credit (tự động) | Balance card tự update sau 30s polling | OrderDeliveredEvent → WalletService.credit(sellerId, amount*0.9). 10% platform fee | wallet.balance += orderAmount * 0.9; Log CREDIT | Chỉ credit sau Order=DELIVERED. 90% seller, 10% platform |

### 4. SECURITY & EDGE CASES

| Vấn đề | Giải pháp |
|---|---|
| Pessimistic Lock | @Lock(LockModeType.PESSIMISTIC_WRITE) khi UPDATE wallet để tránh race condition |
| Idempotency | Check payout_request.status=PENDING trước khi approve. Nếu đã PAID thì return 409 Conflict |
| Số dư âm | CONSTRAINT CHECK(balance >= 0) ở DB level. Service level: validate amount ≤ availableBalance trước khi lock |
| Role validation | @PreAuthorize: /api/payout/** chỉ SELLER/EXPERT. /api/admin/payout/** chỉ ADMIN. Không tự approve của mình |
| Audit trail | Mọi thay đổi wallet đều INSERT vào transaction_log. Không xóa record |
| Concurrent requests | 1 wallet chỉ có tối đa 1 PENDING request (UNIQUE constraint hoặc application-level check) |

---

## 🌱 SEED DATA

### 👥 Users (5 người)

| # | Email | Password | Tên | Role | Status | Notes |
|---|---|---|---|---|---|---|
| 1 | admin@orgo.vn | Admin@123 | Nguyễn Admin | ADMIN | ACTIVE | Tài khoản quản trị viên |
| 2 | truong@gmail.com | User@123 | Nguyễn Trí Trường | USER | ACTIVE | User thường, có orders |
| 3 | user2@gmail.com | User@123 | Trần Thị Bình | USER | ACTIVE | User test mua hàng |
| 4 | seller_pending@gmail.com | User@123 | Lê Văn Minh | USER | ACTIVE | Đang chờ duyệt Seller |
| 5 | expert_pending@gmail.com | User@123 | Phạm Thị Lan | USER | ACTIVE | Đang chờ duyệt Expert |

### 🏪 Sellers (3 người – đã duyệt)

| # | Email | Shop Name | Sản phẩm | Bank | Wallet Balance | Wallet Locked |
|---|---|---|---|---|---|---|
| 1 | seller1@orgo.vn | Vườn Xanh Organic | 4 products | Vietcombank - 1234567890 | 5,200,000 | 500,000 |
| 2 | seller2@orgo.vn | Nông Trại Sạch | 3 products | BIDV - 9876543210 | 2,800,000 | 0 |
| 3 | seller3@orgo.vn | Hữu Cơ Đà Lạt | 3 products | Techcombank - 5555666677 | 1,500,000 | 1,500,000 |

### 🎓 Experts (3 người – đã duyệt)

| # | Email | Tên | Chuyên môn | Bài viết | Wallet Balance | Wallet Locked |
|---|---|---|---|---|---|---|
| 1 | expert1@orgo.vn | TS. Nguyễn Dinh Dưỡng | Dinh dưỡng học | 3 articles approved | 800,000 | 0 |
| 2 | expert2@orgo.vn | BS. Trần Sức Khỏe | Y học cổ truyền | 2 articles approved | 600,000 | 600,000 |
| 3 | expert3@orgo.vn | Th.S Lê Nông Nghiệp | Nông nghiệp hữu cơ | 1 article approved | 200,000 | 0 |

### 🛍️ Products (10 sản phẩm)

| # | Tên | Seller | Category | Giá | Stock | Status |
|---|---|---|---|---|---|---|
| 1 | Rau Cải Hữu Cơ Đà Lạt 500g | Vườn Xanh | Rau củ | 45,000 | 200 | ACTIVE |
| 2 | Dầu Oliu Extra Virgin 500ml | Vườn Xanh | Dầu ăn | 320,000 | 50 | ACTIVE |
| 3 | Gạo Lứt Hữu Cơ 5kg | Vườn Xanh | Ngũ cốc | 180,000 | 100 | ACTIVE |
| 4 | Mật Ong Rừng Tây Nguyên 500g | Vườn Xanh | Mật ong | 250,000 | 80 | ACTIVE |
| 5 | Cà Chua Bi Hữu Cơ 1kg | Nông Trại Sạch | Rau củ | 65,000 | 150 | ACTIVE |
| 6 | Trứng Gà Hữu Cơ (10 quả) | Nông Trại Sạch | Trứng | 95,000 | 60 | ACTIVE |
| 7 | Sữa Hạt Macadamia 1L | Nông Trại Sạch | Đồ uống | 185,000 | 40 | ACTIVE |
| 8 | Bơ Hass Hữu Cơ 1kg | Hữu Cơ Đà Lạt | Trái cây | 120,000 | 30 | ACTIVE |
| 9 | Trà Xanh Matcha Hữu Cơ 100g | Hữu Cơ Đà Lạt | Trà | 210,000 | 70 | ACTIVE |
| 10 | Hạt Chia Hữu Cơ 500g | Hữu Cơ Đà Lạt | Hạt | 195,000 | 90 | PENDING_REVIEW |

### 📦 Orders (5 đơn hàng)

| # | Order ID | User | Seller | Sản phẩm | Tổng tiền | Status | Payment |
|---|---|---|---|---|---|---|---|
| 1 | ORD-001 | truong@gmail.com | Vườn Xanh | Rau Cải + Dầu Oliu | 365,000 | DELIVERED | COD |
| 2 | ORD-002 | user2@gmail.com | Nông Trại Sạch | Cà Chua + Trứng | 160,000 | SHIPPING | Transfer |
| 3 | ORD-003 | truong@gmail.com | Hữu Cơ Đà Lạt | Bơ Hass + Matcha | 330,000 | CONFIRMED | COD |
| 4 | ORD-004 | user2@gmail.com | Vườn Xanh | Gạo Lứt 5kg | 180,000 | PENDING | COD |
| 5 | ORD-005 | truong@gmail.com | Nông Trại Sạch | Sữa Hạt Macadamia | 185,000 | CANCELLED | Transfer |

### ⭐ Reviews (5 đánh giá)

| # | Product | User | Rating | Comment | Status |
|---|---|---|---|---|---|
| 1 | Rau Cải Hữu Cơ | truong@gmail.com | 5 | Rau rất tươi, giao hàng nhanh. Gia đình rất thích! | PUBLISHED |
| 2 | Dầu Oliu Extra Virgin | truong@gmail.com | 4 | Chất lượng tốt, hương vị thơm. Hơi đắt nhưng xứng đáng | PUBLISHED |
| 3 | Cà Chua Bi Hữu Cơ | user2@gmail.com | 5 | Ngon, tươi, không có hóa chất. Sẽ mua lại | PUBLISHED |
| 4 | Trứng Gà Hữu Cơ | user2@gmail.com | 4 | Trứng lòng đỏ đẹp, không tanh. Giao hàng cẩn thận | PUBLISHED |
| 5 | Bơ Hass Hữu Cơ | truong@gmail.com | 3 | Bơ chín vừa phải, ok nhưng size hơi nhỏ | PUBLISHED |

### 💰 Payout Requests (2 requests)

| # | Requester | Role | Số tiền | Ngân hàng | Số TK | Status | Admin Note |
|---|---|---|---|---|---|---|---|
| 1 | seller1@orgo.vn | SELLER | 500,000 | Vietcombank | 1234567890 | PENDING | (chờ Admin duyệt) |
| 2 | expert2@orgo.vn | EXPERT | 600,000 | BIDV | 1111222233 | PAID | Đã chuyển khoản 15/04/2026 |

---

## ⚡ PARALLEL DEVELOPMENT STRATEGY

> Nguyên tắc: FE dùng Mock Service Worker (WireMock) hoặc json-server mock API trước khi BE xong. Connect thật sau.

### Mock API Strategy (Ngày 1-2)

| Endpoint group | Endpoints | FE assignee | Mock strategy | Connect thật |
|---|---|---|---|---|
| Auth endpoints | /auth/register, /auth/login, /auth/verify-otp | Trường | JSON file /mocks/auth.json + WireMock handler | Ngày 2 |
| Product list/detail | /api/products, /api/products/{id} | Duyên | 10 sản phẩm từ seed data làm JSON mock | Ngày 2 |
| Cart APIs | /api/cart (GET/POST/PUT/DELETE) | Vi | Spring Session Cart khi chưa có BE | Ngày 2 |
| Article APIs | /api/articles, /api/articles/{id} | Phú | 3 bài viết mẫu JSON mock | Ngày 2 |
| Wallet + Payout | /api/wallet/me, /api/payout/* | Trường | Mock wallet data, simulate flow | Ngày 3 |

### Dependency Map

| Module | Depends On | Blocks | Phải xong trước | Cách tránh blocking |
|---|---|---|---|---|
| Auth BE (JWT filter) | – | TẤT CẢ modules khác | Ngày 1 | Dùng token hardcode 'Bearer dev-token' ngày 1 |
| Product Schema | – | Cart, Order, Review | Ngày 1 | Cart dùng mock productId = 1,2,3 |
| Wallet Schema | – | Payout request, Credit | Ngày 1 | Lũy làm Wallet schema ngày 1, Payout request ngày 2 |
| Cart API | Product API | Checkout API | Ngày 2 | Vi làm Cart API độc lập với Product |
| Order API | Cart + Product API | Payout Credit (event) | Ngày 3 | Payout credit là downstream event sau Order deliver |
| Article API | Auth API | Admin approval | Ngày 2 | Phú làm Article API song song Auth sau khi có schema |
| Dashboard APIs | Order + User + Wallet | FE Dashboard | Ngày 4 | FE Dashboard dùng mock data đến khi API xong |
| Payout APIs | Wallet + Auth | Admin Payout UI | Ngày 3 | Trường làm FE Wallet UI dùng mock trong khi Lũy làm API |

### Workload Balance Check

| Thành viên | View tasks | BE tasks | Payout tasks | Total tasks | Est hours | Lead modules | Balance |
|---|---|---|---|---|---|---|---|
| Trường | 6 | 8 | 3 (FE Payout+Auth BE) | 14 | ~54h | Auth + Payout FE + Profile | ✅ Balanced |
| Lũy | 5 | 9 | 8 (All Payout BE) | 14 | ~56h | Payout + Wallet + Infra | ✅ Balanced |
| Duyên | 7 | 7 | 0 (support only) | 14 | ~52h | Product + Review + Search | ✅ Balanced |
| Vi | 7 | 7 | 0 (support only) | 14 | ~52h | Cart + Checkout + Order | ✅ Balanced |
| Phú | 7 | 7 | 0 (support only) | 14 | ~52h | Content + Dashboard + Admin | ✅ Balanced |

---

## 📊 GANTT CHART – AI LÀM GÌ NGÀY NÀO

| Thành viên | Module | Ngày 1 | Ngày 2 | Ngày 3 | Ngày 4 | Ngày 5 | Ngày 6 | Ngày 7 | Ngày 8 | Ngày 9 | Ngày 10 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Trường | 🔐 Auth + 💰 Payout FE | JWT+Setup | OTP+Login | Forgot+RBAC | Payout Modal | Profile+Upgrade | Review Auth | E2E Test | Bug Fix | Prod Build | 🎯 Demo |
| Lũy | 💰 Payout BE + 🔧 Infra | DB Schema+Seed | Wallet+Request | Admin Payout | Reject+History | Credit Event | Report API | E2E Payout | Edge Cases | Demo Script | 🎯 Demo |
| Duyên | 🛍️ Product + ⭐ Review + 🔍 Search | Product Schema | Product CRUD | Shop UI+Search | Product Detail | Admin+Review | Search FE | Bug Fix | UI Polish | Final QA | 🎯 Demo |
| Vi | 🛒 Cart + 📦 Order | Cart Schema | Cart CRUD | Checkout API | Checkout FE | Order FE | Seller Orders | E2E Order | Bug Fix | Final QA | 🎯 Demo |
| Phú | 📝 Content + 📊 Dashboard + 👑 Admin | Content Schema | Article CRUD | Blog UI | Expert Editor | Admin UIs | Dashboard UI | E2E Content | Polish | README | 🎯 Demo |

### Legend

- 🔴 Payout-related task (ưu tiên cao nhất)
- 🟢 Demo day task
- 🔵 Trường – Auth + Payout FE
- 🟠 Lũy – Payout BE + Infra
- 🟢 Duyên – Product + Review + Search
- 🟡 Vi – Cart + Order
- 🟣 Phú – Content + Dashboard + Admin
