package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Entity 를 DB에 저장하기위해서는 JPA 에서 제공해주는 Repository 가 필요하다?
//Spring 쪽에서 JPA 를 쓰기 쉽게 만들어주는 특수한 형태의 IF?.
//그래서 JPA 가 뭔데..

//Account 라는 테이블(Entity) 에 접근하기 위한 Repository(IF)
@Repository
public interface AccountRepository
        extends JpaRepository<Account, Long> {

    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
