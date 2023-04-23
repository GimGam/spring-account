package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 지금 사용 중입니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    OVER_MAX_ACCOUNT("사용자의 최대 계좌 갯수를 초과했습니다."),
    USER_ACCOUNT_NOT_MATCH("사용자 아이디와 계좌 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTRED("계좌가 이미 해지 상태 입니다."),
    ACCOUNT_REMAIN_BALANCE("계좌에 잔액이 남아 있습니다."),
    AMOUNT_OVER_BALANCE("잔액이 부족합니다."),
    AMOUNT_IS_TOO_SMALL("거래금액이 너무 작습니다."),
    AMOUNT_IS_TOO_BIG("거래금액이 너무 큽니다"),
    TRANSACTION_ACCOUNT_NOT_MATCH("거래 취소하려는 계좌와 거래 내역 계좌가 다릅니다."),
    TRANSACTION_NOT_FOUND("거래 아이디에 해당하는 거래 내역이 없습니다."),
    TRANSACTION_AMOUNT_NOT_MATCH("거래 금액과 거래 취소금액이 다릅니다."),
    TRANSACTION_TOO_OLD("너무 오래된 거래 내역입니다.");

    private final String description;
}
