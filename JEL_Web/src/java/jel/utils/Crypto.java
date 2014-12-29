/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jel.utils;

import java.security.NoSuchAlgorithmException;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;

/**
 *
 * @author trycoon
 */
public final class Crypto {

    /**
     * Get and Base64 encoded SHA-256 hash back based upon entered message
     * @param message Message to be encoded
     * @return Encoded message
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String getEncodedHash(String message)
    {
         java.security.MessageDigest digest = null;
        try {
            digest = java.security.MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new JelException("Encryptionalgorithm not pressent", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
        }
         digest.reset();
         digest.update(message.getBytes());
         return new sun.misc.BASE64Encoder().encode(digest.digest());
    }
}
