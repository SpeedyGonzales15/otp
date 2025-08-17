package main.java.otp.api;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class HttpServerApp {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/admin/otp-config", new OTPConfigHandler());
        server.createContext("/admin/users", new AdminUsersHandler());
        server.createContext("/otp/generate", new OTPGenerateHandler());
        server.createContext("/otp/validate", new OTPValidateHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("HTTP Server started on port 8080");
    }
}
