-- =============================================
-- Insert Sample Products
-- =============================================

USE orgo;
GO

-- Tạo bảng nếu chưa có
IF OBJECT_ID('SanPham', 'U') IS NULL
BEGIN
    CREATE TABLE SanPham (
        id_san_pham INT PRIMARY KEY IDENTITY(1,1),
        id_nha_ban_hang INT,
        id_danh_muc INT,
        ten_san_pham NVARCHAR(255),
        slug NVARCHAR(255),
        mo_ta_ngan NVARCHAR(MAX),
        mo_ta_chi_tiet NVARCHAR(MAX),
        thanh_phan NVARCHAR(MAX),
        nguon_goc NVARCHAR(255),
        loi_ich_suc_khoe NVARCHAR(MAX),
        huong_dan_su_dung NVARCHAR(MAX),
        trang_thai NVARCHAR(50),
        sao_trung_binh FLOAT,
        tong_danh INT
    );
END
GO

-- Xóa dữ liệu cũ
DELETE FROM SanPham;
GO

-- Insert sample products
SET IDENTITY_INSERT SanPham ON;

INSERT INTO SanPham (id_san_pham, ten_san_pham, slug, mo_ta_ngan, mo_ta_chi_tiet, nguon_goc, 
    loi_ich_suc_khoe, huong_dan_su_dung, thanh_phan, trang_thai, sao_trung_binh, tong_danh)
VALUES 
(1, N'Rau cải hữu cơ Đà Lạt - Chuẩn GlobalGAP', N'rau-cai-huu-co-da-lat',
 N'Rau cải hữu cơ tươi ngon, trồng tại Đà Lạt với quy trình canh tác tự nhiên',
 N'<p>Rau cải hữu cơ được trồng tại Đà Lạt với quy trình canh tác hoàn toàn tự nhiên, không sử dụng thuốc trừ sâu hay phân bón hóa học. Sản phẩm đạt chứng nhận GlobalGAP và được thu hoạch vào buổi sáng sớm để đảm bảo độ tươi ngon tối đa.</p>',
 N'Đà Lạt, Lâm Đồng',
 N'<ul><li>Giàu vitamin A, C, K và các khoáng chất thiết yếu</li><li>Chứa nhiều chất xơ, tốt cho hệ tiêu hóa</li><li>Chất chống oxy hóa cao, giúp tăng cường miễn dịch</li><li>Hỗ trợ giảm cân và kiểm soát đường huyết</li></ul>',
 N'<p>Rửa sạch dưới vòi nước, có thể dùng ngay hoặc chế biến thành các món xào, luộc, nấu canh. Bảo quản trong ngăn mát tủ lạnh, sử dụng trong vòng 3-5 ngày.</p>',
 N'100% rau cải hữu cơ',
 N'AVAILABLE', 4.8, 156),

(2, N'Cà chua bi hữu cơ mọng nước', N'ca-chua-bi-huu-co',
 N'Cà chua bi hữu cơ ngọt tự nhiên, giàu lycopene',
 N'<p>Cà chua bi hữu cơ được trồng tại các nông trại đạt chuẩn VietGAP. Quả nhỏ, vỏ mỏng, thịt chắc, ngọt tự nhiên và giàu dinh dưỡng. Không sử dụng thuốc kích thích tăng trưởng.</p>',
 N'Sun Valley Farm, Đà Lạt',
 N'<ul><li>Giàu lycopene - chất chống oxy hóa mạnh</li><li>Tốt cho tim mạch và làn da</li><li>Chứa vitamin C, K và kali</li><li>Hỗ trợ giảm nguy cơ ung thư</li></ul>',
 N'<p>Rửa sạch và ăn trực tiếp hoặc làm salad. Có thể nấu canh, xào hoặc làm nước ép. Bảo quản ở nhiệt độ phòng hoặc trong tủ lạnh.</p>',
 N'100% cà chua bi hữu cơ',
 N'AVAILABLE', 4.9, 203),

