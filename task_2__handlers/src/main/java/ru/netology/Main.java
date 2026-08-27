package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/messages", new Handler() {
            @Override
            public void handle(
                    Request request,
                    BufferedOutputStream responseStream
            ) {
                try {
                    byte[] content = "Messages handler works"
                            .getBytes(StandardCharsets.UTF_8);

                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes(StandardCharsets.UTF_8));

                    responseStream.write(content);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.addHandler("POST", "/messages", new Handler() {
            @Override
            public void handle(
                    Request request,
                    BufferedOutputStream responseStream
            ) {
                try {
                    byte[] requestBody = request.getBody().readAllBytes();

                    System.out.println(
                            "Получено тело: " +
                                    new String(requestBody, StandardCharsets.UTF_8)
                    );

                    byte[] responseBody = "POST handler works"
                            .getBytes(StandardCharsets.UTF_8);

                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                                    "Content-Length: " + responseBody.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes(StandardCharsets.UTF_8));

                    responseStream.write(responseBody);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.addHandler("GET", "/hello", new Handler() {
            @Override
            public void handle(
                    Request request,
                    BufferedOutputStream responseStream
            ) {
                try {
                    byte[] content = "Hello from server!"
                            .getBytes(StandardCharsets.UTF_8);

                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes(StandardCharsets.UTF_8));

                    responseStream.write(content);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.start();

//        nc localhost 9999
//        http://localhost:9999/index.html
//        http://localhost:9999/classic.html
    }
}
