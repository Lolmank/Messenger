package messenger.model;

import java.time.LocalDateTime;
import messenger.exception.InvalidPasswordException;
import messenger.exception.InvalidUsernameException;

public class User {
    private int id;
    private String username;
    private String password;
    private LocalDateTime createdAt;

    public static final int MinUsernameLength = 3;
    public static final int MinPasswordLength = 8;

    // these functions will probably grow with more checks
    public static boolean UsernameIsValid(String username)
        throws InvalidUsernameException {
        if (username == null || username.length() < User.MinUsernameLength)
            return false;

        return true;
    }

    public static boolean PasswordIsValid(String password)
        throws InvalidPasswordException {
        if (password == null || password.length() < MinPasswordLength)
            return false;

        return true;
    }

    public User(int id, String username, String password, LocalDateTime createdAt)
            throws InvalidPasswordException {
        if (!UsernameIsValid(username))
            throw new InvalidUsernameException("Username too short!");
        if (!PasswordIsValid(password))
            throw new InvalidPasswordException("Password too short!");

        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}