package com.jazara.icu.registration.controller;

import com.jazara.icu.registration.client.AuthServiceClient;
import com.jazara.icu.registration.config.OnRegistrationCompleteEvent;
import com.jazara.icu.registration.domain.UserDTO;
import com.jazara.icu.registration.domain.VerificationToken;
import com.jazara.icu.registration.service.CustomResponse;
import com.jazara.icu.registration.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

@RefreshScope
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class RegistrationController {

    @Autowired
    private CustomResponse customResponse;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    EurekaDiscoveryClient discoveryClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Autowired
    private VerificationTokenService tokenService;

    @PostMapping(value = "/register")
    public ResponseEntity<Map<String, Object>> registerAndSendEmail(@RequestBody UserDTO user, final HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> m = authServiceClient.registerUserAccount(user);
        if (m.getBody().get("success").equals(false)) {
            return customResponse.HandleResponse(false, "Existing Email Or Username !", "", HttpStatus.OK);
        }
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user.getEmail(), request.getLocale(), getAppUrl(request)));
        return customResponse.HandleResponse(true, "Email Sent Successfully, Please Check your inbox", "", HttpStatus.OK);
    }

    @PostMapping(value = "/registrationconfirm")
    public ResponseEntity<String> registrationConfirm(@RequestParam("token") String token, @RequestParam("email") String email) {
        return new ResponseEntity<String>(tokenService.validateVerificationToken(token, email), HttpStatus.OK);
    }

    @GetMapping("/resendEmail")
    public ResponseEntity<Map<String, Object>> resendRegistrationToken(final HttpServletRequest request, @RequestParam("email") final String email) {
        try {
            final VerificationToken newToken = tokenService.generateNewVerificationToken(email);
            mailSender.send(constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, email));
            return customResponse.HandleResponse(true, "Email Sent Successfully, Please Check your inbox", "", HttpStatus.OK);

        } catch (Exception e) {
            return customResponse.HandleResponse(false, e.toString(), "", HttpStatus.OK);
        }
    }

    // ============== NON-API ============

    private SimpleMailMessage constructResendVerificationTokenEmail(final String contextPath, final Locale locale, final VerificationToken newToken, final String emailaddress) {
        final String confirmationUrl = contextPath + "/#/registrationConfirm.html/" + emailaddress + "/" + newToken.getToken();
        final String message = messages.getMessage("message.regSuccLink", null, "You have registered successfully. To confirm your registration, please enter this code:\n " + newToken.getCode() + "\n or click on the below link.", locale);
        return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, emailaddress);
    }

    private SimpleMailMessage constructEmail(String subject, String body, String emailaddress) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(emailaddress);
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}