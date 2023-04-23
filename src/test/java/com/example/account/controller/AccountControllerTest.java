package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccountDto;
import com.example.account.service.AccountService;
import com.example.account.service.LockService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//테스트에 필요한 Bean 들만 만들어서 테스트 하는 방식.
//WebMVC Test
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    //@Mock 과 유사한 @MockBean 을 만든다.
    //@InjectionMock 을 할 필요는 없다. Bean 으로 등록되기 때문에
    @MockBean
    private AccountService accountService;

    @MockBean
    private LockService redisTestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successCreateAccount()  throws Exception{
        //given
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("12345")
                        .registeredAt(LocalDateTime.now())
                        .build());
        //when
        //then
        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccountDto.Request(333L, 1234L)
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andDo(print());
    }
}