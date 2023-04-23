package com.example.account.domain;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BaseEntity {
    @Id                                 //id를 Account 의 PK로 사용할 것이다.
    @GeneratedValue                     //자동으로 증가되는 값
    private Long id;

    //생성일시 & 수정일시는 모든 테이블이 가지고 있으면 좋다.
    @CreatedDate
    private LocalDateTime createdAt;        //생성일시
    @LastModifiedDate
    private LocalDateTime updatedAt;        //수정일시
}
