package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Contact;
import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class ContactDAO {
    private Connection connection;

    public ContactDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Contact> findContactsFromUserID(int userID) throws SQLException {
        List<Contact> UserContacts = new ArrayList<>();
        String query = "SELECT  * FROM contact JOIN account ON contact.element = account.id JOIN user ON account.user = user.id  WHERE contact.owner = ? ORDER BY user.id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(userID));
            try (ResultSet result = preparedStatement.executeQuery();) {
                String previousUser = null;
                User user = null;
                List<String> accountList = new ArrayList<>();
                while (result.next()) {
                    if (!result.getString("user.username").equals(previousUser)) { // first time reading this username
                        if (user != null) { // we have already read a user, so we have to save the previous one
                            UserContacts.add(
                                    new Contact(
                                            user,
                                            accountList
                                    )
                            );
                        }
                        user = new User(
                                result.getInt("user.id"),
                                result.getString("user.username"),
                                result.getString("user.email"),
                                result.getString("user.name"),
                                result.getString("user.surname")
                        );
                        accountList = new ArrayList<>();
                        accountList.add(result.getString("account.code"));
                        previousUser = result.getString("user.username");
                    } else { // username not new, appending account code
                        accountList.add(result.getString("account.code"));
                    }
                }
                if (user != null) { // finally, if we have already read a user
                    UserContacts.add(
                            new Contact(
                                    user,
                                    accountList
                            )
                    );
                }
            }
        }
        return UserContacts;
    }

    public boolean isAccountInContacts(int userID, int accountID) throws SQLException {
        String query = "SELECT * FROM contact WHERE owner = ? AND element = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(userID));
            preparedStatement.setString(2, String.valueOf(accountID));
            try (ResultSet result = preparedStatement.executeQuery();) {
                return result.isBeforeFirst();
            }
        }
    }

    public void addContact (int owner, int element) throws SQLException {
        String query = "INSERT INTO contact (owner, element) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, String.valueOf(owner));
            preparedStatement.setString(2, String.valueOf(element));
            preparedStatement.executeUpdate();
        }
    }

}