package jel.server;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import jel.security.ISession;


/**
 *
 * @author trycoon
 */
public final class ServerInformationManager implements IServerInformationManager
{
    /*
     * !!! INCREASE THIS FOR EVERY RELEASE !!! 
     */

    private final static String VERSION = "0.0.1";
    private final static long mUptimeStart = System.currentTimeMillis();
    private final long[] UPTIME_DEVISORS = {60, 60, 24, 7};
    private final String[] UPTIME_LABELS = {"seconds", "minutes", "hours", "days"};


    public String getServerVersion() {
        return VERSION;
    }


    /**
     * Get percentage of diskfull, in two decimals.
     * @return Percentage of diskfull.
     */
    public double getDiskFull() {
        long freeSpace = getFreeDiskSpace();
        long totalSpace = getTotalDiskSpace();
        long usedSpace = totalSpace - freeSpace;

        if (totalSpace == 0) // Avoid divide by zero.
        {
            return 100;
        } else {
            return new BigDecimal(usedSpace / totalSpace * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
    }


    public String getUptimeString() {
        String resultString = "";
        long deltaTime = System.currentTimeMillis() - mUptimeStart;
        deltaTime /= 1000;

        long[] result = new long[UPTIME_DEVISORS.length];

        for (int index = 0; index < UPTIME_DEVISORS.length; index++) {
            result[index] = deltaTime % UPTIME_DEVISORS[index];
            deltaTime /= UPTIME_DEVISORS[index];
        }
        for (int index = UPTIME_DEVISORS.length - 1; index >= 0; index--) {
            resultString += " " + result[index] + " " + UPTIME_LABELS[index];
        }

        return resultString;
    }


    /**
     * List of serverinformation for easy pressenting to clients.
     * @param session 
     * @return List of serverinformation.
     */
    public List<String> getServerInformation(ISession session) {
        List<String> result = new ArrayList();

        result.add("JEL server version: " + getServerVersion());
        result.add("OS name: " + getOSName());
        result.add("OS version: " + getOSVersion());
        result.add("Java virtual machine: " + getVMName());
        result.add("Java vendor: " + getVMVendor());
        result.add("Java version: " + getVMVersion());
        result.add("Available CPUs: " + getAvailableCPUs());
        result.add("Free java-memory: " + getFreeJVMMemory() / 1024 + " KB of " + getTotalJVMMemory() / 1024 + " KB");
        result.add("Local timezone: " + getUserTimezone());

        result.add("Uptime: " + getUptimeString());

        double loadAverage = getLoadAverage();
        if (loadAverage > 0) {
            result.add("LoadAverage: " + loadAverage);
        }

        return result;
    }


    /**
     * Returns the systems loadaverage.
     * A negative number is returned if not supported by platform.
     * @return Average systemload.
     */
    public static double getLoadAverage() {
        return java.lang.management.ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    }


    /**
     * The directory in which Java is installed
     */
    public static String getJavaHome() {
        return System.getProperties().getProperty("java.home");
    }


    public static String getVMName() {
        return System.getProperties().getProperty("java.vm.name");
    }


    public static String getVMVersion() {
        return System.getProperties().getProperty("java.vm.version");
    }


    public static String getVMVendor() {
        return System.getProperties().getProperty("java.vm.vendor");
    }


    public static String getJavaSpecificationName() {
        return System.getProperty("java.specification.name");
    }


    public static String getOSArchitecture() {
        return System.getProperties().getProperty("os.arch");
    }


    public static String getOSName() {
        return System.getProperties().getProperty("os.name");
    }


    public static boolean isLinux() {
        return (getOSName().toLowerCase().indexOf("linux") != -1);
    }


    public static boolean isLinux64() {
        return (getOSName().toLowerCase().indexOf("linux") != -1);
    }


    public static boolean isWindows() {
        return (getOSName().toLowerCase().indexOf("windows") != -1);
    }


    public static boolean isMacOSX() {
        return (getOSName().toLowerCase().indexOf("mac") != -1);
    }


    public static boolean isSolaris() {
        String name = getOSName().toLowerCase();
        return ((name.indexOf("sunos") != -1) || (name.indexOf("solaris") != -1));
    }


    public static String getOSVersion() {
        return System.getProperties().getProperty("os.version");
    }


    public static String getJavaBootLibraryPath() {
        return System.getProperties().getProperty("sun.boot.library.path");
    }


    /**
     * The current working directory when the properties were initialized
     */
    public static String getWorkingDirectory() {
        return System.getProperties().getProperty("user.dir");
    }


    public static String getJavaEndorsedDirectory() {
        return System.getProperties().getProperty("java.endorsed.dirs");
    }


    /**
     * The directory in which java should create temporary files
     */
    public static String getTempDirectory() {
        return System.getProperties().getProperty("java.io.tmpdir");
    }


    /**
     * The home directory of the current user
     */
    public static String getUserHome() {
        return System.getProperties().getProperty("user.home");
    }


    /**
     * The username of the current user
     */
    public static String getUserName() {
        return System.getProperties().getProperty("user.name");
    }


    /**
     * The two-letter language code of the default locale
     */
    public static String getUserLanguage() {
        return System.getProperties().getProperty("user.language");
    }


    public static String getUserCountry() {
        return System.getProperties().getProperty("user.country");
    }


    /**
     * The default time zone
     */
    public static String getUserTimezone() {
        return System.getProperties().getProperty("user.timezone");
    }


    /**
     * The value of the CLASSPATH environment variable
     */
    public static String getClassPath() {
        return System.getProperties().getProperty("java.class.path");
    }


    /**
     * The platform-dependent path separator (e.g., ":" on UNIX, "," for Windows)
     */
    public static String getPathSeparator() {
        return System.getProperties().getProperty("path.separator");
    }


    /**
     * The platform-dependent file separator (e.g., "/" on UNIX, "\" for Windows)
     */
    public static String getFileSeparator() {
        return System.getProperties().getProperty("file.separator");
    }


    /**
     * The platform-dependent filesuffix separator (e.g., "." on UNIX and Windows)
     */
    public static String getFileSuffixSeparator() {
        return "."; //TODO: Are there any platform-indepentant way?
    }


    public static int getAvailableCPUs() {
        return Runtime.getRuntime().availableProcessors();
    }


    public static long getTotalJVMMemory() {
        return Runtime.getRuntime().totalMemory();
    }


    public static long getFreeJVMMemory() {
        return Runtime.getRuntime().freeMemory();
    }


    public static long getTotalDiskSpace() {
        return new File(getUserHome()).getTotalSpace();
    }


    public static long getFreeDiskSpace() {
        return new File(getUserHome()).getUsableSpace();
    }


    /**
     * Get JEL homedirectory, where all files are stored.
     * e.g. "/home/trycoon/JEL"
     * @return
     */
    public static String getJelHome() {
        return System.getProperties().getProperty("user.home") + File.separator + "JEL";
    }


    /**
     * Get directory where siteimages are stored.
     * @return
     */
    public static String getSiteImageDirectory() {
        return System.getProperties().getProperty("user.home") + File.separator + "JEL" + File.separator + "Siteimages";
    }


    @Override
    public String toString() {
        return "[SYSTEM INFORMATION]" + "\n" +
                "OS Name: " + getOSName() + "\n" +
                "OS Architecture: " + getOSArchitecture() + "\n" +
                "OS Version: " + getOSVersion() + "\n" +
                "Java VirtualMachine: " + getVMName() + "\n" +
                "Java Vendor: " + getVMVendor() + "\n" +
                "Java Version: " + getVMVersion() + "\n" +
                "Java Home: " + getJavaHome() + "\n" +
                "Java librarypath: " + getJavaBootLibraryPath() + "\n" +
                "Available CPUs: " + getAvailableCPUs() + "\n" +
                "Free Java-Memory: " + getFreeJVMMemory() / 1024 + " KB of " + getTotalJVMMemory() / 1024 + " KB" + "\n" +
                "User home-directory: " + getUserHome() + "\n" +
                "User language: " + getUserLanguage() + "\n" +
                "[END SYSTEM INFORMATION]";
    }

}
