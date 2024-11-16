package br.edu.utfpr.bankapi.controller;

import br.edu.utfpr.bankapi.dto.TransferDTO;
import br.edu.utfpr.bankapi.dto.WithdrawDTO;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import br.edu.utfpr.bankapi.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveriaRealizarTransferenciaComSucesso() throws Exception {
        // ### ARRANGE ###
        TransferDTO transferDTO = new TransferDTO(66666, 12346, 15000.0);

        // Criação dos objetos esperados na resposta
        Account sourceAccount = new Account("Guido Power", 66666, 184500.0, 1.0E9);
        Account receiverAccount = new Account("Ana Campos", 12346, 15500.0, 0.0);

        Transaction expectedTransaction = new Transaction(sourceAccount, receiverAccount, 15000.0, TransactionType.TRANSFER);

        BDDMockito.given(transactionService.transfer(transferDTO)).willReturn(expectedTransaction);

        // Transformando a resposta esperada em JSON para validação
        String expectedResponse = objectMapper.writeValueAsString(expectedTransaction);

        // ### ACT & ASSERT ###
        mockMvc.perform(post("/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void deveriaFalharNaTransferencia() throws Exception {
        // ### ARRANGE ###
        TransferDTO transferDTO = new TransferDTO(11111, 22222, 15000);
        String errorMessage = "No balance in account";

        BDDMockito.given(transactionService.transfer(transferDTO))
                .willThrow(new RuntimeException(errorMessage));

        // ### ACT & ASSERT ###
        mockMvc.perform(post("/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void deveriaRealizarSaqueComSucesso() throws Exception {
        // ### ARRANGE ###
        WithdrawDTO withdrawDTO = new WithdrawDTO(11111, 1000.0);

        // Configuração da conta inicial e da resposta esperada
        Account sourceAccount = new Account("Paula Tejando", 11111, 1000.0, 1000.0);
        Transaction expectedTransaction = new Transaction(sourceAccount, null, 1000.0, TransactionType.WITHDRAW);

        // Configurando o mock para retornar a transação esperada
        BDDMockito.given(transactionService.withdraw(withdrawDTO)).willReturn(expectedTransaction);

        // Transformando a resposta esperada em JSON
        String expectedResponse = objectMapper.writeValueAsString(expectedTransaction);

        // ### ACT & ASSERT ###
        mockMvc.perform(post("/transaction/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawDTO)))
                .andExpect(status().isCreated()) // Verifica se o status HTTP é 201
                .andExpect(content().json(expectedResponse)); // Verifica se a resposta JSON corresponde à esperada
    }

    @Test
    void deveriaFalharNoSaque() throws Exception {
        // ### ARRANGE ###
        WithdrawDTO withdrawDTO = new WithdrawDTO(12346, 10000000);
        String errorMessage = "No balance in account";

        BDDMockito.doThrow(new RuntimeException(errorMessage)).when(transactionService).withdraw(withdrawDTO);

        // ### ACT & ASSERT ###
        mockMvc.perform(post("/transaction/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
}
