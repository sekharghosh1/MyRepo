package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.validation.ValidateInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PostMapping("/{sourceAccountId}/transfer/{targetAccountId}")
    public ResponseEntity<String> transferMoney(
            @PathVariable String sourceAccountId,
            @PathVariable String targetAccountId,
            @RequestParam BigDecimal amount
    ) {
        Account sourceAccount = null;
        Account targetAccount = null;

        if (ValidateInput.isValidInput(sourceAccountId, targetAccountId, amount)) {
            sourceAccount = accountsService.getAccount(sourceAccountId);
            targetAccount = accountsService.getAccount(targetAccountId);
        } else {
            return new ResponseEntity<String>("Please check AccNumber or amount to be transfered!!", HttpStatus.BAD_REQUEST);
        }

        if (sourceAccount == null || targetAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid account number");
        }

        if (sourceAccountId.equals(targetAccountId) || sourceAccountId.equalsIgnoreCase(targetAccountId)) {
            return new ResponseEntity<>("Transfer not possible between same account", HttpStatus.NOT_ACCEPTABLE);
        }

            boolean isTransfer = accountsService.transferMoney(sourceAccount, targetAccount, amount);


        if (isTransfer) {
            return ResponseEntity.status(HttpStatus.OK).body("Money transferred successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient Balance");
        }
    }
}
