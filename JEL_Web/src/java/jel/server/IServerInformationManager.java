/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.server;

import java.util.List;
import jel.security.ISession;

/**
 *
 * @author trycoon
 */
public interface IServerInformationManager 
{
    public String getServerVersion();
    public double getDiskFull();
    public List<String> getServerInformation(ISession session);
}
