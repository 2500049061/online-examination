import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ExamServer {
    private static final int[][] ANSWERS = {{1}, {0}, {2}, {1}, {3}};
    private static final Path PUBLIC_DIR = Paths.get("public").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/login", ExamServer::login);
        server.createContext("/api/submit", ExamServer::submit);
        server.createContext("/", ExamServer::staticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Online Examination running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void login(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "{\"error\":\"POST required\"}", "application/json");
            return;
        }
        String body = readBody(ex);
        String user = jsonString(body, "username");
        String pass = jsonString(body, "password");
        boolean ok = "student".equals(user) && "1234".equals(pass);
        send(ex, ok ? 200 : 401,
            ok ? "{\"success\":true,\"name\":\"Student\"}" : "{\"success\":false,\"message\":\"Invalid username or password\"}",
            "application/json");
    }

    private static void submit(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "{\"error\":\"POST required\"}", "application/json");
            return;
        }
        String body = readBody(ex);
        Matcher m = Pattern.compile("\"answers\"\\s*:\\s*\\[([^]]*)\\]").matcher(body);
        int score = 0;
        if (m.find()) {
            Matcher nums = Pattern.compile("-?\\d+").matcher(m.group(1));
            int i = 0;
            while (nums.find() && i < ANSWERS.length) {
                int chosen = Integer.parseInt(nums.group());
                if (chosen == ANSWERS[i][0]) score++;
                i++;
            }
        }
        int total = ANSWERS.length;
        int percent = score * 100 / total;
        String result = "{\"score\":" + score + ",\"total\":" + total +
            ",\"percentage\":" + percent + ",\"status\":\"" + (percent >= 40 ? "Pass" : "Keep practicing") + "\"}";
        send(ex, 200, result, "application/json");
    }

    private static void staticFiles(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if ("/".equals(path)) path = "/index.html";
        Path file = PUBLIC_DIR.resolve(path.substring(1)).normalize();
        if (!file.startsWith(PUBLIC_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
            send(ex, 404, "404 - File not found", "text/plain; charset=utf-8");
            return;
        }
        String type = Files.probeContentType(file);
        if (type == null) type = "application/octet-stream";
        byte[] bytes = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type + (type.startsWith("text/") ? "; charset=utf-8" : ""));
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(bytes); }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String jsonString(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static void send(HttpExchange ex, int status, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(bytes); }
    }
}
