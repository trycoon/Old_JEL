/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.eventlog;

import java.util.Date;
import java.util.List;
import jel.security.ISession;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.storage.StorageManager.SortOrder;
import org.apache.log4j.Logger;

/**
 * Copyright (c) 2009-dec-29 by Liquidbytes. All rights reserved.
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public final class EventlogManager implements IEventLogManager {

    private static Logger mLogger = Logger.getLogger(EventlogManager.class);
    private jel.storage.IStorageManager mStorageManager;
    private jel.security.SecurityManager mSecurityManager;

    public static enum LogType {

        UserLogin, UserLogout, DeviceAdded, DeviceRemoved
    }

    public void setStorageManager(jel.storage.IStorageManager storageManager) {
        this.mStorageManager = storageManager;
    }

    public void setSecurityManager(jel.security.SecurityManager securityManager) {
        this.mSecurityManager = securityManager;
    }

    public List<LogEntry> getEventLog(ISession session, LogType logType, int userID, Date startDate, Date endDate) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (userID < 1) {
            throw new JelException("No userID to has been provided", ExceptionReason.WRONG_PARAMETERS, ExceptionType.ERROR);
        }
        if (session.getUserID() != userID && !mStorageManager.isAdminUser(session)) {
            throw new JelException("Logevents can only be fetched for your own user", ExceptionReason.NO_PRIVILEGES, ExceptionType.WARN);
        }

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            throw new JelException("No dateparameters provided or dateparameters out of range", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        List<LogEntry> entries = mStorageManager.getEventLog(session, logType == null ? null : logType.toString(), userID, startDate, endDate);

        return entries;
    }

    public List<LogEntry> getEventLog(ISession session, LogType logType, int userID, int number, SortOrder order) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (userID < 1) {
            throw new JelException("No userID to has been provided", ExceptionReason.WRONG_PARAMETERS, ExceptionType.ERROR);
        }
        if (session.getUserID() != userID && !mStorageManager.isAdminUser(session)) {
            throw new JelException("Logevents can only be fetched for your own user", ExceptionReason.NO_PRIVILEGES, ExceptionType.WARN);
        }

        if (number < 0) {
            number = 0;
        }

        if (order == null) {
            throw new JelException("No sortorder provided", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        List<LogEntry> entries = mStorageManager.getEventLog(session, logType == null ? null : logType.toString(), userID, number, order);

        return entries;
    }

    public void addEventToLog(ISession session, LogType logType, int userID, String message) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (logType == null) {
            throw new JelException("No logType selected", ExceptionReason.WRONG_PARAMETERS, ExceptionType.ERROR);
        }
        if (userID < 1) {
            throw new JelException("No userID to log this event on has been provided", ExceptionReason.WRONG_PARAMETERS, ExceptionType.ERROR);
        }
        if (session.getUserID() != userID) {
            throw new JelException("Logging of events can only be made to your own user", ExceptionReason.NO_PRIVILEGES, ExceptionType.WARN);
        }

        mStorageManager.addEventToLog(session, logType.toString(), userID, message);
    }
}
