package com.jumbo.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Data
public class ErrorDetails {

    private LocalDateTime timestamp;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
