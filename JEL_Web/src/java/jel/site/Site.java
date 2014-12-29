/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.site;

import java.util.Date;

/**
 *
 * @author trycoon
 */
public class Site implements ISite {

    private int mID;
    private String mName;
    private String mDescription;
    private Date mCreateTime;
    private int mWidth;
    private int mHeight;
    private String mBackgroundImageUrl;
    private String mBackgroundColor;
    private boolean mRepeatX;
    private boolean mRepeatY;
    private boolean mAllowAnonymousUsers;
    private String mLongitude;
    private String mLatitude;
    private boolean mIsAdmin;

    public Site() {
        mID = -1;
        mWidth = 1024;
        mHeight = 768;
        mBackgroundColor = "8821927"; //#869ca7
    }

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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String name) {
        mDescription = name;
    }

    public Date getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(Date time) {
        mCreateTime = time;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public boolean getImageRepeatX() {
        return mRepeatX;
    }

    public void setImageRepeatX(boolean repeat) {
        mRepeatX = repeat;
    }

    public boolean getImageRepeatY() {
        return mRepeatY;
    }

    public void setImageRepeatY(boolean repeat) {
        mRepeatY = repeat;
    }

    public String getBackgroundImageUrl() {
        return mBackgroundImageUrl;
    }

    public void setBackgroundImageUrl(String url) {
        mBackgroundImageUrl = url;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(String color) {
        mBackgroundColor = color;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public boolean getAllowAnonymousUsers() {
        return mAllowAnonymousUsers;
    }

    public void setAllowAnonymousUsers(boolean allow) {
        mAllowAnonymousUsers = allow;
    }

    public boolean getIsAdmin() {
        return mIsAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        mIsAdmin = isAdmin;
    }
}
