package com.dws.challenge.validation;

public class ValidateInput {
    public static boolean isValidInput(String sourceAccountId, String targetAccountId, double amount) {

        return !(sourceAccountId == null || sourceAccountId.isEmpty())
                && !(targetAccountId == null || targetAccountId.isEmpty())
                && amount > 0;
    }
}
