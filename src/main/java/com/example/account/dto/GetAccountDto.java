package com.example.account.dto;

import lombok.*;

public class GetAccountDto {
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    static public class Response{
        private String accountNumber;
        private Long accountBalance;
    }
}
