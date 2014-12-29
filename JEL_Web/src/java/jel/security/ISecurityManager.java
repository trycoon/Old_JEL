/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.security;

/**
 *
 * @author trycoon
 */
public interface ISecurityManager {

    ILoginInformation login(String username, String password);

    void logout(ISession session);

    boolean verifySession(ISession session);

}
