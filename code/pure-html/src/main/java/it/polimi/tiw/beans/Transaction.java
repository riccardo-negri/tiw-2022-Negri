package it.polimi.tiw.beans;

import java.sql.Timestamp;
import it.polimi.tiw.beans.User;
public record Transaction(
        int id,
        Timestamp timestamp,
        float amount, // TODO understand if this can be  int or a float
        String reason,
        String origin, // 12 char code
        User sender,
        String destination, // 12 char code
        User receiver
) {
}
