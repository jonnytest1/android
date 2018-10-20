package com.example.jonathan.ics.exceptions;

public class ExampleException extends Exception {

    final String exampleMessage="example";

    String message=exampleMessage;
    public ExampleException(){
    }
    public ExampleException(String msg){
        message=msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
