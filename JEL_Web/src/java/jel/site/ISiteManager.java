/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

import java.util.List;
import jel.security.ISession;

/**
 *
 * @author trycoon
 */
public interface ISiteManager 
{

    ISite addSite(ISession session, ISitePersistantContainer siteContainer);

    ISite getSite(ISession session, int id);

    List<ISiteSimple> getSiteList(ISession session);

    List<ISite> getSites(ISession session);

    void removeSite(ISession session, int id);

    ISite updateSite(ISession session, ISitePersistantContainer siteContainer);


    void addSiteUser(ISession session, int userID, int siteID, int permissionType);

    void removeSiteUser(ISession session, int userID, int siteID);

    void updateSiteUser(ISession session, int userID, int siteID, int permissionType);
    
    List<SiteUser> getSiteUsers(ISession session, int siteID);

    void setSiteUsers(ISession session, int siteID, List<SiteUser> siteUsers);
}
