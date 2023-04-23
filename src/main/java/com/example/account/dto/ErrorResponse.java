package com.example.account.dto;

import com.example.account.type.ErrorCode;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
