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
public class Linux64DriverInstaller implements IDriverInstaller
{
    private final static String LINUX_DRIVERNAME = "librxtxSerial.so";
    private final static String LINUX_X64_DRIVER_SOURCEPATH = "onewire_drivers/linux-x64";
    private final static String LINUX_X64_DRIVER_DESTINATIONPATH = ServerInformationManager.getJavaBootLibraryPath();
    private static Logger mLogger = Logger.getLogger(Linux64DriverInstaller.class);


    public void installDrivers() {
        String driverInputFile = LINUX_X64_DRIVER_SOURCEPATH + "/" + LINUX_DRIVERNAME;
        String driverOutputFile = LINUX_X64_DRIVER_DESTINATIONPATH + ServerInformationManager.getFileSeparator() + LINUX_DRIVERNAME;

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
