package messenger.db;

import messenger.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {
    private Database db;

    public RoomDao(Database db) {
        this.db = db;
    }

    public Room findByName(String name) throws SQLException {
        if (!Room.NameIsValid(name))
            return null;

        String sql = "SELECT id, name, created_at FROM rooms WHERE name = ?";

        //TODO: possible todo, take this out into a separate function with
        // varargs for the parameters to reduce WET. same w/ other DAOs
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Room create(String name) throws SQLException {
        String sql = "INSERT INTO rooms (name, created_at) VALUES (?, ?)";
        LocalDateTime createdAt = LocalDateTime.now();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setTimestamp(2, Timestamp.valueOf(createdAt));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Room(keys.getInt(1), name, createdAt);
            }
        }
    }

    public Room findOrCreate(String name) throws SQLException {
        if (!Room.NameIsValid(name))
            return null;

        Room room = findByName(name);
        if (room != null)
            return room;

        try {
            return create(name);
        } catch (SQLIntegrityConstraintViolationException e) {
            // another client created the same room between our find and create
            return findByName(name);
        }
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT id, name, created_at FROM rooms ORDER BY name ASC";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        }
        return rooms;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}