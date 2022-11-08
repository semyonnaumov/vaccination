package com.naumov.identityservice.dto.rs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DefaultErrorResponse {
    private String message;

    public DefaultErrorResponse(String message) {
        this.message = message;
    }
}