/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.eventlog;

import java.util.Date;
import java.util.List;
import jel.eventlog.EventlogManager.LogType;
import jel.security.ISession;
import jel.storage.StorageManager.SortOrder;

/**
 * Copyright (c) 2009-dec-30 by Liquidbytes. All rights reserved.
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public interface IEventLogManager
{
    public List<LogEntry> getEventLog( ISession session, LogType logType, int userID, Date start, Date end );
    public List<LogEntry> getEventLog(ISession session, LogType logType, int userID, int number, SortOrder order);
    public void addEventToLog( ISession session, LogType logType, int userID, String message );
}
