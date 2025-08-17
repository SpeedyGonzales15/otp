package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.service.OTPService;
import main.java.otp.service.UserService;
import main.java.otp.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OTPGenerateHandler implements HttpHandler {
    private final OTPService otpService = new OTPService(new your.package.dao.OTPDao(), new your.package.dao.OTPConfigDao());
    private final UserService userService = new UserService(new your.package.dao.UserDao());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
            String token = authHeader.substring(7);
            String username;
            try {
                username = JWTUtil.getUsernameFromToken(token);
                String role = JWTUtil.getRoleFromToken(token);
                if ("ADMIN".equals(role)) {
                    sendResponse(exchange, 403, "Forbidden: Admins do not generate OTP");
                    return;
                }
            } catch (Exception ex) {
                sendResponse(exchange, 401, "Invalid or expired token");
                return;
            }

            Map<String, String> data = objectMapper.readValue(exchange.getRequestBody(), Map.class);

            String operationId = data.get("operationId");
            String channel = data.get("channel"); // SMS, EMAIL, TELEGRAM, FILE

            if (operationId == null || channel == null) {
                sendResponse(exchange, 400, "operationId and channel required");
                return;
            }

            Long userId = userService.userDao.findByUsername(username).getId();

            var otp = otpService.generateOTP(userId, operationId);

            // Эмуляция рассылки
            switch (channel.toUpperCase()) {
                case "SMS" -> System.out.println("SMS to user " + username + ": OTP = " + otp.getCode());
                case "EMAIL" -> System.out.println("EMAIL to user " + username + ": OTP = " + otp.getCode());
                case "TELEGRAM" -> System.out.println("TELEGRAM to user " + username + ": OTP = " + otp.getCode());
                case "FILE" -> {
                    try (var writer = new FileWriter("otp_codes.txt", true)) {
                        writer.write("User " + username + ", OTP: " + otp.getCode() + "\n");
                    }
                }
                default -> {
                    sendResponse(exchange, 400, "Invalid channel");
                    return;
                }
            }

            sendResponse(exchange, 200, "OTP generated and sent");

        } catch (Exception e) {
            e.printStackTrace();
            try { sendResponse(exchange, 500, "Internal Server Error"); } catch (Exception ignored) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String msg) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"message\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
}
