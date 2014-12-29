/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.utils;

/**
 *
 * @author trycoon
 */
public final class JelUtils
{

    public static boolean isNullOrEmpty(String input) {
        if (input == null || input.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

}
