/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.security;

/**
 *
 * @author trycoon
 */
public class LoginSession implements ISession
{
    private int mUserID;
    private String mToken;


    public LoginSession()
    {
        mUserID = -1;
        mToken = null;
    }

    public int getUserID()
    {
        return mUserID;
    }

    public void setUserID(int userID)
    {
        mUserID = userID;
    }

    public String getToken()
    {
        return mToken;
    }

    public void setToken(String token)
    {
        mToken = token;
    }
}
