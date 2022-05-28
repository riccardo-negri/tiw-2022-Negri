package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

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

}