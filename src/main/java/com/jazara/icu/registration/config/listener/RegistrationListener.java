package com.jazara.icu.registration.config.listener;


import java.util.Random;
import java.util.UUID;

import com.jazara.icu.registration.config.OnRegistrationCompleteEvent;
import com.jazara.icu.registration.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private VerificationTokenService service;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    // API

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) {
        Random rnd = new Random();
        String code = String.format("%06d", rnd.nextInt(999999));

        final String user = event.getUser();
        final String token = UUID.randomUUID().toString();
        service.createVerificationTokenForUser(user, token, code);

        final SimpleMailMessage email = constructEmailMessage(event, user, token,code);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final String user, final String token, final String code) {
        final String recipientAddress = user;
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/#/registrationConfirm/"+user+"/" + token;
        final String message = messages.getMessage("message.regSuccLink", null, "You have registered successfully. To confirm your registration, please enter this code:\n "+code+"\n or click on the below link.", event.getLocale());
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

}