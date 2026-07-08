package com.multitenant.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:test@test.ro}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void trimiteEmailCuTemplate(String catre, String subiect, String numeTemplate, Map<String, Object> variabile) {
        try {
            Context context = new Context();
            context.setVariables(variabile);

            // Compiled from templates/email/numeTemplate.html
            String continutHtml = templateEngine.process("email/" + numeTemplate, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(catre);
            helper.setSubject(subiect);
            helper.setText(continutHtml, true);

            mailSender.send(message);
            System.out.println("Email trimis cu succes către: " + catre);

        } catch (MessagingException e) {
            System.err.println("Eroare gravă la generarea/trimiterea emailului: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Eroare neprevăzută la trimiterea emailului: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
