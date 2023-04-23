package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.AccountDto;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.*;
import static com.example.account.type.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {
    final private AccountUserRepository accountUserRepository;
    final private AccountRepository accountRepository;
    final private TransactionRepository transactionRepository;


    @Transactional
    public TransactionDto useBalance(Long id, String accountNumber, Long amount) {
        AccountUser accountUser = accountUserRepository.findById(id)
                .orElseThrow(()->new AccountException(USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ACCOUNT_NOT_FOUND));

        validateAccountRelation(accountUser, account);
        validateUseBalance(account, amount);


        //잔액 사용.
        account.setAccountBalance(account.getAccountBalance() - amount);

        final Transaction transaction = transactionRepository.save(
                Transaction.builder()
                .transactionType(USE)
                .transactionResultType(S)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getAccountBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());

        return TransactionDto.fromEntity(transaction);
    }

    @Transactional
    public TransactionDto cancelTransaction(String transactionId, String accountNumber, Long amount) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new AccountException(TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ACCOUNT_NOT_FOUND));

        validateCancelTransaction(transaction, account, amount);


        final Transaction savedTransaction = transactionRepository.save(
                Transaction.builder()
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getAccountBalance())
                        .transactionId(transaction.getTransactionId())
                        .transactedAt(LocalDateTime.now())
                        .build());

        return TransactionDto.fromEntity(savedTransaction);
    }

    @Transactional
    public TransactionDto queryTransaction(String transactionId)
    {
        return TransactionDto.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new AccountException(TRANSACTION_NOT_FOUND)));
    }

    //------------------------------------------------------------------------//
    @Transactional
    public void saveFailedTransaction(String accountNumber, Long amount)
    {
        //유저나 계좌가 없어서 에러가 발생한경우는
        //처리하지 않음.
        //if(USER_NOT_FOUND == errorCode || ACCOUNT_NOT_FOUND == errorCode){
            //return;
        //}

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        transactionRepository.save(
                Transaction.builder()
                .transactionType(USE)
                .transactionResultType(F)
                .account(account)
                .amount(amount)
                .balanceSnapshot(account.getAccountBalance())
                .transactionId(UUID.randomUUID().toString().replace("-", ""))
                .transactedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void cancelFailedTransaction(String accountNumber, Long amount)
    {
        //유저나 계좌가 없어서 에러가 발생한경우는
        //처리하지 않음.
        //if(USER_NOT_FOUND == errorCode || ACCOUNT_NOT_FOUND == errorCode){
        //return;
        //}

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        transactionRepository.save(
                Transaction.builder()
                        .transactionType(CANCEL)
                        .transactionResultType(F)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getAccountBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build());
    }

    //------------------------------------------------------------------------//
    private void validateAccountRelation(AccountUser accountUser, Account account){
        if(!accountUser.getId().equals(account.getAccountUser().getId()))
        {
            throw new AccountException(USER_ACCOUNT_NOT_MATCH);
        }
    }

    private void validateUseBalance(Account account,
                                        Long amount) {
        //3.계좌가 이미 해지 상태인경우.
        if(AccountStatus.UNREGISTERED == account.getAccountStatus()){
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTRED);
        }
        //4.거래 금액이 잔액보다 큰 경우.
        if(account.getAccountBalance() < amount){
            throw new AccountException(AMOUNT_OVER_BALANCE);
        }
        //5.거래 금액이 너무 작거나 큰 경우.
        if(100L > amount){
            throw new AccountException(AMOUNT_IS_TOO_SMALL);
        }
        if(100_000L < amount){
            throw new AccountException(AMOUNT_IS_TOO_BIG);
        }
    }

    private void validateCancelTransaction(Transaction transaction,
                                           Account account, Long amount) {
        //정책
        //1.계좌가 없는경우.
        //2.거래 아이디에 해당하는 거래가 없는경우.
        //--> 이전 단계에서 처리 완료.

        //3.거래와 계좌가 일치하지 않는경우.
        if(transaction.getAccount().getId() != account.getId()){
            throw new AccountException(TRANSACTION_ACCOUNT_NOT_MATCH);
        }

        //4.거래금액와 거래 취소 금액이 다른경우.
        if(transaction.getAmount() != amount){
            throw new AccountException(TRANSACTION_AMOUNT_NOT_MATCH);
        }

        //5.1년이 넘은 거래는 거래 취소 불가능.
        if(transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))){
            throw new AccountException(TRANSACTION_TOO_OLD);
        }
    }
}
