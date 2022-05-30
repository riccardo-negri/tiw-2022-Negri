package it.polimi.tiw.beans;

import java.sql.Timestamp;
import java.util.List;

import it.polimi.tiw.beans.User;

public record Contact(
        User user,
        List<String> accountList
) {
}
