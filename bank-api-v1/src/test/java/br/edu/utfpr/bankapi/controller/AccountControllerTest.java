package br.edu.utfpr.bankapi.controller;

import br.edu.utfpr.bankapi.dto.AccountDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService; // Atualizado para @MockBean

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveriaRetornarContaPorNumero() throws Exception {
        // ### ARRANGE ###
        long accountNumber = 12345;
        Account account = new Account("John Doe", accountNumber, 1000.0, 500.0);

        BDDMockito.given(accountService.getByNumber(accountNumber)).willReturn(Optional.of(account));

        // ### ACT & ASSERT ###
        mockMvc.perform(get("/account/{number}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(account.getName()))
                .andExpect(jsonPath("$.balance").value(account.getBalance()))
                .andExpect(jsonPath("$.specialLimit").value(account.getSpecialLimit()));
    }

    @Test
    void deveriaRetornarNotFoundQuandoContaNaoExistir() throws Exception {
        // ### ARRANGE ###
        long accountNumber = 99999;

        BDDMockito.given(accountService.getByNumber(accountNumber)).willReturn(Optional.empty());

        // ### ACT & ASSERT ###
        mockMvc.perform(get("/account/{number}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveriaRetornarListaDeContas() throws Exception {
        // ### ARRANGE ###
        Account account1 = new Account("John Doe", 12345, 1000.0, 500.0);
        Account account2 = new Account("Jane Doe", 67890, 2000.0, 1000.0);

        BDDMockito.given(accountService.getAll()).willReturn(Arrays.asList(account1, account2));

        // ### ACT & ASSERT ###
        mockMvc.perform(get("/account")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(account1.getName()))
                .andExpect(jsonPath("$[1].name").value(account2.getName()));
    }

    @Test
    void deveriaCriarNovaConta() throws Exception {
        // ### ARRANGE ###
        AccountDTO accountDTO = new AccountDTO("John Doe", 12345L, 1000.0, 500.0);
        Account savedAccount = new Account(accountDTO.name(), accountDTO.number(), accountDTO.balance(), accountDTO.specialLimit());

        BDDMockito.given(accountService.save(accountDTO)).willReturn(savedAccount);

        // ### ACT & ASSERT ###
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(savedAccount.getName()))
                .andExpect(jsonPath("$.balance").value(savedAccount.getBalance()))
                .andExpect(jsonPath("$.specialLimit").value(savedAccount.getSpecialLimit()));
    }

    @Test
    void deveriaAtualizarConta() throws Exception {
        // ### ARRANGE ###
        long accountId = 1L;
        AccountDTO accountDTO = new AccountDTO("John Doe Updated", 12345L, 1500.0, 700.0);
        Account updatedAccount = new Account(accountDTO.name(), accountDTO.number(), accountDTO.balance(), accountDTO.specialLimit());

        BDDMockito.given(accountService.update(accountId, accountDTO)).willReturn(updatedAccount);

        // ### ACT & ASSERT ###
        mockMvc.perform(put("/account/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedAccount.getName()))
                .andExpect(jsonPath("$.balance").value(updatedAccount.getBalance()))
                .andExpect(jsonPath("$.specialLimit").value(updatedAccount.getSpecialLimit()));
    }

    @Test
    void deveriaRetornarNotFoundQuandoAtualizarContaInexistente() throws Exception {
        // ### ARRANGE ###
        long accountId = 999L;
        AccountDTO accountDTO = new AccountDTO("Non-existent", 33333L, 1500.0, 700.0);

        BDDMockito.given(accountService.update(accountId, accountDTO)).willThrow(new NotFoundException("Account not found"));

        // ### ACT & ASSERT ###
        mockMvc.perform(put("/account/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found"));
    }
}