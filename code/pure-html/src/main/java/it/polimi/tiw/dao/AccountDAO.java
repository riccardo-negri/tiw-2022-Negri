package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class AccountDAO {
    private Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Account> findAccountsWithLastActivity(int userID) throws SQLException {
        List<Account> UserAccounts = new ArrayList<Account>();
        String query = "SELECT  * FROM account  WHERE user = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(userID));
            try (ResultSet result = preparedStatement.executeQuery();) {
                while (result.next()) {
                    Timestamp lastActivity = getLastActivity(result.getInt("id"));
                    Account a = new Account(
                            result.getInt("id"),
                            result.getString("code"),
                            result.getFloat("balance"),
                            result.getInt("user"),
                            lastActivity.toString().split(" ")[0]
                    );
                    UserAccounts.add(a);
                }
            }
        }
        return UserAccounts;
    }

    public Timestamp getLastActivity(int accountID) throws SQLException {
        String query = "SELECT  * FROM transaction  WHERE origin = ? OR destination = ? ORDER BY timestamp DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(accountID));
            preparedStatement.setString(2, String.valueOf(accountID));
            try (ResultSet result = preparedStatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, there is no account
                    return null;
                else {
                    result.next();
                    return result.getTimestamp("timestamp");
                }
            }
        }
    }

    public Account getAccountFromID (int accountID) throws SQLException {
        String query = "SELECT  * FROM account  WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(accountID));
            try (ResultSet result = preparedStatement.executeQuery();) {
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
}
