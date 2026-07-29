package storage;

import domain.*;
import enums.BookingStatus;
import enums.InstrumentType;
import enums.ReturnCondition;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class JdbcStorage {
    private final String url;
    private final String user;
    private final String password;

    public JdbcStorage() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new IllegalStateException("Файл db.properties не найден в src/main/resources");
            Properties props = new Properties();
            props.load(in);
            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки конфигурации БД: " + e.getMessage(), e);
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean testConnection() {
        try (Connection conn = connect()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("[DB] " + DbErrorHandler.translate(e));
            return false;
        }
    }


    public List<Instrument> loadAllInstruments() throws SQLException {
        List<Instrument> list = new ArrayList<>();
        String sql = "SELECT id, type FROM instruments";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Instrument(
                        rs.getLong("id"), InstrumentType.valueOf(rs.getString("type"))
                ));
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
        return list;
    }

    public long insertInstrument(Instrument i) throws SQLException {
        String sql = "INSERT INTO instruments(type) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, i.getType().name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("БД не вернула сгенерированный ID для прибора");
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Booking> loadAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, instrument_id, start_at, end_at, status, owner_id, created_at, updated_at FROM bookings";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Booking(
                        rs.getLong("id"),
                        rs.getLong("instrument_id"),
                        rs.getTimestamp("start_at").toInstant(),
                        rs.getTimestamp("end_at").toInstant(),
                        rs.getLong("owner_id"),
                        BookingStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()
                ));
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
        return list;
    }

    public long insertBooking(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings(instrument_id, start_at, end_at, status, " +
                "owner_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, b.getInstrumentId());
            stmt.setTimestamp(2, java.sql.Timestamp.from(b.getStartAt()));
            stmt.setTimestamp(3, java.sql.Timestamp.from(b.getEndAt()));
            stmt.setString(4, b.getStatus().name());
            stmt.setLong(5, b.getOwnerUserId());
            stmt.setTimestamp(6, java.sql.Timestamp.from(b.getCreatedAt()));
            stmt.setTimestamp(7, java.sql.Timestamp.from(b.getUpdatedAt()));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("БД не вернула сгенерированный ID для брони");
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void updateBooking(Booking b) throws SQLException {
        String sql = "UPDATE bookings SET start_at=?, end_at=?, status=?, updated_at=? WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.from(b.getStartAt()));
            stmt.setTimestamp(2, java.sql.Timestamp.from(b.getEndAt()));
            stmt.setString(3, b.getStatus().name());
            stmt.setTimestamp(4, java.sql.Timestamp.from(b.getUpdatedAt()));
            stmt.setLong(5, b.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Checkout> loadAllCheckouts() throws SQLException {
        List<Checkout> list = new ArrayList<>();
        String sql = "SELECT id, instrument_id, userId, comment, taken_at, returned_at, return_condition, ownerUsername, created_at FROM checkouts";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ReturnCondition cond = null;
                String condStr = rs.getString("return_condition");
                if (condStr != null) cond = ReturnCondition.valueOf(condStr);

                list.add(new Checkout(
                        rs.getLong("id"),
                        rs.getLong("instrument_id"),
                        rs.getLong("userId"),
                        rs.getString("comment"),
                        rs.getTimestamp("taken_at").toInstant(),
                        rs.getTimestamp("returned_at") != null ? rs.getTimestamp("returned_at").toInstant() : null,
                        cond,
                        rs.getString("ownerUsername"),
                        rs.getTimestamp("created_at").toInstant()
                ));
            }
        } catch (SQLException e) { throw dbError(e); }
        return list;
    }

    public long insertCheckout(Checkout c) throws SQLException {
        String sql = "INSERT INTO checkouts(instrument_id, userId, comment, taken_at, returned_at, return_condition, ownerUsername, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, c.getInstrumentId());
            stmt.setLong(2, c.getUserId());
            stmt.setString(3, c.getComment());
            stmt.setTimestamp(4, java.sql.Timestamp.from(c.getTakenAt()));
            if (c.getReturnedAt() != null) stmt.setTimestamp(5, java.sql.Timestamp.from(c.getReturnedAt()));
            else stmt.setNull(5, Types.TIMESTAMP);
            stmt.setString(6, c.getReturnCondition() != null ? c.getReturnCondition().name() : null);
            stmt.setString(7, c.getOwnerUsername() != null ? c.getOwnerUsername() : "SYSTEM");
            stmt.setTimestamp(8, java.sql.Timestamp.from(c.getCreatedAt()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("БД не вернула ID для выдачи");
            }
        } catch (SQLException e) { throw dbError(e); }
    }

    public void updateCheckout(Checkout c) throws SQLException {
        String sql = "UPDATE checkouts SET returned_at=?, return_condition=? WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (c.getReturnedAt() != null) stmt.setTimestamp(1, java.sql.Timestamp.from(c.getReturnedAt()));
            else stmt.setNull(1, Types.TIMESTAMP);
            stmt.setString(2, c.getReturnCondition() != null ? c.getReturnCondition().name() : null);
            stmt.setLong(3, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { throw dbError(e); }
    }


    private RuntimeException dbError(SQLException e) {
        return new RuntimeException(DbErrorHandler.translate(e), e);
    }

    public List<User> loadAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, login, password_hash, created_at FROM users";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                        rs.getLong("id"),
                        rs.getString("login"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at").toInstant()
                ));
            }
        } catch (SQLException e) { throw dbError(e); }
        return list;
    }

    public long insertUser(User u) throws SQLException {
        String sql = "INSERT INTO users(login, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getLogin());
            stmt.setString(2, u.getPassword());
            stmt.setTimestamp(3, java.sql.Timestamp.from(u.getCreatedAt()));            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("БД не вернула ID для пользователя");
            }
        } catch (SQLException e) { throw dbError(e); }
    }
}