/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.device;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author trycoon
 */
public abstract class Device implements Serializable {

    protected static enum SETTINGSTYPE { HardwareID, InputAdjustment, InvertInput, SampleDelay, SamplesPerEntry, XPos, YPos };  // List in alphabetical order.
    private short mID;
    private short mSiteID;
    private String mName;
    private String mDescription;
    private DeviceType mDeviceType;
    private Map<String, String> mSettings;


    public Device(short id, short siteID, Map<String, String> settings) {
        mID = id;
        mSiteID = siteID;

        if (settings == null) {
            mSettings = new HashMap<String, String>(10);
        } else {
            mSettings = settings;
        }
    }

    public Device() {
        this((short)0, (short)0, null);
    }



    public short getID() {
        return mID;
    }

    public void setID(short id) {
        mID = id;
    }

    public short getSiteID() {
        return mSiteID;
    }

    public void setSiteID(short siteID) {
        mSiteID = siteID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.mDeviceType = deviceType;
    }

    public short getXpos() {
        return (short) this.getSettingAsInt(SETTINGSTYPE.XPos);
    }

    public void setXpos(short xPosition) {
        this.setSetting(SETTINGSTYPE.XPos, String.valueOf(xPosition));
    }

    public short getYpos() {
        return (short) this.getSettingAsInt(SETTINGSTYPE.YPos);
    }

    public void setYpos(short yPosition) {
        this.setSetting(SETTINGSTYPE.YPos, String.valueOf(yPosition));
    }

    protected void setSetting(String key, String value) {
        if (key != null && key.length() > 0) {
            mSettings.put(key, value);
        }
    }

    protected void setSetting(SETTINGSTYPE type, String value) {
        if (type != null) {
            mSettings.put(type.toString(), value);
        }
    }

    protected String getSetting(String key) {
        if (key == null || key.length() == 0) {
            return null;
        } else {
            return mSettings.get(key);
        }
    }

    protected String getSetting(SETTINGSTYPE type) {
        if (type == null) {
            return null;
        } else {
            return mSettings.get(type.toString());
        }
    }

    protected int getSettingAsInt(String key) {
        int value = 0;
        if (key != null && key.length() > 0) {
            try {
                String valueString = mSettings.get(key);

                if (valueString != null) {
                    value = Integer.parseInt(valueString);
                }
            } catch (Throwable exception) {
            }
        }
        return value;
    }

    protected int getSettingAsInt(SETTINGSTYPE type) {
        int value = 0;
        if (type != null) {
            try {
                String valueString = mSettings.get(type.toString());

                if (valueString != null) {
                    value = Integer.parseInt(valueString);
                }
            } catch (Throwable exception) {
            }
        }
        return value;
    }

    protected boolean getSettingAsBoolean(String key) {
        if (key == null || key.length() == 0) {
            return false;
        } else {
            String value = mSettings.get(key);
            if (value != null && value.equals("1") || value.equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean getSettingAsBoolean(SETTINGSTYPE type) {
        if (type == null) {
            return false;
        } else {
            String value = mSettings.get(type.toString());
            if (value != null && value.equals("1") || value.equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
