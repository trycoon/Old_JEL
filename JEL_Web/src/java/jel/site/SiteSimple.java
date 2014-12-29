/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

/**
 *
 * @author trycoon
 */
public class SiteSimple implements ISiteSimple
{
    private int mID;
    private String mName;
    private boolean mIsAdmin;
    
    
    public int getID() {
        return mID;
    }

    public void setID(int id) {
        mID = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean getIsAdmin() {
        return mIsAdmin;
    }

    public void setIsAdmin(boolean admin) {
        mIsAdmin = admin;
    }
}
