package com.waltercraft.notification.service;

import com.waltercraft.notification.exception.EmailDeliveryUnavailableException;
import jakarta.mail.internet.MimeMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailSenderService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.from:${spring.mail.username:Waltercraft <noreply@waltercraft.ru>}}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @CircuitBreaker(name = "smtp", fallbackMethod = "sendVerificationCodeFallback")
    public void sendVerificationCode(String toEmail, String code, String purpose) {
        String title;
        String description;
        String subject;

        if ("REGISTRATION".equalsIgnoreCase(purpose)) {
            title = "Подтверждение регистрации";
            description = "Вы указали этот адрес электронной почты для регистрации на сервере Waltercraft. Введите следующий одноразовый код для подтверждения:";
            subject = "Код подтверждения регистрации: " + code;
        } else if ("PASSWORD_RESET".equalsIgnoreCase(purpose)) {
            title = "Сброс пароля";
            description = "Вы запросили сброс пароля для вашей учётной записи на Waltercraft. Введите следующий одноразовый код:";
            subject = "Код для сброса пароля: " + code;
        } else {
            title = "Подтверждение действия";
            description = "Вы запросили подтверждение операции на сервере Waltercraft. Введите следующий одноразовый код:";
            subject = "Код подтверждения Waltercraft: " + code;
        }

        String plainText = """
                Здравствуйте!

                %s
                Код подтверждения: %s

                Код действителен в течение 10 минут. Если вы не запрашивали данный код, проигнорируйте это письмо.

                С уважением,
                Команда Waltercraft
                """.formatted(description, code);

        String htmlText = """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                </head>
                <body style="margin: 0; padding: 32px 16px; background-color: #0c0d0e; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #f3f4f6;">
                  <div style="max-width: 480px; margin: 0 auto; background-color: #16181a; border: 1px solid #272a2e; padding: 32px 28px;">
                    <div style="font-size: 22px; font-weight: 800; letter-spacing: 1px; color: #ffffff; margin: 0 0 24px 0; text-transform: uppercase;">
                      WALTER<span style="color: #3b82f6;">CRAFT</span>
                    </div>
                    <h2 style="color: #ffffff; margin: 0 0 12px 0; font-size: 20px; font-weight: 700;">
                      %s
                    </h2>
                    <p style="color: #9ca3af; font-size: 14px; line-height: 1.6; margin: 0 0 24px 0;">
                      %s
                    </p>
                    <div style="background-color: #1f2226; border: 1px solid #32363c; padding: 18px 24px; text-align: center; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #ffffff; font-family: 'Courier New', Courier, monospace; margin: 0 0 24px 0;">
                      %s
                    </div>
                    <p style="color: #6b7280; font-size: 12px; line-height: 1.5; margin: 0 0 24px 0;">
                      Код действует 10 минут. Если вы не совершали данное действие, не передавайте код никому и просто проигнорируйте письмо.
                    </p>
                    <div style="border-top: 1px solid #272a2e; padding-top: 16px;">
                      <p style="color: #4b5563; font-size: 12px; line-height: 1.4; margin: 0;">
                        С уважением,<br>
                        Команда Waltercraft
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(title, title, description, code);

        if (mailSender == null) {
            throw new EmailDeliveryUnavailableException();
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, htmlText);
            mailSender.send(mimeMessage);
            log.info("Verification email sent purpose={}", purpose);
        } catch (Exception ex) {
            throw new EmailDeliveryUnavailableException(ex);
        }
    }

    private void sendVerificationCodeFallback(String toEmail, String code, String purpose, Throwable cause) {
        log.warn("Email delivery fallback invoked purpose={} exceptionType={}",
                purpose, cause.getClass().getSimpleName());
        if (cause instanceof EmailDeliveryUnavailableException unavailable) {
            throw unavailable;
        }
        throw new EmailDeliveryUnavailableException(cause);
    }
}
