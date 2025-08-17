package main.java.otp.model;

import java.time.LocalDateTime;

public class OTP {
    private Long id;
    private Long userId;
    private String operationId;
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    public OTP(Long id, Long userId, String operationId, String code, String status, LocalDateTime createdAt, LocalDateTime expiredAt) {
        this.id = id;
        this.userId = userId;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getOperationId() { return operationId; }
    public String getCode() { return code; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}
