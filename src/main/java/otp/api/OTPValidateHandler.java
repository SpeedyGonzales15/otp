package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.service.OTPService;
import main.java.otp.service.UserService;
import main.java.otp.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OTPValidateHandler implements HttpHandler {
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
            } catch (Exception ex) {
                sendResponse(exchange, 401, "Invalid or expired token");
                return;
            }

            Map<String, String> data = objectMapper.readValue(exchange.getRequestBody(), Map.class);
            String code = data.get("otpCode");
            if (code == null) {
                sendResponse(exchange, 400, "otpCode is required");
                return;
            }

            Long userId = userService.userDao.findByUsername(username).getId();

            boolean valid = otpService.validateOTP(userId, code);
            if (valid) {
                sendResponse(exchange, 200, "OTP valid");
            } else {
                sendResponse(exchange, 400, "Invalid or expired OTP");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try { sendResponse(exchange, 500, "Internal Server Error"); } catch (Exception ignored) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String msg) throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"message\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
}
