const express = require('express');
const bodyParser = require('body-parser');
require('dotenv').config();

const app = express();

// Chống lỗi 302 và xử lý dữ liệu đầu vào
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Cho phép route linh hoạt để tránh redirect 302
app.post(['/api/sepay-webhook', '/api/sepay-webhook/'], (req, res) => {
    const data = req.body;
    
    // Lấy Header không phân biệt hoa thường
    const authHeader = req.headers['authorization'] || req.headers['Authorization'];

    console.log("--- Nhận tín hiệu từ SePay ---");

    // Xác thực API Key (Đảm bảo process.env.SEPAY_WEBHOOK_TOKEN khớp chính xác mã bạn nhập trên web)
    const expectedAuth = `Apikey ${process.env.SEPAY_WEBHOOK_TOKEN}`;
    
    if (!authHeader || authHeader !== expectedAuth) {
        console.error("❌ Xác thực thất bại!");
        console.log("Nhận được:", authHeader);
        console.log("Kỳ vọng:", expectedAuth);
        // Trả về 401 thay vì để mặc định để SePay biết lỗi bảo mật
        return res.status(401).json({ message: "Unauthorized" });
    }

    const { amount, content } = data;
    console.log(`✅ Xác thực thành công! Số tiền: ${amount} | Nội dung: ${content}`);

    // TRẢ VỀ 200 NGAY LẬP TỨC để tránh SePay timeout hoặc gửi lại
    res.status(200).json({ status: 'success' });

    // Sau đó mới xử lý logic Database phía dưới để không làm chậm phản hồi Webhook
    // const orderId = content.replace(/[^\d]/g, ''); 
});

const PORT = process.env.PORT || 8081;
app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy tại cổng: ${PORT}`);
});