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
            try(ResultSet result = preparedStatement.executeQuery();) {
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

    /**
     * Find the user with a specific id
     *
     * @param idUser: the id i want to find
     * @return the user with that id
     * @throws sqlException if i have problem with the interaction with the db
     */
    public User getUserWithId(int idUser) throws SQLException {
        String query = "SELECT * FROM user WHERE idUser=?";
        User user = new User();
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setInt(1, idUser);
            result = pstatement.executeQuery();
            //TODO: penso di spossa togliere il while e lasciare le istruzioni senza il ciclo, sono certo ci sarà sempre 1 sola riga
            while (result.next()) {
                user.setName(result.getString("name"));
                user.setId(idUser);
                user.setEmail(result.getString("email"));
                user.setPassword(result.getString("password"));
            }
        } catch (SQLException e) {
            throw new SQLException(e);

        } finally {
            try {
                result.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            try {
                pstatement.close();
            } catch (Exception e2) {
                throw new SQLException(e2);
            }
        }
        return user;
    }

    /**
     * Insert a new row in db
     *
     * @param email the email to insert
     * @param pass  the pass to insert
     * @param name  the name to insert
     * @throws sqlException if the query does not work
     */
    public void newUser(String email, String pass, String name) throws SQLException {
        String query = "INSERT into playlistmusicalecordioli.user (email, name, password)   VALUES(?, ?, ?)";

        PreparedStatement pstatement = null;
        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setString(1, email);
            pstatement.setString(2, name);
            pstatement.setString(3, pass);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                pstatement.close();
            } catch (Exception e1) {

            }
        }
    }
}