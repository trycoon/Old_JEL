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
public interface ISiteUpdatedListener extends EventListener {
    public void siteUpdated(SiteEvent event);
}
