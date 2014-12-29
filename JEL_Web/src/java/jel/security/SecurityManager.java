/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jel.eventlog.EventlogManager;
import jel.eventlog.EventlogManager.LogType;
import jel.security.event.IUserLoginListener;
import jel.security.event.IUserLogoutListener;
import jel.security.event.SecurityEvent;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.user.IUser;

/**
 *
 * @author trycoon
 */
public final class SecurityManager implements ISecurityManager
{

    private jel.storage.IStorageManager mStorageManager;
    private jel.eventlog.IEventLogManager mEventlogManager;
    private List<IUserLoginListener> mUserLoginListeners = new ArrayList<IUserLoginListener>();
    private List<IUserLogoutListener> mUserLogoutListeners = new ArrayList<IUserLogoutListener>();


    public void setStorageManager(jel.storage.IStorageManager storageManager) {
        this.mStorageManager = storageManager;
    }


    public void setEventlogManager( jel.eventlog.IEventLogManager eventlogManager ) {
        this.mEventlogManager = eventlogManager;
    }


    public ILoginInformation login(String username, String password) {
        if (username == null || username.length() == 0 || password == null || password.length() == 0) {
            throw new JelException("No valid username or password provided", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        ILoginInformation information = mStorageManager.login(username, password);
        IUser user = mStorageManager.getUser(information.getSession(), information.getSession().getUserID());
        mEventlogManager.addEventToLog(null, LogType.UserLogin, user.getID(), "User logged in");
        
        this.notifyUserLogin(user);

        return information;
    }


    public void logout(ISession session) {
        if (session != null && session.getUserID() > 0 && session.getToken() != null) {
            IUser user = mStorageManager.getUser(session, session.getUserID());
            mStorageManager.logout(session);
            mEventlogManager.addEventToLog(null, LogType.UserLogout, user.getID(), "User logged out");
            
            this.notifyUserLogout(user);
        }
    }


    public boolean verifySession(ISession session) {
        if (session != null && session.getUserID() > 0 && session.getToken() != null && session.getToken().length() > 0) {
            return mStorageManager.verifySession(session);
        }
        return false;
    }


    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS //////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    public void addUserLoginListener(IUserLoginListener listener) {
        if (listener != null && !mUserLoginListeners.contains(listener)) {
            mUserLoginListeners.add(listener);
        }
    }


    public void removeUserLoginListener(IUserLoginListener listener) {
        mUserLoginListeners.remove(listener);
    }


    private void notifyUserLogin(IUser user) {
        try {
            for (IUserLoginListener listener : mUserLoginListeners) {
                listener.userLogin(new SecurityEvent(this, user, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addUserLogoutListener(IUserLogoutListener listener) {
        if (listener != null && !mUserLogoutListeners.contains(listener)) {
            mUserLogoutListeners.add(listener);
        }
    }


    public void removeUserLogoutListener(IUserLogoutListener listener) {
        mUserLogoutListeners.remove(listener);
    }


    private void notifyUserLogout(IUser user) {
        try {
            for (IUserLogoutListener listener : mUserLogoutListeners) {
                listener.userLogout(new SecurityEvent(this, user, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }
    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS END //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}
