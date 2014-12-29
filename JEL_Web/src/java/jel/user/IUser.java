/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.user;

import java.util.Date;

/**
 *
 * @author trycoon
 */
public interface IUser 
{
    public int getID();
    public void setID(int id);
    
    public String getUsername();
    public void setUsername(String name);
    
    public String getFirstname();
    public void setFirstname(String name);
    
    public String getLastname();
    public void setLastname(String name);
    
    public String getDescription();
    public void setDescription(String description);
    
    public String getPassword();
    public void setPassword(String password);
    
    public Date getCreateTime();
    public void setCreateTime(Date time);
    
    public Date getLastLoggedInTime();
    public void setLastLoggedInTime(Date time);
    
    public boolean getIsDisabled();
    public void setIsDisabled(boolean state);
}
