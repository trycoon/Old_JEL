/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.eventlog;

import java.util.Date;

/**
 * Copyright (c) 2009-dec-29 by Liquidbytes. All rights reserved.
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public class LogEntry
{
    private Date mEventTime;
    private String mMessage;
    private String mEventName;


    public LogEntry() {
        mEventTime = null;
        mMessage = null;
        mEventName = null;
    }


    public LogEntry( Date time, String message, String eventName ) {
        mEventTime = time;
        mMessage = message;
        mEventName = eventName;
    }


    public Date getTime() {
        return mEventTime;
    }


    public void setTime( Date time ) {
        mEventTime = time;
    }


    public String getMessage() {
        return mMessage;
    }


    public void setMessage( String message ) {
        mMessage = message;
    }


    public String getEventName() {
        return mEventName;
    }


    public void setEventName( String eventName ) {
        mEventName = eventName;
    }
}
