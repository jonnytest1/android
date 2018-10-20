package com.example.jonathan.ics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
public class LoggingElement extends Object {

    String date;
    String content;
    String title;

    LoggingElement(){

    }

    LoggingElement(Throwable throwable){
        date=new Date().toLocaleString();
        title=throwable.getMessage();
        content=Arrays.stream(throwable.getStackTrace()).map(sE->sE.toString()).collect(Collectors.joining("\n"));
    }

    LoggingElement(String value){
        ObjectMapper mapper = new ObjectMapper();
        try {
            LoggingElement lE=mapper.readValue(value,LoggingElement.class);
            content=lE.content;
            date=lE.date;
            title= lE.getTitle();
        } catch (IOException e) {
            date=new Date().toLocaleString();
            title=value;
        }
    }

    public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
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
}
