package it.polimi.tiw.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHashing {

    // Argon2Types.ARGON2i
    // salt 16 bytes
    // Hash length 32 bytes
    static final Argon2 argon2 = Argon2Factory.create(16, 32);

    public static String encode(String userPassword) {
        char[] password = userPassword.toCharArray();
        String encoded = argon2.hash(10, 65536, 1, password);
        argon2.wipeArray(password);
        return encoded;
    }

    public static boolean verify(String encoded, String userPassword) {
        char[] password = userPassword.toCharArray();
        boolean verified = argon2.verify(encoded, password);
        argon2.wipeArray(password);
        return verified;
    }
}
