package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.web.AccountsController;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountsController.class)
public class ControllerTransferTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsService accountsService;


    /*
    * Test Case: Transfer Money - Invalid Source Account
      Description: Test when the source account ID is invalid or missing.
    * */
    @Test
    public void testTransferMoney_InvalidSourceAccount() throws Exception {

        String sourceAccountId = "invalidSourceAccount";
        String targetAccountId = "targetAccount";
        double amount = 100.0;

        when(accountsService.getAccount(sourceAccountId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Invalid account number"));
    }

    /*
    * Test Case: Transfer Money - Invalid Target Account
      Description: Test when the target account ID is invalid or missing.
    * */

    @Test
    public void testTransferMoney_InvalidTargetAccount() throws Exception {

        String sourceAccountId = "invalidTargetAccount";
        String targetAccountId = "targetAccount";
        double amount = 100.0;

        when(accountsService.getAccount(targetAccountId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", "100.0"))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Invalid account number"));
    }


    /*
    * Test Case: Transfer Money - Negative Amount
      Description: Test when the transfer amount is negative.
    *
    * */

    @Test
    void transferMoney_negativeAmount() throws Exception {
       
        String sourceAccountId = "sourceAccount";
        String targetAccountId = "targetAccount";
        double amount = -100.0;


        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(accountsService);
    }

    /*
    *Test Case: Transfer Money - Insufficient Balance
    Description: Test when the source account has insufficient balance for the transfer.
    * */
    @Test
    void transferMoney_insufficientBalance() throws Exception {

        String sourceAccountId = "sourceAccount";
        String targetAccountId = "targetAccount";
        double amount = 100.0;

        Account sourceAccount = new Account(sourceAccountId, BigDecimal.valueOf(50.0));
        Account targetAccount = new Account(targetAccountId, BigDecimal.valueOf(1000.0));

        when(accountsService.getAccount(sourceAccountId)).thenReturn(sourceAccount);
        when(accountsService.getAccount(targetAccountId)).thenReturn(targetAccount);

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /*
    * Test Case: Transfer Money - Same Account
      Description: Test when the source and target account IDs are the same.
    * */
    @Test
    void transferMoney_sameAccount() throws Exception {

        String sourceAccountId = "sourceAccount";
        String targetAccountId = "sourceAccount";
        double amount = 100.0;

        Account sourceAccount = new Account(sourceAccountId, BigDecimal.valueOf(5000.0));
        Account targetAccount = new Account(targetAccountId, BigDecimal.valueOf(1000.0));

        when(accountsService.getAccount(sourceAccountId)).thenReturn(sourceAccount);
        when(accountsService.getAccount(targetAccountId)).thenReturn(targetAccount);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());

    }


    /*
    * Test Case: Transfer Money - Account Not Found
    Description: Test when the source or target account is not found.
    * */
    @Test
    void transferMoney_accountNotFound() throws Exception {

        String sourceAccountId = "sourceAccount";
        String targetAccountId = "targetAccount";
        double amount = 100.0;

        when(accountsService.getAccount(anyString())).thenReturn(null);


        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /*
    * Test Case: Transfer Money - Success - multi Threaded Environment
    Description: Test the successful transfer of money between two accounts with multiple Threads.
    *
    * */

    @Test
    void transferMoney_success() throws Exception {

        String sourceAccountId = "sourceAccount";
        String targetAccountId = "targetAccount";
        BigDecimal amount = new BigDecimal(100.0);

        Account sourceAccount = new Account(sourceAccountId, new BigDecimal(10000.0));
        Account targetAccount = new Account(targetAccountId, new BigDecimal(5000.0));

        when(accountsService.getAccount(sourceAccountId)).thenReturn(sourceAccount);
        when(accountsService.getAccount(targetAccountId)).thenReturn(targetAccount);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                                    .param("amount", String.valueOf(amount))
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        verify(accountsService, times(10)).getAccount(sourceAccountId);
        verify(accountsService, times(10)).getAccount(targetAccountId);
        verify(accountsService, times(10)).transferMoney(sourceAccount, targetAccount, amount);

        executorService.shutdown();
    }

    /*
    * Test Case: Transfer Money - Success - multiple transfer
    Description: Test the successful transfer of money between two accounts,
    * multiple transfers happening at the same time and all succeeding.
    * */

    @Test
    void transferMoney_success_multipleTransfers() throws Exception {

        String sourceAccountId = "sourceAccount";
        String targetAccountId = "targetAccount";
        BigDecimal amount = new BigDecimal(100.0);
        int numThreads = 10;

        Account sourceAccount = new Account(sourceAccountId, new BigDecimal(10000.0));
        Account targetAccount = new Account(targetAccountId, new BigDecimal(5000.0));

        when(accountsService.getAccount(sourceAccountId)).thenReturn(sourceAccount);
        when(accountsService.getAccount(targetAccountId)).thenReturn(targetAccount);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{sourceAccountId}/transfer/{targetAccountId}", sourceAccountId, targetAccountId)
                                    .param("amount", String.valueOf(amount))
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        verify(accountsService, times(numThreads)).getAccount(sourceAccountId);
        verify(accountsService, times(numThreads)).getAccount(targetAccountId);
        verify(accountsService, times(numThreads)).transferMoney(sourceAccount, targetAccount, amount);

        executorService.shutdown();
    }

}
