package main.java.otp.service;

import main.java.otp.dao.OTPDao;
import main.java.otp.dao.OTPConfigDao;
import main.java.otp.model.OTP;
import main.java.otp.model.OTPConfig;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

public class OTPService {
    private final OTPDao otpDao;
    private final OTPConfigDao configDao;

    public OTPService(OTPDao otpDao, OTPConfigDao configDao) {
        this.otpDao = otpDao;
        this.configDao = configDao;
    }

    public OTP generateOTP(Long userId, String operationId) throws SQLException {
        OTPConfig config = configDao.fetchConfig();
        String code = generateCode(config.getCodeLength());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusSeconds(config.getExpirationTimeSeconds());
        OTP otp = new OTP(null, userId, operationId, code, "ACTIVE", now, expiry);
        otpDao.saveOTP(otp);
        return otp;
    }

    public boolean validateOTP(Long userId, String code) throws SQLException {
        OTP otp = otpDao.findByCodeAndUser(code, userId);
        if (otp == null) return false;
        if (otp.getStatus().equals("ACTIVE") && otp.getExpiredAt().isAfter(LocalDateTime.now())) {
            otpDao.updateOTPStatus(otp.getId(), "USED");
            return true;
        } else {
            return false;
        }
    }

    public void expireOTPs() throws SQLException {
        var otps = otpDao.findAllActiveOTPs();
        LocalDateTime now = LocalDateTime.now();
        for (OTP otp : otps) {
            if (otp.getExpiredAt().isBefore(now)) {
                otpDao.updateOTPStatus(otp.getId(), "EXPIRED");
            }
        }
    }

    private String generateCode(int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }
}
