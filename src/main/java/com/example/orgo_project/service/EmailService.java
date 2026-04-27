package com.example.orgo_project.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpMail(String toEmail, String otpCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Mã OTP khôi phục mật khẩu - ORGO");

        String htmlContent = """
        <div style="font-family: Arial, sans-serif; background-color: #f0f7f0; padding: 30px;">
            <div style="max-width: 520px; margin: auto; background: #ffffff; padding: 40px;
                        border-radius: 12px; border-top: 5px solid #198754; text-align: center;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.08);">

                <!-- Logo -->
                <h1 style="color: #198754; font-size: 32px; font-weight: 900;
                           letter-spacing: 2px; margin-bottom: 4px;">ORGO</h1>
                <div style="width: 48px; height: 4px; background: #198754;
                            border-radius: 99px; margin: 0 auto 24px;"></div>

                <!-- Title -->
                <h2 style="color: #1a1a1a; font-size: 20px; margin-bottom: 12px;">
                    Xác thực mã OTP
                </h2>

                <p style="font-size: 15px; color: #555; line-height: 1.6;">
                    Xin chào,<br>
                    Bạn vừa yêu cầu khôi phục mật khẩu tại <b>ORGO</b>.<br>
                    Vui lòng sử dụng mã bên dưới để tiếp tục.
                </p>

                <!-- OTP Box -->
                <div style="margin: 28px 0;">
                    <span style="display: inline-block; font-size: 32px; font-weight: bold;
                                 color: #ffffff; background: #198754;
                                 padding: 16px 40px; border-radius: 10px;
                                 letter-spacing: 6px;">
                        """ + otpCode + """
                    </span>
                </div>

                <p style="color: #888; font-size: 14px;">
                    Mã này sẽ hết hạn sau <b>10 phút</b>.
                </p>

                <hr style="margin: 24px 0; border: none; border-top: 1px solid #e8e8e8;">

                <p style="font-size: 13px; color: #aaa; line-height: 1.6;">
                    Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.<br>
                    Tài khoản của bạn vẫn hoàn toàn an toàn.
                </p>

                <!-- Footer -->
                <p style="font-size: 14px; color: #198754; font-weight: bold; margin-top: 16px;">
                    🌿 ORGO — Thuần khiết từ thiên nhiên
                </p>
            </div>
        </div>
    """;

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
