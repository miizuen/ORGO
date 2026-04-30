-- =============================================
-- ORGO Database - Complete Setup Script
-- Tạo database, tables, và insert dữ liệu mẫu
-- Author: Võ Đức Phú (MSSV: 23115053122130)
-- =============================================

-- Tạo database nếu chưa có
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'orgo')
BEGIN
    CREATE DATABASE orgo;
    PRINT 'Database orgo created successfully!';
END
ELSE
BEGIN
    PRINT 'Database orgo already exists.';
END
GO

USE orgo;
GO

-- =============================================
-- DROP TABLES (nếu đã tồn tại - để chạy lại script)
-- =============================================
IF OBJECT_ID('ThongKeBaiViet', 'U') IS NOT NULL DROP TABLE ThongKeBaiViet;
IF OBJECT_ID('BaiViet_SanPham', 'U') IS NOT NULL DROP TABLE BaiViet_SanPham;
IF OBJECT_ID('BaiViet', 'U') IS NOT NULL DROP TABLE BaiViet;
IF OBJECT_ID('NguoiDung', 'U') IS NOT NULL DROP TABLE NguoiDung;
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('VaiTro', 'U') IS NOT NULL DROP TABLE VaiTro;
GO

PRINT 'Old tables dropped (if existed).';
GO

-- =============================================
-- CREATE TABLES
-- =============================================

-- Table 1: VaiTro (Roles)
CREATE TABLE VaiTro (
    id_vai_tro INT PRIMARY KEY IDENTITY(1,1),
    ten_vai_tro NVARCHAR(255) NOT NULL
);
PRINT '✓ Table VaiTro created';
GO

-- Table 2: TaiKhoan (Accounts)
CREATE TABLE TaiKhoan (
    id_tai_khoan INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(255) NOT NULL UNIQUE,
    mat_khau NVARCHAR(255) NOT NULL,
    anhDaiDien NVARCHAR(255),
    id_vai_tro INT NOT NULL,
    FOREIGN KEY (id_vai_tro) REFERENCES VaiTro(id_vai_tro)
);
PRINT '✓ Table TaiKhoan created';
GO

-- Table 3: NguoiDung (User Profiles)
CREATE TABLE NguoiDung (
    id_nguoi_dung INT PRIMARY KEY IDENTITY(1,1),
    ho_ten NVARCHAR(255),
    so_dien_thoai NVARCHAR(255),
    email NVARCHAR(255),
    trang_thai NVARCHAR(50),
    id_tai_khoan INT,
    reset_otp NVARCHAR(6),
    otp_expiry DATETIME,
    FOREIGN KEY (id_tai_khoan) REFERENCES TaiKhoan(id_tai_khoan)
);
PRINT '✓ Table NguoiDung created';
GO

-- Table 4: BaiViet (Articles)
CREATE TABLE BaiViet (
    id_chuyen_muc INT PRIMARY KEY IDENTITY(1,1),
    id_chuyen_gia INT,
    id_nguoi_duyet INT,
    tieu_de NVARCHAR(255),
    slug NVARCHAR(255),
    anh_bia NVARCHAR(255),
    tom_tat NVARCHAR(MAX),
    noi_dung NVARCHAR(MAX),
    ngay_dang DATETIME,
    ngay_cap_nhat DATETIME,
    luot_xem INT DEFAULT 0,
    trang_thai NVARCHAR(50),
    ly_do_tu_choi NVARCHAR(255)
);
PRINT '✓ Table BaiViet created';
GO

-- Table 5: BaiViet_SanPham (Article-Product Link)
CREATE TABLE BaiViet_SanPham (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_bai_viet INT NOT NULL,
    id_san_pham INT NOT NULL,
    FOREIGN KEY (id_bai_viet) REFERENCES BaiViet(id_chuyen_muc)
);
PRINT '✓ Table BaiViet_SanPham created';
GO

