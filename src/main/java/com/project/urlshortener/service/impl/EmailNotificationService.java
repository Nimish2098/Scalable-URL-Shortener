package com.project.urlshortener.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void notify(String tag, String location, String userAgent) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail);
            message.setSubject("Link opened – " + tag);

            message.setText("""
                A tracked link was opened.

                Tag: %s
                Location: %s
                Device: %s
                Time: %s
                """.formatted(
                    tag,
                    location,
                    userAgent,
                    LocalDateTime.now()
            ));

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Email notification failed");
        }
    }
}

