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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountsService {

    private Map<Account, LockWrapper> accountLocks = new ConcurrentHashMap<>();

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


    public boolean transferMoney(Account sourceAccount, Account targetAccount, BigDecimal amount) {

        LockWrapper sourceLock = accountLocks.computeIfAbsent(sourceAccount, k -> new ReentrantLockWrapper());
        LockWrapper targetLock = accountLocks.computeIfAbsent(targetAccount, k -> new ReentrantLockWrapper());

        boolean isSourceLockAcquired = false;
        boolean isTargetLockAcquired = false;

        try{
            isSourceLockAcquired = sourceLock.tryLock();
            isTargetLockAcquired = targetLock.tryLock();

            if(isSourceLockAcquired && isTargetLockAcquired){
                if (sourceAccount.getBalance().compareTo(amount) >= 0) {

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
                            .amount(amount)
                            .build();

                    sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
                    targetAccount.setBalance(targetAccount.getBalance().add(amount));

                    NotificationService notificationService = new EmailNotificationService();
                    notificationService.notifyAboutTransfer(sourceAccount,"Your account has been sucessfully debited with "+transaction.getAmount()
                            +" on "+transaction.getTransactionDatetime()
                            +" TransactionID "+transaction.getTransactionId()
                            +" Available Balance :"+sourceAccount.getBalance());

                    notificationService.notifyAboutTransfer(targetAccount,"Your account has been sucessfully credited with "+transaction.getAmount()
                            +" on "+transaction.getTransactionDatetime()
                            +" TransactionID "+transaction.getTransactionId()
                            +" Available Balance :"+targetAccount.getBalance());

                    return true;
                }
                    return false;

            }else{
                return  false;
            }
        }
        catch (Exception ex){
            throw new TransactionFailedException("Transaction failed!!",ex);
        }
        finally {
            if(isSourceLockAcquired){
                sourceLock.unlock();
            }
            if(isTargetLockAcquired){
                targetLock.unlock();
            }
        }

    }
    public void setLock(Account account, LockWrapper lock) {
        accountLocks.put(account, lock);
    }
}
