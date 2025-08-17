package main.java.otp.util;

import java.util.Date;

import io.jsonwebtoken.*;

public class JWTUtil {
    private static final String SECRET_KEY = "YourVeryStrongSecretKeyChangeMe!";

    public static String generateToken(String username, String role, long expirationMillis) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Jws<Claims> validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token);
    }

    public static String getUsernameFromToken(String token) {
        return validateToken(token).getBody().getSubject();
    }

    public static String getRoleFromToken(String token) {
        return (String) validateToken(token).getBody().get("role");
    }
}
