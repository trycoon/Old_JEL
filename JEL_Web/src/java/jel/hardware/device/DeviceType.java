/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.device;

import java.io.Serializable;

/**
 *
 * @author trycoon
 */
public class DeviceType implements Serializable
{
    private int mId;
    private String mName;

    public DeviceType(int id, String name) {
        mId = id;
        mName = name;
    }

    public DeviceType()
    {
        this (0, "");
    }

    /**
     * @return the mId
     */
    public int getmId() {
        return mId;
    }

    /**
     * @param mId the mId to set
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * @return the mName
     */
    public String getName() {
        return mName;
    }

    /**
     * @param mName the mName to set
     */
    public void setName(String name) {
        this.mName = name;
    }
}
