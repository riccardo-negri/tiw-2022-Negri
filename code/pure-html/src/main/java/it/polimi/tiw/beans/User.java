package it.polimi.tiw.beans;

public record User(
        int id,
        String username,
        String email,
        String name,
        String surname
) {
}