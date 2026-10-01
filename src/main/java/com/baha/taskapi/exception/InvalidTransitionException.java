package com.baha.taskapi.exception;

public class InvalidTransitionException extends RuntimeException{
    public InvalidTransitionException() {
        super("Invalid status transition");
    }
}