/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware;

import jel.hardware.driverinstaller.Linux64DriverInstaller;
import jel.hardware.driverinstaller.MacOsDriverInstaller;
import jel.hardware.driverinstaller.WindowsDriverInstaller;
import jel.hardware.driverinstaller.Linux32DriverInstaller;
import java.io.IOException;
import java.util.List;
import jel.hardware.adapter.AdapterDescription;
import jel.security.ISession;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.server.ServerInformationManager;
import org.apache.log4j.Logger;

/**
 *
 * @author trycoon
 */
public final class HardwareManager implements IHardwareManager
{
    private static Logger mLogger = Logger.getLogger(HardwareManager.class);
    private jel.security.ISecurityManager mSecurityManager;
    private jel.storage.IStorageManager mStorageManager;
    private jel.eventlog.IEventLogManager mEventlogManager;
    private AdapterManager mAdapterManager;
    private Sampler mSampler;


    public void setSecurityManager( jel.security.SecurityManager securityManager ) {
        this.mSecurityManager = securityManager;
    }


    public void setStorageManager( jel.storage.IStorageManager storageManager ) {
        this.mStorageManager = storageManager;
    }


   public void setEventlogManager( jel.eventlog.IEventLogManager eventlogManager ) {
        this.mEventlogManager = eventlogManager;
   }
        

    public void init() throws IOException {

        installDrivers();

        mAdapterManager = new AdapterManager();
        mAdapterManager.installAdapters();
        mAdapterManager.loadAdapters();
        mAdapterManager.initUsedAdapters();
    }


    public List<AdapterDescription> getAvailableAdapters( ISession session ) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        return mAdapterManager.getAvailableAdapters();
    }


    public List<AdapterDescription> getUsedAdapters( ISession session ) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        return mAdapterManager.getUsedAdapters();
    }


    public AdapterDescription addUsedAdapter( ISession session, String name, String selectedPort ) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        if (session.getUserID() != 1) {
            throw new JelException("Only master-admin is allowed to modify adapters.", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        return mAdapterManager.addUsedAdapter(name, selectedPort);
    }


    public void removeUsedAdapter( ISession session, String name, String selectedPort ) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        if (session.getUserID() != 1) {
            throw new JelException("Only master-admin is allowed to modify adapters.", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        mAdapterManager.removeUsedAdapter(name, selectedPort);
    }


    private void installDrivers() {
        if (ServerInformationManager.isLinux()) {
            new Linux32DriverInstaller().installDrivers();
        } else if (ServerInformationManager.isLinux64()) {
            new Linux64DriverInstaller().installDrivers();
        } else if (ServerInformationManager.isWindows()) {
            new WindowsDriverInstaller().installDrivers();
        } else if (ServerInformationManager.isMacOSX()) {
            new MacOsDriverInstaller().installDrivers();
        } else {
            throw new JelException("Your operatingsystem is not supported yet!", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
        }
    }


    public void shutdown() {
        if (mSampler != null) {
            mSampler.stop();
        }
        if (mAdapterManager != null) {
            mAdapterManager.shutdownUsedAdapters();
        }
    }
}
