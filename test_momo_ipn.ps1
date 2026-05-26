# Test MoMo IPN manually
# Thay ORDER_CODE và TRANS_ID bằng giá trị thực tế từ đơn hàng

$orderCode = "ORD-0165DF21"  # Thay bằng mã đơn hàng thực tế
$transId = "4087654321"       # Thay bằng transaction ID từ MoMo (nếu có)
$amount = 5400                # Số tiền đơn hàng

# Tạo IPN request giả lập thanh toán thành công
$ipnRequest = @{
    partnerCode = "MOMOBKUN20180529"
    orderId = $orderCode
    requestId = [string]([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())
    amount = $amount
    orderInfo = "Thanh toan don hang $orderCode"
    orderType = "momo_wallet"
    transId = $transId
    resultCode = 0  # 0 = thành công
    message = "Successful."
    payType = "qr"
    responseTime = [string]([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())
    extraData = ""
    signature = "dummy_signature_for_test"  # Sẽ cần tính đúng nếu verify signature
}

# Gọi IPN endpoint
$response = Invoke-RestMethod `
    -Uri "http://localhost:8081/momo/ipn" `
    -Method Post `
    -ContentType "application/json" `
    -Body ($ipnRequest | ConvertTo-Json)

Write-Host "IPN Response: $response"
