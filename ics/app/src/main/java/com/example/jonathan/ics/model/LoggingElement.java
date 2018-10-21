package com.example.jonathan.ics.model;

import com.example.jonathan.ics.util.Converters;

import java.util.Date;
public class LoggingElement {

    private String date;
    private String content;
    private String title;

    private boolean errorStack;

    //implicit contructor for jsonParsing
    public  LoggingElement(){

    }
    public LoggingElement(Throwable throwable){
        date=new Date().toLocaleString();
        title=throwable.getMessage();
        content=Converters.toString(throwable.getStackTrace());
        errorStack =true;
    }

    public LoggingElement(String value){
       date=new Date().toLocaleString();
       title=value;
       content=Converters.toString(Thread.currentThread().getStackTrace());
       errorStack =false;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isErrorStack() {
        return errorStack;
    }

}
