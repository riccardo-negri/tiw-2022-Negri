package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.dao.AccountDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Transaction> findTransactions(int userID) throws SQLException {
        List<Transaction> userTransactions = new ArrayList<Transaction>();
        String query = "SELECT  * FROM transaction JOIN account AS a1 ON transaction.origin = a1.id JOIN account AS a2 ON transaction.destination = a2.id " +
                "JOIN user AS u1 ON a1.user = u1.id JOIN user AS u2 ON a2.user = u2.id WHERE origin = ? OR destination = ? ORDER BY transaction.timestamp DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(userID));
            preparedStatement.setString(2, String.valueOf(userID));
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Transaction t = new Transaction(
                            result.getInt("id"),
                            result.getTimestamp("timestamp"),
                            result.getFloat("amount"),
                            result.getString("reason"),
                            result.getString("a1.code"),
                            new it.polimi.tiw.beans.User(
                                    result.getInt("u1.id"),
                                    result.getString("u1.username"),
                                    result.getString("u1.email"),
                                    result.getString("u1.name"),
                                    result.getString("u1.surname")
                            ),
                            result.getString("a2.code"),
                            new it.polimi.tiw.beans.User(
                                    result.getInt("u2.id"),
                                    result.getString("u2.username"),
                                    result.getString("u2.email"),
                                    result.getString("u2.name"),
                                    result.getString("u2.surname")
                            )
                    );
                    userTransactions.add(t);
                }
            }
        }
        return userTransactions;
    }

    public Transaction getTransactionFromID(int transactionID) throws SQLException {
        String query = "SELECT  * FROM transaction JOIN account AS a1 ON transaction.origin = a1.id JOIN account AS a2 ON transaction.destination = a2.id JOIN user AS u1 ON a1.user = u1.id JOIN user AS u2 ON a2.user = u2.id WHERE transaction.id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(transactionID));
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, there is no transaction
                    return null;
                else {
                    result.next();
                    return new Transaction(
                            result.getInt("id"),
                            result.getTimestamp("timestamp"),
                            result.getFloat("amount"),
                            result.getString("reason"),
                            result.getString("a1.code"),
                            new it.polimi.tiw.beans.User(
                                    result.getInt("u1.id"),
                                    result.getString("u1.username"),
                                    result.getString("u1.email"),
                                    result.getString("u1.name"),
                                    result.getString("u1.surname")
                            ),
                            result.getString("a2.code"),
                            new it.polimi.tiw.beans.User(
                                    result.getInt("u2.id"),
                                    result.getString("u2.username"),
                                    result.getString("u2.email"),
                                    result.getString("u2.name"),
                                    result.getString("u2.surname")
                            )
                    );
                }
            }
        }
    }
    public Integer addTransaction(int amount, String reason, int origin, int destination) throws SQLException {
        AccountDAO accountDAO = new AccountDAO(connection);
        String query = "INSERT INTO transaction (amount, reason, origin, destination) VALUES (?, ?, ?, ?)";
        connection.setAutoCommit(false);
        Integer id = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, String.valueOf(amount));
            preparedStatement.setString(2, String.valueOf(reason));
            preparedStatement.setString(3, String.valueOf(origin));
            preparedStatement.setString(4, String.valueOf(destination));
            // first update: create transaction
            preparedStatement.executeUpdate();

            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if(rs.first()) {
                    id = rs.getInt(1);
                }
            }
            // second update: decrease balance of sender
            accountDAO.updateBalance(origin, -amount);
            // third update: increase balance of sender
            accountDAO.updateBalance(destination, amount);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        return id;
    }

    public Timestamp getLastActivity(int accountID) throws SQLException {
        String query = "SELECT  * FROM transaction  WHERE origin = ? OR destination = ? ORDER BY timestamp DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(accountID));
            preparedStatement.setString(2, String.valueOf(accountID));
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, there is no last activity so no transactions
                    return null;
                else {
                    result.next();
                    return result.getTimestamp("timestamp");
                }
            }
        }
    }
}
