package it.polimi.tiw.dao;

import java.sql.*;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.PasswordHashing;
import it.polimi.tiw.dao.AccountDAO;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean existsUserWithUsername(String username) throws SQLException {
        String query = "SELECT  * FROM user  WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.isBeforeFirst();
            }
        }
    }

    public boolean existsUserWithEmail(String email) throws SQLException {
        String query = "SELECT  * FROM user  WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.isBeforeFirst();
            }
        }
    }

    public User checkUserLogin(String username, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, there are no users with that username
                    return null;
                else {
                    result.next();

                    // check the correctness of the password
                    String encoded = result.getString("encoded");
                    if (!PasswordHashing.verify(encoded, password)) {
                        return null; // wrong password
                    }

                    // authenticated correctly
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

    public User getUserFromUsername(String username) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet result = preparedStatement.executeQuery()) {
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
        String query = "INSERT INTO user(username, email, encoded, name, surname) VALUES (?, ?, ?, ?, ?)";
        connection.setAutoCommit(false);
        AccountDAO accountDAO = new AccountDAO(connection);
        int id;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            String encoded = PasswordHashing.encode(password);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, encoded);
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
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
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