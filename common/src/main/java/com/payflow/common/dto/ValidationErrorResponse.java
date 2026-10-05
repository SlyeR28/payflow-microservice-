package com.payflow.common.dto;


import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorResponse {


    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String , String> fieldsErrors;

}
