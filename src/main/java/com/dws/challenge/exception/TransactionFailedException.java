package com.dws.challenge.exception;

public class TransactionFailedException extends RuntimeException{

    public TransactionFailedException(String message){
        super(message);
    }

    public TransactionFailedException(String messgage, Throwable cause){
        super(messgage,cause);
    }
}
