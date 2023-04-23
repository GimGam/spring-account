package com.example.account.service;

import com.example.account.controller.AccountController;
import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) //Mock 테스트를 위한 어노테이션
class AccountServiceTest {

    //1.AccountService 클래스는
    // AccountRepository 클래스에 대한 의존성을 가지고 있다.

    //2.@Mock 으로 가짜로 만든 accountRepository 를
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    //3. @InjectMocks 으로 accountService 에 주입한다.
    @InjectMocks
    private AccountService accountService;

    //==========================================================================
    //TEST UTILITY
    //==========================================================================
    AccountUser getEmptyAccountUser(){
        return AccountUser.builder()
                .id(1L)
                .userName("test user")
                .build();
    }

    List<Account> getEmptyAccounts(Integer size, AccountUser accountUser){
       Account[] accountArr = new Account[size];

        for (int i = 1; i <= size; i++) {
            accountArr[i - 1] = Account.builder()
                    .accountUser(accountUser)
                    .accountNumber((1 + "").repeat(5))
                    .accountBalance(i * 100L)
                    .build();
        }

        return Arrays.stream(accountArr).toList();
    }

    //==========================================================================
    //CREATE TEST
    //==========================================================================

    @Test
    @DisplayName("유저가 없는 경우.")
    void createAccountTest_Failed_UserNotFound() {
        //given
        AccountException expectException =
                AccountException.builder()
                        .errorCode(ErrorCode.USER_NOT_FOUND)
                        .errorMessage(ErrorCode.USER_NOT_FOUND.getDescription())
                        .build();

        given(accountUserRepository.findById(anyLong()))
                .willThrow(expectException);

        //when
        AccountException resultException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 10000L));

        assertEquals(expectException.getErrorCode(), resultException.getErrorCode());
    }

    @Test
    @DisplayName("유저가 최대 생성 가능한 계좌 갯수를 초과했을경우.")
    void createAccountTest_Failed_OverMaxAccount() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .userName("user_1")
                        .build();


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.countByAccountUser(accountUser))
                .willReturn(10);

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.OVER_MAX_ACCOUNT, exception.getErrorCode());
    }

    //==========================================================================
    //DELETE TEST
    //==========================================================================
    @Test
    @DisplayName("계좌 삭제 성공 케이스.")
    void deleteAccountTest_Success() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .userName("user_1")
                        .build();


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        //1.사용자 또는 계좌가 없는경우.
        //2.사용자 아이디와, 계좌 소유주가 다른경우.
        //3.계좌가 이미 해지 상태인 경우.
        //4.잔액이 남아 있는 경우.
        //exception 케이스 다회피
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountBalance(0L)
                        .accountNumber("123456789")
                        .accountStatus(AccountStatus.IN_USE)
                        .build()));


        //when
        //then
        assertDoesNotThrow(() -> {
            accountService.deleteAccount(1L, "123456789");
        });
    }


    @Test
    @DisplayName("삭제시 사용자가 없는경우")
    void deleteAccountTest_Failed_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "Number"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("삭제시 계좌가 없는경우")
    void deleteAccountTest_Failed_AccountNotFound() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .userName("user_1")
                        .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "Number"));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    //2.사용자 아이디와, 계좌 소유주가 다른경우.
    @Test
    @DisplayName("사용자 아이디와, 계좌 소유주가 다른경우.")
    void deleteAccountTest_Failed_User_Account_NotMatch() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .build();

        AccountUser otherUser =
                AccountUser.builder()
                        .id(2L)
                        .build();


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "Number"));

        assertEquals(ErrorCode.USER_ACCOUNT_NOT_MATCH, accountException.getErrorCode());
    }


    //3.계좌가 이미 해지 상태인 경우.
    @Test
    @DisplayName("계좌가 이미 해지 상태인 경우.")
    void deleteAccountTest_Failed_AccountAleadyUnregistred() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "Number"));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTRED, accountException.getErrorCode());
    }

    //4.잔액이 남아 있는 경우.
    @Test
    @DisplayName("잔액이 남아 있는 경우.")
    void deleteAccountTest_Failed_RemainBalance() {
        //given
        AccountUser accountUser =
                AccountUser.builder()
                        .id(1L)
                        .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountBalance(1000L)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "Number"));

        assertEquals(ErrorCode.ACCOUNT_REMAIN_BALANCE, accountException.getErrorCode());
    }

    //==========================================================================
    //GET TEST
    //==========================================================================
    @Test
    @Description("GET ACCOUNTS 성공")
    void getAccountsTest_Success()  {
        //given
        AccountUser accountUser = getEmptyAccountUser();
        List<Account> emptyAccounts = getEmptyAccounts(3, accountUser);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountUser(accountUser))
                .willReturn(emptyAccounts);
        //when
        List<AccountDto> accounts = accountService.getAccounts(1L);
        //then

        assertEquals(3, accounts.size());
        assertEquals(accountUser.getId(), accounts.get(0).getUserId());
    }

    @Test
    @Description("GET ACCOUNT - 유저가 없는 경우.")
    void getAccountsTest_Failed_UserNotFound()  {
        //given
        AccountException expectException =
                AccountException.builder()
                        .errorCode(ErrorCode.USER_NOT_FOUND)
                        .errorMessage(ErrorCode.USER_NOT_FOUND.getDescription())
                        .build();

        given(accountUserRepository.findById(anyLong()))
                .willThrow(expectException);

        //when
        AccountException resultException = assertThrows(AccountException.class,
                () -> accountService.getAccounts(1L));

        assertEquals(expectException.getErrorCode(), resultException.getErrorCode());
    }
}