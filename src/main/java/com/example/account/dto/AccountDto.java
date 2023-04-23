package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long accountBalance;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;

    static public AccountDto fromEntity(final Account account){
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .accountBalance(account.getAccountBalance())
                .registeredAt(account.getRegisteredAt())
                .unregisteredAt(account.getUnregisteredAt())
                .build();
    }
}
