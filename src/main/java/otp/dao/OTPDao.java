package main.java.otp.dao;

import main.java.otp.model.OTP;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OTPDao {
    public void saveOTP(OTP otp) throws SQLException {
        String sql = "INSERT INTO otp (user_id, operation_id, code, status, created_at, expired_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, otp.getUserId());
            ps.setString(2, otp.getOperationId());
            ps.setString(3, otp.getCode());
            ps.setString(4, otp.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(otp.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(otp.getExpiredAt()));
            ps.executeUpdate();
        }
    }

    public OTP findByCodeAndUser(String code, Long userId) throws SQLException {
        String sql = "SELECT id, user_id, operation_id, code, status, created_at, expired_at FROM otp WHERE code = ? AND user_id = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OTP(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("operation_id"),
                            rs.getString("code"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("expired_at").toLocalDateTime()
                    );
                }
                return null;
            }
        }
    }

    public List<OTP> findAllActiveOTPs() throws SQLException {
        String sql = "SELECT id, user_id, operation_id, code, status, created_at, expired_at FROM otp WHERE status = 'ACTIVE'";
        List<OTP> otps = new ArrayList<>();
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                otps.add(new OTP(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("operation_id"),
                        rs.getString("code"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expired_at").toLocalDateTime()
                ));
            }
        }
        return otps;
    }

    public void updateOTPStatus(Long id, String status) throws SQLException {
        String sql = "UPDATE otp SET status = ? WHERE id = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteOTPsByUserId(Long userId) throws SQLException {
        String sql = "DELETE FROM otp WHERE user_id = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
}
