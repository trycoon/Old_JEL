/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.facade;

import java.util.List;
import jel.eventlog.EventlogManager.LogType;
import jel.eventlog.LogEntry;
import jel.hardware.adapter.AdapterDescription;
import jel.security.*;
import jel.server.*;
import jel.site.ISite;
import jel.site.ISitePersistantContainer;
import jel.site.ISiteSimple;
import jel.site.SiteUser;
import jel.storage.StorageManager.SortOrder;
import jel.user.IUser;

/**
 *
 * @author trycoon
 */
public class ServerFacade implements ISecurityManager, IServerInformationManager {

    private jel.storage.IStorageManager mStorageManager;
    private jel.eventlog.IEventLogManager mEventlogManager;
    private jel.hardware.IHardwareManager mHardwareManager;
    private jel.security.SecurityManager mSecurityManager;
    private jel.site.SiteManager mSiteManager;
    private jel.user.UserManager mUserManager;

    public void setStorageManager(jel.storage.IStorageManager storageManager) {
        this.mStorageManager = storageManager;
    }

    public void setEventlogManager(jel.eventlog.IEventLogManager eventlogManager) {
        this.mEventlogManager = eventlogManager;
    }

    public void setHardwareManager(jel.hardware.IHardwareManager hardwareManager) {
        this.mHardwareManager = hardwareManager;
    }

    public void setSecurityManager(jel.security.SecurityManager securityManager) {
        this.mSecurityManager = securityManager;
    }

    public void setSiteManager(jel.site.SiteManager siteManager) {
        this.mSiteManager = siteManager;
    }

    public void setUserManager(jel.user.UserManager userManager) {
        this.mUserManager = userManager;
    }

    ///////////////////////////////
    // Interface implementations //
    ///////////////////////////////
    public ILoginInformation login(String username, String password) {
        return mSecurityManager.login(username, password);
    }

    public void logout(ISession session) {
        mSecurityManager.logout(session);
    }

    public boolean verifySession(ISession session) {
        return mSecurityManager.verifySession(session);
    }

    public String getServerVersion() {
        return new jel.server.ServerInformationManager().getServerVersion();
    }

    public double getDiskFull() {
        return new jel.server.ServerInformationManager().getDiskFull();
    }

    public List<String> getServerInformation(ISession session) {
        return new jel.server.ServerInformationManager().getServerInformation(session);
    }

    public List<LogEntry> getEventLog(ISession session, LogType logType, int userID, int number, SortOrder order) {
       return mEventlogManager.getEventLog(session, logType, userID, number, order);
    }

    // begin Site
    public ISite addSite(ISession session, ISitePersistantContainer siteContainer) {
        return mSiteManager.addSite(session, siteContainer);
    }

    public ISite getSite(ISession session, int id) {
        return mSiteManager.getSite(session, id);
    }

    public List<ISiteSimple> getSiteList(ISession session) {
        return mSiteManager.getSiteList(session);
    }

    public List<ISite> getSites(ISession session) {
        return mSiteManager.getSites(session);
    }

    public void removeSite(ISession session, int id) {
        mSiteManager.removeSite(session, id);
    }

    public ISite updateSite(ISession session, ISitePersistantContainer siteContainer) {
        return mSiteManager.updateSite(session, siteContainer);
    }

    public void addSiteUser(ISession session, int userID, int siteID, int permissionType) {
        mSiteManager.addSiteUser(session, userID, siteID, permissionType);
    }

    public void removeSiteUser(ISession session, int userID, int siteID) {
        mSiteManager.removeSiteUser(session, userID, siteID);
    }

    public void updateSiteUser(ISession session, int userID, int siteID, int permissionType) {
        mSiteManager.updateSiteUser(session, userID, siteID, permissionType);
    }

    public List<SiteUser> getSiteUsers(ISession session, int siteID) {
        return mSiteManager.getSiteUsers(session, siteID);
    }

    public void setSiteUsers(ISession session, int siteID, List<SiteUser> siteUsers) {
        mSiteManager.setSiteUsers(session, siteID, siteUsers);
    }
    // end Site

    // begin User
    public IUser addUser(ISession session, IUser user) {
        return mUserManager.addUser(session, user);
    }

    public IUser getUser(ISession session, int id) {
        return mUserManager.getUser(session, id);
    }

    public List<IUser> getUsers(ISession session) {
        return mUserManager.getUsers(session);
    }

    public void removeUser(ISession session, int id) {
        mUserManager.removeUser(session, id);
    }

    public IUser updateUser(ISession session, IUser user) {
        return mUserManager.updateUser(session, user);
    }
    // end User

    // begin Adapter
    public List<AdapterDescription> getAvailableAdapters(ISession session) {
        return mHardwareManager.getAvailableAdapters(session);
    }

    public List<AdapterDescription> getUsedAdapters(ISession session) {
        return mHardwareManager.getUsedAdapters(session);
    }

    public AdapterDescription addUsedAdapter(ISession session, String name, String selectedPort) {
        return mHardwareManager.addUsedAdapter(session, name, selectedPort);
    }

    public void removeUsedAdapter(ISession session, String name, String selectedPort) {
        mHardwareManager.removeUsedAdapter(session, name, selectedPort);
    }
    // end Adapter

    // begin Device
    
    // end Device
}
