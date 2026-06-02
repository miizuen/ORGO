-- Script bổ sung các cột phục vụ tính năng hoàn tiền cho bảng DonHang
-- Chạy script này nếu không sử dụng hibernate.ddl-auto=update

USE orgo;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[DonHang]') AND name = N'ten_ngan_hang_hoan')
BEGIN
    ALTER TABLE [dbo].[DonHang] ADD [ten_ngan_hang_hoan] NVARCHAR(255) NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[DonHang]') AND name = N'so_tai_khoan_hoan')
BEGIN
    ALTER TABLE [dbo].[DonHang] ADD [so_tai_khoan_hoan] NVARCHAR(255) NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[DonHang]') AND name = N'ten_chu_tai_khoan_hoan')
BEGIN
    ALTER TABLE [dbo].[DonHang] ADD [ten_chu_tai_khoan_hoan] NVARCHAR(255) NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[DonHang]') AND name = N'ma_giao_dich_hoan')
BEGIN
    ALTER TABLE [dbo].[DonHang] ADD [ma_giao_dich_hoan] NVARCHAR(255) NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[DonHang]') AND name = N'ngay_hoan_tien')
BEGIN
    ALTER TABLE [dbo].[DonHang] ADD [ngay_hoan_tien] DATETIME2 NULL;
END
GO
