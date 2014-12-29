/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site.event;

import java.util.EventListener;

/**
 *
 * @author trycoon
 */
public interface ISiteRemovedListener extends EventListener {
    public void siteRemoved(SiteEvent event);
}