-- Table 6: ThongKeBaiViet (Article Stats)
CREATE TABLE ThongKeBaiViet (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_bai_viet INT NOT NULL,
    luot_xem INT DEFAULT 0,
    luot_thich INT DEFAULT 0,
    ngay DATE DEFAULT CAST(GETDATE() AS DATE),
    FOREIGN KEY (id_bai_viet) REFERENCES BaiViet(id_chuyen_muc)
);
PRINT '✓ Table ThongKeBaiViet created';
GO

PRINT '';
PRINT '===========================================';
PRINT 'ALL TABLES CREATED SUCCESSFULLY!';
PRINT '===========================================';
PRINT '';
GO

-- =============================================
-- INSERT SAMPLE DATA
-- =============================================

PRINT 'Inserting sample data...';
GO

-- =====================
-- 1. INSERT VAI TRO
-- =====================
SET IDENTITY_INSERT VaiTro ON;
INSERT INTO VaiTro (id_vai_tro, ten_vai_tro) VALUES (1, N'ADMIN');
INSERT INTO VaiTro (id_vai_tro, ten_vai_tro) VALUES (2, N'USER');
INSERT INTO VaiTro (id_vai_tro, ten_vai_tro) VALUES (3, N'SELLER');
INSERT INTO VaiTro (id_vai_tro, ten_vai_tro) VALUES (4, N'EXPERT');
SET IDENTITY_INSERT VaiTro OFF;
PRINT '✓ Inserted 4 roles (ADMIN, USER, SELLER, EXPERT)';
GO

-- =====================
-- 2. INSERT TAI KHOAN
-- =====================
-- Password: 123456 (BCrypt hash)
-- Hash: $2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO
SET IDENTITY_INSERT TaiKhoan ON;

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (1, N'admin', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 1, NULL);

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (2, N'truong', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 2, NULL);

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (3, N'phu', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 3, NULL);

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (4, N'vi', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 2, NULL);

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (5, N'expert1', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 4, NULL);

INSERT INTO TaiKhoan (id_tai_khoan, username, mat_khau, id_vai_tro, anhDaiDien)
VALUES (6, N'seller1', N'$2a$10$xYYyjqaquTOFhQ0sx24CruTMoKPJP/nrAhN5jrx5BRjdiOClvcNZO', 3, NULL);

SET IDENTITY_INSERT TaiKhoan OFF;
PRINT '✓ Inserted 6 accounts (all with password: 123456)';
GO

-- =====================
-- 3. INSERT NGUOI DUNG
-- =====================
SET IDENTITY_INSERT NguoiDung ON;

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (1, 1, N'Administrator', N'admin@orgo.com', N'0900000001', N'ACTIVE');

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (2, 2, N'Nguyễn Trí Trường', N'nguyentritruong2005@gmail.com', N'0935233627', N'ACTIVE');

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (3, 3, N'Võ Đức Phú', N'voducphu1912005@gmail.com', N'0900000003', N'ACTIVE');

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (4, 4, N'Nguyễn Hà Vi', N'vi@orgo.com', N'0900000004', N'ACTIVE');

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (5, 5, N'Lê Văn Chuyên Gia', N'expert1@orgo.com', N'0901234569', N'ACTIVE');

