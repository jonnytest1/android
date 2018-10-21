package com.example.jonathan.ics.exceptions;

public class ExampleException extends Exception {

    private String message;

    private final String exampleMessage="example Exception";

    public ExampleException(){
        this.message=exampleMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
