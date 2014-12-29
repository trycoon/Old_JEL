/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.hardware.adapter.event;

import java.util.EventListener;

/**
 *
 * @author trycoon
 */
public interface IAdapterAddedListener extends EventListener  {
    public void adapterAdded(AdapterEvent event);
}
