-- =====================================================
-- SCRIPT SỬA DỮ LIỆU CŨ: Map id_nguoi_dung từ id_tai_khoan
-- =====================================================
-- Vấn đề: Đơn hàng cũ lưu id_tai_khoan vào cột id_nguoi_dung (sai)
-- Giải pháp: Map sang NguoiDung.id_nguoi_dung (đúng)
-- =====================================================

-- Bước 1: Kiểm tra dữ liệu cũ (đơn hàng có id_nguoi_dung = id_tai_khoan)
SELECT 
    dh.id_don_hang,
    dh.maDonHang,
    dh.id_nguoi_dung AS current_user_id,
    nd.id_nguoi_dung AS correct_user_id,
    nd.ho_ten,
    nd.email
FROM DonHang dh
LEFT JOIN NguoiDung nd ON nd.id_tai_khoan = dh.id_nguoi_dung
WHERE nd.id_nguoi_dung IS NOT NULL
  AND dh.id_nguoi_dung != nd.id_nguoi_dung
ORDER BY dh.ngay_dat DESC;

-- Bước 2: Backup dữ liệu trước khi sửa (QUAN TRỌNG!)
SELECT * 
INTO DonHang_Backup_20260524
FROM DonHang;

-- Bước 3: Cập nhật id_nguoi_dung từ id_tai_khoan → NguoiDung.id_nguoi_dung
UPDATE dh
SET dh.id_nguoi_dung = nd.id_nguoi_dung
FROM DonHang dh
INNER JOIN NguoiDung nd ON nd.id_tai_khoan = dh.id_nguoi_dung
WHERE dh.id_nguoi_dung != nd.id_nguoi_dung;

-- Bước 4: Kiểm tra kết quả sau khi update
SELECT 
    dh.id_don_hang,
    dh.maDonHang,
    dh.id_nguoi_dung,
    nd.ho_ten,
    nd.email,
    nd.id_tai_khoan
FROM DonHang dh
INNER JOIN NguoiDung nd ON nd.id_nguoi_dung = dh.id_nguoi_dung
ORDER BY dh.ngay_dat DESC;

-- Bước 5: Kiểm tra có đơn hàng nào không map được không
SELECT 
    dh.id_don_hang,
    dh.maDonHang,
    dh.id_nguoi_dung,
    dh.ngay_dat
FROM DonHang dh
LEFT JOIN NguoiDung nd ON nd.id_nguoi_dung = dh.id_nguoi_dung
WHERE nd.id_nguoi_dung IS NULL;

-- =====================================================
-- LƯU Ý:
-- 1. Chạy Bước 1 trước để xem có bao nhiêu đơn cần sửa
-- 2. Chạy Bước 2 để backup (BẮT BUỘC)
-- 3. Chạy Bước 3 để update
-- 4. Chạy Bước 4 và 5 để verify
-- 5. Nếu có lỗi, restore từ DonHang_Backup_20260524
-- =====================================================
