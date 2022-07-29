package com.jazara.icu.registration.service;

import com.jazara.icu.registration.client.AuthServiceClient;
import com.jazara.icu.registration.domain.VerificationToken;
import com.jazara.icu.registration.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";
    public static final String CONFLICT = "conflict";
    public static final String CANNOT = "canoot";

    public void createVerificationTokenForUser(final String user, final String token, final String code) {
        final VerificationToken myToken = new VerificationToken(token, code, user);
        tokenRepository.save(myToken);
    }

    public String getEmailByToken(final String verificationToken) {
        VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getEmail();
        } else {
            token = tokenRepository.findByCode(verificationToken);
            if (token != null) {
                return token.getEmail();
            }
        }
        return null;
    }

    public VerificationToken generateNewVerificationToken(final String email) throws Exception {
        VerificationToken vToken = tokenRepository.findByEmail(email);
        if (vToken != null) {
            Random rnd = new Random();
            int code = rnd.nextInt(999999);
            vToken.updateToken(UUID.randomUUID()
                    .toString(), String.format("%06d", code));
            return tokenRepository.save(vToken);
        } else {
            System.err.println("token Not Found");
            throw new Exception("Unregistered , Or Account is already Activated");
        }
    }

    public void deleteToken(final String email) {
        final VerificationToken verificationToken = tokenRepository.findByEmail(email);

        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
        } else {
            LOGGER.error("verificationToken Not Found");
            return;
        }
    }

    public String validateVerificationToken(String token, String email) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            verificationToken = tokenRepository.findByCode(token);
        }
        if (verificationToken == null) {
            LOGGER.error("Token not Found");
            return TOKEN_INVALID;
        }
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        if (verificationToken.getEmail().equals(email)) {
            ResponseEntity<Map<String, Object>> m = authServiceClient.activateUserAccount(verificationToken.getEmail());
            if (m.getBody().get("success").equals(true)) {
                tokenRepository.delete(verificationToken);
                return TOKEN_VALID;
            }
            return CANNOT;
        }
        return CONFLICT;
    }


}
