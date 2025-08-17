package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.dao.UserDao;
import main.java.otp.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUsersHandler implements HttpHandler {
    private final UserDao userDao = new UserDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

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

            List<?> users = userDao.findAllNonAdminUsers().stream()
                    .map(u -> {
                        return new Object() {
                            public final Long id = u.getId();
                            public final String username = u.getUsername();
                            public final String email = u.getEmail();
                        };
                    }).collect(Collectors.toList());

            String responseJson = objectMapper.writeValueAsString(users);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
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
