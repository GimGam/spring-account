package com.example.account.domain;

import com.example.account.type.AccountStatus;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {
    @Id                                 //id를 Account 의 PK로 사용할 것이다.
    @GeneratedValue                     //자동으로 증가되는 값
    private Long id;


    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    //n:1의 연결 {many account -> one user}
    @ManyToOne
    private Account account;

    private Long amount;                    //거리 금액
    private Long balanceSnapshot;           //거래후 계좌 잔액

    private String transactionId;           //거래 아이디.

    private LocalDateTime transactedAt;     //거래 일시.

    //생성일시 & 수정일시는 모든 테이블이 가지고 있으면 좋다.
    @CreatedDate
    private LocalDateTime createdAt;        //생성일시
    @LastModifiedDate
    private LocalDateTime updatedAt;        //수정일시
}
