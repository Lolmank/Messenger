package messenger.db;

import messenger.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: take a look at this class. it's 12 AM i'm not doing it now

public class MessageDao {
    private static final String SELECT_WITH_SENDER =
            "SELECT m.id, m.sender_id, u.username, m.room_id, m.receiver_id, m.text, m.sent_at "
                    + "FROM messages m JOIN users u ON u.id = m.sender_id ";

    private Database db;

    public MessageDao(Database db) {
        this.db = db;
    }

    public Message saveRoomMessage(int senderId, int roomId, String text) throws SQLException {
        return save(senderId, roomId, null, text);
    }

    public Message savePrivateMessage(int senderId, int receiverId, String text) throws SQLException {
        return save(senderId, null, receiverId, text);
    }

    public Message findById(int id) throws SQLException {
        String sql = SELECT_WITH_SENDER + "WHERE m.id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Message> findLastInRoom(int roomId, int limit) throws SQLException {
        String sql = SELECT_WITH_SENDER + "WHERE m.room_id = ? ORDER BY m.id DESC LIMIT ?";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        }
        // query returns newest first, the spec wants oldest first
        Collections.reverse(messages);
        return messages;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM messages";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Message save(int senderId, Integer roomId, Integer receiverId, String text)
            throws SQLException {
        String sql = "INSERT INTO messages (sender_id, room_id, receiver_id, text, sent_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        int newId;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, senderId);
            setNullableInt(ps, 2, roomId);
            setNullableInt(ps, 3, receiverId);
            ps.setString(4, text);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                newId = keys.getInt(1);
            }
        }
        // read it back so the result is exactly as stored, including senderName
        return findById(newId);
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null)
            ps.setNull(index, Types.INTEGER);
        else
            ps.setInt(index, value);
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getString("username"),
                rs.getObject("room_id", Integer.class),
                rs.getObject("receiver_id", Integer.class),
                rs.getString("text"),
                rs.getTimestamp("sent_at").toLocalDateTime());
    }
}