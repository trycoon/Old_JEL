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
 * Copyright (c) 2009-dec-29 by Liquidbytes. All rights reserved.
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public class Linux32DriverInstaller implements IDriverInstaller
{
    private final static String LINUX_DRIVERNAME = "librxtxSerial.so";
    private final static String LINUX_I686_DRIVER_SOURCEPATH = "onewire_drivers/linux-i686";
    private final static String LINUX_I686_DRIVER_DESTINATIONPATH = ServerInformationManager.getJavaBootLibraryPath();  //e.g. /usr/lib/jvm/java-6-sun-1.6.0.00/jre/lib/i386
    private static Logger mLogger = Logger.getLogger(Linux32DriverInstaller.class);


    public void installDrivers() {
        String driverInputFile = LINUX_I686_DRIVER_SOURCEPATH + "/" + LINUX_DRIVERNAME;
        String driverOutputFile = LINUX_I686_DRIVER_DESTINATIONPATH + ServerInformationManager.getFileSeparator() + LINUX_DRIVERNAME;

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
