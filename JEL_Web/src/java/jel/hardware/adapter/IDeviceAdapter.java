/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.adapter;

import jel.hardware.device.Device;
import java.util.List;

/**
 *
 * @author trycoon
 */
public interface IDeviceAdapter
{

    public void init() throws Exception;


    public void shutdown();


    public boolean isInitialized();


    public boolean isPressent();


    public String getName();


    public void setName(String name);


    public String[] getPossiblePorts();

    
    public String getSelectedPort();


    public void setSelectedPort(String port);


    public String getVersion();


    public List<Device> scanForDevices(); //FIXME: Fix returntype.
}
