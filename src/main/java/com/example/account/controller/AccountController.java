package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccountDto;
import com.example.account.dto.DeleteAccountDto;
import com.example.account.dto.GetAccountDto;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccountDto.Response createAccount(
            @RequestBody @Valid CreateAccountDto.Request request)
    {
        final AccountDto accountDto = accountService.createAccount(request.getUserId(),
                request.getInitialBalance());

        return CreateAccountDto.Response.from(accountDto);
    }

    @DeleteMapping("/account")
    public DeleteAccountDto.Response deleteAccount(
            @RequestBody @Valid DeleteAccountDto.Request request)
    {
        final AccountDto accountDto = accountService.deleteAccount(request.getUserId(),
                request.getAccountNumber());

        return DeleteAccountDto.Response.from(accountDto);
    }

    @GetMapping("/account")
    public List<GetAccountDto.Response> getAccount(@RequestParam("user_id") Long userId){
        return accountService.getAccounts(userId)
                .stream().map(accountDto -> GetAccountDto.Response.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .accountBalance(accountDto.getAccountBalance())
                        .build())
                .collect(Collectors.toList());
    }
}
