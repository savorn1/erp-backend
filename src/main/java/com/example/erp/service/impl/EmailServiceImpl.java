package com.example.erp.service.impl;

import com.example.erp.exception.AppException;
import com.example.erp.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

// Unlike AutoPostingService's "opt-in, never blocks the caller" philosophy,
// a failure here IS the caller's whole request (emailing the document is
// the entire point) — so this throws rather than swallowing errors.
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from-address}")
    private String fromAddress;

    @Value("${mail.from-name}")
    private String fromName;

    @Override
    public void sendWithAttachment(String to, String subject, String body,
                                    byte[] attachment, String attachmentFilename, String attachmentContentType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentFilename, new org.springframework.core.io.ByteArrayResource(attachment), attachmentContentType);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Could not send email: " + e.getMessage());
        }
    }
}
