package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.dao.OTPConfigDao;
import main.java.otp.model.OTPConfig;
import main.java.otp.util.JWTUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OTPConfigHandler implements HttpHandler {
    private final OTPConfigDao otpConfigDao = new OTPConfigDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();

           String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
            String token = authHeader.substring(7);
            String role;
            try {
                role = JWTUtil.getRoleFromToken(token);
                if (!"ADMIN".equals(role)) {
                    sendResponse(exchange, 403, "Forbidden: Admins only");
                    return;
                }
            } catch (Exception ex) {
                sendResponse(exchange, 401, "Invalid or expired token");
                return;
            }

            if ("GET".equals(method)) {
                OTPConfig config = otpConfigDao.fetchConfig();
                if (config == null) {
                    sendResponse(exchange, 404, "OTP Config not found");
                    return;
                }
                String responseJson = objectMapper.writeValueAsString(Map.of(
                        "codeLength", config.getCodeLength(),
                        "expirationTimeSeconds", config.getExpirationTimeSeconds()
                ));
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else if ("PUT".equals(method)) {
                InputStream is = exchange.getRequestBody();
                Map<String, Integer> updateData = objectMapper.readValue(is, Map.class);
                Integer newCodeLength = updateData.get("codeLength");
                Integer newExpiration = updateData.get("expirationTimeSeconds");

                OTPConfig config = otpConfigDao.fetchConfig();
                if (config == null) {
                    sendResponse(exchange, 404, "OTP Config not found");
                    return;
                }

                if (newCodeLength != null) config = new OTPConfig(config.getId(), newCodeLength, config.getExpirationTimeSeconds());
                if (newExpiration != null) config = new OTPConfig(config.getId(), config.getCodeLength(), newExpiration);

                otpConfigDao.updateConfig(config);
                sendResponse(exchange, 200, "OTP Config updated");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendResponse(exchange, 500, "Internal Server Error");
            } catch (Exception ignored) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String msg) throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"message\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
}
