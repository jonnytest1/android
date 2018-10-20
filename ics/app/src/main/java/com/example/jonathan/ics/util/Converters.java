package com.example.jonathan.ics.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Converters {


    public static String toString (StackTraceElement[] stackArray){
        return Arrays.stream(stackArray).map(sE->sE.toString()).collect(Collectors.joining("\n"));
    }
}
