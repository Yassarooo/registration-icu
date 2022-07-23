package com.jazara.icu.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class CustomResponse {

    public ResponseEntity<Map<String, Object>> HandleResponse(Boolean success, String message, Object result, HttpStatus status) {
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put("success", success);
        tokenMap.put("message", message);
        tokenMap.put("result", result);
        return new ResponseEntity<Map<String, Object>>(tokenMap, status);
    }
}

