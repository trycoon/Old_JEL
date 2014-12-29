/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site.event;

import java.util.Date;
import jel.site.ISite;
import jel.utils.JelEvent;

/**
 *
 * @author trycoon
 */
public class SiteEvent extends JelEvent {

    public SiteEvent(Object source, ISite target, Date time)
    {
        super(source, target, time);
    }


    public SiteEvent(Object source, ISite target)
    {
        super(source, target);
    }
}
