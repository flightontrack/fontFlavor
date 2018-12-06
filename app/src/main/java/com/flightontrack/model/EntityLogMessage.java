package com.flightontrack.model;

public class EntityLogMessage {
    public String tag;
    public String msg;
    public char msgType;
    public Exception e;

    public EntityLogMessage(String p1, String p2, char p3){
        tag = p1;
        msg = p2;
        msgType = p3;
    }
    public EntityLogMessage(String p1, String p2, char p3, Exception p4){
        tag = p1;
        msg = p2;
        msgType = p3;
        e = p4;
    }
}
