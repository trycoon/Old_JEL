/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.user;

import java.util.List;
import jel.security.ISession;

/**
 *
 * @author trycoon
 */
public interface IUserManager 
{

    IUser addUser(ISession session, IUser user);

    IUser getUser(ISession session, int id);

    List<IUser> getUsers(ISession session);

    void removeUser(ISession session, int id);

    IUser updateUser(ISession session, IUser user);
    
}
