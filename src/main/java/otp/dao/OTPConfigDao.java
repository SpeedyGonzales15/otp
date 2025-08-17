package main.java.otp.dao;

import main.java.otp.model.OTPConfig;
import java.sql.*;

public class OTPConfigDao {
    public OTPConfig fetchConfig() throws SQLException {
        String sql = "SELECT id, code_length, expiration_time_seconds FROM otp_config LIMIT 1";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new OTPConfig(
                        rs.getLong("id"),
                        rs.getInt("code_length"),
                        rs.getInt("expiration_time_seconds")
                );
            }
            return null;
        }
    }

    public void updateConfig(OTPConfig config) throws SQLException {
        String sql = "UPDATE otp_config SET code_length = ?, expiration_time_seconds = ? WHERE id = ?";
        try (Connection conn = DataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, config.getCodeLength());
            ps.setInt(2, config.getExpirationTimeSeconds());
            ps.setLong(3, config.getId());
            ps.executeUpdate();
        }
    }
}
