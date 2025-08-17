package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.service.UserService;
import main.java.otp.dao.UserDao;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisterHandler implements HttpHandler {
    private final UserService userService = new UserService(new UserDao());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            InputStream is = exchange.getRequestBody();
            Map<String, String> data = objectMapper.readValue(is, Map.class);

            String username = data.get("username");
            String password = data.get("password");
            String role = data.getOrDefault("role", "USER").toUpperCase();
            String email = data.get("email");
            String phone = data.get("phone");
            String telegramId = data.get("telegramId");

            if (!role.equals("ADMIN") && !role.equals("USER")) {
                sendResponse(exchange, 400, "Invalid role");
                return;
            }

            userService.registerUser(username, password, role, email, phone, telegramId);

            sendResponse(exchange, 201, "User registered successfully");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
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
