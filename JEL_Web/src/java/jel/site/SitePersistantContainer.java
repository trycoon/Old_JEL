/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.site;

/**
 *
 * @author trycoon
 */
public class SitePersistantContainer implements ISitePersistantContainer
{
    private ISite mSite;
    private byte[] mRawImage;
    private String mImageName;
    
    
    public ISite getSite() {
        return mSite;
    }

    public void setSite(ISite site) {
        mSite = site;
    }

    public byte[] getRawImage() {
        return mRawImage;
    }

    public void setRawImage(byte[] image) {
        mRawImage = image;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String name) {
        mImageName = name;
    }
}
