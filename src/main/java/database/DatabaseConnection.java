package database;

import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.BasicAccount;
import model.asset_checker.OfflineAssetChecker;

import javax.security.auth.login.CredentialException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;


public class DatabaseConnection {

    private final static String DATABASE_USER = "User";
    private final static String DATABASE_USER_PASSWORD = "SecretPassword";
    private static final String DATABASE_LOCATION = ".MoneyManager/database";

    private static final int ASSET_SOURCE_CREDENTIALS_COLUMN_TYPE = 1;
    private static final int ASSET_SOURCE_CREDENTIALS_COLUMN_USER = 2;
    private static final int ASSET_SOURCE_CREDENTIALS_COLUMN_KEY = 3;
    private static final int ASSET_SOURCE_CREDENTIALS_COLUMN_SECRET = 4;
    private static final int OFFLINE_ACCOUNTS_COLUMN_NAME = 1;
    private static final int OFFLINE_ACCOUNTS_COLUMN_BALANCE = 2;

    private final Connection databaseConnection;

    public static boolean doesDatabaseExist() {
        String homeDirectory = System.getProperty("user.home");
        File databaseFile = new File(homeDirectory + "/" + DATABASE_LOCATION + ".mv.db");
        if (databaseFile.exists() && databaseFile.isFile()) {
            return true;
        }
        return false;
    }

    public DatabaseConnection(String databasePassword) throws CredentialException, IOException {
        this.databaseConnection = connectToOrCreateDatabase(databasePassword);
    }

    private Connection connectToOrCreateDatabase(String password) throws CredentialException, IOException {
        try {
            String createSqlFilePath = DatabaseConnection.class.getClassLoader().getResource("create.sql").getPath();
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:~/" + DATABASE_LOCATION + ";CIPHER=AES;INIT=RUNSCRIPT FROM '" + createSqlFilePath + "'";
            String passwords = password + " " + DATABASE_USER_PASSWORD;
            return DriverManager.getConnection(url, DATABASE_USER, passwords);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() instanceof IllegalStateException) {
                throw new CredentialException("Wrong encryption password used.");
            } else {
                throw new IOException("Could not access database.");
            }
        }

    }

    public void addAssetSourceCredentials(AssetSourceCredentials credentials) throws SQLException {
        PreparedStatement preparedStatement = this.databaseConnection.prepareStatement("INSERT INTO AssetSourceCredentials(Type, User, Key, Secret) VALUES(?, ?, ?, ?);");
        preparedStatement.setString(ASSET_SOURCE_CREDENTIALS_COLUMN_TYPE, credentials.getType());
        preparedStatement.setString(ASSET_SOURCE_CREDENTIALS_COLUMN_USER, credentials.getUser());
        preparedStatement.setString(ASSET_SOURCE_CREDENTIALS_COLUMN_KEY, credentials.getKey());
        preparedStatement.setString(ASSET_SOURCE_CREDENTIALS_COLUMN_SECRET, credentials.getSecret());
        preparedStatement.execute();
    }

    public List<AssetSourceCredentials> getAssetSourceCredentials() throws SQLException {
        List<AssetSourceCredentials> assetSourceCredentials = new LinkedList<>();
        PreparedStatement preparedStatement = this.databaseConnection.prepareStatement("SELECT Type, User, Key, Secret FROM AssetSourceCredentials ORDER BY Type");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            char[] name = resultSet.getString(ASSET_SOURCE_CREDENTIALS_COLUMN_TYPE).toCharArray();
            char[] user = resultSet.getString(ASSET_SOURCE_CREDENTIALS_COLUMN_USER).toCharArray();
            char[] key = resultSet.getString(ASSET_SOURCE_CREDENTIALS_COLUMN_KEY).toCharArray();
            char[] secret = resultSet.getString(ASSET_SOURCE_CREDENTIALS_COLUMN_SECRET).toCharArray();
            assetSourceCredentials.add(new AssetSourceCredentials(name, user, key, secret));
        }
        return assetSourceCredentials;
    }

    public void addOfflineAccount(Account account) throws SQLException {
        PreparedStatement preparedStatement = this.databaseConnection.prepareStatement("INSERT INTO OfflineAccounts(Name, Balance) VALUES(?, ?);");
        preparedStatement.setString(OFFLINE_ACCOUNTS_COLUMN_NAME, account.getName());
        preparedStatement.setBigDecimal(OFFLINE_ACCOUNTS_COLUMN_BALANCE, account.getCurrentBalance().getBalanceValue());
        preparedStatement.execute();
    }

    public AssetSource getOfflineAccountAssetSource() throws SQLException {
        List<Account> offlineAccounts = new LinkedList<>();
        PreparedStatement preparedStatement = this.databaseConnection.prepareStatement("SELECT Name, Balance FROM OfflineAccounts ORDER BY Name");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String name = resultSet.getString(OFFLINE_ACCOUNTS_COLUMN_NAME);
            BigDecimal balance = resultSet.getBigDecimal(OFFLINE_ACCOUNTS_COLUMN_BALANCE);
            offlineAccounts.add(new BasicAccount(name, balance));
        }
        return new AssetSource(new OfflineAssetChecker("Offline Accounts", offlineAccounts));
    }

}
