package it.polimi.tiw.beans;
import java.sql.Timestamp;

public record Account(
        int id,
        String code,
        float balance,
        int user,
        String lastActivity
) {
}
