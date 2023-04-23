package com.example.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class AccountUser {
    @Id                                 //id를 Account 의 PK로 사용할 것이다.
    @GeneratedValue                     //자동으로 증가되는 값
    private Long id;

    private String userName;

    //생성일시 & 수정일시는 모든 테이블이 가지고 있으면 좋다.
    @CreatedDate
    private LocalDateTime createdAt;        //생성일시
    @LastModifiedDate
    private LocalDateTime updatedAt;        //수정일시
}
