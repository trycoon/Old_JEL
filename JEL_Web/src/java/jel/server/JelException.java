/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.server;

/**
 *
 * @author trycoon
 */
public class JelException extends RuntimeException
{
    public enum ExceptionType {DEBUG, INFO, WARN, ERROR, FATAL};
    public enum ExceptionReason {
    NO_VALID_SESSION,           // No session provided or session no longer valid. Login to get a new session.
    NO_PRIVILEGES,              // User has not permission to execute specified operation.
    WRONG_PARAMETERS,           // Missing parameters to execute specified operation or parameters contains values outside bounds.
    SERVER_ERROR,               // Error occured on server that does not relate to bad user input. DB exceptions or hardware errors are examples.
    INCORRECT_SETTINGS};        // Configfiles are corrupt or contains bad settings. Usually the server will not even start when we hava an error like this.
    
    private String mDescription;
    private ExceptionReason mReason;
    private ExceptionType mType;
    
    
    JelException()
    { 
        super();
    }
       
    
    public JelException(String description, ExceptionReason reason, ExceptionType type)
    {
        this(description, reason, type, null);
    }
    
    
    public JelException(String description, ExceptionReason reason, ExceptionType type, Exception exception)
    {
        super(description, exception);
        mDescription = description;
        mReason = reason;
        mType = type;   
        
        if (type == ExceptionType.DEBUG)
            org.apache.log4j.Logger.getLogger("jel").debug("[" + reason + "] " + description, exception);
        else if (type == ExceptionType.INFO)
            org.apache.log4j.Logger.getLogger("jel").info("[" + reason + "] " + description, exception);
        else if (type == ExceptionType.WARN)
            org.apache.log4j.Logger.getLogger("jel").warn("[" + reason + "] " + description, exception);
        else if (type == ExceptionType.ERROR)
            org.apache.log4j.Logger.getLogger("jel").error("[" + reason + "] " + description, exception);
        else if (type == ExceptionType.FATAL)
            org.apache.log4j.Logger.getLogger("jel").fatal("[" + reason + "] " + description, exception);
    }
        
    
    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public ExceptionReason getReason() {
        return mReason;
    }

    public void setReason(ExceptionReason mReason) {
        this.mReason = mReason;
    }

    public ExceptionType getType() {
        return mType;
    }
    
    public void setType(ExceptionType mType) {
        this.mType = mType;
    }
}