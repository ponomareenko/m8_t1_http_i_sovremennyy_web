package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port = 9999;

    private final Map<String, Handler> getHandlers = new ConcurrentHashMap<>();
    private final Map<String, Handler> postHandlers = new ConcurrentHashMap<>();

    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html",
            "/events.js");

    private final ExecutorService executorService = Executors.newFixedThreadPool(64);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket  = serverSocket.accept();
                executorService.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection(Socket socket) {
        System.out.println("Новый поток");

        try (
//                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = readLine(in);
            if (requestLine == null) {
                return;
            }
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            String methodRequest = parts[0];
            String pathRequest = parts[1];

            Map<String, String> headers = new HashMap<>();
            while (true) {
                String line = readLine(in);
                if (line == null || line.isEmpty()) {
                    break;
                }

                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    headers.put(line.substring(0, colonIndex),
                            line.substring(colonIndex + 1).trim());
                }
            }

            String contentLengthHeader = headers.get("Content-Length");
            InputStream body = InputStream.nullInputStream();
            if (contentLengthHeader != null) {
                int contentLength = Integer.parseInt(contentLengthHeader);

                byte[] bodyBytes = in.readNBytes(contentLength);

                body = new ByteArrayInputStream(bodyBytes);
            }


            Request request = new Request(methodRequest, pathRequest, headers, body);

            Handler handler = findHandler(request);
            if (handler != null) {
                handler.handle(request, out);
                return;
            }

            final var path = pathRequest;
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic // TODO: потом включить
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if ("GET".equals(method)) {
            getHandlers.put(path, handler);
        } else if ("POST".equals(method)) {
            postHandlers.put(path, handler);
        } else {
            System.out.println("Не известный метод!");
        }
    }

    public Handler findHandler(Request request) {
        // ТЕСТ прочитать из getHandlers реализацию
        if (request.getMethod().equals("GET")) {
            if (getHandlers.containsKey(request.getPath())) {
                return getHandlers.get(request.getPath());
            }
        } else if (request.getMethod().equals("POST")) {
            if (postHandlers.containsKey(request.getPath())) {
                return postHandlers.get(request.getPath());
            }
        }
        return null;
    }

    private String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int currentByte;

        while ((currentByte = in.read()) != -1) {
            if (currentByte == '\n') {
                break;
            }

            buffer.write(currentByte);
        }

        if (currentByte == -1 && buffer.size() == 0) {
            return null;
        }

        byte[] lineBytes = buffer.toByteArray();
        int length = lineBytes.length;

        // Убираем \r, который находится перед \n
        if (length > 0 && lineBytes[length - 1] == '\r') {
            length--;
        }

        return new String(
                lineBytes,
                0,
                length,
                StandardCharsets.ISO_8859_1
        );
    }
}
