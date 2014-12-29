/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.storage;

import java.util.Date;

/**
 *
 * @author trycoon
 */
public class DeviceSample {

    private Date mTime;
    private Double mValue;

    public DeviceSample() {
        mTime = null;
        mValue = 0d;
    }

    public DeviceSample(Date time, Double value) {
        mTime = time;
        mValue = value;
    }

    public Date getTime() {
        return mTime;
    }

    public void SetTime(Date time) {
        mTime = time;
    }

    public Double getValue() {
        return mValue;
    }

    public void setValue(Double value) {
        mValue = value;
    }
}
