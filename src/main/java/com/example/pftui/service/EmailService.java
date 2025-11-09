package com.example.pftui.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(body, true); // true indicates html
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("quangvinh3020@gmail.com", "Personal Finance Tracker");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to set sender name", e);
        }
    }

    private String buildEmailTemplate(String title, String preheader, String content) {
        return "<!DOCTYPE html>"
            + "<html lang='en'>"
            + "<head><style>"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 0; }"
            + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; }"
            + ".header { background-color: #4338ca; color: #ffffff; padding: 36px; text-align: center; }"
            + ".header h1 { margin: 0; font-size: 28px; }"
            + ".content { padding: 32px 40px; color: #334155; line-height: 1.7; }"
            + ".content p { margin: 0 0 16px; }"
            + ".code-box { background-color: #eef2ff; border: 1px dashed #c7d2fe; color: #4338ca; font-size: 36px; font-weight: 700; text-align: center; padding: 20px; margin: 30px 0; border-radius: 8px; letter-spacing: 5px; }"
            + ".footer { background-color: #f8fafc; color: #64748b; text-align: center; padding: 20px; font-size: 14px; border-top: 1px solid #e2e8f0; }"
            + "</style></head>"
            + "<body>"
            + "<div class='container'>"
            + "<div class='header'><h1>" + title + "</h1></div>"
            + "<div class='content'>"
            + "<p style='display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;'>" + preheader + "</p>"
            + content
            + "</div>"
            + "<div class='footer'>&copy; " + java.time.Year.now() + " Personal Finance Tracker. All rights reserved.</div>"
            + "</div>"
            + "</body></html>";
    }

    public void sendVerificationCode(String to, String code) {
        String subject = "Your Account Verification Code";
        String preheader = "Use this code to verify your account.";
        String content = "<p>Hi there,</p>"
                       + "<p>Thank you for registering. Please use the following code to verify your account:</p>"
                       + "<div class='code-box'>" + code + "</div>"
                       + "<p>This code will expire in 10 minutes. If you did not register, please ignore this email.</p>"
                       + "<p>Best regards,<br>The Personal Finance Tracker Team</p>";
        
        String body = buildEmailTemplate("Verify Your Email", preheader, content);
        sendEmail(to, subject, body);
    }

    public void send2faCode(String to, String code) {
        String subject = "Your Two-Factor Authentication Code";
        String preheader = "Use this code to complete your login.";
        String content = "<p>Hi there,</p>"
                       + "<p>A login attempt requires a two-factor authentication code. Use the code below to complete your login:</p>"
                       + "<div class='code-box'>" + code + "</div>"
                       + "<p>This code will expire in 10 minutes. If you are not trying to log in, please change your password immediately.</p>"
                       + "<p>Best regards,<br>The Personal Finance Tracker Team</p>";

        String body = buildEmailTemplate("2FA Login Code", preheader, content);
        sendEmail(to, subject, body);
    }
}