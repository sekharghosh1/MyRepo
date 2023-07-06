package com.dws.challenge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class Transaction {

    @NotNull
    @NotEmpty
    private String transactionId;

    @NotNull
    @Min(value = 1, message = "Minmum amount to be transfered")
    @Max(value = 100000, message = "Max amount to be transfered per transaction")
    private BigDecimal amount;

    private String transactionDatetime;

    @NotNull
    private Account sourceAccount;
    @NotNull
    private Account targetAccount;


}
