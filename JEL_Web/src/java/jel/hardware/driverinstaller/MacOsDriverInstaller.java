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
public class MacOsDriverInstaller implements IDriverInstaller
{
    private final static String MAC_OSX_DRIVERNAME = "librxtxSerial.jnilib";
    private final static String MAC_OSX_DRIVER_SOURCEPATH = "onewire_drivers/mac-osx";
    private final static String MAC_OSX_DRIVER_DESTINATIONPATH = ServerInformationManager.getJavaBootLibraryPath(); //e.g. /System/Library/Frameworks/JavaVM.framework/Version/1.5.0/Libraries
    private static Logger mLogger = Logger.getLogger(MacOsDriverInstaller.class);


    public void installDrivers() {
        String driverInputFile = MAC_OSX_DRIVER_SOURCEPATH + "/" + MAC_OSX_DRIVERNAME;
        String driverOutputFile = MAC_OSX_DRIVER_DESTINATIONPATH + ServerInformationManager.getFileSeparator() + MAC_OSX_DRIVERNAME;

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
