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
public class Sensor extends Device implements Serializable {

    public Sensor(short id, short siteID, Map<String, String> settings)
    {
        super(id, siteID, settings);

        this.setSetting(Device.SETTINGSTYPE.SampleDelay, "10000");
        this.setSetting(Device.SETTINGSTYPE.SamplesPerEntry, "1");
        this.setSetting(Device.SETTINGSTYPE.InputAdjustment, "i");
    }

    public Sensor() {
        super((short)0, (short)0, null);
    }


    public boolean isInvertInput() {
        return this.getSettingAsBoolean(SETTINGSTYPE.InvertInput);
    }


    public void setInvertInput(boolean invertInput) {
        this.setSetting(SETTINGSTYPE.InvertInput, String.valueOf(invertInput));
    }


    public int getSampleDelay() {
        return this.getSettingAsInt(SETTINGSTYPE.SampleDelay);
    }


    public void setSampleDelay(int sampleDelay) {
        if (sampleDelay < 0) sampleDelay = 0;
        this.setSetting(SETTINGSTYPE.SampleDelay, String.valueOf(sampleDelay));
    }


    public int getSamplesPerEntry() {
        return this.getSettingAsInt(SETTINGSTYPE.SamplesPerEntry);
    }


    public void setSamplesPerEntry(int samplesPerEntry) {
        if (samplesPerEntry < 1) samplesPerEntry = 1;
        this.setSetting(SETTINGSTYPE.SamplesPerEntry, String.valueOf(samplesPerEntry));
    }


    public String getInputAdjustment() {
        return this.getSetting(SETTINGSTYPE.InputAdjustment);
    }


    public void setInputAdjustment(String inputAdjustment) {
        if (inputAdjustment == null || inputAdjustment.length() == 0)
            inputAdjustment = "i";
        this.setSetting(SETTINGSTYPE.InputAdjustment, String.valueOf(inputAdjustment));
    }


    public String getHardwareID() {
        return this.getSetting(SETTINGSTYPE.HardwareID);
    }

    public void setHardwareID(String id) {
        this.setSetting(SETTINGSTYPE.HardwareID, id);
    }
}
