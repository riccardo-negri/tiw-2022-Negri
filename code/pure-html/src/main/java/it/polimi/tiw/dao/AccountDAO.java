package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Account;
import it.polimi.tiw.dao.TransactionDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.Random;

public class AccountDAO {
    private final Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Account> findAccountsWithLastActivity(int userID) throws SQLException {
        List<Account> UserAccounts = new ArrayList<>();
        String query = "SELECT  * FROM account  WHERE user = ?";
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(userID));
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Timestamp lastActivity = transactionDAO.getLastActivity(result.getInt("id"));
                    Account a = new Account(
                            result.getInt("id"),
                            result.getString("code"),
                            result.getFloat("balance"),
                            result.getInt("user"),
                            (lastActivity != null ? lastActivity.toString().split(" ")[0] : "No recent activity")
                    );
                    UserAccounts.add(a);
                }
            }
        }
        return UserAccounts;
    }

    public Account getAccountFromID (int accountID) throws SQLException {
        String query = "SELECT  * FROM account  WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(accountID));
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, there is no account
                    return null;
                else {
                    result.next();
                    return new Account(
                            result.getInt("id"),
                            result.getString("code"),
                            result.getFloat("balance"),
                            result.getInt("user"),
                            ""
                    );
                }
            }
        }
    }

    public Account getAccountFromCode (String code) throws SQLException {
        String query = "SELECT  * FROM account  WHERE code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(code));
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, there is no account
                    return null;
                else {
                    result.next();
                    return new Account(
                            result.getInt("id"),
                            result.getString("code"),
                            result.getFloat("balance"),
                            result.getInt("user"),
                            ""
                    );
                }
            }
        }
    }

    public void updateBalance (int accountID, int delta) throws SQLException {
        String query = "UPDATE account SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(delta));
            preparedStatement.setString(2, String.valueOf(accountID));
            preparedStatement.executeUpdate();
        }
    }

    public String findAvailableAccountCode() throws SQLException {
        char[] chars = "0123456789".toCharArray();
        Random rnd = new Random();
        String query = "SELECT * FROM account WHERE code = ?";
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) { // Generate random account code
                sb.append(chars[rnd.nextInt(chars.length)]);
            }
            String code = sb.toString();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, code);
                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (!result.isBeforeFirst()) // no results, there are no users with hat email or username
                        return code;
                }
            }

        }
    }
    public void createAccount(int userID)
            throws SQLException {
        String query = "INSERT INTO account (code, balance, user) VALUES (?, ?, ?)";
        String code = findAvailableAccountCode();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, code);
            preparedStatement.setFloat(2, 0);
            preparedStatement.setString(3, String.valueOf(userID));
            preparedStatement.executeUpdate();
        }
    }


}
