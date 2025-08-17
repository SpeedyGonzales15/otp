package main.java.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.otp.dao.UserDao;
import main.java.otp.dao.OTPDao;
import main.java.otp.util.JWTUtil;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminUserDeleteHandler implements HttpHandler {
    private final UserDao userDao = new UserDao();
    private final OTPDao otpDao = new OTPDao();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"DELETE".equals(exchange.getRequestMethod())) {
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

            String path = exchange.getRequestURI().getPath();
            String[] splits = path.split("/");
            if (splits.length < 4) { // /admin/users/{id}
                sendResponse(exchange, 400, "User ID missing");
                return;
            }
            Long userId = Long.parseLong(splits[3]);

            otpDao.deleteOTPsByUserId(userId);
            userDao.deleteUserById(userId);

            sendResponse(exchange, 200, "User and OTPs deleted");
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
