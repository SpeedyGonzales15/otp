package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.service.UserService;
import main.java.otp.dao.UserDao;
import main.java.otp.model.User;
import main.java.otp.util.JWTUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginHandler implements HttpHandler {
    private final UserService userService = new UserService(new UserDao());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 минут

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            Map<String, String> data = objectMapper.readValue(is, Map.class);

            String username = data.get("username");
            String password = data.get("password");

            User user = userService.authenticate(username, password);

            if (user == null) {
                sendResponse(exchange, 401, "Invalid username or password");
                return;
            }

            String token = JWTUtil.generateToken(user.getUsername(), user.getRole(), TOKEN_EXPIRATION_MS);

            String jsonResp = objectMapper.writeValueAsString(Map.of("token", token));

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] bytes = jsonResp.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendResponse(exchange, 500, "Internal Server Error");
            } catch (Exception ignored) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"message\":\"" + response + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
