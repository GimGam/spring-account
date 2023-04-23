package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    //RedisRepositoryConfig.java에서
    //Bean어노테이션으로 등록해 놓은 redissonClient가 있다.

    //만약 변수의 이름(final이여만하나?)과 Bean의 이름이 같다면.
    //자동으로 주입이 되게 된다.
    private final RedissonClient redissonClient;

    public String lock(String accountNumber){
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try{
            boolean isLock = lock.tryLock(1,15, TimeUnit.SECONDS);

            if(!isLock){
                log.error("---------------Lock Failed----------------");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
            }

        }catch(Exception e){
            log.error("Redis lock failed");
        }

        return "get lock success";
    }

    public void unlock(String accountNumber){
        log.debug("Unlock for accountNumber : {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
