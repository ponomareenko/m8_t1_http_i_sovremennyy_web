package ru.netology;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public class Request {
    private final String method;
    private final String path;
    private final InputStream body;
    private final Map<String, String> headers;

    public Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public InputStream getBody() {
        return body;
    }

    public String getHeader(String title) {
        return headers.get(title);
    }
}
