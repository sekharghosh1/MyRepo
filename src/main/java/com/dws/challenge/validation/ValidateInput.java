package com.dws.challenge.validation;

import java.math.BigDecimal;

public class ValidateInput {
    public static boolean isValidInput(String sourceAccountId, String targetAccountId, BigDecimal amount) {

        return !(sourceAccountId == null || sourceAccountId.isEmpty())
                && !(targetAccountId == null || targetAccountId.isEmpty())
                && amount.compareTo(BigDecimal.ZERO) >=0;
    }
}
