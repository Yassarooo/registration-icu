package com.jazara.icu.registration.client;

import com.jazara.icu.registration.domain.UserDTO;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", fallback = AuthServiceClient.AuthServiceClientClientFallback.class)

public interface AuthServiceClient {

    @PostMapping(value = "/api/register")
    ResponseEntity<String> registerUserAccount(UserDTO user);

    @PostMapping(value = "/api/activate")
    ResponseEntity<String> activateUserAccount(String emailorusername,@RequestParam("serviceid") String serviceid);

    @Component
    class AuthServiceClientClientFallback implements AuthServiceClient {

        private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClientClientFallback.class);

        @Override
        public ResponseEntity<String> registerUserAccount(UserDTO user) {
            LOGGER.info("fallback");
            return new ResponseEntity<String>("failed", HttpStatus.BAD_REQUEST);
        }

        @Override
        public ResponseEntity<String> activateUserAccount(String emailorusername,@RequestParam("serviceid") String serviceid) {
            LOGGER.info("fallback");
            return new ResponseEntity<String>("failed", HttpStatus.BAD_REQUEST);
        }
    }
}