/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware.adapter;

import jel.hardware.device.Device;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import org.apache.log4j.Logger;

/**
 *
 * @author trycoon
 */
public final class NetAdapter implements IDeviceAdapter
{
    private static Logger mLogger = Logger.getLogger(NetAdapter.class);
    private String mName;
    private String mPort;
    private DSPortAdapter mAdapter;
    private boolean mInitialized;
    private Thread mSamplerThread;
    private boolean mRunSampler;


    public NetAdapter() {
        setName("NetAdapter");
    }


    public String getName() {
        return new String(mName);
    }


    public void setName( String name ) {
        mName = name;
    }


    public String[] getPossiblePorts() {
        List<String> possiblePorts = new ArrayList<String>();

        try {
            for (Enumeration<DSPortAdapter> adapter_enum = com.dalsemi.onewire.OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements();) {
                DSPortAdapter adapter = adapter_enum.nextElement();

                if (adapter.getAdapterName().equalsIgnoreCase(mName)) {
                    for (Enumeration<String> port_enum = adapter.getPortNames(); port_enum.hasMoreElements();) {
                        possiblePorts.add(port_enum.nextElement());
                    }
                }
            }
        } catch (Throwable exception) {
            mLogger.warn("Failed to get possible ports from netadapter \"" + mName + "\".", exception);
        }

        return possiblePorts.toArray(new String[possiblePorts.size()]);
    }


    public String getSelectedPort() {
        return new String(mPort);
    }


    public void setSelectedPort( String port ) {
        mPort = port;
    }


    public boolean isPressent() {
        boolean pressent = false;

        try {
            if (mAdapter != null) {
                pressent = mAdapter.adapterDetected();
            }
        } catch (Throwable exception) {
            mLogger.info("Failed to detect if adapter(name=\"" + mName + "\" was pressent.", exception);
        }

        return pressent;
    }


    public List<Device> scanForDevices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public String getVersion() {
        return com.dalsemi.onewire.OneWireAccessProvider.getVersion();
    }


    public synchronized void init() throws OneWireException {
        if (!mInitialized) {
            mAdapter = com.dalsemi.onewire.OneWireAccessProvider.getAdapter(mName, mPort);
            mRunSampler = true;

            mSamplerThread = new Thread(new Runnable()
            {
                public void run() {
                    try {
                        mLogger.debug("Sampler for adapter \"" + mName + "\" started.");

                        while (mRunSampler) {
                        }

                        mLogger.debug("Sampler for adapter \"" + mName + "\" stopped.");
                    } finally {
                        if (mAdapter != null) {
                            mAdapter.endExclusive();    // Just to be sure, incase we forgot or an error occur.
                        }
                    }
                }
            });
            mSamplerThread.start();
            mInitialized = true;
        }
    }


    public synchronized void shutdown() {
        if (mInitialized) {
            mRunSampler = false;
            try {
                mSamplerThread.join(3000);
            } catch (InterruptedException exception) { /* Do nothing */ }
            mSamplerThread = null;

            mAdapter = null;
            mInitialized = false;
        }
    }


    public boolean isInitialized() {
        return new Boolean(mInitialized);
    }


    @Override
    public String toString() {
        return getName() + "-adapter";
    }
}


