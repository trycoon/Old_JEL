/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

/**
 *
 * @author trycoon
 */
public interface ISiteSimple 
{
    public int getID();
    public void setID(int id);
    
    public String getName();
    public void setName(String name);
    
    public boolean getIsAdmin();
    public void setIsAdmin(boolean admin);
}
