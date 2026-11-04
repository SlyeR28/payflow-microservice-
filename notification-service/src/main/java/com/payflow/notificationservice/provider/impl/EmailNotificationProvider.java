package com.payflow.notificationservice.provider.impl;

import com.payflow.notificationservice.model.document.NotificationMessages;
import com.payflow.notificationservice.model.enums.NotificationChannel;
import com.payflow.notificationservice.provider.NotificationProvider;
import com.payflow.notificationservice.provider.ProviderResult;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:jaatboykumar12@gmail.com}")
    private String mailUsername;

    @Value("${payflow.mail.from-address:}")
    private String fromAddress;

    @Value("${payflow.mail.from-name:PayFlow Alerts}")
    private String fromName;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ProviderResult send(NotificationMessages messages) {
        long start = System.currentTimeMillis();
        String messageId = "EML-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            if (messages.getRecipient() == null || messages.getRecipient().isBlank()) {
                throw new IllegalArgumentException("Recipient email address cannot be empty");
            }

            // Gmail requires From address to match authenticated username
            String effectiveSender = (fromAddress != null && !fromAddress.isBlank()) ? fromAddress : mailUsername;
            if (effectiveSender == null || effectiveSender.isBlank() || effectiveSender.contains("@payflow.com")) {
                effectiveSender = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : "jaatboykumar12@gmail.com";
            }
            String senderPersonal = (fromName != null && !fromName.isBlank()) ? fromName : "PayFlow Alerts";

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setTo(messages.getRecipient());
            mimeMessageHelper.setSubject(messages.getSubject() != null ? messages.getSubject() : "PayFlow Notification");
            mimeMessageHelper.setFrom(effectiveSender, senderPersonal);

            // Wrap plain text in a styled responsive HTML card if not already HTML
            String rawContent = messages.getContent() != null ? messages.getContent() : "";
            String htmlContent;
            if (rawContent.contains("<html") || rawContent.contains("<body") || rawContent.contains("<table")) {
                htmlContent = rawContent;
            } else {
                String formattedBody = rawContent.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br/>");

                htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head><meta charset="utf-8"/></head>
                        <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 24px;">
                            <table width="100%%" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td align="center">
                                        <table width="600" border="0" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); overflow: hidden;">
                                            <tr>
                                                <td style="background-color: #1a237e; padding: 20px 32px; color: #ffffff;">
                                                    <h2 style="margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0.5px;">PayFlow Secure Alert</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 32px; font-size: 15px; color: #333333; line-height: 1.6;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="background-color: #fafbfc; border-top: 1px solid #e1e4e8; padding: 16px 32px; font-size: 12px; color: #6a737d; text-align: center;">
                                                    &copy; 2026 PayFlow Payment Gateway Systems. All rights reserved.<br/>
                                                    This is an automated operational notification. Please do not reply directly.
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """.formatted(formattedBody);
            }

            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

            long latency = System.currentTimeMillis() - start;
            log.info("Email Successfully Dispatched via Gmail SMTP to [{}] in {}ms", messages.getRecipient(), latency);
            return ProviderResult.success("JavaMailSender-Gmail", messageId, 200, latency);

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("Email dispatch failed to [{}] via Gmail SMTP: {}", messages.getRecipient(), e.getMessage(), e);
            log.info("""

                ╔═══════════════════════════════════════════════════════════╗
                ║          DEV CONSOLE EMAIL (DISPATCH FAILED)              ║
                ╠═══════════════════════════════════════════════════════════╣
                ║ To:      {}
                ║ Subject: {}
                ║ Content: {}
                ║ Reason:  {}
                ╚═══════════════════════════════════════════════════════════╝
                """, messages.getRecipient(), messages.getSubject(), messages.getContent(), e.getMessage());
            return ProviderResult.failure("JavaMailSender-Gmail", "Email dispatch failed: " + e.getMessage(), 500, latency);
        }
    }
}
