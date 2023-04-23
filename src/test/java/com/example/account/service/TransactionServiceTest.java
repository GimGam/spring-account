package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.ErrorCode.AMOUNT_IS_TOO_BIG;
import static com.example.account.type.ErrorCode.AMOUNT_IS_TOO_SMALL;
import static com.example.account.type.TransactionResultType.*;
import static com.example.account.type.TransactionType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class) //Mock 테스트를 위한 어노테이션
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private TransactionService transactionService;

    //==========================================================================
    //TEST UTILITY
    //==========================================================================
    AccountUser getAccountUser(){
        return AccountUser.builder()
                .id(1L)
                .userName("test user")
                .build();
    }

    Account getEmptyAccount(AccountUser accountUser){
        return Account.builder()
                .id(1L)
                .accountUser(accountUser)
                .accountNumber("111")
                .build();
    }

    Transaction getEmptyTransaction(Account account){
        return Transaction.builder()
                .id(1L)
                .account(account)
                .build();
    }

    //==========================================================================
    //USE BALANCE TEST
    //==========================================================================
    @Test
    @DisplayName("잔액 사용 성공.")
    void useBalance_Success(){
        //사용자 & 계좌 존재.
        //사용자 & 계좌간 관계 문제없음. & 계좌는 IN_USE 상태
        AccountUser accountUser = getAccountUser();
        Account account = getEmptyAccount(accountUser);
        Transaction transaction = getEmptyTransaction(account);

        ArgumentCaptor<Transaction> captor
                = ArgumentCaptor.forClass(Transaction.class);

        //amount 는 최소, 최대 거래금액을 만족함.
        //      현재 service 에서 100L ~ 100_000L 의 최소, 최대 범위가짐.
        //amount 는 account 의 balance 보다 작음.
        Long accountBalance = 100_000L;
        Long amount = accountBalance - 1L;

        account.setAccountBalance(accountBalance);

        //정상적인 Transaction 성공 값.
        transaction.setTransactionType(USE);
        transaction.setTransactionResultType(S);
        transaction.setBalanceSnapshot(accountBalance - amount);
        transaction.setAmount(amount);

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(transaction);


        //when
        TransactionDto transactionDto =
                transactionService.useBalance(1L, account.getAccountNumber(), amount);

        //then
        verify(transactionRepository, times(1))
                .save(captor.capture());
        assertEquals(1L, captor.getValue().getAccount().getAccountUser().getId());
        assertEquals(account.getAccountNumber(), captor.getValue().getAccount().getAccountNumber());
        assertEquals(amount, captor.getValue().getAmount());

        assertEquals(USE,transactionDto.getTransactionType());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(accountBalance - amount, transactionDto.getBalanceSnapshot());
    }

    @Test
    @DisplayName("잔액 사용 실패 : USER_NOT_FOUND")
    void useBalance_Failed_NotFoundUser()  {
       //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : ACCOUNT_NOT_FOUND")
    void useBalance_Failed_NotFoundAccount()  {
        //given
        AccountUser accountUser = getAccountUser();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : USER_ACCOUNT_NOT_MATCH")
    void useBalance_Failed_UserAccountNotMatch()  {
        //given
        AccountUser accountUser_1 = getAccountUser();

        AccountUser accountUser_2 = getAccountUser();
        accountUser_2.setId(2L);
        Account accountOfUser_2 = getEmptyAccount(accountUser_2);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser_1));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountOfUser_2));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", 1000L));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : ACCOUNT_ALREADY_UNREGISTRED")
    void useBalance_Failed_AccountAlreadyUnregistred()  {
        //given
        AccountUser accountUser = getAccountUser();

        //**!이미 해지된 계좌!**//
        Account account = getEmptyAccount(accountUser);
        account.setAccountStatus(AccountStatus.UNREGISTERED);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : AMOUNT_OVER_BALANCE")
    void useBalance_Failed_AmountOverBalance()  {
        //given
        AccountUser accountUser = getAccountUser();
        Account account = getEmptyAccount(accountUser);

        /*!거래 금액이 > 잔액보다 많은 경우.!*/
        final Long amount  = 10000L;
        final Long balance = 1000L;

        account.setAccountBalance(balance);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", amount));
        //then
        assertEquals(ErrorCode.AMOUNT_OVER_BALANCE, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : AMOUNT_IS_TOO_SMALL")
    void useBalance_Failed_AmountIsTooSmall()  {
        //--------------------------------------------------------
        //5.거래 금액이 너무 작거나 큰 경우.
        //   if(100L > amount){
        //       throw new AccountException(AMOUNT_IS_TOO_SMALL);
        //   }
        //   if(100_000L < amount){
        //       throw new AccountException(AMOUNT_IS_TOO_BIG);
        //   }
        //
        //   TransactionService.java 코드 일부.
        //   Min 은 100L & Max 는 100_100L 로 설정되어 있음.
        //--------------------------------------------------------

        //given
        AccountUser accountUser = getAccountUser();
        Account account = getEmptyAccount(accountUser);

        /*!거래 금액이 > 잔액보다 많은 경우.!*/
        final Long minAmount  = 100L;
        final Long amount = minAmount - 1L;
        final Long balance = 1000L;

        account.setAccountBalance(balance);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", amount));
        //then
        assertEquals(ErrorCode.AMOUNT_IS_TOO_SMALL, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 실패 : AMOUNT_IS_TOO_BIG")
    void useBalance_Failed_AmountIsTooBig()  {
        //--------------------------------------------------------
        //5.거래 금액이 너무 작거나 큰 경우.
        //   if(100L > amount){
        //       throw new AccountException(AMOUNT_IS_TOO_SMALL);
        //   }
        //   if(100_000L < amount){
        //       throw new AccountException(AMOUNT_IS_TOO_BIG);
        //   }
        //
        //   TransactionService.java 코드 일부.
        //   Min 은 100L & Max 는 100_100L 로 설정되어 있음.
        //--------------------------------------------------------

        //given
        AccountUser accountUser = getAccountUser();
        Account account = getEmptyAccount(accountUser);

        /*!거래 금액이 > 잔액보다 많은 경우.!*/
        final Long maxAmount  = 100_000L;
        final Long amount = maxAmount + 1L;
        final Long balance = amount;

        account.setAccountBalance(balance);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000", amount));
        //then
        assertEquals(AMOUNT_IS_TOO_BIG, exception.getErrorCode());
    }

    //==========================================================================
    //CANCEL TRANSACTION TEST
    //==========================================================================
    @Test
    @DisplayName("거래 취소 성공.")
    void cancelTransaction_Success(){
        //계좌 & 거래내역 존재.
        //계좌 & 거래내역 관계 문제없음.
        //거래내역의 금액과 request 로 받은 금액 문제없음.
        //1년 이내의 거래.
        Account account = getEmptyAccount(getAccountUser());
        Transaction transaction = getEmptyTransaction(account);

        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        account.setAccountNumber(accountNumber);
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setAccount(account);

        //정상적인 Transaction Cancel 성공 값.
        transaction.setTransactionType(CANCEL);
        transaction.setTransactionResultType(S);
        transaction.setTransactedAt(LocalDateTime.now());

        ArgumentCaptor<Transaction> captor
                = ArgumentCaptor.forClass(Transaction.class);

        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(transaction);

        //when
        TransactionDto transactionDto =
                transactionService.cancelTransaction(transactionId
                        , accountNumber, amount);

        //then
        verify(transactionRepository, times(1))
                .save(captor.capture());
        assertEquals(transactionId, captor.getValue().getTransactionId());
        assertEquals(accountNumber, captor.getValue().getAccount().getAccountNumber());
        assertEquals(amount, captor.getValue().getAmount());

        assertEquals(CANCEL,transactionDto.getTransactionType());
        assertEquals(S,transactionDto.getTransactionResultType());
        assertEquals(transactionId, transactionDto.getTransactionId());
        assertEquals(accountNumber, transactionDto.getAccountNumber());
        assertEquals(amount, transactionDto.getAmount());
    }

    @Test
    @DisplayName("거래 취소 실패 : TRANSACTION_NOT_FOUND")
    void cancelTransaction_Failed_NotFoundTransaction()  {
        //given
        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelTransaction(transactionId,
                        accountNumber, amount));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 : ACCOUNT_NOT_FOUND")
    void cancelTransaction_Failed_NotFoundAccount()  {
        //given
        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        Transaction transaction = getEmptyTransaction(getEmptyAccount(getAccountUser()));

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelTransaction(transactionId,
                        accountNumber, amount));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 : TRANSACTION_ACCOUNT_NOT_MATCH")
    void cancelTransaction_Failed_Transaction_Account_NotMatch()  {
        //given
        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        Transaction transaction = getEmptyTransaction(getEmptyAccount(getAccountUser()));
        //**!거래 내역과 다른 잘못된 계좌!**//
        //**!서로의 아이디를 다르게 설정함!**//
        Account wrongAccount = getEmptyAccount(getAccountUser());
        wrongAccount.setId(transaction.getAccount().getId() + 1L);

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(wrongAccount));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelTransaction(transactionId,
                        accountNumber, amount));
        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 : TRANSACTION_AMOUNT_NOT_MATCH")
    void cancelTransaction_Failed_Transaction_Amount_NotMatch()  {
        //given
        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        Transaction transaction = getEmptyTransaction(getEmptyAccount(getAccountUser()));
        Account account = getEmptyAccount(getAccountUser());

        //**!request 로 받은 amount 와 내역이 다른 잘못된 amount!**//
        final Long wrongAmount = amount + 1L;
        transaction.setAmount(wrongAmount);


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelTransaction(transactionId,
                        accountNumber, amount));
        //then
        assertEquals(ErrorCode.TRANSACTION_AMOUNT_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 취소 실패 : TRANSACTION_TOO_OLD")
    void cancelTransaction_Failed_Transaction_Too_Old()  {
        //given
        final String transactionId = "transaction_id";
        final String accountNumber = "account_number";
        final Long amount = 100_000L;

        Transaction transaction = getEmptyTransaction(getEmptyAccount(getAccountUser()));
        transaction.setAmount(amount);
        Account account = getEmptyAccount(getAccountUser());

        //**!현재 시점으로부터 2년이 지난 너무 오래된 거래 내역!**//
        transaction.setTransactedAt(LocalDateTime.now().minusYears(2));

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelTransaction(transactionId,
                        accountNumber, amount));
        //then
        assertEquals(ErrorCode.TRANSACTION_TOO_OLD, exception.getErrorCode());
    }

    //==========================================================================
    //QUERY TRANSACTION TEST
    //==========================================================================
    @Test
    @DisplayName("잔액 사용 확인 성공")
    void queryTransaction_Success()  {
        //given
        final String accountNumber = "account_number";
        final String transactionId = "transaction_id";
        final Long amount = 100_100L;

        Transaction transaction = getEmptyTransaction(getEmptyAccount(getAccountUser()));
        transaction.getAccount().setAccountNumber(accountNumber);
        transaction.setTransactionType(USE);
        transaction.setTransactionResultType(S);
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        TransactionDto transactionDto =
                transactionService.queryTransaction(transactionId);
        //then
        assertEquals(accountNumber, transactionDto.getAccountNumber());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(transactionId, transactionDto.getTransactionId());
        assertEquals(amount, transactionDto.getAmount());
    }

    @Test
    @DisplayName("잔액 사용 확인 실패 : TRANSACTION_NOT_FOUND")
    void queryTransaction_Failed_NotFoundTransaction()  {
        //given
        final String transactionId = "transaction_id";

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction(transactionId));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}