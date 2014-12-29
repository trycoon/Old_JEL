/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.security;

import java.util.Calendar;

/**
 *
 * @author trycoon
 */
public class LoginInformation implements ILoginInformation 
{
    private ISession mSession;
    private String mFirstName;
    private String mLastName;
    private boolean mIsMasterAdmin;
    private Calendar mServerTime;
    
    
    public LoginInformation()
    {
        mSession = null;
        mFirstName = "";
        mLastName = "";
        mIsMasterAdmin = false;
        mServerTime = null;
    }
    
    
    public ISession getSession() {
        return mSession;
    }

    public void setSession(ISession session) {
        mSession = session;
    }

    public Calendar getServerTime() {
        return Calendar.getInstance();
    }
    
    public void setServerTime(Calendar time) {
        mServerTime = time;
    }
    
    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }
    
    public boolean getIsMasterAdmin() {
        return mIsMasterAdmin;
    }

    public void setIsMasterAdmin(boolean admin) {
        mIsMasterAdmin = admin;
    }
}
