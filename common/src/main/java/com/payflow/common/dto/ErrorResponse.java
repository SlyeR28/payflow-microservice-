package com.payflow.common.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {


    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String , String> fieldsErrors;

}
