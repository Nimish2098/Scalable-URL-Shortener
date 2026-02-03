package com.project.urlshortener.exception;

public class InvalidUrlExecption extends RuntimeException{
    public InvalidUrlExecption(String message){
        super(message);
    }
}
