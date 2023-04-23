package com.example.account.dto;

import com.example.account.aop.AccountLockIdInterface;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class DeleteAccountDto {
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request implements AccountLockIdInterface {
        //@Valid 처리를 위한 어노테이션
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        private String accountNumber;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Response{
        private Long userId;
        private String accountNumber;
        private LocalDateTime unregisterAt;

        static public Response from(final AccountDto accountDto){
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .unregisterAt(accountDto.getUnregisteredAt())
                    .build();
        }
    }
}
