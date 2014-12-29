/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.driverinstaller;

import java.io.FileNotFoundException;
import java.io.IOException;
import jel.server.ServerInformationManager;
import jel.utils.FileUtilities;
import org.apache.log4j.Logger;

/**
 * Copyright (c) 2009-dec-30 by Liquidbytes. All rights reserved.
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public class WindowsDriverInstaller implements IDriverInstaller
{
    private final static String WINDOWS_DRIVERNAME = "rxtxSerial.dll";
    private final static String WINDOWS_DRIVER_SOURCEPATH = "onewire_drivers/windows";
    private final static String WINDOWS_DRIVER_DESTINATIONPATH = ServerInformationManager.getJavaBootLibraryPath(); //e.g. \\jre\bin
    private static Logger mLogger = Logger.getLogger(WindowsDriverInstaller.class);


    public void installDrivers() {
        String driverInputFile = WINDOWS_DRIVER_SOURCEPATH + "/" + WINDOWS_DRIVERNAME;
        String driverOutputFile = WINDOWS_DRIVER_DESTINATIONPATH + ServerInformationManager.getFileSeparator() + WINDOWS_DRIVERNAME;

        try {
            mLogger.info("Installing/upgrading hardwaredriver \"" + driverOutputFile + "\".");
            FileUtilities.copyFile(this.getClass().getClassLoader().getResourceAsStream(driverInputFile), driverOutputFile);
        } catch (FileNotFoundException exception) {
            mLogger.error("Could not read or write driverfile", exception);
        } catch (IOException exception) {
            mLogger.error("Failed to install hardware driver", exception);
        }
    }
}
