package main.java.otp.model;

public class OTPConfig {
    private Long id;
    private Integer codeLength;
    private Integer expirationTimeSeconds;

    public OTPConfig(Long id, Integer codeLength, Integer expirationTimeSeconds) {
        this.id = id;
        this.codeLength = codeLength;
        this.expirationTimeSeconds = expirationTimeSeconds;
    }
    public Long getId() { return id; }
    public Integer getCodeLength() { return codeLength; }
    public Integer getExpirationTimeSeconds() { return expirationTimeSeconds; }
}
