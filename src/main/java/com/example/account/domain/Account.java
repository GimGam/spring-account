package com.example.account.domain;

import com.example.account.type.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//@Entity -- 자바 객체처럼 보이지만 설정을 위한 클래스.
//이게 왜 자동으로 쿼리로 동작하는거지??????????
    //일단 JPA 와 관련이 있다고 이해하고 넘어감. - 메소드로 쿼리조작.

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Account {
    @Id                                 //id를 Account 의 PK로 사용할 것이다.
    @GeneratedValue                     //자동으로 증가되는 값
    private Long id;

    //n:1의 연결 {many account -> one user}
    //다른 Entity 를 안에 포함시키려는 경우에는 관계에 대한 어노테이션이 필요한것같다.
    @ManyToOne
    private AccountUser accountUser;

    private String accountNumber;

    @Enumerated(EnumType.STRING)            //Enum 의 값을 문자열로 사용할 것이다.
    private AccountStatus accountStatus;

    private Long accountBalance;

    private LocalDateTime registeredAt;     //등록일시
    private LocalDateTime unregisteredAt;   //해지일시

    //생성일시 & 수정일시는 모든 테이블이 가지고 있으면 좋다.
    @CreatedDate
    private LocalDateTime createdAt;        //생성일시
    @LastModifiedDate
    private LocalDateTime updatedAt;        //수정일시
}
