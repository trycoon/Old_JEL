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
public class User implements IUser
{
    private int mID;
    private String mUsername;
    private String mFirstname;
    private String mLastname;
    private String mDescription;
    private String mPassword;
    private Date mCreateTime;
    private Date mLastLoggedInTime;
    private boolean mIsDisabled;
    
   
    public int getID() {
        return mID;
    }

    public void setID(int id) {
        mID = id;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String name) {
        mUsername = name;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String name) {
        mFirstname = name;
    }

    public String getLastname() {
        return mLastname;
    }

    public void setLastname(String name) {
        mLastname = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public Date getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(Date time) {
        mCreateTime = time;
    }

    public Date getLastLoggedInTime() {
        return mLastLoggedInTime;
    }

    public void setLastLoggedInTime(Date time) {
        mLastLoggedInTime = time;
    }

    public boolean getIsDisabled() {
        return mIsDisabled;
    }

    public void setIsDisabled(boolean state) {
        mIsDisabled = state;
    }

}
