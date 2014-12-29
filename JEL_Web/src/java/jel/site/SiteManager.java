/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.site;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import javax.imageio.ImageIO;
import jel.security.ISession;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.server.ServerInformationManager;
import jel.site.event.ISiteAddedListener;
import jel.site.event.ISiteRemovedListener;
import jel.site.event.ISiteUpdatedListener;
import jel.site.event.SiteEvent;

/**
 *
 * @author trycoon
 */
public final class SiteManager implements ISiteManager
{

    private static Logger mLogger = Logger.getLogger(SiteManager.class);
    private jel.storage.IStorageManager mStorageManager;
    private jel.security.SecurityManager mSecurityManager;
    private List<ISiteAddedListener> mSiteAddedListeners = new ArrayList<ISiteAddedListener>();
    private List<ISiteRemovedListener> mSiteRemovedListeners = new ArrayList<ISiteRemovedListener>();
    private List<ISiteUpdatedListener> mSiteUpdatedListeners = new ArrayList<ISiteUpdatedListener>();


    public void setStorageManager(jel.storage.IStorageManager storageManager) {
        this.mStorageManager = storageManager;
    }


    public void setSecurityManager(jel.security.SecurityManager securityManager) {
        this.mSecurityManager = securityManager;
    }


    /**
     * Add a new site.
     * Only a logged in master-admin is allowed to do this.
     *
     * @param session
     * @param siteContainer
     * @return
     */
    public ISite addSite(ISession session, ISitePersistantContainer siteContainer) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        if (session.getUserID() != 1) {
            throw new JelException("Only master-admin is allowed to add sites", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        if (siteContainer == null || siteContainer.getSite() == null) {
            throw new JelException("Siteobject not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        ISite newSite = siteContainer.getSite();

        if (newSite.getName() == null || newSite.getName().trim().length() == 0) {
            throw new JelException("Sitename not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getName().trim().length() > 50) {
            throw new JelException("Sitename too long(max 50 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getDescription() != null) {
            newSite.setDescription(newSite.getDescription().trim());
            if (newSite.getDescription().length() > 512) {
                throw new JelException("Description too long(max 512 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (newSite.getBackgroundColor() != null) {
            newSite.setBackgroundColor(newSite.getBackgroundColor().trim());
            if (newSite.getBackgroundColor().length() > 20) {
                throw new JelException("Background color value long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (newSite.getWidth() < 640) {
            throw new JelException("Background width can not be smaller than 640 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getWidth() > 32000) {
            throw new JelException("Background width can not be larger than 32000 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getHeight() < 480) {
            throw new JelException("Background height can not be smaller than 480 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getHeight() > 32000) {
            throw new JelException("Background height can not be larger than 32000 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        newSite.setID(-1);
        newSite.setIsAdmin(false);
        newSite.setCreateTime(new Date());

        newSite.setName(newSite.getName().trim());

        String fileSuffix = null;
        BufferedImage image = null;

        // If not empty then we have a new image to save.
        if (siteContainer.getImageName() != null && siteContainer.getImageName().trim().length() > 0) {
            if (siteContainer.getRawImage().length == 0) {
                throw new JelException("Imagename but no imagedata was provided to server", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
            }

            String[] tmpNames = siteContainer.getImageName().split("\\.");
            if (tmpNames.length < 2) {
                throw new JelException("Imagename(\"" + siteContainer.getImageName() + "\") does not contain a filesuffix, eg. \".jpg\"", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
            }

            fileSuffix = tmpNames[tmpNames.length - 1].toLowerCase();  // Get last one, e.g. "gif" from "myfile.is.great.gif"

            String[] supportedFormats = ImageIO.getWriterFormatNames();

            if (mLogger.isDebugEnabled()) {
                String debugLine = "Supported imageformats by server(";
                for (String line : supportedFormats) {
                    debugLine += " " + line;
                }

                mLogger.debug(debugLine += ")");
            }


            if (!Arrays.asList(supportedFormats).contains(fileSuffix)) {
                throw new JelException("Imageformat \"" + fileSuffix + "\" not supported by server, please convert image to another format", ExceptionReason.SERVER_ERROR, ExceptionType.WARN);
            }

            try {
                mLogger.debug("Loading image to byte-array");

                ByteArrayInputStream inputStream = new ByteArrayInputStream(siteContainer.getRawImage());
                image = ImageIO.read(inputStream);

                if (image == null && inputStream != null) {
                    inputStream.close();
                    inputStream = null;

                    throw new JelException("Server failed to load image(" + siteContainer.getImageName() + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
                }
            } catch (IOException exception) {
                throw new JelException("Server failed to load image(" + siteContainer.getImageName() + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
            }
        }

        ISite persistedSite = mStorageManager.addSite(session, siteContainer);

        // Now that site has been persisted to database we now have a valid siteID we could save the image with.
        // Incase an image was selected in the first place that is.
        if (fileSuffix != null && image != null) {
            try {
                String imagePath = ServerInformationManager.getSiteImageDirectory() + File.separator + persistedSite.getID() + ServerInformationManager.getFileSuffixSeparator() + fileSuffix;
                mLogger.debug("Saving image(" + siteContainer.getImageName() + ") to disk as " + imagePath);
                ImageIO.write(image, fileSuffix, new File(imagePath));

                String imageURL = "/" + "Siteimages" + "/" + persistedSite.getID() + ServerInformationManager.getFileSuffixSeparator() + fileSuffix;
                mLogger.debug("Setting imageURL for site(id=" + persistedSite.getID() + ") to " + imageURL);
                mStorageManager.updateSiteImage(persistedSite.getID(), imageURL);
            } catch (Exception exception) {
                throw new JelException("Server failed to save image(" + persistedSite.getID() + fileSuffix + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
            }
        }

        this.notifyAddedSite(persistedSite);

        return persistedSite;
    }


    /**
     * Returns specified site, if it exists.
     * If provided session is null or not valid, only a public site will be returned otherwise null is returned.
     *
     * @param session
     * @param id
     * @return
     */
    public ISite getSite(ISession session, int id) {
        if (id < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        boolean onlyPublic = !mSecurityManager.verifySession(session);

        return mStorageManager.getSite(session, id, onlyPublic);
    }


    /**
     * Return sites we are member of.
     *
     * @param session
     * @return
     */
    public List<ISite> getSites(ISession session) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }

        return mStorageManager.getSites(session);
    }


    /**
     * Returns a simple list of sites.
     * If provided session is null or not valid, only public sites will be returned.
     *
     * @param session
     * @return
     */
    public List<ISiteSimple> getSiteList(ISession session) {
        boolean onlyPublic = !mSecurityManager.verifySession(session);

        return mStorageManager.getSiteList(session, onlyPublic);
    }


    /**
     * Update an existing site.
     * Only sites we are administrators of are allowed to be updated.
     * 
     * @param session
     * @param siteContainer
     * @return
     */
    public ISite updateSite(ISession session, ISitePersistantContainer siteContainer) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (siteContainer == null || siteContainer.getSite() == null) {
            throw new JelException("Siteobject not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        ISite newSite = siteContainer.getSite();

        if (!mStorageManager.isAdminOfSite(session, newSite.getID())) {
            throw new JelException("User has not permissions to update specified site", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        if (newSite.getName() == null || newSite.getName().trim().length() == 0) {
            throw new JelException("Sitename not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getName().trim().length() > 50) {
            throw new JelException("Sitename too long(max 50 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getDescription() != null) {
            newSite.setDescription(newSite.getDescription().trim());
            if (newSite.getDescription().length() > 512) {
                throw new JelException("Description too long(max 512 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (newSite.getBackgroundColor() != null) {
            newSite.setBackgroundColor(newSite.getBackgroundColor().trim());
            if (newSite.getBackgroundColor().length() > 20) {
                throw new JelException("Background color value long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (newSite.getWidth() < 640) {
            throw new JelException("Background width can not be smaller than 640 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getWidth() > 32000) {
            throw new JelException("Background width can not be larger than 32000 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getHeight() < 480) {
            throw new JelException("Background height can not be smaller than 480 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (newSite.getHeight() > 32000) {
            throw new JelException("Background height can not be larger than 32000 pixels", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        newSite.setName(newSite.getName().trim());

        String fileSuffix = null;
        BufferedImage image = null;

        // If not empty then we have a new image to save.
        if (siteContainer.getImageName() != null && siteContainer.getImageName().trim().length() > 0) {
            if (siteContainer.getRawImage().length == 0) {
                throw new JelException("Imagename but no imagedata was provided to server", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
            }

            String[] tmpNames = siteContainer.getImageName().split("\\.");
            if (tmpNames.length < 2) {
                throw new JelException("Imagename(\"" + siteContainer.getImageName() + "\") does not contain a filesuffix, eg. \".jpg\"", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
            }

            fileSuffix = tmpNames[tmpNames.length - 1].toLowerCase();  // Get last one, e.g. "gif" from "myfile.is.great.gif"

            String[] supportedFormats = ImageIO.getWriterFormatNames();

            if (mLogger.isDebugEnabled()) {
                String debugLine = "Supported imageformats by server(";
                for (String line : supportedFormats) {
                    debugLine += " " + line;
                }

                mLogger.debug(debugLine += ")");
            }


            if (!Arrays.asList(supportedFormats).contains(fileSuffix)) {
                throw new JelException("Imageformat \"" + fileSuffix + "\" not supported by server, please convert image to another format", ExceptionReason.SERVER_ERROR, ExceptionType.WARN);
            }

            try {
                mLogger.debug("Loading image to byte-array");

                ByteArrayInputStream inputStream = new ByteArrayInputStream(siteContainer.getRawImage());
                image = ImageIO.read(inputStream);

                if (image == null && inputStream != null) {
                    inputStream.close();
                    inputStream = null;

                    throw new JelException("Server failed to load image(" + siteContainer.getImageName() + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
                }
            } catch (IOException exception) {
                throw new JelException("Server failed to load image(" + siteContainer.getImageName() + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
            }
        }

        ISite updatedSite = mStorageManager.updateSite(session, siteContainer);

        // Now that site has been persisted to database we can update the image.
        // Incase an image was selected in the first place that is.
        if (fileSuffix != null && image != null) {
            int id = updatedSite.getID();

            try {
                String imagePath = ServerInformationManager.getSiteImageDirectory() + File.separator + id + ServerInformationManager.getFileSuffixSeparator() + fileSuffix;
                mLogger.debug("Saving image(" + siteContainer.getImageName() + ") to disk as " + imagePath);
                ImageIO.write(image, fileSuffix, new File(imagePath));

                String imageURL = "/" + "Siteimages" + "/" + id + ServerInformationManager.getFileSuffixSeparator() + fileSuffix;
                mLogger.debug("Setting imageURL for site(id=" + id + ") to " + imageURL);

                updatedSite.setBackgroundImageUrl(imageURL);

                mStorageManager.updateSiteImage(id, imageURL);
            } catch (Exception exception) {
                throw new JelException("Server failed to save image(" + String.valueOf(id) + fileSuffix + ")", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
            }
        }

        this.notifyUpdatedSite(updatedSite);

        return updatedSite;
    }


    public void removeSite(ISession session, final int id) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (id < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, id)) {
            throw new JelException("User has not permissions to remove specified site", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }
        ISite site = mStorageManager.getSite(session, id, false);

        mStorageManager.removeSite(session, id);
        this.notifyRemovedSite(site);

        try {
            // And last but not least, remove siteimage if any.
            class ImageFilter implements FilenameFilter
            {

                public boolean accept(File dir, String name) {
                    if (name.split("\\.")[0].equals(String.valueOf(id))) {
                        return true;
                    } else {
                        return false;
                    }
                }

            }
            String[] imageArray = new File(ServerInformationManager.getSiteImageDirectory()).list(new ImageFilter());
            for (String imageName : imageArray) {
                mLogger.debug("Deleting image(" + imageName + ") from site(" + id + ")");
                new File(ServerInformationManager.getSiteImageDirectory() + File.separator + imageName).delete();
            }
        } catch (Exception exception) {
            throw new JelException("Failed to remove possible siteimage, image may have to be removed manually.", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
        }
    }


    public void addSiteUser(ISession session, int userID, int siteID, int permissionType) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (userID == 1) {
            throw new JelException("Master-admin is allways automatically added to sites", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (userID < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (siteID < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        // Change this if more permissiontypes are added!
        if (permissionType < 0 || permissionType > 1) {
            throw new JelException("No valid permissiontype specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, siteID)) {
            throw new JelException("User has not permission to add users to site", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }
        if (mStorageManager.getSiteUser(session, userID, siteID) != null) {
            throw new JelException("User is already member of site", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        mStorageManager.addSiteUser(session, userID, siteID, permissionType);
    }


    public void removeSiteUser(ISession session, int userID, int siteID) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (userID == 1) {
            throw new JelException("Master-admin can never be removed from a site", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (userID < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (siteID < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, siteID)) {
            throw new JelException("User has not permission to remove users to site", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        mStorageManager.removeSiteUser(session, userID, siteID);
    }


    public void updateSiteUser(ISession session, int userID, int siteID, int permissionType) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (userID == 1) {
            throw new JelException("Master-admin can not be modified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (userID < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (siteID < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        // Change this if more permissiontypes are added!
        if (permissionType < 0 || permissionType > 1) {
            throw new JelException("No valid permissiontype specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, siteID)) {
            throw new JelException("User has not permission to remove users to site", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        mStorageManager.updateSiteUser(session, userID, siteID, permissionType);
    }


    public List<SiteUser> getSiteUsers(ISession session, int siteID) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (siteID < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, siteID)) {
            throw new JelException("User has not permission to view the sites users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        return mStorageManager.getSiteUsers(session, siteID);
    }


    public void setSiteUsers(ISession session, int siteID, List<SiteUser> siteUsers) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (siteID < 1) {
            throw new JelException("SiteID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminOfSite(session, siteID)) {
            throw new JelException("User has not permission to modify the sites users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        List<SiteUser> cleanedList = new ArrayList<SiteUser>();

        // Remove master-admin and duplicates
        if (siteUsers != null) {
            for (SiteUser user : siteUsers) {
                if (user.getUserID() != 1 && !cleanedList.contains(user)) {
                    cleanedList.add(user);
                }
            }
        }

        mStorageManager.setSiteUsers(session, siteID, cleanedList);
    }


    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS //////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    public void addAddedSiteListener(ISiteAddedListener listener) {
        if (listener != null && !mSiteAddedListeners.contains(listener)) {
            mSiteAddedListeners.add(listener);
        }
    }


    public void removeAddedSiteListener(ISiteAddedListener listener) {
        mSiteAddedListeners.remove(listener);
    }


    private void notifyAddedSite(ISite site) {
        try {
            for (ISiteAddedListener listener : mSiteAddedListeners) {
                listener.siteAdded(new SiteEvent(this, site, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addRemovedSiteListener(ISiteRemovedListener listener) {
        if (listener != null && !mSiteRemovedListeners.contains(listener)) {
            mSiteRemovedListeners.add(listener);
        }
    }


    public void removeRemovedSiteListener(ISiteRemovedListener listener) {
        mSiteRemovedListeners.remove(listener);
    }


    private void notifyRemovedSite(ISite site) {
        try {
            for (ISiteRemovedListener listener : mSiteRemovedListeners) {
                listener.siteRemoved(new SiteEvent(this, site, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addUpdatedSiteListener(ISiteUpdatedListener listener) {
        if (listener != null && !mSiteUpdatedListeners.contains(listener)) {
            mSiteUpdatedListeners.add(listener);
        }
    }


    public void removeUpdatedSiteListener(ISiteUpdatedListener listener) {
        mSiteUpdatedListeners.remove(listener);
    }


    private void notifyUpdatedSite(ISite site) {
        try {
            for (ISiteUpdatedListener listener : mSiteUpdatedListeners) {
                listener.siteUpdated(new SiteEvent(this, site, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }

    //TODO: Add eventlisteners for adding, removing and updating siteusers!
    
    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS END //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}

