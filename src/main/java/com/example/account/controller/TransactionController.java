package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelTransactionDto;
import com.example.account.dto.QueryTransactionDto;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalanceDto;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    final private TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalanceDto.Response useBalance(
           @RequestBody @Valid UseBalanceDto.Request request) {

        try {
            return UseBalanceDto.Response.from(
                    transactionService.useBalance(request.getUserId()
                            ,request.getAccountNumber()
                            ,request.getAmount())
            );
        }catch(AccountException e){
            transactionService.saveFailedTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public UseBalanceDto.Response cancelTransaction(
            @RequestBody @Valid CancelTransactionDto.Request request) {

        try {
            return UseBalanceDto.Response.from(
                    transactionService.cancelTransaction(request.getTransactionId(),
                            request.getAccountNumber(), request.getAmount()));
        }catch(AccountException e){
            transactionService.cancelFailedTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @GetMapping ("/transaction/{transactionId}")
    public QueryTransactionDto.Response queryTransaction(
            @PathVariable String transactionId) {
        return QueryTransactionDto.Response.from(
                transactionService.queryTransaction(transactionId));
    }
}
