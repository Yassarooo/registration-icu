package com.jazara.icu.registration.client;

import com.jazara.icu.registration.domain.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@FeignClient(name = "auth-service", fallback = AuthServiceClient.AuthServiceClientClientFallback.class)
public interface AuthServiceClient {

    @PostMapping(value = "/api/auth/register")
    @CrossOrigin(origins = "*", maxAge = 3600)
    ResponseEntity<Map<String, Object>> registerUserAccount(UserDTO user);

    @PostMapping(value = "/api/auth/activate")
    @CrossOrigin(origins = "*", maxAge = 3600)
    ResponseEntity<Map<String, Object>> activateUserAccount(String emailorusername);

    @Component
    class AuthServiceClientClientFallback implements AuthServiceClient {

        private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClientClientFallback.class);

        @Override
        public ResponseEntity<Map<String, Object>> registerUserAccount(UserDTO user) {
            LOGGER.info("fallback");
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("message", "fallback method");
            map.put("result", "");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
        }

        @Override
        public ResponseEntity<Map<String, Object>> activateUserAccount(String emailorusername) {
            LOGGER.info("fallback");
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("message", "fallback method");
            map.put("result", "");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.BAD_REQUEST);
        }
    }
}