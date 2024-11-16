package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.exception.WithoutBalanceException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AvailableBalanceValidationTest {

    private final AvailableBalanceValidation availableBalanceValidation = new AvailableBalanceValidation();

    @Test
    void deveriaPermitirTransacaoComSaldoSuficiente() {
        // ### ARRANGE ###
        Account sourceAccount = new Account("John Doe", 12345, 1000.0, 500.0); // Saldo total: 1500
        Transaction transaction = new Transaction(sourceAccount, null, 1000.0, TransactionType.WITHDRAW);

        // ### ACT & ASSERT ###
        availableBalanceValidation.validate(transaction); // Não deve lançar exceção
    }

    @Test
    void deveriaLancarWithoutBalanceExceptionQuandoSaldoInsuficiente() {
        // ### ARRANGE ###
        Account sourceAccount = new Account("John Doe", 12345, 1000.0, 500.0); // Saldo total: 1500
        Transaction transaction = new Transaction(sourceAccount, null, 2000.0, TransactionType.WITHDRAW);

        // ### ACT & ASSERT ###
        assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }
}
