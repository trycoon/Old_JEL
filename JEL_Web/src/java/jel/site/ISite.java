/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

import java.util.Date;

/**
 *
 * @author trycoon
 */
public interface ISite 
{
    public int getID();
    public void setID(int id);
    
    public String getName();
    public void setName(String name);
    
    public String getDescription();
    public void setDescription(String name);

    public Date getCreateTime();
    public void setCreateTime(Date time);

    public int getWidth();
    public void setWidth(int width);
    
    public int getHeight();
    public void setHeight(int height);
    
    public String getBackgroundImageUrl();
    public void setBackgroundImageUrl(String url);
    
    public String getBackgroundColor();
    public void setBackgroundColor(String color);
    
    public boolean getImageRepeatX();
    public void setImageRepeatX(boolean repeat);
    
    public boolean getImageRepeatY();
    public void setImageRepeatY(boolean repeat);
    
    public boolean getAllowAnonymousUsers();
    public void setAllowAnonymousUsers(boolean allow);

    public String getLongitude();

    public void setLongitude(String longitude);

    public String getLatitude();

    public void setLatitude(String latitude);

    public boolean getIsAdmin();
    public void setIsAdmin(boolean isAdmin);
}
