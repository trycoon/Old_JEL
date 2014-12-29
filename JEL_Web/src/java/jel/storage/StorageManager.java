package jel.storage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import jel.site.*;
import jel.user.*;
import jel.security.ISession;
import javax.sql.DataSource;
import jel.eventlog.LogEntry;
import jel.hardware.device.*;
import jel.security.ILoginInformation;
import jel.security.LoginInformation;
import jel.security.LoginSession;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.utils.Crypto;
import jel.utils.FileUtilities;
import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

public final class StorageManager implements IStorageManager {

    public static enum SortOrder {

        First, Last
    };
    private static Logger mLogger = Logger.getLogger(StorageManager.class);
    private DataSource mDataSource;
    private SimpleJdbcTemplate mJdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        mDataSource = dataSource;
        this.mJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public void init() {
        // I didn't know where else I should place this. It should be one of the first methods to run.
        FileUtilities.setupEnvironment();

        try {
            String sqlLines = FileUtilities.loadDefaultConfigfiles(this.getClass().getClassLoader().getResourceAsStream("jel/storage/script/1-JEL_DB.sql"));

            if (sqlLines != null && sqlLines.length() > 0) {
                mJdbcTemplate.getJdbcOperations().execute(sqlLines);
                mLogger.info("Databasescripts successfully executed.");
            }
        } catch (IOException exception) {
            throw new JelException("Failed to load or execute database-script, file could be corrupt or databaseengine not working", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR, exception);
        }

        mLogger.info("Storagemanager started.");
    }

    public void shutdown() {
    }

    /**
     * Returns the version of the databasesystem that the database was created with.
     * @return versionnumber
     */
    public int getDatabaseBuildVersion() {
        String sql = "SELECT value FROM information_schema.settings WHERE NAME='CREATE_BUILD'";
        int version = mJdbcTemplate.queryForInt(sql, new Object[0]);

        return version;
    }

    @Transactional(readOnly = true)
    public List<LogEntry> getEventLog(ISession session, String eventLog, int userID, Date start, Date end) {
        ParameterizedRowMapper<LogEntry> mapper = new ParameterizedRowMapper<LogEntry>() {

            public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                LogEntry entry = new LogEntry();
                entry.setTime(rs.getTimestamp("EventTime"));
                entry.setMessage(rs.getString("Message"));
                entry.setEventName(rs.getString("Eventname"));

                return entry;
            }
        };

        List<LogEntry> entries = null;

        if (eventLog == null || eventLog.length() == 0) {
            String selectSQL = "SELECT EventTime, Message, Eventname FROM EVENT ev inner join EVENT_TYPE evt WHERE ev.EVENTTYPE = evt.id AND ev.USERID = ? AND EventTime >= ? AND EventTime <= ? ORDER BY EventTime";
            entries = mJdbcTemplate.query(selectSQL, mapper, new Object[]{userID, start, end});
        } else {
            String selectSQL = "SELECT EventTime, Message, Eventname FROM EVENT ev inner join EVENT_TYPE evt WHERE ev.EVENTTYPE = evt.id AND ev.USERID = ? AND evt.Eventname = ? AND EventTime >= ? AND EventTime <= ? ORDER BY EventTime";
            entries = mJdbcTemplate.query(selectSQL, mapper, new Object[]{userID, eventLog, start, end});
        }

