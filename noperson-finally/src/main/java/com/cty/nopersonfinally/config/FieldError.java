package com.cty.nopersonfinally.config;

public class FieldError {
    public static String getDefaultMessage(org.springframework.validation.FieldError fieldError) {
        return fieldError.getDefaultMessage();
    }
}