INSERT INTO NguoiDung (id_nguoi_dung, id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (6, 6, N'Hoàng Văn Bán Hàng', N'seller1@orgo.com', N'0901234571', N'ACTIVE');

SET IDENTITY_INSERT NguoiDung OFF;
PRINT '✓ Inserted 6 user profiles';
GO

-- =====================
-- 4. INSERT BAI VIET
-- =====================
SET IDENTITY_INSERT BaiViet ON;

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, ngay_dang, luot_xem)
VALUES (1, 5, N'10 Siêu thực phẩm hữu cơ tốt cho sức khỏe', N'sieu-thuc-pham-huu-co',
 N'<h2>Giới thiệu về siêu thực phẩm</h2><p>Siêu thực phẩm hữu cơ là những thực phẩm giàu dinh dưỡng, chứa nhiều vitamin, khoáng chất và chất chống oxy hóa. Chúng giúp tăng cường sức khỏe, cải thiện hệ miễn dịch và phòng ngừa bệnh tật.</p><h3>1. Quả việt quất hữu cơ</h3><p>Việt quất chứa nhiều anthocyanin, giúp cải thiện trí nhớ và sức khỏe tim mạch. Ăn việt quất thường xuyên có thể giảm nguy cơ mắc bệnh tim mạch và tiểu đường.</p><h3>2. Rau bina hữu cơ</h3><p>Rau bina giàu sắt, canxi và vitamin K, tốt cho xương và máu. Đây là nguồn dinh dưỡng tuyệt vời cho người ăn chay.</p>', 
 N'APPROVED', N'https://images.unsplash.com/photo-1490645935967-10de6ba17061', GETDATE(), 1250);

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, ngay_dang, luot_xem)
VALUES (2, 5, N'Cách trồng rau hữu cơ tại nhà', N'trong-rau-huu-co',
 N'<h2>Bắt đầu vườn rau hữu cơ</h2><p>Trồng rau hữu cơ tại nhà không chỉ giúp bạn có nguồn thực phẩm sạch mà còn tiết kiệm chi phí và bảo vệ môi trường.</p><h3>Chuẩn bị đất</h3><p>Đất trồng cần được bổ sung phân hữu cơ, đảm bảo thoát nước tốt và có độ pH từ 6.0-7.0.</p><h3>Chọn giống rau</h3><p>Nên chọn các loại rau dễ trồng như rau muống, rau cải, cà chua bi.</p>',
 N'APPROVED', N'https://images.unsplash.com/photo-1464226184884-fa280b87c399', GETDATE(), 980);

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, ngay_dang, luot_xem)
VALUES (3, 5, N'Công thức sinh tố detox cho buổi sáng', N'sinh-to-detox',
 N'<h2>Nguyên liệu</h2><ul><li>1 quả chuối chín</li><li>1 cốc rau bina tươi</li><li>1/2 quả bơ</li><li>200ml sữa hạnh nhân</li><li>1 thìa mật ong</li></ul><h2>Cách làm</h2><p>1. Cho tất cả nguyên liệu vào máy xay sinh tố</p><p>2. Xay nhuyễn trong 2-3 phút</p><p>3. Rót ra ly và thưởng thức ngay</p><h3>Lợi ích</h3><p>Sinh tố này giúp thanh lọc cơ thể, cung cấp năng lượng cho cả ngày và tốt cho làn da.</p>',
 N'APPROVED', N'https://images.unsplash.com/photo-1505252585461-04db1eb84625', GETDATE(), 1450);

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, luot_xem)
VALUES (4, 5, N'Lợi ích của nông nghiệp hữu cơ', N'nong-nghiep-huu-co',
 N'<h2>Nông nghiệp hữu cơ là gì?</h2><p>Nông nghiệp hữu cơ là phương pháp canh tác không sử dụng hóa chất tổng hợp, thuốc trừ sâu độc hại hay phân bón hóa học.</p><h3>Lợi ích cho môi trường</h3><p>Giảm ô nhiễm đất, nước và không khí. Bảo vệ đa dạng sinh học và hệ sinh thái.</p><h3>Lợi ích cho sức khỏe</h3><p>Thực phẩm hữu cơ an toàn hơn, không chứa hóa chất độc hại.</p>',
 N'PENDING', N'https://images.unsplash.com/photo-1500382017468-9049fed747ef', 45);

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, luot_xem)
VALUES (5, 5, N'Thực đơn ăn chay 7 ngày', N'thuc-don-an-chay',
 N'<h2>Ngày 1: Thứ Hai</h2><p>Sáng: Cháo yến mạch với trái cây</p><p>Trưa: Salad quinoa với rau củ nướng</p><p>Tối: Đậu hũ xào rau củ</p><h2>Ngày 2: Thứ Ba</h2><p>Sáng: Bánh mì nguyên cám với bơ đậu phộng</p><p>Trưa: Bún chay với nấm và rau thơm</p><p>Tối: Cơm gạo lứt với canh rau</p>',
 N'PENDING', N'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', 32);

