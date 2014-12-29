/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.user.event;

import java.util.EventListener;

/**
 *
 * @author trycoon
 */
public interface IUserUpdatedListener extends EventListener {
    public void userUpdated(UserEvent event);
}
