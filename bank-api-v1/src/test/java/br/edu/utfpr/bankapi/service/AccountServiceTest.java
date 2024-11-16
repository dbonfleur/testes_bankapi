package br.edu.utfpr.bankapi.service;

import br.edu.utfpr.bankapi.dto.AccountDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Captor
    ArgumentCaptor<Account> accountCaptor;

    AccountDTO accountDTO;

    Account account;

    @Test
    void deveriaObterTodasAsContas() {
        // ### ARRANGE ###
        Account account1 = new Account(1L, "Juca Silva", 11111, 0, 3000);
        Account account2 = new Account(2L, "Ana Campos", 12346, 0, 0);
        
        BDDMockito.when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        // ### ACT ###
        List<Account> accounts = accountService.getAll();

        // ### ASSERT ###
        Assertions.assertNotNull(accounts);
        Assertions.assertEquals(2, accounts.size());
        Assertions.assertEquals("Juca Silva", accounts.get(0).getName());
        Assertions.assertEquals("Ana Campos", accounts.get(1).getName());
    }

    @Test
    void deveriaObterContaPorNumero() {
        // ### ARRANGE ###
        Account account = new Account("Ricardo Sobjak", 12345, 0, 1000);
        BDDMockito.when(accountRepository.getByNumber(12345)).thenReturn(Optional.of(account));

        // ### ACT ###
        Optional<Account> result = accountService.getByNumber(12345);

        // ### ASSERT ###
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Ricardo Sobjak", result.get().getName());
        Assertions.assertEquals(12345, result.get().getNumber());
    }

    @Test
    void deveriaSalvarUmaConta() {
        // ### ARRANGE ###
        Account account = new Account("Ricardo Sobjak", 11111, 0, 3000);

        BDDMockito.when(accountRepository.save(BDDMockito.any(Account.class))).thenReturn(account);

        long numberAcount = 11111;
        accountDTO = new AccountDTO("Ricardo Sobjack", numberAcount, 0, 3000);

        // ### ACT ###
        Account result = accountService.save(accountDTO);

        // ### ASSERT ###
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Ricardo Sobjak", result.getName());
        Assertions.assertEquals(11111, result.getNumber());
        Assertions.assertEquals(0, result.getBalance());
        Assertions.assertEquals(3000, result.getSpecialLimit());
    }

    @Test
    void deveriaAtualizarUmaConta() throws NotFoundException {
        // ### ARRANGE ###
        Account existingAccount = new Account(1L, "Juca Silva", 11111, 0, 3000);

        // Definindo retorno ao buscar uma conta existente
        BDDMockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        BDDMockito.when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

        long id = existingAccount.getId();
        long numberExistingAccount = 11111;
        accountDTO = new AccountDTO("Juca Silva de Pedra", numberExistingAccount, 0, 4000);

        // ### ACT ###
        Account result = accountService.update(id, accountDTO);

        // ### ASSERT ###
        BDDMockito.then(accountRepository).should().save(accountCaptor.capture());
        Account accountSalva = accountCaptor.getValue();

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Juca Silva de Pedra", accountSalva.getName());
        Assertions.assertEquals(4000, accountSalva.getSpecialLimit());
    }

    @Test
    void deveriaLancarExcecaoQuandoContaNaoExistirParaAtualizacao() {
        // ### ARRANGE ###
        long idInexistente = 999L; // Um ID que não existe no banco de dados
        long numberAccountDTO = 12345;
        AccountDTO accountDTO = new AccountDTO("Nome Qualquer", numberAccountDTO, 0, 4000);

        // Simulando que o repositório não encontra uma conta com o ID especificado
        BDDMockito.when(accountRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // ### ACT & ASSERT ###
        // Verifica se a exceção NotFoundException é lançada ao tentar atualizar uma conta inexistente
        Assertions.assertThrows(NotFoundException.class, () -> {
            accountService.update(idInexistente, accountDTO);
        });

        // Verifica que o método save nunca foi chamado
        BDDMockito.then(accountRepository).should(BDDMockito.never()).save(BDDMockito.any());
    }
}
