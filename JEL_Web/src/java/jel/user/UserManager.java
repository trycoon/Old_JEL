/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jel.security.ISession;
import jel.server.JelException;
import jel.server.JelException.*;
import jel.user.event.IUserAddedListener;
import jel.user.event.IUserRemovedListener;
import jel.user.event.IUserUpdatedListener;
import jel.user.event.UserEvent;

/**
 *
 * @author trycoon
 */
public final class UserManager implements IUserManager
{

    private jel.storage.IStorageManager mStorageManager;
    private jel.eventlog.IEventLogManager mEventlogManager;
    private jel.security.SecurityManager mSecurityManager;
    private List<IUserAddedListener> mUserAddedListeners = new ArrayList<IUserAddedListener>();
    private List<IUserRemovedListener> mUserRemovedListeners = new ArrayList<IUserRemovedListener>();
    private List<IUserUpdatedListener> mUserUpdatedListeners = new ArrayList<IUserUpdatedListener>();


    public void setStorageManager(jel.storage.IStorageManager storageManager) {
        this.mStorageManager = storageManager;
    }

    public void setEventlogManager( jel.eventlog.IEventLogManager eventlogManager ) {
        this.mEventlogManager = eventlogManager;
    }

    public void setSecurityManager(jel.security.SecurityManager securityManager) {
        this.mSecurityManager = securityManager;
    }


    public IUser addUser(ISession session, IUser user) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (user == null) {
            throw new JelException("Userobject not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (user.getUsername() == null || user.getUsername().trim().length() == 0) {
            throw new JelException("Username not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getUsername().trim().length() > 20) {
            throw new JelException("Username too long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getPassword() == null || user.getPassword().trim().length() == 0) {
            throw new JelException("Password not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getPassword().trim().length() > 20) {
            throw new JelException("Password too long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getDescription() != null) {
            user.setDescription(user.getDescription().trim());
            if (user.getDescription().length() > 2000) {
                throw new JelException("Description too long(max 2000 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (user.getFirstname() != null) {
            user.setFirstname(user.getFirstname().trim());
            if (user.getFirstname().length() > 30) {
                throw new JelException("Firstname too long(max 30 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (user.getLastname() != null) {
            user.setLastname(user.getLastname().trim());
            if (user.getLastname().length() > 30) {
                throw new JelException("Lastname too long(max 30 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (!mStorageManager.isAdminUser(session)) {
            throw new JelException("User has not permission to add new users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        user.setID(-1);
        user.setLastLoggedInTime(null);
        user.setCreateTime(new Date());

        user.setUsername(user.getUsername().trim());
        user.setPassword(user.getPassword().trim());

        user = mStorageManager.addUser(session, user);
        this.notifyAddedUser(user);

        return user;
    }


    public IUser getUser(ISession session, int id) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (id < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        //FIXME: Make sure that user only can get information about themself.

        return mStorageManager.getUser(session, id);
    }


    public List<IUser> getUsers(ISession session) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (!mStorageManager.isAdminUser(session)) {
            throw new JelException("User has not permission to view users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        return mStorageManager.getUsers(session);
    }


    public void removeUser(ISession session, int id) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (id < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (id == 1) {
            throw new JelException("Master-admin can't be removed", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (!mStorageManager.isAdminUser(session)) {
            throw new JelException("User has not permission to remove users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        IUser user = mStorageManager.getUser(session, id);        
        mStorageManager.removeUser(session, id);
        this.notifyRemovedUser(user);
    }


    public IUser updateUser(ISession session, IUser user) {
        if (!mSecurityManager.verifySession(session)) {
            throw new JelException("No valid session, please login first", ExceptionReason.NO_VALID_SESSION, ExceptionType.INFO);
        }
        if (user == null) {
            throw new JelException("Userobject not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (user.getID() < 1) {
            throw new JelException("UserID not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }
        if (user.getUsername() == null || user.getUsername().trim().length() == 0) {
            throw new JelException("Username not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getUsername().trim().length() > 20) {
            throw new JelException("Username too long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getPassword() == null || user.getPassword().trim().length() == 0) {
            throw new JelException("Password not specified", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getPassword().trim().length() > 20) {
            throw new JelException("Password too long(max 20 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (user.getDescription() != null) {
            user.setDescription(user.getDescription().trim());
            if (user.getDescription().length() > 2000) {
                throw new JelException("Description too long(max 2000 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (user.getFirstname() != null) {
            user.setFirstname(user.getFirstname().trim());
            if (user.getFirstname().length() > 30) {
                throw new JelException("Firstname too long(max 30 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (user.getLastname() != null) {
            user.setLastname(user.getLastname().trim());
            if (user.getLastname().length() > 30) {
                throw new JelException("Lastname too long(max 30 characters)", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
            }
        }
        if (user.getID() == 1 && user.getIsDisabled()) {
            throw new JelException("Master-admin can not be disabled", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }
        if (!mStorageManager.isAdminUser(session)) {
            throw new JelException("User has not permission to update users", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
        }

        user.setUsername(user.getUsername().trim());
        user.setPassword(user.getPassword().trim());

        user = mStorageManager.updateUser(session, user);
        this.notifyUpdatedUser(user);

        return user;
    }


    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS //////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    public void addAddedUserListener(IUserAddedListener listener) {
        if (listener != null && !mUserAddedListeners.contains(listener)) {
            mUserAddedListeners.add(listener);
        }
    }


    public void removeAddedUserListener(IUserAddedListener listener) {
        mUserAddedListeners.remove(listener);
    }


    private void notifyAddedUser(IUser user) {
        try {
            for (IUserAddedListener listener : mUserAddedListeners) {
                listener.userAdded(new UserEvent(this, user, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addRemovedUserListener(IUserRemovedListener listener) {
        if (listener != null && !mUserRemovedListeners.contains(listener)) {
            mUserRemovedListeners.add(listener);
        }
    }


    public void removeRemovedUserListener(IUserRemovedListener listener) {
        mUserRemovedListeners.remove(listener);
    }


    private void notifyRemovedUser(IUser user) {
        try {
            for (IUserRemovedListener listener : mUserRemovedListeners) {
                listener.userRemoved(new UserEvent(this, user, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addUpdatedUserListener(IUserUpdatedListener listener) {
        if (listener != null && !mUserUpdatedListeners.contains(listener)) {
            mUserUpdatedListeners.add(listener);
        }
    }


    public void removeUpdatedUserListener(IUserUpdatedListener listener) {
        mUserUpdatedListeners.remove(listener);
    }


    private void notifyUpdatedUser(IUser user) {
        try {
            for (IUserUpdatedListener listener : mUserUpdatedListeners) {
                listener.userUpdated(new UserEvent(this, user, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }
    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS END //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}
