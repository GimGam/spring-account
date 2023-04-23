package com.example.account.controller;

import com.example.account.dto.CancelTransactionDto;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalanceDto;
import com.example.account.repository.AccountRepository;
import com.example.account.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.example.account.type.TransactionResultType.*;
import static com.example.account.type.TransactionType.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    //@Mock 과 유사한 @MockBean 을 만든다.
    //@InjectionMock 을 할 필요는 없다. Bean 으로 등록되기 때문에
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void useBalanceSuccess() throws Exception {

        //given
        given(transactionService.useBalance(anyLong(),anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("100000000")
                        .transactionType(USE)
                        .transactionResultType(S)
                        .amount(100_000L)
                        .balanceSnapshot(99_999L)
                        .transactionId("transaction_ID")
                        .transactedAt(LocalDateTime.now())
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UseBalanceDto.Request(1L, "1000", 1000L)
                ))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("100000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transaction_ID"))
                .andExpect(jsonPath("$.amount").value(100_000L))
                .andDo(print());

    }

    @Test
    void cancelTransactionSuccess() throws Exception {

        final String accountNumber = "100000000";
        final String transactionId = "transaction_id";
        final Long amount = 100_000L;

        //given
        given(transactionService.cancelTransaction(anyString(),anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber(accountNumber)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .amount(amount)
                        .balanceSnapshot(99_999L)
                        .transactionId(transactionId)
                        .transactedAt(LocalDateTime.now())
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelTransactionDto.Request(transactionId, "100000000", 100_000L)
                        ))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("transaction_id"))
                .andExpect(jsonPath("$.accountNumber").value("100000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.amount").value(amount))
                .andDo(print());

    }

    @Test
    void queryTransactionSuccess() throws Exception {

        final String accountNumber = "100000000";
        final String transactionId = "transaction_id";
        final Long amount = 100_000L;

        //given
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber(accountNumber)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId(transactionId)
                        .amount(amount)
                        .transactedAt(LocalDateTime.now())
                        .build());
        //when
        //then
        mockMvc.perform(get("/transaction/" + transactionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.amount").value(amount))
                .andDo(print());

    }
}