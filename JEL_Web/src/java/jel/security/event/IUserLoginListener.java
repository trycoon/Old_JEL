/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.security.event;

import java.util.EventListener;

/**
 *
 * @author trycoon
 */
public interface IUserLoginListener extends EventListener  {
    public void userLogin(SecurityEvent event);
}
