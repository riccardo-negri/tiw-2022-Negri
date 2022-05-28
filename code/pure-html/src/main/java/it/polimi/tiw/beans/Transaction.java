package it.polimi.tiw.beans;

import java.sql.Timestamp;
import it.polimi.tiw.beans.User;
public record Transaction(
        int id,
        Timestamp timestamp,
        float amount,
        String reason,
        String origin,
        User sender,
        String destination,
        User receiver
) {
}
