package week04.data;

import week04.app.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * DataAccess Singleton class used to connect to and query local MySQL database.
 *
 * Created by Matthew on 9/11/2015.
 */
public class DataAccess {

    private static DataAccess m_singleton = null;

    private Connection m_connect = null;

    private PreparedStatement m_insertUser;
    private PreparedStatement m_selectAllUsers;
    private PreparedStatement m_deleteUser;
    private PreparedStatement m_selectSingleUser;
    private PreparedStatement m_updateUser;

    private static String m_user = "root";
    private static String m_password = "root";

    /**
     * Default Constructor
     */
    private DataAccess(String user, String password) {
        m_user = user;
        m_password = password;
    }

    /**
     * Get instance of class with default login parameters
     *
     * @return - Instance of DataAccess class
     */
    public synchronized static DataAccess getInstance() {
        return getInstance(m_user, m_password);
    }

    /**
     * Get instance of class. User supplies DB login information.
     *
     * @param user     - user DB username
     * @param password - user DB password
     * @return - Instance of DataAccess class
     */
    public synchronized static DataAccess getInstance(String user, String password) {
        if (m_singleton == null) {
            m_singleton = new DataAccess(user, password);
        }

        return m_singleton;
    }

    /**
     * Get the database connection
     *
     * @return - database connection
     */
    public Connection getConnection() {
        try {
            if (m_connect == null || m_connect.isClosed()) connect();
        } catch (Exception ex) {
            // DataAccessTestCase does not allow exception so it will be swallowed here
        }
        return m_connect;
    }

    /**
     * Connect to the MySQL database
     *
     * @throws AtmDataException
     */
    public void connect() throws AtmDataException {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            m_connect = DriverManager.getConnection("jdbc:mysql://localhost/atm?" +
                    "user=root&password=root");

            setupPreparedStatements();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new AtmDataException(ex);
        }
    }

    private void setupPreparedStatements() throws SQLException {
        String INSERT_USER_SQL = "INSERT INTO atm.user VALUES (?, ?, ?, ?)";
        String SELECT_ALL_USERS_SQL = "SELECT id, first_name, last_name FROM atm.user";
        String DELETE_USER_BY_ID_SQL = "DELETE FROM atm.user WHERE id = (?)";
        String SELECT_USER_SQL_FMT = "SELECT id, first_name, last_name FROM atm.user WHERE id = (?)";
        String UPDATE_USER_SQL = "UPDATE atm.user SET first_name = (?), " +
                "last_name = (?), last_update = (?) WHERE id = (?)";

        m_insertUser = m_connect.prepareStatement(INSERT_USER_SQL);
        m_selectAllUsers = m_connect.prepareStatement(SELECT_ALL_USERS_SQL);
        m_deleteUser = m_connect.prepareStatement(DELETE_USER_BY_ID_SQL);
        m_selectSingleUser = m_connect.prepareStatement(SELECT_USER_SQL_FMT);
        m_updateUser = m_connect.prepareStatement(UPDATE_USER_SQL);
    }

    /**
     * Close the connection to the database
     */
    public void close() {
        try {
            if (m_connect != null) m_connect.close();
        } catch (SQLException ex) {
            // DataAccessTestCase does not allow an exception to be thrown so it will be swallowed here
        }
    }

    /**
     * Get a single user object by id from the database
     *
     * @param id - user id to be used for query
     * @return - user found in database
     * @throws AtmDataException
     */
    public User getUserById(long id) throws AtmDataException {
        System.out.println("Getting user with id: " + id);
        User user = null;
        ResultSet resultSet;

        try {
            if (m_connect == null || m_connect.isClosed()) connect();

            m_selectSingleUser.setLong(1, id);
            resultSet = m_selectSingleUser.executeQuery();

            while (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getLong("id"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                System.out.println("User found: " + user.toString());
            }

        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }

        return user;
    }

    /**
     * Save the given user object in the database. If the user id already exists, the user will be updated.
     *
     * @param user - user to be saved or updated
     * @return - user that is saved or updated
     * @throws AtmDataException
     */
    public User saveUser(User user) throws AtmDataException {
        Calendar now = Calendar.getInstance();
        Date updateDate = new Date(now.getTime().getTime());

        try {

            if (m_connect == null || m_connect.isClosed()) connect();

            // first see it user already exists
            User existingUser = getUserById(user.getUserId());
            if (existingUser != null && existingUser.getUserId() == user.getUserId()) {
                System.out.println("Updating user with id: " + user.getUserId());
                m_updateUser.setString(1, user.getFirstName());
                m_updateUser.setString(2, user.getLastName());
                m_updateUser.setDate(3, updateDate);
                m_updateUser.setLong(4, user.getUserId());
                m_updateUser.executeUpdate();
            } else {
                System.out.println("Inserting user with id: " + user.getUserId());
                m_insertUser.setLong(1, user.getUserId());
                m_insertUser.setString(2, user.getFirstName());
                m_insertUser.setString(3, user.getLastName());
                m_insertUser.setDate(4, updateDate);
                m_insertUser.executeUpdate();
            }
            return getUserById(user.getUserId());
        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }
    }

    /**
     * Delete the given user object from the database
     *
     * @param user - user object to be deleted
     * @throws AtmDataException
     */
    public void removeUser(User user) throws AtmDataException {

        try {
            if (m_connect == null || m_connect.isClosed()) connect();
            m_deleteUser.setLong(1, user.getUserId());
            m_deleteUser.executeUpdate();

        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }
    }

    /**
     * Get a list of all users in the database
     *
     * @return - List of user objects
     * @throws AtmDataException
     */
    public List<User> getUsers() throws AtmDataException {
        List<User> userList = new ArrayList<>();
        ResultSet resultSet;

        try {
            if (m_connect == null || m_connect.isClosed()) connect();
            resultSet = m_selectAllUsers.executeQuery();

            while (resultSet.next()) {
                long userId = resultSet.getLong("id");
                String first = resultSet.getString("first_name");
                String last = resultSet.getString("last_name");
                userList.add(new User(userId, first, last));
            }

        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }

        return userList;
    }
}
