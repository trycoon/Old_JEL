/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

/**
 *
 * @author trycoon
 */
public interface ISitePersistantContainer 
{
    ISite getSite();
    void setSite(ISite site);
    
    byte[] getRawImage();
    void setRawImage(byte[] image);
    
    String getImageName();
    void setImageName(String name);
}