        return entries;
    }

    @Transactional(readOnly = true)
    public List<LogEntry> getEventLog(ISession session, String eventLog, int userID, int number, SortOrder order) {
        ParameterizedRowMapper<LogEntry> mapper = new ParameterizedRowMapper<LogEntry>() {

            public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                LogEntry entry = new LogEntry();
                entry.setTime(rs.getTimestamp("EventTime"));
                entry.setMessage(rs.getString("Message"));
                entry.setEventName(rs.getString("Eventname"));

                return entry;
            }
        };

        List<LogEntry> entries = null;

        if (eventLog == null || eventLog.length() == 0) {
            String selectSQL = "SELECT TOP ? EventTime, Message, Eventname FROM EVENT ev inner join EVENT_TYPE evt WHERE ev.EVENTTYPE = evt.id AND ev.USERID = ? ORDER BY EventTime ?";
            entries = mJdbcTemplate.query(selectSQL, mapper, new Object[]{number, userID, order == SortOrder.First ? "ASC" : "DESC"});
        } else {
            if (order == SortOrder.First) {
                String selectSQL = "SELECT TOP ? EventTime, Message, Eventname FROM EVENT ev inner join EVENT_TYPE evt WHERE ev.EVENTTYPE = evt.id AND ev.USERID = ? AND evt.Eventname = ? ORDER BY EventTime ASC";
                entries = mJdbcTemplate.query(selectSQL, mapper, new Object[]{number, userID, eventLog});
            } else {
                String selectSQL = "SELECT TOP ? EventTime, Message, Eventname FROM EVENT ev inner join EVENT_TYPE evt WHERE ev.EVENTTYPE = evt.id AND ev.USERID = ? AND evt.Eventname = ? ORDER BY EventTime DESC";
                entries = mJdbcTemplate.query(selectSQL, mapper, new Object[]{number, userID, eventLog});
            }
        }

        return entries;
    }

    @Transactional
    public void addEventToLog(ISession session, String eventlog, int userID, String message) {
        if (eventlog != null && eventlog.length() > 0) {
            String insertSQL = "INSERT INTO EVENT(EventType, UserID, EventTime, Message) VALUES((SELECT ID FROM EVENT_TYPE WHERE Eventname = ?), ?, CURRENT_TIMESTAMP, ?)";
            Object[] params = new Object[]{eventlog, userID, message};

            mJdbcTemplate.update(insertSQL, params);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /// Securitymanagement begins
    ////////////////////////////////////////////////////////////////////////////
    @Transactional
    public ILoginInformation login(String username, String password) {
        String encryptedPassword = Crypto.getEncodedHash(password);

        String selectSQL = "SELECT ID, Firstname, Lastname, IsDisabled FROM JELUSER WHERE Username = ? AND Password = ?";
        ParameterizedRowMapper<ILoginInformation> mapper = new ParameterizedRowMapper<ILoginInformation>() {

            public ILoginInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
                ILoginInformation information = new LoginInformation();
                ISession session = new LoginSession();

                int userID = rs.getInt("ID");
                boolean isDisabled = rs.getBoolean("IsDisabled");
                information.setFirstName(rs.getString("Firstname"));
                information.setLastName(rs.getString("Lastname"));
                information.setIsMasterAdmin(userID == 1 ? true : false);
                information.setServerTime(Calendar.getInstance());
                session.setUserID(userID);

                information.setSession(session);

                if (isDisabled) {
                    throw new JelException("Useraccount has been disabled, please contact administrator.", ExceptionReason.NO_PRIVILEGES, ExceptionType.INFO);
                }

                return information;
            }
        };

        List<ILoginInformation> userList = mJdbcTemplate.query(selectSQL, mapper, username, encryptedPassword);

        if (userList == null || userList.size() == 0) {
            throw new JelException("Wrong username or password", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        ILoginInformation result = userList.get(0);
        String token = UUID.randomUUID().toString();

        String insertSQL = "INSERT INTO USER_SESSION(UserID, Token, ActiveTime) VALUES(?, ?, ?)";
        Object[] params = new Object[]{result.getSession().getUserID(), token, new Date()};
        mJdbcTemplate.update(insertSQL, params);

        result.getSession().setToken(token);

        return result;
    }

    @Transactional
    public void logout(ISession session) {
        String deleteSQL = "DELETE FROM USER_SESSION WHERE UserID = ?";
        Object[] params = new Object[]{session.getUserID()};

        mJdbcTemplate.update(deleteSQL, params);
    }

    @Transactional(readOnly = true)
    public boolean verifySession(ISession session) {
        String sql = "SELECT COUNT(*) FROM USER_SESSION WHERE UserID = ? AND Token = ?";
        Object[] params = new Object[]{session.getUserID(), session.getToken()};

        return mJdbcTemplate.queryForInt(sql, params) > 0 ? true : false;
    }
    ////////////////////////////////////////////////////////////////////////////
    /// Securitymanagement ends
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /// Usermanagement begins
    ////////////////////////////////////////////////////////////////////////////

    @Transactional
    public IUser addUser(ISession session, IUser user) {
        //TODO: Should use "IF EXISTS (SELECT 1 FROM JELUSER WHERE Username = ?) THEN SELECT 1 ELSE SELECT 0 END" when H2 has support for this, it's more efficient.
        int alreadyExists = mJdbcTemplate.queryForInt("SELECT Count(Username) FROM JELUSER WHERE Username = ?", new Object[]{user.getUsername()});
        if (alreadyExists > 0) {
            throw new JelException("A user with username \"" + user.getUsername() + "\" already exists. Please enter another username.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        // Insert user and obtain generated ID
        SqlUpdate su = new SqlUpdate();
        su.setDataSource(mDataSource);
        su.setSql("INSERT INTO JELUSER(Username, Password, Firstname, Lastname, Description, CreateTime, IsDisabled) VALUES(?, ?, ?, ?, ?, ?, ?)");
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.TIMESTAMP));
        su.declareParameter(new SqlParameter(Types.BOOLEAN));
        su.setReturnGeneratedKeys(true);
        su.compile();

        Object[] params = new Object[]{user.getUsername(), Crypto.getEncodedHash(user.getPassword()), user.getFirstname(), user.getLastname(), user.getDescription(), user.getCreateTime(), user.getIsDisabled()};
        KeyHolder keyHolder = new GeneratedKeyHolder();

        su.update(params, keyHolder);

        int id = keyHolder.getKey().intValue();


        // Get created user from database just to be sure everything is correct.
        String selectSQL = "SELECT ID, Username, Password, Firstname, Lastname, Description, CreateTime, LastLoggedInTime, IsDisabled FROM JELUSER WHERE ID = ?";
        ParameterizedRowMapper<IUser> mapper = new ParameterizedRowMapper<IUser>() {

            public IUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                IUser user = new User();

                user.setID(rs.getInt("ID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFirstname(rs.getString("Firstname"));
                user.setLastname(rs.getString("Lastname"));
                user.setDescription(rs.getString("Description"));
                user.setCreateTime(rs.getTimestamp("CreateTime"));
                user.setLastLoggedInTime(rs.getTimestamp("LastLoggedInTime"));
                user.setIsDisabled(rs.getBoolean("IsDisabled"));

                return user;
            }
        };
        IUser newUser = mJdbcTemplate.queryForObject(selectSQL, mapper, id);

        return newUser;
    }

    @Transactional(readOnly = true)
    public IUser getUser(ISession session, int id) {
        String selectSQL = "SELECT ID, Username, Password, Firstname, Lastname, Description, CreateTime, LastLoggedInTime, IsDisabled FROM JELUSER WHERE ID = ?";
        ParameterizedRowMapper<IUser> mapper = new ParameterizedRowMapper<IUser>() {

            public IUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                IUser user = new User();

                user.setID(rs.getInt("ID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFirstname(rs.getString("Firstname"));
                user.setLastname(rs.getString("Lastname"));
                user.setDescription(rs.getString("Description"));
                user.setCreateTime(rs.getTimestamp("CreateTime"));
                user.setLastLoggedInTime(rs.getTimestamp("LastLoggedInTime"));
                user.setIsDisabled(rs.getBoolean("IsDisabled"));

                return user;
            }
        };

        IUser user = null;

        try {
            user = mJdbcTemplate.queryForObject(selectSQL, mapper, id);
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no user found, return null.
            }

        return user;
    }

    @Transactional(readOnly = true)
    public IUser getUserByUsername(ISession session, String username) {
        String selectSQL = "SELECT ID, Username, Password, Firstname, Lastname, Description, CreateTime, LastLoggedInTime, IsDisabled FROM JELUSER WHERE Username = ?";
        ParameterizedRowMapper<IUser> mapper = new ParameterizedRowMapper<IUser>() {

            public IUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                IUser user = new User();

                user.setID(rs.getInt("ID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFirstname(rs.getString("Firstname"));
                user.setLastname(rs.getString("Lastname"));
                user.setDescription(rs.getString("Description"));
                user.setCreateTime(rs.getTimestamp("CreateTime"));
                user.setLastLoggedInTime(rs.getTimestamp("LastLoggedInTime"));
                user.setIsDisabled(rs.getBoolean("IsDisabled"));

                return user;
            }
        };

        IUser user = null;

        try {
            user = mJdbcTemplate.queryForObject(selectSQL, mapper, username);
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no user found, return null.
            }

        return user;
    }

    @Transactional(readOnly = true)
    public List<IUser> getUsers(ISession session) {
        String selectSQL = "SELECT ID, Username, Password, Firstname, Lastname, Description, CreateTime, LastLoggedInTime, IsDisabled FROM JELUSER ORDER BY Username";
        ParameterizedRowMapper<IUser> mapper = new ParameterizedRowMapper<IUser>() {

            public IUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                IUser user = new User();

                user.setID(rs.getInt("ID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFirstname(rs.getString("Firstname"));
                user.setLastname(rs.getString("Lastname"));
                user.setDescription(rs.getString("Description"));
                user.setCreateTime(rs.getTimestamp("CreateTime"));
                user.setLastLoggedInTime(rs.getTimestamp("LastLoggedInTime"));
                user.setIsDisabled(rs.getBoolean("IsDisabled"));

                return user;
            }
        };
        List<IUser> users = mJdbcTemplate.query(selectSQL, mapper, new Object[]{});

        return users;
    }

    @Transactional
    public void removeUser(ISession session, int id) {
        if (id > 1) // Safty incase we do something stupid in the code like trying to remove MasterAdmin.
        {
            String deleteSQL = "DELETE FROM JELUSER WHERE ID = ?";
            Object[] params = new Object[]{id};

            mJdbcTemplate.update(deleteSQL, params);
        }
    }

    @Transactional
    public IUser updateUser(ISession session, IUser user) {
        IUser oldUser = getUser(session, user.getID());

        if (oldUser == null) {
            throw new JelException("Could not update user, user not found in database.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        //TODO: Should use "IF EXISTS (SELECT 1 FROM JELUSER WHERE Username = ?) THEN SELECT 1 ELSE SELECT 0 END" when H2 has support for this, it's more efficient.
        int usernameExists = mJdbcTemplate.queryForInt("SELECT Count(Username) FROM JELUSER WHERE Username = ? AND ID <> ?", new Object[]{user.getUsername(), user.getID()});
        if (usernameExists > 0) {
            throw new JelException("A user with username \"" + user.getUsername() + "\" already exists. Please enter another username.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        // If password is returned empty, the old/current one will still be used, if password is not returned empty then this is the new password that should be saved.
        // A user can never have a blank password thats why we can get away with the logic above.
        List paramsList = new ArrayList();
        String updateSQL = "UPDATE JELUSER SET ";

        // Master admin can't change username!
        if (user.getID() > 1) {
            updateSQL += "Username = ?, ";
            paramsList.add(user.getUsername());
        }

        // Only set password if a new one is provided.
        if (user.getPassword() != null && user.getPassword().length() > 0) {
            updateSQL += "Password = ?, ";
            paramsList.add(Crypto.getEncodedHash(user.getPassword()));
        }

        updateSQL += "Firstname = ?, Lastname = ?, Description = ?, IsDisabled = ? WHERE ID = ?";
        paramsList.addAll(Arrays.asList(new Object[]{user.getFirstname(), user.getLastname(), user.getDescription(), user.getIsDisabled(), user.getID()}));

        mJdbcTemplate.update(updateSQL, paramsList.toArray());

        user = getUser(session, user.getID());

        return user;
    }

    /**
     * This will return true if user is admin of one or more sites.
     * Note that this don't mean that the user has the right to administrate ALL sites,
     * just those which he/she has been assigned to.
     * @param session
     * @return
     */
    @Transactional(readOnly = true)
    public boolean isAdminUser(ISession session) {
        boolean isAdmin = false;

        if (session != null) {
            if (session.getUserID() == 1) {
                isAdmin = true; // MasterAdmin(id=1) is always admin
                } else {
                //TODO: Should use "IF EXISTS.." when H2 supports it.
                String sql = "SELECT COUNT(*) FROM SITE_USER WHERE UserID = ? AND PermissionID > 0";
                Object[] params = new Object[]{session.getUserID()};

                isAdmin = mJdbcTemplate.queryForInt(sql, params) > 0 ? true : false;
            }
        }
        return isAdmin;
    }

    /**
     * Returns whether user has adminrights of specified site or not
     * @param session
     * @param siteID
     * @return
     */
    @Transactional(readOnly = true)
    public boolean isAdminOfSite(ISession session, int siteID) {
        boolean isAdmin = false;

        if (session != null) {
            if (session.getUserID() == 1) {
                isAdmin = true; // MasterAdmin(id=1) is always admin
                } else {
                //TODO: Should use "IF EXISTS.." when H2 supports it.
                String sql = "SELECT COUNT(*) FROM SITE_USER WHERE UserID = ? AND SiteID = ? AND PermissionID > 0";
                Object[] params = new Object[]{session.getUserID(), siteID};

                isAdmin = mJdbcTemplate.queryForInt(sql, params) > 0 ? true : false;
            }
        }
        return isAdmin;
    }
    ////////////////////////////////////////////////////////////////////////////
    /// Usermanagement ends
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /// Sitemanagement begins
    ////////////////////////////////////////////////////////////////////////////

    @Transactional
    public ISite addSite(final ISession session, ISitePersistantContainer siteContainer) {
        ISite newSite = siteContainer.getSite();

        //TODO: Should use "IF EXISTS (SELECT 1 FROM SITE WHERE Sitename = ?) THEN SELECT 1 ELSE SELECT 0 END" when H2 has support for this, it's more efficient.
        int alreadyExists = mJdbcTemplate.queryForInt("SELECT Count(Sitename) FROM SITE WHERE Sitename = ?", new Object[]{newSite.getName()});
        if (alreadyExists > 0) {
            throw new JelException("A site with name \"" + newSite.getName() + "\" already exists. Please enter another name.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        // Insert site and obtain generated ID
        SqlUpdate su = new SqlUpdate();
        su.setDataSource(mDataSource);
        su.setSql("INSERT INTO SITE(Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, Longitude, Latitude, AllowAnonymousUsers) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.TIMESTAMP));
        su.declareParameter(new SqlParameter(Types.SMALLINT));
        su.declareParameter(new SqlParameter(Types.SMALLINT));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.BOOLEAN));
        su.declareParameter(new SqlParameter(Types.BOOLEAN));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.BOOLEAN));
        su.setReturnGeneratedKeys(true);
        su.compile();

        Object[] params = new Object[]{newSite.getName(), newSite.getDescription(), newSite.getCreateTime(), newSite.getWidth(), newSite.getHeight(), newSite.getBackgroundColor(), newSite.getBackgroundImageUrl(), newSite.getImageRepeatX(), newSite.getImageRepeatY(), newSite.getLongitude(), newSite.getLatitude(), newSite.getAllowAnonymousUsers()};
        KeyHolder keyHolder = new GeneratedKeyHolder();

        su.update(params, keyHolder);

        int id = keyHolder.getKey().intValue();


        // Get created site from database just to be sure everything is correct.
        String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, Longitude, Latitude, AllowAnonymousUsers FROM SITE WHERE ID = ?";
        ParameterizedRowMapper<ISite> mapper = new ParameterizedRowMapper<ISite>() {

            public ISite mapRow(ResultSet rs, int rowNum) throws SQLException {
                ISite site = new Site();

                site.setID(rs.getInt("ID"));
                site.setName(rs.getString("Sitename"));
                site.setDescription(rs.getString("Description"));
                site.setCreateTime(rs.getTimestamp("CreateTime"));
                site.setWidth(rs.getInt("SizeWidth"));
                site.setHeight(rs.getInt("SizeHeight"));
                site.setBackgroundColor(rs.getString("BgColor"));
                site.setBackgroundImageUrl(rs.getString("BgImageUrl"));
                site.setImageRepeatX(rs.getBoolean("BgImageRepeatX"));
                site.setImageRepeatY(rs.getBoolean("BgImageRepeatY"));
                site.setLongitude(rs.getString("Longitude"));
                site.setLatitude(rs.getString("Latitude"));
                site.setAllowAnonymousUsers(rs.getBoolean("AllowAnonymousUsers"));
                site.setIsAdmin(isAdminOfSite(session, site.getID()));

                return site;
            }
        };
        ISite persistedSite = mJdbcTemplate.queryForObject(selectSQL, mapper, id);

        return persistedSite;
    }

    @Transactional
    public void updateSiteImage(int siteID, String url) {
        String updateSQL = "UPDATE SITE SET BgImageUrl = ? WHERE ID = ?";
        Object[] params = new Object[]{url, siteID};

        mJdbcTemplate.update(updateSQL, params);
    }

    @Transactional(readOnly = true)
    public ISite getSite(
            final ISession session, int id,
            boolean onlyPublic) {
        ParameterizedRowMapper<ISite> mapper = new ParameterizedRowMapper<ISite>() {

            public ISite mapRow(ResultSet rs, int rowNum) throws SQLException {
                ISite site = new Site();

                site.setID(rs.getInt("ID"));
                site.setName(rs.getString("Sitename"));
                site.setDescription(rs.getString("Description"));
                site.setCreateTime(rs.getTimestamp("CreateTime"));
                site.setWidth(rs.getInt("SizeWidth"));
                site.setHeight(rs.getInt("SizeHeight"));
                site.setBackgroundColor(rs.getString("BgColor"));
                site.setBackgroundImageUrl(rs.getString("BgImageUrl"));
                site.setImageRepeatX(rs.getBoolean("BgImageRepeatX"));
                site.setImageRepeatY(rs.getBoolean("BgImageRepeatY"));
                site.setAllowAnonymousUsers(rs.getBoolean("AllowAnonymousUsers"));
                site.setLongitude(rs.getString("Longitude"));
                site.setLatitude(rs.getString("Latitude"));
                site.setIsAdmin(isAdminOfSite(session, site.getID()));

                return site;
            }
        };

        List<ISite> sites = null;

        // Anonymous users have only access to public sites
        if (session == null || onlyPublic) {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE WHERE ID = ? AND AllowAnonymousUsers = true";
            sites = mJdbcTemplate.query(selectSQL, mapper, id);
        } // Master-admin has access to ALL sites
        else if (session.getUserID() == 1) {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE WHERE ID = ?";
            sites = mJdbcTemplate.query(selectSQL, mapper, id);
        } // Normal users only have access to sites they have been added to or to public sites
        else {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE WHERE AllowAnonymousUsers = true OR ID IN (SELECT SiteID FROM SITE_USER WHERE UserID = ? AND SiteID = ?)";
            sites = mJdbcTemplate.query(selectSQL, mapper, session.getUserID(), id);
        }

        if (sites != null && sites.size() > 0) {
            return sites.get(0);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<ISiteSimple> getSiteList(
            final ISession session,
            boolean onlyPublic) {
        ParameterizedRowMapper<ISiteSimple> mapper = new ParameterizedRowMapper<ISiteSimple>() {

            public ISiteSimple mapRow(ResultSet rs, int rowNum) throws SQLException {
                ISiteSimple site = new SiteSimple();

                site.setID(rs.getInt("ID"));
                site.setName(rs.getString("Sitename"));
                site.setIsAdmin(isAdminOfSite(session, site.getID()));

                return site;
            }
        };

        List<ISiteSimple> sites = null;

        // Anonymous users have only access to public sites
        if (session == null || onlyPublic) {
            String selectSQL = "SELECT ID, Sitename FROM SITE WHERE AllowAnonymousUsers = true ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{});
        } // Master-admin has access to ALL sites
        else if (session.getUserID() == 1) {
            String selectSQL = "SELECT ID, Sitename FROM SITE ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{});
        } // Normal users only have access to sites they have been added to or to public sites
        else {
            String selectSQL = "SELECT ID, Sitename FROM SITE WHERE AllowAnonymousUsers = true OR ID IN (SELECT SiteID FROM SITE_USER WHERE UserID = ?) ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{session.getUserID()});
        }

        return sites;
    }

    @Transactional(readOnly = true)
    public List<ISite> getSites(
            final ISession session) {
        ParameterizedRowMapper<ISite> mapper = new ParameterizedRowMapper<ISite>() {

            public ISite mapRow(ResultSet rs, int rowNum) throws SQLException {
                ISite site = new Site();

                site.setID(rs.getInt("ID"));
                site.setName(rs.getString("Sitename"));
                site.setDescription(rs.getString("Description"));
                site.setCreateTime(rs.getTimestamp("CreateTime"));
                site.setWidth(rs.getInt("SizeWidth"));
                site.setHeight(rs.getInt("SizeHeight"));
                site.setBackgroundColor(rs.getString("BgColor"));
                site.setBackgroundImageUrl(rs.getString("BgImageUrl"));
                site.setImageRepeatX(rs.getBoolean("BgImageRepeatX"));
                site.setImageRepeatY(rs.getBoolean("BgImageRepeatY"));
                site.setAllowAnonymousUsers(rs.getBoolean("AllowAnonymousUsers"));
                site.setLongitude(rs.getString("Longitude"));
                site.setLatitude(rs.getString("Latitude"));
                site.setIsAdmin(isAdminOfSite(session, site.getID()));

                return site;
            }
        };
        List<ISite> sites = null;

        // Anonymous users have only access to public sites
        if (session == null) {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE WHERE AllowAnonymousUsers = true ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{});
        } // Master-admin has access to ALL sites
        else if (session.getUserID() == 1) {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{});
        } // Normal users only have access to sites they have been added to or to public sites
        else {
            String selectSQL = "SELECT ID, Sitename, Description, CreateTime, SizeWidth, SizeHeight, BgColor, BgImageUrl, BgImageRepeatX, BgImageRepeatY, AllowAnonymousUsers FROM SITE WHERE AllowAnonymousUsers = true OR ID IN (SELECT SiteID FROM SITE_USER WHERE UserID = ?) ORDER BY Sitename";
            sites = mJdbcTemplate.query(selectSQL, mapper, new Object[]{session.getUserID()});
        }

        return sites;
    }

    @Transactional
    public void removeSite(ISession session, int id) {
        String deleteSQL = "DELETE FROM SITE WHERE ID = ?";
        Object[] params = new Object[]{id};

        mJdbcTemplate.update(deleteSQL, params);
    }

    @Transactional
    public ISite updateSite(ISession session, ISitePersistantContainer siteContainer) {
        ISite updateSite = siteContainer.getSite();
        ISite oldSite = getSite(session, updateSite.getID(), false);

        if (oldSite == null) {
            throw new JelException("Could not update site, site not found in database.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.WARN);
        }

        //TODO: Should use "IF EXISTS (SELECT 1 FROM SITE WHERE Sitename = ?) THEN SELECT 1 ELSE SELECT 0 END" when H2 has support for this, it's more efficient.
        int sitenameExists = mJdbcTemplate.queryForInt("SELECT Count(Sitename) FROM SITE WHERE Sitename = ? AND ID <> ?", new Object[]{updateSite.getName(), updateSite.getID()});
        if (sitenameExists > 0) {
            throw new JelException("A site with name \"" + updateSite.getName() + "\" already exists. Please enter another name.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        String updateSQL = "UPDATE SITE SET Sitename = ?, Description = ?, SizeWidth = ?, SizeHeight = ?, BgColor = ?, BgImageUrl = ?, BgImageRepeatX = ? , BgImageRepeatY = ?, AllowAnonymousUsers = ?, Longitude = ?, Latitude = ? WHERE ID = ?";
        Object[] params = new Object[]{updateSite.getName(), updateSite.getDescription(), updateSite.getWidth(), updateSite.getHeight(), updateSite.getBackgroundColor(), updateSite.getBackgroundImageUrl(), updateSite.getImageRepeatX(), updateSite.getImageRepeatY(), updateSite.getAllowAnonymousUsers(), updateSite.getLongitude(), updateSite.getLatitude(), updateSite.getID()};

        mJdbcTemplate.update(updateSQL, params);

        updateSite = getSite(session, updateSite.getID(), false);

        return updateSite;
    }

    @Transactional
    public void addSiteUser(ISession session, int userID, int siteID, int permissionType) {
        String insertSQL = "INSERT INTO SITE_USER(UserID, SiteID, PermissionID) VALUES(?, ?, ?)";
        Object[] params = new Object[]{userID, siteID, permissionType};

        mJdbcTemplate.update(insertSQL, params);
    }

    @Transactional
    public void removeSiteUser(ISession session, int userID, int siteID) {
        String deleteSQL = "DELETE FROM SITE_USER WHERE UserID = ? AND SiteID = ?";
        Object[] params = new Object[]{userID, siteID};

        mJdbcTemplate.update(deleteSQL, params);
    }

    @Transactional
    public void updateSiteUser(ISession session, int userID, int siteID, int permissionType) {
        String updateSQL = "UPDATE SITE_USER SET PermissionID = ? WHERE UserID = ? AND SiteID = ?";
        Object[] params = new Object[]{permissionType, userID, siteID};

        mJdbcTemplate.update(updateSQL, params);
    }

    @Transactional(readOnly = true)
    public List<SiteUser> getSiteUsers(
            ISession session, int siteID) {
        ParameterizedRowMapper<SiteUser> mapper = new ParameterizedRowMapper<SiteUser>() {

            public SiteUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteUser user = new SiteUser();

                user.setUsername(rs.getString("Username"));
                user.setUserID(rs.getInt("UserID"));
                user.setSiteID(rs.getInt("SiteID"));
                user.setPermissionType(rs.getInt("PermissionID"));

                return user;
            }
        };

        String selectSQL = "SELECT Username, UserID, SiteID, PermissionID FROM SITE_USER su INNER JOIN JELUSER ju ON ju.ID = su.UserID WHERE SiteID = ? ORDER BY Username";
        List<SiteUser> users = mJdbcTemplate.query(selectSQL, mapper, new Object[]{siteID});

        return users;
    }

    @Transactional(readOnly = true)
    public SiteUser getSiteUser(
            ISession session, int userID,
            int siteID) {
        ParameterizedRowMapper<SiteUser> mapper = new ParameterizedRowMapper<SiteUser>() {

            public SiteUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteUser user = new SiteUser();

                user.setUsername(rs.getString("Username"));
                user.setUserID(rs.getInt("UserID"));
                user.setSiteID(rs.getInt("SiteID"));
                user.setPermissionType(rs.getInt("PermissionID"));

                return user;
            }
        };

        String selectSQL = "SELECT Username, UserID, SiteID, PermissionID FROM SITE_USER su INNER JOIN JELUSER ju ON ju.ID = su.UserID WHERE UserID = ? AND SiteID = ?";
        SiteUser user = null;

        try {
            user = mJdbcTemplate.queryForObject(selectSQL, mapper, userID, siteID);
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no user found, return null.
            }

        return user;
    }

    @Transactional
    public void setSiteUsers(ISession session, int siteID, List<SiteUser> siteUsers) {
        String deleteSQL = "DELETE FROM SITE_USER WHERE SiteID = ?";
        Object[] params = new Object[]{siteID};
        mJdbcTemplate.update(deleteSQL, params);

        if (siteUsers != null) {
            for (SiteUser user : siteUsers) {
                String insertSQL = "INSERT INTO SITE_USER(UserID, SiteID, PermissionID) VALUES(?, ?, ?)";
                params = new Object[]{user.getUserID(), user.getSiteID(), user.getPermissionType()};

                mJdbcTemplate.update(insertSQL, params);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    /// Sitemanagement ends
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ///Devicemanagement begins
    ////////////////////////////////////////////////////////////////////////////

    @Transactional(readOnly = true)
    public Map<String, String> getDeviceSettings(short deviceID) {
        Map<String, String> settingsMapp = new LinkedHashMap<String, String>();

        String selectSQL = "SELECT SettingsKey, SettingsValue FROM DEVICE_SETTINGS WHERE DeviceID = ?";
        try {
            settingsMapp = (Map<String, String>) new JdbcTemplate().query(selectSQL, new Object[]{deviceID},
                    new ResultSetExtractor() {

                        public Object extractData(ResultSet rs) throws SQLException {
                            Map<String, String> map = new LinkedHashMap<String, String>();
                            while (rs.next()) {
                                String key = rs.getString("SettingsKey");
                                String value = rs.getString("SettingsValue");
                                map.put(key, value);
                            }
                            return map;
                        }

                        ;
                    });
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no settings found, return null.
            }

        return settingsMapp;
    }

    @Transactional
    public void setDeviceSettings(short deviceID, Map<String, String> settings) {
        if (deviceID > 0) {
            if (settings == null || settings.size() == 0) {
                String deleteSQL = "DELETE FROM DEVICE_SETTINGS WHERE DeviceID = ?";
                Object[] params = new Object[]{deviceID};

                mJdbcTemplate.update(deleteSQL, params);
            } else {
                List<Object[]> mergeList = new ArrayList<Object[]>(settings.size());
                for (String key : settings.keySet()) {
                    Object[] values = new Object[]{deviceID, key, settings.get(key)};
                    mergeList.add(values);
                }

                mJdbcTemplate.batchUpdate("MERGE INTO DEVICE_SETTINGS(DeviceID, SettingsKey, SettingsValue) KEY(DeviceID, SettingsKey) VALUES(?, ?, ?)", mergeList);
            }
        }
    }

    @Transactional
    public Device addDevice(ISession session, short siteID, String name, String description, short deviceTypeID) {
        // Insert device and obtain generated ID
        SqlUpdate su = new SqlUpdate();
        su.setDataSource(mDataSource);
        su.setSql("INSERT INTO DEVICE(SiteID, Devicename, Description, DeviceTypeID) VALUES(?, ?, ?, ?)");
        su.declareParameter(new SqlParameter(Types.SMALLINT));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.VARCHAR));
        su.declareParameter(new SqlParameter(Types.SMALLINT));
        su.setReturnGeneratedKeys(true);
        su.compile();

        Object[] params = new Object[]{siteID, name, description, deviceTypeID};
        KeyHolder keyHolder = new GeneratedKeyHolder();

        su.update(params, keyHolder);

        int id = keyHolder.getKey().intValue();

        // Get created device, from database just to be sure everything is correct.
        Device newDevice = this.getDevice(session, (short) id);
        return newDevice;
    }

    @Transactional(readOnly = true)
    public List<Device> getAllDevices(ISession session) {
        ParameterizedRowMapper<Device> mapper = new ParameterizedRowMapper<Device>() {

            public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
                short deviceID = rs.getShort("ID");
                short siteID = rs.getShort("SiteID");
                DeviceType deviceType = new DeviceType(rs.getShort("DeviceTypeID"), rs.getString("Typename"));

                Device device = null;

                if (deviceType.getName().equalsIgnoreCase("Sensor")) {
                    device = new Sensor(deviceID, siteID, getDeviceSettings(deviceID));
                } else if (deviceType.getName().equalsIgnoreCase("Actuator")) {
                    device = new Actuator(deviceID, siteID, getDeviceSettings(deviceID));
                }

                device.setName(rs.getString("Devicename"));
                device.setDescription(rs.getString("Description"));
                device.setDeviceType(deviceType);

                return device;
            }
        };

        String selectSQL = "SELECT dv.ID, SiteID, Devicename, Description, dt.ID AS DeviceTypeID, Typename FROM DEVICE dv INNER JOIN DEVICE_TYPE dt ON dv.DeviceTypeID = dt.ID ORDER BY Devicename DESC";
        List<Device> devices = null;

        try {
            devices = mJdbcTemplate.query(selectSQL, mapper);
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no device found, return null.
            }

        return devices;
    }

    @Transactional(readOnly = true)
    public Device getDevice(ISession session, short deviceID) {
        ParameterizedRowMapper<Device> mapper = new ParameterizedRowMapper<Device>() {

            public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
                short deviceID = rs.getShort("ID");
                short siteID = rs.getShort("SiteID");
                DeviceType deviceType = new DeviceType(rs.getShort("DeviceTypeID"), rs.getString("Typename"));

                Device device = null;

                if (deviceType.getName().equalsIgnoreCase("Sensor")) {
                    device = new Sensor(deviceID, siteID, getDeviceSettings(deviceID));
                } else if (deviceType.getName().equalsIgnoreCase("Actuator")) {
                    device = new Actuator(deviceID, siteID, getDeviceSettings(deviceID));
                }

                device.setName(rs.getString("Devicename"));
                device.setDescription(rs.getString("Description"));
                device.setDeviceType(deviceType);

                return device;
            }
        };

        String selectSQL = "SELECT dv.ID, SiteID, Devicename, Description, dt.ID AS DeviceTypeID, Typename FROM DEVICE dv INNER JOIN DEVICE_TYPE dt ON dv.DeviceTypeID = dt.ID WHERE dv.ID = ?";
        Device device = null;

        try {
            device = mJdbcTemplate.queryForObject(selectSQL, mapper, deviceID);
        } catch (IncorrectResultSizeDataAccessException exception) {
            // If no device found, return null.
            }

        return device;
    }

    @Transactional
    public void addDeviceSample(short deviceID, DeviceSample sample) {
        if (deviceID > 0 && sample != null) {
            String insertSQL = "INSERT INTO DEVICE_SAMPLE(DeviceID, DeviceTimestamp, DeviceValue) VALUES(?, ?, ?)";
            Object[] params = new Object[]{deviceID, sample.getTime(), sample.getValue()};

            mJdbcTemplate.update(insertSQL, params);
        }
    }

    @Transactional(readOnly = true)
    public List<DeviceSample> getDeviceSamples(short deviceID, Date fromTime, Date toTime) {

        ParameterizedRowMapper<DeviceSample> mapper = new ParameterizedRowMapper<DeviceSample>() {

            public DeviceSample mapRow(ResultSet rs, int rowNum) throws SQLException {
                DeviceSample sample = new DeviceSample();
                sample.SetTime(rs.getDate("DeviceTimestamp"));
                sample.setValue(rs.getDouble("DeviceValue"));
                return sample;
            }
        };

        List<DeviceSample> samples = new ArrayList<DeviceSample>();

        // Anonymous users have only access to public sites
        if (deviceID > 0 && fromTime != null && toTime != null && fromTime.before(toTime)) {
            String selectSQL = "SELECT DeviceTimestamp, DeviceValue FROM DEVICE_SAMPLE WHERE DeviceID = ? AND DeviceTimestamp >= ? AND DeviceTimestamp <= ? ORDER BY DeviceTimestamp";
            samples = mJdbcTemplate.query(selectSQL, mapper, new Object[]{deviceID, fromTime, toTime});
        }

        return samples;
    }

    @Transactional(readOnly = true)
    public List<DeviceSample> getDeviceSamples(short deviceID, int number, SortOrder order) {
        ParameterizedRowMapper<DeviceSample> mapper = new ParameterizedRowMapper<DeviceSample>() {

            public DeviceSample mapRow(ResultSet rs, int rowNum) throws SQLException {
                DeviceSample sample = new DeviceSample();
                sample.SetTime(rs.getDate("DeviceTimestamp"));
                sample.setValue(rs.getDouble("DeviceValue"));
                return sample;
            }
        };

        List<DeviceSample> samples = new ArrayList<DeviceSample>();

        // Anonymous users have only access to public sites
        if (deviceID > 0 && number > 0) {
            String selectSQL = "SELECT TOP ? DeviceTimestamp, DeviceValue FROM DEVICE_SAMPLE WHERE DeviceID = ? ORDER BY DeviceTimestamp DESC";
            if (order != null && order == SortOrder.First) {
                selectSQL = "SELECT TOP ? DeviceTimestamp, DeviceValue FROM DEVICE_SAMPLE WHERE DeviceID = ? ORDER BY DeviceTimestamp ASC";
            }

            samples = mJdbcTemplate.query(selectSQL, mapper, new Object[]{number, deviceID});
        }

        return samples;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSamplesAverage(short deviceID) {
        if (deviceID > 0) {
            String selectSQL = "SELECT AVG(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ?";
            String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID});
            if (valueString != null && valueString.length() > 0) {
                return Double.parseDouble(valueString);
            }
        }
        return 0d;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSamplesAverage(short deviceID, Date fromTime, Date toTime) {
        if (deviceID > 0 && fromTime != null && toTime != null && fromTime.before(toTime)) {
            String selectSQL = "SELECT AVG(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ? AND DeviceTimestamp >= ? AND DeviceTimestamp <= ?";
            String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID, fromTime, toTime});
            if (valueString != null && valueString.length() > 0) {
                return Double.parseDouble(valueString);
            }
        }
        return 0d;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSampleMax(short deviceID) {
        if (deviceID > 0) {
            String selectSQL = "SELECT MAX(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ?";
            String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID});
            if (valueString != null && valueString.length() > 0) {
                return Double.parseDouble(valueString);
            }
        }
        return 0d;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSampleMax(short deviceID, Date fromTime, Date toTime) {
        if (deviceID > 0 && fromTime != null && toTime != null && fromTime.before(toTime)) {
            String selectSQL = "SELECT MAX(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ? AND DeviceTimestamp >= ? AND DeviceTimestamp <= ?";
            String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID, fromTime, toTime});
            if (valueString != null && valueString.length() > 0) {
                return Double.parseDouble(valueString);
            }
        }
        return 0d;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSampleMin(short deviceID) {
        String selectSQL = "SELECT MIN(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ?";
        String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID});
        if (valueString != null && valueString.length() > 0) {
            return Double.parseDouble(valueString);
        }
        return 0d;
    }

    @Transactional(readOnly = true)
    public Double getDeviceSampleMin(short deviceID, Date fromTime, Date toTime) {
        if (deviceID > 0 && fromTime != null && toTime != null && fromTime.before(toTime)) {
            String selectSQL = "SELECT MIN(DeviceValue) FROM DEVICE_SAMPLE WHERE DeviceID = ? AND DeviceTimestamp >= ? AND DeviceTimestamp <= ?";
            String valueString = mJdbcTemplate.queryForObject(selectSQL, String.class, new Object[]{deviceID, fromTime, toTime});
            if (valueString != null && valueString.length() > 0) {
                return Double.parseDouble(valueString);
            }
        }
        return 0d;
    }
////////////////////////////////////////////////////////////////////////////
/// Devicemanagement ends
////////////////////////////////////////////////////////////////////////////
}
//
// TODO: LÄS DENNA: http://www.zabada.com/tutorials/simplifying-jdbc-with-the-spring-jdbc-abstraction-framework.php
//
// http://sujitpal.blogspot.com/2007/03/spring-jdbctemplate-and-transactions.html