(3, N'Bơ Hass hữu cơ loại 1 - Nhập khẩu', N'bo-hass-huu-co',
 N'Bơ Hass hữu cơ cao cấp, béo ngậy, giàu omega-3',
 N'<p>Bơ Hass hữu cơ nhập khẩu từ các nông trại đạt chuẩn quốc tế. Quả to, thịt béo ngậy, hạt nhỏ. Giàu chất béo lành mạnh và các vitamin thiết yếu.</p>',
 N'Premium Green Farm',
 N'<ul><li>Giàu chất béo không bão hòa đơn (omega-3)</li><li>Tốt cho tim mạch và não bộ</li><li>Chứa vitamin E, K, B6</li><li>Hỗ trợ giảm cholesterol xấu</li><li>Tốt cho làn da và tóc</li></ul>',
 N'<p>Để chín tự nhiên ở nhiệt độ phòng. Khi quả mềm là đã chín. Có thể ăn trực tiếp, làm sinh tố, salad hoặc bánh mì bơ. Bảo quản trong tủ lạnh sau khi chín.</p>',
 N'100% bơ Hass hữu cơ',
 N'AVAILABLE', 4.7, 89),

(4, N'Rau muống hữu cơ tươi ngon', N'rau-muong-huu-co',
 N'Rau muống hữu cơ tươi, giòn, ngọt tự nhiên',
 N'<p>Rau muống hữu cơ được trồng bằng phương pháp thủy canh sạch, không sử dụng hóa chất. Rau tươi, giòn, ngọt và giàu dinh dưỡng.</p>',
 N'Đà Lạt, Lâm Đồng',
 N'<ul><li>Giàu vitamin A, C và sắt</li><li>Tốt cho mắt và hệ miễn dịch</li><li>Chứa nhiều chất xơ</li><li>Hỗ trợ tiêu hóa</li></ul>',
 N'<p>Rửa sạch, có thể luộc, xào tỏi hoặc nấu canh. Bảo quản trong tủ lạnh, sử dụng trong 2-3 ngày.</p>',
 N'100% rau muống hữu cơ',
 N'AVAILABLE', 4.6, 124),

(5, N'Dâu tây Đà Lạt hữu cơ', N'dau-tay-da-lat-huu-co',
 N'Dâu tây hữu cơ ngọt thanh, thơm tự nhiên',
 N'<p>Dâu tây hữu cơ Đà Lạt được trồng trong nhà kính, kiểm soát chặt chẽ. Quả to, đỏ đều, ngọt thanh và thơm tự nhiên.</p>',
 N'Đà Lạt, Lâm Đồng',
 N'<ul><li>Giàu vitamin C và chất chống oxy hóa</li><li>Tốt cho làn da</li><li>Hỗ trợ tăng cường miễn dịch</li><li>Giúp kiểm soát đường huyết</li></ul>',
 N'<p>Rửa nhẹ nhàng trước khi ăn. Có thể ăn trực tiếp, làm sinh tố, salad trái cây hoặc trang trí bánh. Bảo quản trong tủ lạnh, sử dụng trong 3-4 ngày.</p>',
 N'100% dâu tây hữu cơ',
 N'AVAILABLE', 4.9, 267),

(6, N'Xà lách lô lô hữu cơ', N'xa-lach-lo-lo-huu-co',
 N'Xà lách lô lô hữu cơ giòn, ngọt, tươi mát',
 N'<p>Xà lách lô lô hữu cơ được trồng bằng phương pháp thủy canh hiện đại. Lá giòn, ngọt và giàu dinh dưỡng, lý tưởng cho salad.</p>',
 N'Bio Farm, Đà Lạt',
 N'<ul><li>Giàu vitamin K và folate</li><li>Ít calo, tốt cho giảm cân</li><li>Chứa nhiều chất xơ</li><li>Tốt cho xương và tim mạch</li></ul>',
 N'<p>Rửa sạch, để ráo nước. Dùng làm salad, gỏi hoặc ăn kèm. Bảo quản trong tủ lạnh, sử dụng trong 3-5 ngày.</p>',
 N'100% xà lách lô lô hữu cơ',
 N'AVAILABLE', 4.5, 98);

SET IDENTITY_INSERT SanPham OFF;
GO

PRINT '✓ Inserted 6 sample products';
GO

-- Verify
SELECT id_san_pham, ten_san_pham, slug, trang_thai, sao_trung_binh, tong_danh 
FROM SanPham;
GO
