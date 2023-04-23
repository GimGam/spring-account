package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.dto.GetAccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {

        //서비스 정책.
        //1.사용자가 없는 경우
        //2.사용자가 최대 보유 가능 계좌 수 를 가지고 있는 경우
        //위 두가지 경우에는 createAccount 는 실패한다.

        AccountUser accountUser = getAccountUser(userId);

        validateCreateAccount(accountUser);

        String accountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        final Account account = accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber(accountNumber)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountBalance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        );

        //Repository 의 응답 에서 나온 Entity 를
        //Service -> Controller 간의 통신에 이용하는것은 문제가 있을수있다.
        //Dto 를 사용하여 전달.
        return AccountDto.fromEntity(account);
    }


    private void validateCreateAccount(AccountUser accountUser){
        if(10 <= accountRepository.countByAccountUser(accountUser)){
            throw new AccountException(ErrorCode.OVER_MAX_ACCOUNT);
        }
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber){

        AccountUser accountUser = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(()-> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(account, accountUser);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnregisteredAt(LocalDateTime.now());

        return AccountDto.fromEntity(account);
    }

    private AccountUser getAccountUser(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(()->new AccountException(ErrorCode.USER_NOT_FOUND));
        return accountUser;
    }

    private void validateDeleteAccount(Account account, AccountUser accountUser){
        //정책.
        //1.사용자 또는 계좌가 없는경우. --> deleteAccount 메서드 에서 처리
        //2.사용자 아이디와, 계좌 소유주가 다른경우.
        if(accountUser.getId() != account.getAccountUser().getId()){
            throw new AccountException(ErrorCode.USER_ACCOUNT_NOT_MATCH);
        }
        //3.계좌가 이미 해지 상태인 경우.
        if(AccountStatus.UNREGISTERED ==  account.getAccountStatus()){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTRED);
        }
        //4.잔액이 남아 있는 경우.
        if(0L != account.getAccountBalance()){
            throw new AccountException(ErrorCode.ACCOUNT_REMAIN_BALANCE);
        }
    }

    @Transactional
    public List<AccountDto> getAccounts(Long userId){
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accountList = accountRepository.findByAccountUser(accountUser);

        return accountList.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

}
