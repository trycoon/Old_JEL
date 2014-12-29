/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.user.event;

import java.util.Date;
import jel.user.IUser;
import jel.utils.JelEvent;

/**
 *
 * @author trycoon
 */
public class UserEvent extends JelEvent {

    public UserEvent(Object source, IUser target, Date time)
    {
        super(source, target, time);
    }


    public UserEvent(Object source, IUser target)
    {
        super(source, target);
    }
}
