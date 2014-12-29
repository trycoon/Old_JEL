/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.adapter;

import java.io.Serializable;

/**
 *
 * @author trycoon
 */
public final class AdapterDescription implements Serializable
{
    private boolean mPressent;
    private String mName;
    private String mSelectedPort;
    private String mVersion;
    private String[] mPossiblePorts;

    
    public AdapterDescription()
    {
        mPossiblePorts = new String[0];
    }


    public boolean getIsPressent() {
        return mPressent;
    }


    public void setIsPressent(boolean pressent) {
        mPressent = pressent;
    }


    public String getName() {
        return mName;
    }


    public void setName(String name) {
        mName = name;
    }


    public String getSelectedPort() {
        return mSelectedPort;
    }


    public void setSelectedPort(String port) {
        mSelectedPort = port;
    }


    public String getVersion() {
        return mVersion;
    }


    public void setVersion(String version) {
        mVersion = version;
    }


    public String[] getPossiblePorts() {
        return mPossiblePorts;
    }


    public void setPossiblePorts(String[] ports) {
        this.mPossiblePorts = ports;
    }

}
