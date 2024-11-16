package br.edu.utfpr.bankapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.utfpr.bankapi.dto.TransferDTO;
import br.edu.utfpr.bankapi.dto.WithdrawDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.repository.TransactionRepository;
import br.edu.utfpr.bankapi.validations.AvailableAccountValidation;
import br.edu.utfpr.bankapi.validations.AvailableBalanceValidation;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
	AvailableAccountValidation availableAccountValidation;

    @Mock
    AvailableBalanceValidation availableBalanceValidation;

    @InjectMocks
    TransactionService transactionService;

    @Mock
	TransactionRepository transactionRepository;

    @Mock
	Transaction transaction;

	@Captor
	ArgumentCaptor<Transaction> transactionCaptor;

    WithdrawDTO withdrawDTO;

    TransferDTO transferDTO;

    Account withdrawAccount;

    Account receiverAccount;

    Account sourceAccount;

    /**
	 * @throws NotFoundException
	 * 
	 */
    @Test
    void deveriaSacar() throws Exception {
        // ### ARRANGE ###

        double valor = 1000; // Valor a ser sacado
        double saldo = 2000; // Saldo da conta

        withdrawDTO = new WithdrawDTO(54321, valor);
        withdrawAccount = new Account("Power Guido", 54321, saldo, 0);
        
        BDDMockito.when(availableAccountValidation.validate(withdrawDTO.sourceAccountNumber())).thenReturn(withdrawAccount);
        availableBalanceValidation.validate(transaction);
        // ### ACT ###

        transactionService.withdraw(withdrawDTO);

        // ### ASSERT ###

        BDDMockito.then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction transactionSalva = transactionCaptor.getValue();

        // Verifica se o saldo foi atualizado corretamente
        Assertions.assertEquals(saldo - valor, transactionSalva.getSourceAccount().getBalance());
    }

    @Test
    void deveriaTransferir() throws Exception {
        // ### ARRANGE ###

        double valor = 1000; // Valor a ser transferido
        double saldoOrigem = 2000; // Saldo da conta de origem
        double saldoDestino = 500; // Saldo da conta de destino

        transferDTO = new TransferDTO(54321, 88888, valor);
        sourceAccount = new Account("Power Guido", 54321, saldoOrigem, 0);
        receiverAccount = new Account("John Smith", 88888, saldoDestino, 0);
        
        BDDMockito.when(availableAccountValidation.validate(transferDTO.sourceAccountNumber())).thenReturn(sourceAccount);
        BDDMockito.when(availableAccountValidation.validate(transferDTO.receiverAccountNumber())).thenReturn(receiverAccount);
        availableBalanceValidation.validate(transaction);
        // ### ACT ###

        transactionService.transfer(transferDTO);

        // ### ASSERT ###

        BDDMockito.then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction transactionSalva = transactionCaptor.getValue();

        // Verifica se o saldo foi atualizado corretamente
        Assertions.assertEquals(saldoOrigem - valor, transactionSalva.getSourceAccount().getBalance());
        Assertions.assertEquals(saldoDestino + valor, transactionSalva.getReceiverAccount().getBalance());
    }
}
