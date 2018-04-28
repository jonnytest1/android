package com.example.jonathan.ics;

/**
 * Created by Jonathan on 28.04.2018.
 */

public interface ListenerInterface {

    public interfaceHandler.tts targetIdentifier=null;

    public String OnReceiveMessage(interfaceHandler.tts target, Enum action, String value, String value2);


    public void OnNewListenerRegistered(interfaceHandler.tts target);
}
