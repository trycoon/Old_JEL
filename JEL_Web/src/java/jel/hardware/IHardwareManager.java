/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.hardware;

import java.util.List;
import jel.hardware.adapter.AdapterDescription;
import jel.security.ISession;

/**
 *
 * @author trycoon
 */
public interface IHardwareManager {


    AdapterDescription addUsedAdapter(ISession session, String name, String selectedPort);


    List<AdapterDescription> getAvailableAdapters(ISession session);


    List<AdapterDescription> getUsedAdapters(ISession session);


    void removeUsedAdapter(ISession session, String name, String selectedPort);

}
