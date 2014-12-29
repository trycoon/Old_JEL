/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.server.ServerInformationManager;


/**
 * FileUtilities.java
 *
 * Created on 2007-jun-11, 01:38:04
 * @author trycoon
 */
public final class FileUtilities
{
    /**
     * Set up filedirectories, config-files and more that is needed for basic funktionallity.
     * Should be run once application is started and be among the first methods to be executed.
     */
    public static void setupEnvironment() {
        String home = ServerInformationManager.getJelHome();

        // Create imagedirectory
        if (!new File(home + File.separator + "Siteimages").exists()) {
            if (!new File(home + File.separator + "Siteimages").mkdirs()) {
                throw new JelException("Could not create directory " + home + File.separator + "Siteimages", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
            }
        }

        // Add more stuff here
    }


    /**
     * Copies a File from one location to another.
     *
     * @param in  The File to copy from.
     * @param out The File to copy to.
     * @throws IOException
     */
    public static void copyFile(File in, File out) throws IOException {
        copyFile(new FileInputStream(in), new FileOutputStream(out));
    }

    /**
     * Copies a File from one location to another.
     * Please note that inputstream and outputstream are closed when copy is done.
     *
     * @param in  The File to copy from.
     * @param out The File to copy to.
     * @throws IOException
     */
    public static void copyFile(FileInputStream in, FileOutputStream out) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;

        try {
            sourceChannel = in.getChannel();
            destinationChannel = out.getChannel();

            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }

            if (destinationChannel != null) {
                destinationChannel.close();
            }
        }
    }

    /**
     * Copies a file from one location to a selected location on the filesystem.
     * Please note that inputstream are closed when copy is done.
     *
     * @param in The file to copy from
     * @param out To this location and filename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copyFile(InputStream in, String out) throws FileNotFoundException, IOException {
        OutputStream outStream = new FileOutputStream(out);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            outStream.write(buf, 0, len);
        }
        in.close();
        outStream.close();
    }

    /**
     * Load a specified configfile and return it's content as a string
     *
     * @param in The stream to load from
     * @return array of content in file
     * @throws IOException
     */
    public static String loadDefaultConfigfiles(InputStream in) throws IOException {
        String fullString = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int character = 0;

        while ((character = in.read()) != -1) {
            output.write(character);
        }

        fullString = new String(output.toByteArray());

        return fullString;
    }

    /**
     * Load a specified configfile and return it's content as an array of strings(one for every line)
     *
     * @param in The stream to load from
     * @param delimiter String that is used to identify linebreaks. Can be a regual expression.
     * @return array of content in file
     * @throws IOException
     */
    public static String[] loadDefaultConfigfiles(InputStream in, String delimiter) throws IOException {
        String fullString = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int character = 0;

        while ((character = in.read()) != -1) {
            output.write(character);
        }

        fullString = new String(output.toByteArray());

        return fullString.split(delimiter);
    }
}