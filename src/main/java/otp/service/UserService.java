package main.java.otp.service;

import main.java.otp.dao.UserDao;
import main.java.otp.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean usernameExists(String username) throws SQLException {
        return userDao.findByUsername(username) != null;
    }

    public boolean adminExists() throws SQLException {
        return userDao.findAdmin() != null;
    }

    public void registerUser(String username, String password, String role, String email, String phone, String telegramId) throws Exception {
        if (role.equals("ADMIN") && adminExists()) {
            throw new IllegalArgumentException("Admin already exists!");
        }
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists!");
        }
        String hash = hashPassword(password);
        User user = new User(null, username, hash, role, email, phone, telegramId);
        userDao.saveUser(user);
    }

    public User authenticate(String username, String password) throws Exception {
        User user = userDao.findByUsername(username);
        if (user == null) return null;
        if (!verifyPassword(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashed = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashed);
    }

    public boolean verifyPassword(String password, String hash) throws NoSuchAlgorithmException {
        return hashPassword(password).equals(hash);
    }
}
