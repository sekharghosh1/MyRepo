package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.LockWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;


    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    void transferMoney_insufficientBalance() {
        Account sourceAccount = new Account("sourceAccount", BigDecimal.valueOf(100.0));
        Account targetAccount = new Account("targetAccount", BigDecimal.valueOf(500.0));
        BigDecimal transferAmount = BigDecimal.valueOf(200.0);

        boolean transferResult = accountsService.transferMoney(sourceAccount, targetAccount, transferAmount);

        assertFalse(transferResult);
        assertEquals(BigDecimal.valueOf(100.0), sourceAccount.getBalance());
        assertEquals(BigDecimal.valueOf(500.0), targetAccount.getBalance());
    }
    @Test
    void transferMoney_onSuccess() {
        Account sourceAccount = new Account("s1");
        sourceAccount.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(sourceAccount);

        Account targetAccount = new Account("t1");
        targetAccount.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(targetAccount);

        BigDecimal transferAmount = BigDecimal.valueOf(100.0);

        LockWrapper sourceLock = mock(LockWrapper.class);
        LockWrapper targetLock = mock(LockWrapper.class);

        when(sourceLock.tryLock()).thenReturn(true);
        when(targetLock.tryLock()).thenReturn(true);

        boolean transferResult = accountsService.transferMoney(sourceAccount, targetAccount, transferAmount);

        assertTrue(transferResult);
        assertEquals(BigDecimal.valueOf(900.0), sourceAccount.getBalance());
        assertEquals(BigDecimal.valueOf(600.0), targetAccount.getBalance());

    }

    @Test
    void transferMoney_unableToAcquireLocks() {

        Account sourceAccount = new Account("123467");
        sourceAccount.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(sourceAccount);

        Account targetAccount = new Account("134562");
        targetAccount.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(targetAccount);

        BigDecimal transferAmount = BigDecimal.valueOf(100.0);

        LockWrapper sourceLock = mock(LockWrapper.class);
        LockWrapper targetLock = mock(LockWrapper.class);

        when(sourceLock.tryLock()).thenReturn(false);
        when(targetLock.tryLock()).thenReturn(false);

        accountsService.setLock(sourceAccount, sourceLock);
        accountsService.setLock(targetAccount, targetLock);

        boolean result = accountsService.transferMoney(sourceAccount, targetAccount, transferAmount);


        assertFalse(result);
        verify(sourceLock, times(1)).tryLock();
        verify(targetLock, times(1)).tryLock();
        verify(sourceLock, never()).unlock();
        verify(targetLock, never()).unlock();
    }
}
