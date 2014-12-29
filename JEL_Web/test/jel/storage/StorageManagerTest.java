/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.storage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import jel.eventlog.LogEntry;
import jel.hardware.device.Device;
import jel.security.ILoginInformation;
import jel.security.ISession;
import jel.site.ISite;
import jel.site.ISitePersistantContainer;
import jel.site.SiteUser;
import jel.storage.StorageManager.SortOrder;
import jel.user.IUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.Assert.*;

/**
 *
 * @author Henrik Östman (henrik at liquidbytes.se)
 */
public class StorageManagerTest
{
    private static StorageManager mStorageManager;
    private static ISession mSession;
    

    public StorageManagerTest() {
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
        mStorageManager = new StorageManager();

        DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource("jdbc:h2:~/JEL/DB/logDB_test;IGNORECASE=TRUE;CACHE_SIZE=32768;AUTO_SERVER=TRUE", "jelusr", "jelpass");
        mStorageManager.setDataSource(dataSource);
        mStorageManager.init();
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
        mStorageManager.shutdown();
        mStorageManager = null;
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of getDatabaseBuildVersion method, of class StorageManager.
     */
    @Test
    public void testGetDatabaseBuildVersion() {
        System.out.println("getDatabaseBuildVersion");
       
        int result = mStorageManager.getDatabaseBuildVersion();
        assertTrue(result > 0);
    }


    /**
     * Test of login method, of class StorageManager.
     */
    @Test
    public void testLogin() {
        System.out.println("login");
        String username = "admin";
        String password = "admin";

        ILoginInformation result = mStorageManager.login(username, password);
        assertNotNull(result);
        assertTrue(result.getSession() != null && result.getSession().getUserID() == 1 && result.getSession().getToken() != null);
        mSession = result.getSession();
    }


    /**
     * Test of verifySession method, of class StorageManager.
     */
    @Test
    public void testVerifySession() {
        System.out.println("verifySession");

        boolean result = mStorageManager.verifySession(mSession);
        assertTrue(result);
    }


    /**
     * Test of logout method, of class StorageManager.
     */
    @Test
    public void testLogout() {
        System.out.println("logout");

        mStorageManager.logout(mSession);
        boolean result = mStorageManager.verifySession(mSession);
        assertFalse(result);
        mSession = null;
    }


    /**
     * Test of addEventToLog method, of class StorageManager.
     */
    @Test
    public void testAddEventToLog() {

        testLogin(); // Set global session. How can we make this testflow better?

        System.out.println("addEventToLog");
        String eventlog = "UserLogin";
        int userID = 1;
        String message = "Test1";

        mStorageManager.addEventToLog(mSession, eventlog, userID, message);
        assertTrue(true);
    }


    /**
     * Test of getEventLog method, of class StorageManager.
     */
    @Test
    public void testGetEventLog() {
        System.out.println("getEventLog");
        String eventLog = "UserLogin";
        int userID = 1;
        mStorageManager.addEventToLog(mSession, eventLog, userID, "Test2");
        Date start = new Date();
        start.setTime(start.getTime() - 5000);
        Date end = new Date();

        List<LogEntry> result = mStorageManager.getEventLog(mSession, eventLog, userID, start, end);
        assertNotNull(result);
        LogEntry entry = result.get(result.size() - 1);
        assertTrue(entry.getEventName().equals(eventLog));
        assertTrue(entry.getMessage().equals("Test2"));
    }


    /**
     * Test of getEventLog method, of class StorageManager.
     */
    @Test
    public void testGetEventLog2() {
        System.out.println("getEventLog2");
        String eventLog = "UserLogin";
        int userID = 1;
        mStorageManager.addEventToLog(mSession, eventLog, userID, "Test2");
        mStorageManager.addEventToLog(mSession, eventLog, userID, "Test2");
        Date start = new Date();
        start.setTime(start.getTime() - 5000);
        Date end = new Date();

        List<LogEntry> result = mStorageManager.getEventLog(mSession, eventLog, userID, 1, SortOrder.Last);
        assertNotNull(result);
        assertTrue("Test returned more than one entry", result.size() == 1);
        LogEntry entry = result.get(0);
        assertTrue(entry.getEventName().equals(eventLog));
        assertTrue(entry.getMessage().equals("Test2"));
    }


    /**
     * Test of addUser method, of class StorageManager.
     */
    @Test
    public void testAddUser() {
        System.out.println("addUser");
        ISession session = null;
        IUser user = null;

        IUser expResult = null;
        IUser result = mStorageManager.addUser(session, user);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getUser method, of class StorageManager.
     */
    @Test
    public void testGetUser() {
        System.out.println("getUser");
        ISession session = null;
        int id = 0;

        IUser expResult = null;
        IUser result = mStorageManager.getUser(session, id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getUserByUsername method, of class StorageManager.
     */
    @Test
    public void testGetUserByUsername() {
        System.out.println("getUserByUsername");
        ISession session = null;
        String username = "";

        IUser expResult = null;
        IUser result = mStorageManager.getUserByUsername(session, username);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getUsers method, of class StorageManager.
     */
    @Test
    public void testGetUsers() {
        System.out.println("getUsers");
        ISession session = null;

        List expResult = null;
        List result = mStorageManager.getUsers(session);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of removeUser method, of class StorageManager.
     */
    @Test
    public void testRemoveUser() {
        System.out.println("removeUser");
        ISession session = null;
        int id = 0;

        mStorageManager.removeUser(session, id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of updateUser method, of class StorageManager.
     */
    @Test
    public void testUpdateUser() {
        System.out.println("updateUser");
        ISession session = null;
        IUser user = null;

        IUser expResult = null;
        IUser result = mStorageManager.updateUser(session, user);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of isAdminUser method, of class StorageManager.
     */
    @Test
    public void testIsAdminUser() {
        System.out.println("isAdminUser");
        ISession session = null;

        boolean expResult = false;
        boolean result = mStorageManager.isAdminUser(session);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of isAdminOfSite method, of class StorageManager.
     */
    @Test
    public void testIsAdminOfSite() {
        System.out.println("isAdminOfSite");
        ISession session = null;
        int siteID = 0;

        boolean expResult = false;
        boolean result = mStorageManager.isAdminOfSite(session, siteID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of addSite method, of class StorageManager.
     */
    @Test
    public void testAddSite() {
        System.out.println("addSite");
        ISession session = null;
        ISitePersistantContainer siteContainer = null;

        ISite expResult = null;
        ISite result = mStorageManager.addSite(session, siteContainer);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of updateSiteImage method, of class StorageManager.
     */
    @Test
    public void testUpdateSiteImage() {
        System.out.println("updateSiteImage");
        int siteID = 0;
        String url = "";

        mStorageManager.updateSiteImage(siteID, url);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getSite method, of class StorageManager.
     */
    @Test
    public void testGetSite() {
        System.out.println("getSite");
        ISession session = null;
        int id = 0;
        boolean onlyPublic = false;

        ISite expResult = null;
        ISite result = mStorageManager.getSite(session, id, onlyPublic);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getSiteList method, of class StorageManager.
     */
    @Test
    public void testGetSiteList() {
        System.out.println("getSiteList");
        ISession session = null;
        boolean onlyPublic = false;

        List expResult = null;
        List result = mStorageManager.getSiteList(session, onlyPublic);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getSites method, of class StorageManager.
     */
    @Test
    public void testGetSites() {
        System.out.println("getSites");
        ISession session = null;

        List expResult = null;
        List result = mStorageManager.getSites(session);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of removeSite method, of class StorageManager.
     */
    @Test
    public void testRemoveSite() {
        System.out.println("removeSite");
        ISession session = null;
        int id = 0;

        mStorageManager.removeSite(session, id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of updateSite method, of class StorageManager.
     */
    @Test
    public void testUpdateSite() {
        System.out.println("updateSite");
        ISession session = null;
        ISitePersistantContainer siteContainer = null;

        ISite expResult = null;
        ISite result = mStorageManager.updateSite(session, siteContainer);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of addSiteUser method, of class StorageManager.
     */
    @Test
    public void testAddSiteUser() {
        System.out.println("addSiteUser");
        ISession session = null;
        int userID = 0;
        int siteID = 0;
        int permissionType = 0;

        mStorageManager.addSiteUser(session, userID, siteID, permissionType);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of removeSiteUser method, of class StorageManager.
     */
    @Test
    public void testRemoveSiteUser() {
        System.out.println("removeSiteUser");
        ISession session = null;
        int userID = 0;
        int siteID = 0;

        mStorageManager.removeSiteUser(session, userID, siteID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of updateSiteUser method, of class StorageManager.
     */
    @Test
    public void testUpdateSiteUser() {
        System.out.println("updateSiteUser");
        ISession session = null;
        int userID = 0;
        int siteID = 0;
        int permissionType = 0;

        mStorageManager.updateSiteUser(session, userID, siteID, permissionType);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getSiteUsers method, of class StorageManager.
     */
    @Test
    public void testGetSiteUsers() {
        System.out.println("getSiteUsers");
        ISession session = null;
        int siteID = 0;

        List expResult = null;
        List result = mStorageManager.getSiteUsers(session, siteID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getSiteUser method, of class StorageManager.
     */
    @Test
    public void testGetSiteUser() {
        System.out.println("getSiteUser");
        ISession session = null;
        int userID = 0;
        int siteID = 0;

        SiteUser expResult = null;
        SiteUser result = mStorageManager.getSiteUser(session, userID, siteID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of setSiteUsers method, of class StorageManager.
     */
    @Test
    public void testSetSiteUsers() {
        System.out.println("setSiteUsers");
        ISession session = null;
        int siteID = 0;
        List<SiteUser> siteUsers = null;

        mStorageManager.setSiteUsers(session, siteID, siteUsers);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSettings method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSettings() {
        System.out.println("getDeviceSettings");
        short deviceID = 0;

        Map expResult = null;
        Map result = mStorageManager.getDeviceSettings(deviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of setDeviceSettings method, of class StorageManager.
     */
    @Test
    public void testSetDeviceSettings() {
        System.out.println("setDeviceSettings");
        short deviceID = 0;
        Map<String, String> settings = null;

        mStorageManager.setDeviceSettings(deviceID, settings);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getAllDevices method, of class StorageManager.
     */
    @Test
    public void testGetAllDevices() {
        System.out.println("getAllDevices");
        ISession session = null;

        List expResult = null;
        List result = mStorageManager.getAllDevices(session);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDevice method, of class StorageManager.
     */
    @Test
    public void testGetDevice() {
        System.out.println("getDevice");
        ISession session = null;
        short deviceID = 0;

        Device expResult = null;
        Device result = mStorageManager.getDevice(session, deviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of addDeviceSample method, of class StorageManager.
     */
    @Test
    public void testAddDeviceSample() {
        System.out.println("addDeviceSample");
        short deviceID = 0;
        DeviceSample sample = null;

        mStorageManager.addDeviceSample(deviceID, sample);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSamples method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSamples_3args_1() {
        System.out.println("getDeviceSamples");
        short deviceID = 0;
        Date fromTime = null;
        Date toTime = null;

        List expResult = null;
        List result = mStorageManager.getDeviceSamples(deviceID, fromTime, toTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSamples method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSamples_3args_2() {
        System.out.println("getDeviceSamples");
        short deviceID = 0;
        int number = 0;
        SortOrder order = null;

        List expResult = null;
        List result = mStorageManager.getDeviceSamples(deviceID, number, order);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSamplesAverage method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSamplesAverage_int() {
        System.out.println("getDeviceSamplesAverage");
        short deviceID = 0;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSamplesAverage(deviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSamplesAverage method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSamplesAverage_3args() {
        System.out.println("getDeviceSamplesAverage");
        short deviceID = 0;
        Date fromTime = null;
        Date toTime = null;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSamplesAverage(deviceID, fromTime, toTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSampleMax method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSampleMax_int() {
        System.out.println("getDeviceSampleMax");
        short deviceID = 0;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSampleMax(deviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSampleMax method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSampleMax_3args() {
        System.out.println("getDeviceSampleMax");
        short deviceID = 0;
        Date fromTime = null;
        Date toTime = null;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSampleMax(deviceID, fromTime, toTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSampleMin method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSampleMin_int() {
        System.out.println("getDeviceSampleMin");
        short deviceID = 0;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSampleMin(deviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of getDeviceSampleMin method, of class StorageManager.
     */
    @Test
    public void testGetDeviceSampleMin_3args() {
        System.out.println("getDeviceSampleMin");
        short deviceID = 0;
        Date fromTime = null;
        Date toTime = null;

        Double expResult = null;
        Double result = mStorageManager.getDeviceSampleMin(deviceID, fromTime, toTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
