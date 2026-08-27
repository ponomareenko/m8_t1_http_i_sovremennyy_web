package ru.netology;

public class Main {
    public static void main(String[] args) {
        int port = 9999;
        Server server = new Server(port);
        server.start();

//        nc localhost 9999
//        http://localhost:9999/index.html
//        http://localhost:9999/classic.html
    }
}
