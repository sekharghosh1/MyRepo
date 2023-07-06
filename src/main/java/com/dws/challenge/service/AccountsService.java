package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.TransactionFailedException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }


    public void transferMoney(Account sourceAccount, Account targetAccount, double amount) {

        UUID randomUUID = UUID.randomUUID();
        String uuidString = randomUUID.toString();

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        Transaction transaction =  Transaction.builder()
                .transactionId(uuidString)
                .transactionDatetime(formattedDateTime)
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .amount(BigDecimal.valueOf(amount))
                .build();

        try{
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(BigDecimal.valueOf(amount)));
            targetAccount.setBalance(targetAccount.getBalance().add(BigDecimal.valueOf(amount)));

            NotificationService notificationService = new EmailNotificationService();
            notificationService.notifyAboutTransfer(sourceAccount,"Your account has been sucessfully debited with "+transaction.getAmount()
                    +"on "+transaction.getTransactionDatetime()
                    +"TransactionID "+transaction.getTransactionId()
                    +"Available Balance :"+sourceAccount.getBalance());

            notificationService.notifyAboutTransfer(targetAccount,"Your account has been sucessfully credited with "+transaction.getAmount()
                    +"on "+transaction.getTransactionDatetime()
                    +"TransactionID "+transaction.getTransactionId()
                    +"Available Balance :"+targetAccount.getBalance());

        }catch (Exception ex){
            throw new TransactionFailedException("Transaction failed!!",ex);
        }
    }
}
