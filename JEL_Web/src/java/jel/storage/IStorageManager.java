/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.storage;

import java.util.Date;
import java.util.List;
import jel.eventlog.LogEntry;
import jel.security.ILoginInformation;
import jel.security.ISession;
import jel.site.ISite;
import jel.site.ISitePersistantContainer;
import jel.site.ISiteSimple;
import jel.site.SiteUser;
import jel.storage.StorageManager.SortOrder;
import jel.user.IUser;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author trycoon
 */
public interface IStorageManager {

    @Transactional
    void addEventToLog(ISession session, String eventlog, int userID, String message);

    @Transactional(readOnly = true)
    List<LogEntry> getEventLog(ISession session, String eventLog, int userID, Date start, Date end);

    @Transactional(readOnly = true)
    List<LogEntry> getEventLog(ISession session, String eventLog, int userID, int number, SortOrder order);

    @Transactional
    ISite addSite(final ISession session, ISitePersistantContainer siteContainer);

    @Transactional
    void addSiteUser(ISession session, int userID, int siteID, int permissionType);

    @Transactional
    IUser addUser(ISession session, IUser user);

    @Transactional(readOnly = true)
    ISite getSite(final ISession session, int id, boolean onlyPublic);

    @Transactional(readOnly = true)
    List<ISiteSimple> getSiteList(final ISession session, boolean onlyPublic);

    @Transactional(readOnly = true)
    SiteUser getSiteUser(ISession session, int userID, int siteID);

    @Transactional(readOnly = true)
    List<SiteUser> getSiteUsers(ISession session, int siteID);

    @Transactional(readOnly = true)
    List<ISite> getSites(final ISession session);

    @Transactional(readOnly = true)
    IUser getUser(ISession session, int id);

    @Transactional(readOnly = true)
    IUser getUserByUsername(ISession session, String username);

    @Transactional(readOnly = true)
    List<IUser> getUsers(ISession session);

    /**
     * Returns whether user has adminrights of specified site or not
     * @param session
     * @param siteID
     * @return
     */
    @Transactional(readOnly = true)
    boolean isAdminOfSite(ISession session, int siteID);

    /**
     * This will return true if user is admin of one or more sites.
     * Note that this don't mean that the user has the right to administrate all sites,
     * just those which he/she has been asigned to.
     * @param session
     * @return
     */
    @Transactional(readOnly = true)
    boolean isAdminUser(ISession session);

    @Transactional
    ILoginInformation login(String username, String password);

    @Transactional
    void logout(ISession session);

    @Transactional
    void removeSite(ISession session, int id);

    @Transactional
    void removeSiteUser(ISession session, int userID, int siteID);

    @Transactional
    void removeUser(ISession session, int id);

    @Transactional
    void setSiteUsers(ISession session, int siteID, List<SiteUser> siteUsers);

    @Transactional
    ISite updateSite(ISession session, ISitePersistantContainer siteContainer);

    @Transactional
    void updateSiteImage(int siteID, String url);

    @Transactional
    void updateSiteUser(ISession session, int userID, int siteID, int permissionType);

    @Transactional
    IUser updateUser(ISession session, IUser user);

    @Transactional(readOnly = true)
    boolean verifySession(ISession session);
}
