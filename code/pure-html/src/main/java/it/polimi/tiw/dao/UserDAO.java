package it.polimi.tiw.dao;

import java.sql.*;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AccountDAO;

public class UserDAO {
    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean existsUserWithUsername(String username) throws SQLException {
        String query = "SELECT  * FROM user  WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, username);
            try (ResultSet result = preparedStatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, there are no users with hat email or username
                    return false;
                else {
                    return true;
                }
            }
        }
    }

    public boolean existsUserWithEmail(String email) throws SQLException {
        String query = "SELECT  * FROM user  WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, email);
            try (ResultSet result = preparedStatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, there are no users with hat email or username
                    return false;
                else {
                    return true;
                }
            }
        }
    }

    public User checkUserLogin(String username, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet result = preparedStatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, there are no users with that username or the password is incorrect
                    return null;
                else {
                    result.next();
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("email"),
                            result.getString("name"),
                            result.getString("surname")
                    );
                }
            }
        }
    }

    public void createUser(String username, String email, String password, String name, String surname)
            throws SQLException {
        String query = "INSERT INTO user(username, email, password, name, surname) VALUES (?, ?, ?, ?, ?)";
        connection.setAutoCommit(false);
        AccountDAO accountDAO = new AccountDAO(connection);
        int id;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, name);
            preparedStatement.setString(5, surname);
            // first update: create user
            preparedStatement.executeUpdate();
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.first()) {
                    id = rs.getInt(1); // retrieve userID
                    // second update: create account
                    accountDAO.createAccount(id);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

}