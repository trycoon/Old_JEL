/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.hardware.adapter.event;

import java.util.Date;
import jel.hardware.adapter.IDeviceAdapter;
import jel.utils.JelEvent;

/**
 *
 * @author trycoon
 */
public class AdapterEvent extends JelEvent {

    public AdapterEvent(Object source, IDeviceAdapter target, Date time)
    {
        super(source, target, time);
    }


    public AdapterEvent(Object source, IDeviceAdapter target)
    {
        super(source, target);
    }
}
