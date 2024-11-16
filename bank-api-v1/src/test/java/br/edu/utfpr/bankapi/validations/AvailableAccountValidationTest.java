package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvailableAccountValidationTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AvailableAccountValidation availableAccountValidation;

    public AvailableAccountValidationTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveriaRetornarContaExistente() throws Exception {
        // ### ARRANGE ###
        long accountNumber = 12345L;
        Account expectedAccount = new Account("John Doe", accountNumber, 1000.0, 500.0);

        // Configura o repositório para retornar a conta
        BDDMockito.given(accountRepository.getByNumber(accountNumber)).willReturn(Optional.of(expectedAccount));

        // ### ACT ###
        Account result = availableAccountValidation.validate(accountNumber);

        // ### ASSERT ###
        assertEquals(expectedAccount, result);
    }

    @Test
    void deveriaLancarNotFoundExceptionQuandoContaNaoExistir() {
        // ### ARRANGE ###
        long accountNumber = 99999L;

        // Configura o repositório para retornar um Optional vazio
        BDDMockito.given(accountRepository.getByNumber(accountNumber)).willReturn(Optional.empty());

        // ### ACT & ASSERT ###
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> availableAccountValidation.validate(accountNumber));

        assertEquals("Conta 99999 inexistente", exception.getMessage());
    }
}
