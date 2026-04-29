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

    public void sendExpertDecisionEmail(String toEmail, String fullName, String statusText, String reason) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Kết quả đăng ký Expert - ORGO");
        String htmlContent = """
        <div style="font-family: Arial, sans-serif; background-color: #f6f8f6; padding: 30px;">
            <div style="max-width: 560px; margin: auto; background: #ffffff; padding: 40px;
                        border-radius: 12px; border-top: 5px solid #198754; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                <h1 style="color: #198754; font-size: 28px; font-weight: 900; margin-bottom: 8px;">ORGO</h1>
                <h2 style="color: #1a1a1a; font-size: 22px; margin-bottom: 18px;">Thông báo kết quả đăng ký Expert</h2>
                <p style="font-size: 15px; color: #555; line-height: 1.7;">
                    Xin chào <b>%s</b>,<br>
                    Hồ sơ đăng ký <b>Expert</b> của bạn đã được admin xem xét.
                </p>
                <div style="margin: 24px 0; padding: 16px; background: #f8f9fa; border-radius: 10px;">
                    <p style="margin: 0; font-size: 16px; color: #1a1a1a;">
                        Trạng thái: <b style="color: %s">%s</b>
                    </p>
                    <p style="margin: 10px 0 0; font-size: 14px; color: #666;">
                        %s
                    </p>
                </div>
                <p style="font-size: 14px; color: #666; line-height: 1.6;">
                    Cảm ơn bạn đã đồng hành cùng ORGO.
                </p>
            </div>
        </div>
        """.formatted(
                fullName,
                "APPROVED".equals(statusText) ? "#198754" : "#dc3545",
                statusText,
                reason
        );

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendSellerDecisionMail(String toEmail, String fullName, String statusText, String reason) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Kết quả đăng ký Seller - ORGO");
        String htmlContent = """
        <div style="font-family: Arial, sans-serif; background-color: #f6f8f6; padding: 30px;">
            <div style="max-width: 560px; margin: auto; background: #ffffff; padding: 40px;
                        border-radius: 12px; border-top: 5px solid #198754; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                <h1 style="color: #198754; font-size: 28px; font-weight: 900; margin-bottom: 8px;">ORGO</h1>
                <h2 style="color: #1a1a1a; font-size: 22px; margin-bottom: 18px;">Thông báo kết quả đăng ký Seller</h2>
                <p style="font-size: 15px; color: #555; line-height: 1.7;">
                    Xin chào <b>%s</b>,<br>
                    Hồ sơ đăng ký <b>Seller</b> của bạn đã được admin xem xét.
                </p>
                <div style="margin: 24px 0; padding: 16px; background: #f8f9fa; border-radius: 10px;">
                    <p style="margin: 0; font-size: 16px; color: #1a1a1a;">
                        Trạng thái: <b style="color: %s">%s</b>
                    </p>
                    <p style="margin: 10px 0 0; font-size: 14px; color: #666;">
                        %s
                    </p>
                </div>
                <p style="font-size: 14px; color: #666; line-height: 1.6;">
                    Cảm ơn bạn đã đồng hành cùng ORGO.
                </p>
            </div>
        </div>
        """.formatted(
                fullName,
                "APPROVED".equals(statusText) ? "#198754" : "#dc3545",
                statusText,
                reason
        );
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
