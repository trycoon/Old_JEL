/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.device;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author trycoon
 */
public class Actuator extends Device implements Serializable {

    public Actuator(short id, short siteID, Map<String, String> settings) {
        super(id, siteID, settings);
    }

    public Actuator() {
        super((short)0, (short)0, null);
    }

    public String getHardwareID() {
        return this.getSetting(SETTINGSTYPE.HardwareID);
    }

    public void setHardwareID(String id) {
        this.setSetting(SETTINGSTYPE.HardwareID, id);
    }
}
