/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware;

import jel.storage.IStorageManager;
import org.apache.log4j.Logger;

/**
 *
 * @author trycoon
 */
public final class Sampler
{

    private static Logger mLogger = Logger.getLogger(Sampler.class);
    private IStorageManager mStorageManager;
    private AdapterManager mAdapterManager;
    private Thread mSamplerThread;
    private boolean mRunSampler;


    public Sampler(IStorageManager storageManager, AdapterManager adapterManager) {
        this.mStorageManager = storageManager;
        this.mAdapterManager = adapterManager;
        mRunSampler = true;
    }


    public synchronized void start() {
        // Only start once.
        if (mSamplerThread == null) {
            mSamplerThread = new Thread(new Runnable()
            {

                public void run() {
                    mLogger.info("Sampler started.");

                    while (mRunSampler) {



                        System.out.println("sampling.");

                        try {
                            mSamplerThread.join(5000);
                        } catch (InterruptedException exception) { /* Do nothing */ }
                        



                    }

                    mLogger.info("Sampler stopped.");
                }

            });
            mSamplerThread.start();
        }
    }


    public synchronized void stop() {
        mRunSampler = false;
        try {
            mSamplerThread.join(3000);
        } catch (InterruptedException exception) { /* Do nothing */ }
        mSamplerThread = null;
    }

}
