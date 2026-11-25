package messenger.db;

import messenger.exception.UsernameTakenException;
import messenger.exception.InvalidUsernameException;
import messenger.exception.InvalidPasswordException;
import messenger.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDao {
    private Database db;

    public UserDao(Database db) {
        this.db = db;
    }

    //TODO: instead of writing out the queries inside strings,
    // make procedures in MySQL and call them directly
    public User register(String username, String password)
            throws SQLException, UsernameTakenException, InvalidPasswordException {
        if (!User.UsernameIsValid(username))
            throw new InvalidUsernameException("Username too short!");
        if (!User.PasswordIsValid(password))
            throw new InvalidPasswordException("Password too short!");

        String sql = "INSERT INTO users (username, password, created_at) VALUES (?, ?, ?)";
        LocalDateTime createdAt = LocalDateTime.now();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setTimestamp(3, Timestamp.valueOf(createdAt));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), username, password, createdAt);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // TODO: check e if it's because of a duplicate username
            throw new UsernameTakenException(username);
        }
    }

    public User findByUsername(String username) throws SQLException {
        if (!User.UsernameIsValid(username))
            return null;

        String sql = "SELECT id, username, password, created_at FROM users WHERE username = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, created_at FROM users WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User checkLogin(String username, String password) throws SQLException {
        User user = findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}