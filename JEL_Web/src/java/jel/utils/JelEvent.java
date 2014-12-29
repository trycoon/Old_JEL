/*
 * Baseclass for all events
 */

package jel.utils;

import java.util.Date;
import java.util.EventObject;

/**
 *
 * @author trycoon
 */
public abstract class JelEvent extends EventObject {
    private Object mTarget;
    private Date mTime;


    public JelEvent(Object source, Object target, Date time)
    {
        super(source);
        mTarget = target;
        mTime = time;
    }


    public JelEvent(Object source, Object target)
    {
        this(source, target, new Date());
    }


    public Object getTarget()
    {
        return mTarget;
    }


    public Date getEventTime()
    {
        return mTime;
    }
}