INSERT INTO BaiViet (id_chuyen_muc, id_chuyen_gia, tieu_de, slug, noi_dung, trang_thai, anh_bia, luot_xem)
VALUES (6, 5, N'Câu hỏi thường gặp về thực phẩm hữu cơ', N'faq-thuc-pham-huu-co',
 N'<h3>Thực phẩm hữu cơ có đắt hơn không?</h3><p>Có, thực phẩm hữu cơ thường đắt hơn 20-30% so với thực phẩm thông thường do chi phí sản xuất cao hơn và năng suất thấp hơn.</p><h3>Làm sao nhận biết thực phẩm hữu cơ thật?</h3><p>Kiểm tra tem chứng nhận hữu cơ từ các tổ chức uy tín như USDA Organic, EU Organic, hoặc các tổ chức chứng nhận trong nước.</p><h3>Thực phẩm hữu cơ có bảo quản được lâu không?</h3><p>Do không sử dụng chất bảo quản, thực phẩm hữu cơ thường có thời gian bảo quản ngắn hơn.</p>',
 N'PENDING', N'https://images.unsplash.com/photo-1488459716781-31db52582fe9', 28);

SET IDENTITY_INSERT BaiViet OFF;
PRINT '✓ Inserted 6 articles (3 APPROVED, 3 PENDING)';
GO

-- =====================
-- 5. INSERT THONG KE BAI VIET
-- =====================
SET IDENTITY_INSERT ThongKeBaiViet ON;

INSERT INTO ThongKeBaiViet (id, id_bai_viet, luot_xem, luot_thich, ngay)
VALUES 
(1, 1, 1250, 89, CAST(GETDATE() AS DATE)),
(2, 2, 980, 67, CAST(GETDATE() AS DATE)),
(3, 3, 1450, 102, CAST(GETDATE() AS DATE)),
(4, 4, 45, 3, CAST(GETDATE() AS DATE)),
(5, 5, 32, 2, CAST(GETDATE() AS DATE)),
(6, 6, 28, 1, CAST(GETDATE() AS DATE));

SET IDENTITY_INSERT ThongKeBaiViet OFF;
PRINT '✓ Inserted 6 article stats records';
GO

-- =============================================
-- VERIFICATION
-- =============================================
PRINT '';
PRINT '===========================================';
PRINT 'DATABASE SETUP COMPLETED SUCCESSFULLY!';
PRINT '===========================================';
PRINT '';

SELECT 'VaiTro' AS TableName, COUNT(*) AS RecordCount FROM VaiTro
UNION ALL
SELECT 'TaiKhoan', COUNT(*) FROM TaiKhoan
UNION ALL
SELECT 'NguoiDung', COUNT(*) FROM NguoiDung
UNION ALL
SELECT 'BaiViet', COUNT(*) FROM BaiViet
UNION ALL
SELECT 'ThongKeBaiViet', COUNT(*) FROM ThongKeBaiViet;
GO

PRINT '';
PRINT '===========================================';
PRINT 'LOGIN CREDENTIALS';
PRINT '===========================================';
PRINT 'All accounts use password: 123456';
PRINT '';
PRINT 'Test accounts:';
PRINT '  - admin/123456 (ADMIN role)';
PRINT '  - truong/123456 (USER role)';
PRINT '  - phu/123456 (SELLER role)';
PRINT '  - vi/123456 (USER role)';
PRINT '  - expert1/123456 (EXPERT role)';
PRINT '  - seller1/123456 (SELLER role)';
PRINT '';
PRINT 'You can now run: gradlew bootRun';
PRINT '===========================================';
GO
