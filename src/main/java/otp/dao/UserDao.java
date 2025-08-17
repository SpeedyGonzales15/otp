package main.java.otp.dao;

import main.java.otp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, email, phone, telegram_id FROM users WHERE username = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("telegram_id"));
                }
                return null;
            }
        }
    }

    public User findAdmin() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, email, phone, telegram_id FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("telegram_id"));
                }
                return null;
            }
        }
    }

    public void saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, email, phone, telegram_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getTelegramId());
            ps.executeUpdate();
        }
    }

    public List<User> findAllNonAdminUsers() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, email, phone, telegram_id FROM users WHERE role != 'ADMIN'";
        List<User> users = new ArrayList<>();
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("telegram_id")));
            }
        }
        return users;
    }

    public void deleteUserById(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
