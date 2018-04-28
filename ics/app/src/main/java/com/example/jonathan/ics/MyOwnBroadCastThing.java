package com.example.jonathan.ics;

import java.util.HashMap;
import java.util.Map;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Jonathan on 28.04.2018.
 */

public class MyOwnBroadCastThing {

    private static Map<interfaceHandler.tts,ListenerInterface> listeners=new HashMap<>();

    static ExecutorService es= Executors.newSingleThreadExecutor();


    public static Map<interfaceHandler.tts,ListenerInterface> register(ListenerInterface Li,interfaceHandler.tts target){
        listeners.put(target,Li);
        return listeners;
    }

    public static String push(interfaceHandler.tts target,Enum action,String value,String value2){
            for (Map.Entry<interfaceHandler.tts,ListenerInterface> i : listeners.entrySet()){
                if(i.getKey().equals(target)){
                    try {
                        return i.getValue().OnReceiveMessage(target, action, value, value2);
                    }catch (Exception e){
                        interfaceHandler.note("EXCEPTION",conv(e));
                    }
                }
            }
            return null;
    }
    static String conv(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();



        String out=e.getMessage();
        for(StackTraceElement sE :e.getStackTrace()){
            if(sE.toString().contains("com.example.jonathan.ics")){
                out+="\n"+sE.getClassName().replace("com.example.jonathan.ics.","")+"."+sE.getMethodName()+":"+sE.getLineNumber();
            }
        }
        return out;
    }
}
class listnerStruct
{
    listnerStruct(ListenerInterface Li,interfaceHandler.tts target){
        this.Li=Li;
        this.target=target;
    }

    public ListenerInterface Li;
    public interfaceHandler.tts target;

    public interfaceHandler.tts getTarget(){
        return target;
    }
};