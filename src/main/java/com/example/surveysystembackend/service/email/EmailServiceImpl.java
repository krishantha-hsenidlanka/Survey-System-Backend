package com.example.surveysystembackend.service.email;

import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.model.User;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontEndUrl}")
    private String frontEndUrl;

    @Value("${app.emailSender}")
    private String emailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(User user) {
        String subject = "Verify your email address";
        String verificationLink = frontEndUrl + "/verify?token=" + user.getVerificationToken();

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("verificationLink", verificationLink);

        String emailBody = templateEngine.process("emails/verification-email", context);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(new InternetAddress(emailSender));
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject(subject);
            messageHelper.setText(emailBody, true);
        };

        try {
            log.info("Sending verification email for {}", user.getEmail());
            javaMailSender.send(messagePreparator);
            log.info("Verification email sent successfully to {}", user.getEmail());
        } catch (MailException e) {
            log.error("Error sending verification email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new CustomRuntimeException("Error sending verification email", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error during sending verification email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new CustomRuntimeException("Unexpected error during sending verification email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}