/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.security;


/**
 *
 * @author trycoon
 */
public interface ISession 
{
    public int getUserID();
    
    public void setUserID(int userID);

    public String getToken();
    
    public void setToken(String handle);
}
