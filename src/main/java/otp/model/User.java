package main.java.otp.model;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String email;
    private String phone;
    private String telegramId;

    public User(Long id, String username, String passwordHash, String role, String email, String phone, String telegramId) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.telegramId = telegramId;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getTelegramId() { return telegramId; }
}
