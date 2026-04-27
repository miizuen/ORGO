# ORGO
Chạy chương trình thì sẽ thấy một dòng mã hashcode in dưới terminal (dòng cuối cùng sau khi terminal chạy xong) giống dạng như: $2a$10$bkVcIBbOEZSkI79.kObjOOmm5NwdRu28mbgtujHsKPUpc6a2cvQ82


Sau khi chạy chương trình thì nhập dữ liệu trong database như sau
-- =====================
-- 1. INSERT VAI TRO
-- =====================
INSERT INTO vai_tro (ten_vai_tro) VALUES ('ADMIN');
INSERT INTO vai_tro (ten_vai_tro) VALUES ('USER');
INSERT INTO vai_tro (ten_vai_tro) VALUES ('SELLER');
INSERT INTO vai_tro (ten_vai_tro) VALUES ('EXPERT');

-- =====================
-- 2. INSERT TAI KHOAN
-- =====================
-- Password: 123456 (đã BCrypt)
INSERT INTO tai_khoan (username, mat_khau, id_vai_tro, anh_dai_dien)
VALUES ('admin',  'nhập mã hashcode vừa copy', 1, NULL);

INSERT INTO tai_khoan (username, mat_khau, id_vai_tro, anh_dai_dien)
VALUES ('truong', 'nhập mã hashcode', 2, NULL);

INSERT INTO tai_khoan (username, mat_khau, id_vai_tro, anh_dai_dien)
VALUES ('phu', 'nhập mã hashcode', 3, NULL);

INSERT INTO tai_khoan (username, mat_khau, id_vai_tro, anh_dai_dien)
VALUES ('vi', 'Nhập mã hashcode', 2, NULL);

-- =====================
-- 3. INSERT NGUOI DUNG
-- =====================
INSERT INTO nguoi_dung (id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (1, N'Administrator', 'admin@orgo.com', '0900000001', 'ACTIVE');

INSERT INTO nguoi_dung (id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (2, N'Nguyễn Trí Trường', 'nguyentritruong2005@gmail.com', '0935233627', 'ACTIVE');

INSERT INTO nguoi_dung (id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (3, N'Nguyễn Văn Phú', 'phu@orgo.com', '0900000003', 'ACTIVE'); --Nên nhập email thật

INSERT INTO nguoi_dung (id_tai_khoan, ho_ten, email, so_dien_thoai, trang_thai)
VALUES (4, N'Nguyễn Hà Vi', 'vi@orgo.com', '0900000004', 'ACTIVE'); -- nên nhập email thật
