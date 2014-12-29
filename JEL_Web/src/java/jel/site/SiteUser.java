/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;


/**
 *
 * @author trycoon
 */
public class SiteUser
{
    private int mUserID;
    private int mSiteID;
    private String mUsername;
    private int mPermissionType;


    public int getUserID() {
        return mUserID;
    }

    public void setUserID(int userID) {
        this.mUserID = userID;
    }

    public int getSiteID() {
        return mSiteID;
    }

    public void setSiteID(int siteID) {
        this.mSiteID = siteID;
    }

    public void setUsername(String username)
    {
        this.mUsername = username;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public int getPermissionType() {
        return mPermissionType;
    }

    public void setPermissionType(int permissionType) {
        this.mPermissionType = permissionType;
    }

   
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SiteUser other = (SiteUser) obj;
        if (this.mUserID != other.mUserID) {
            return false;
        }
        if (this.mSiteID != other.mSiteID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.mUserID;
        hash = 41 * hash + this.mSiteID;
        return hash;
    }

}
