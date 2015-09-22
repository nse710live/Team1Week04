package week04.data;

import week04.app.Account;
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
 */
public class DataAccess {

    private static DataAccess m_singleton = null;

    private Connection m_connect = null;

    private PreparedStatement m_insertUser;
    private PreparedStatement m_selectAllUsers;
    private PreparedStatement m_deleteUser;
    private PreparedStatement m_selectSingleUser;
    private PreparedStatement m_updateUser;

    private PreparedStatement m_selectAllAccounts;
    private PreparedStatement m_selectAccountById;
    private PreparedStatement m_insertAccount;
    private PreparedStatement m_updateAccount;
    private PreparedStatement m_deleteAccount;

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
        final String INSERT_USER_SQL = "INSERT INTO atm.user VALUES (?, ?, ?, ?)";
        final String SELECT_ALL_USERS_SQL = "SELECT id, first_name, last_name FROM atm.user";
        final String DELETE_USER_BY_ID_SQL = "DELETE FROM atm.user WHERE id = (?)";
        final String SELECT_USER_SQL_FMT = "SELECT id, first_name, last_name FROM atm.user WHERE id = (?)";
        final String UPDATE_USER_SQL = "UPDATE atm.user SET first_name = (?), " +
                "last_name = (?), last_update = (?) WHERE id = (?)";

        m_insertUser = m_connect.prepareStatement(INSERT_USER_SQL);
        m_selectAllUsers = m_connect.prepareStatement(SELECT_ALL_USERS_SQL);
        m_deleteUser = m_connect.prepareStatement(DELETE_USER_BY_ID_SQL);
        m_selectSingleUser = m_connect.prepareStatement(SELECT_USER_SQL_FMT);
        m_updateUser = m_connect.prepareStatement(UPDATE_USER_SQL);

        final String SELECT_ALL_ACCOUNTS = "SELECT id, user_id, name, balance FROM atm.account";
        final String SELECT_ACCOUNT_BY_ID = "SELECT id, user_id, name, balance FROM atm.account WHERE id = (?)";
        final String INSERT_ACCOUNT_SQL = "INSERT INTO atm.account VALUES (?, ?, ?, ?, ?)";
        final String UPDATE_ACCOUNT_SQL = "UPDATE atm.account SET user_id = (?), name = (?), " +
                "balance = (?), last_update = (?) WHERE id = (?)";
        final String DELETE_ACCOUNT_SQL = "DELETE FROM atm.account WHERE id = (?)";

        m_selectAllAccounts = m_connect.prepareStatement(SELECT_ALL_ACCOUNTS);
        m_selectAccountById = m_connect.prepareStatement(SELECT_ACCOUNT_BY_ID);
        m_insertAccount = m_connect.prepareStatement(INSERT_ACCOUNT_SQL);
        m_updateAccount = m_connect.prepareStatement(UPDATE_ACCOUNT_SQL);
        m_deleteAccount = m_connect.prepareStatement(DELETE_ACCOUNT_SQL);
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

    /**
     * Get a list of all Accounts
     *
     * @return - List of Accounts
     * @throws AtmDataException
     */
    public List<Account> getAccounts() throws AtmDataException {
        List<Account> accounts = new ArrayList<>();
        ResultSet resultSet;

        try {
            if (m_connect == null || m_connect.isClosed()) connect();
            resultSet = m_selectAllAccounts.executeQuery();

            while (resultSet.next()) {
                long accountId = resultSet.getLong("id");
                long userId = resultSet.getLong("user_id");
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");
                User user = getUserById(userId);
                accounts.add(new Account(accountId, user, name, balance));
            }
        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }

        return accounts;
    }

    /**
     * Get an account by ID
     *
     * @param id - Id of Account to be retrieved
     * @return - Account Object
     * @throws AtmDataException
     */
    public Account getAccountById(long id) throws AtmDataException {
        Account account = null;
        ResultSet resultSet;

        try {
            if (m_connect == null || m_connect.isClosed()) connect();

            m_selectAccountById.setLong(1, id);
            resultSet = m_selectAccountById.executeQuery();

            while (resultSet.next()) {
                account = new Account();
                account.setAccountId(resultSet.getLong("id"));
                account.setUser(getUserById(resultSet.getLong("user_id")));
                account.setName(resultSet.getString("name"));
                account.setBalance(resultSet.getDouble("balance"));
            }

        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }

        return account;
    }

    /**
     * Save account. If account already exists, it will be updated.
     *
     * @param account - Account to be saved
     * @return - updated account
     * @throws AtmDataException
     */
    public Account saveAccount(Account account) throws AtmDataException {
        Calendar now = Calendar.getInstance();
        Date updateDate = new Date(now.getTime().getTime());

        try {

            if (m_connect == null || m_connect.isClosed()) connect();

            // first see it user already exists
            Account existingAccount = getAccountById(account.getAccountId());
            if (existingAccount != null && existingAccount.getAccountId() == account.getAccountId()) {
                m_updateAccount.setLong(1, account.getUser().getUserId());
                m_updateAccount.setString(2, account.getName());
                m_updateAccount.setDouble(3, account.getBalance());
                m_updateAccount.setDate(4, updateDate);
                m_updateAccount.setLong(5, account.getAccountId());
                m_updateAccount.executeUpdate();
            } else {
                m_insertAccount.setLong(1, account.getAccountId());
                m_insertAccount.setLong(2, account.getUser().getUserId());
                m_insertAccount.setString(3, account.getName());
                m_insertAccount.setDouble(4, account.getBalance());
                m_insertAccount.setDate(5, updateDate);
                m_insertAccount.executeUpdate();
            }
            return getAccountById(account.getAccountId());
        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }
    }

    /**
     * Remove an account from database
     *
     * @param account - Account to be removed
     * @throws AtmDataException
     */
    public void removeAccount(Account account) throws AtmDataException {
        try {
            if (m_connect == null || m_connect.isClosed()) connect();
            m_deleteAccount.setLong(1, account.getAccountId());
            m_deleteAccount.executeUpdate();

        } catch (SQLException ex) {
            throw new AtmDataException(ex);
        }
    }
}
