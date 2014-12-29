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
public interface ILoginInformation {
    
    public ISession getSession();
    
    public void setSession(ISession session);
    
    public Calendar getServerTime();
    
    public void setServerTime(Calendar time);
    
    public String getFirstName();
    
    public void setFirstName(String firstName);
    
    public String getLastName();
    
    public void setLastName(String lastName);
    
    public boolean getIsMasterAdmin();
    
    public void setIsMasterAdmin(boolean admin);
}
