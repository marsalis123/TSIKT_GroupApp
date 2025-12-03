package com.example.test_1;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class NotificationWebSocketClient {

    public interface Listener {
        void onNotification(String type, String message, int groupId);
    }

    private WebSocket webSocket;

    public NotificationWebSocketClient(Listener listener) {
        HttpClient client = HttpClient.newHttpClient();
        this.webSocket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:8080/ws-notify"),
                        new WebSocket.Listener() {
                            @Override
                            public void onOpen(WebSocket webSocket) {
                                System.out.println("WS OPEN");
                                webSocket.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                System.out.println("WS MSG: " + data);
                                String json = data.toString();

                                String type = extractString(json, "\"type\":\"");
                                String msg  = extractString(json, "\"message\":\"");
                                int groupId = extractInt(json, "\"groupId\":");

                                listener.onNotification(type, msg, groupId);

                                webSocket.request(1);
                                return null;
                            }
                        }
                ).join();
    }

    // Použi rovnakú implementáciu ako v UserManageri
    private static String extractString(String json, String key) {
        int i = json.indexOf(key);
        if (i < 0) return "";
        i += key.length();
        int j = json.indexOf("\"", i);
        if (j < 0) return "";
        return json.substring(i, j);
    }

    private static int extractInt(String json, String key) {
        int i = json.indexOf(key);
        if (i < 0) return 0;
        i += key.length();
        int j = i;
        while (j < json.length() && Character.isDigit(json.charAt(j))) j++;
        try {
            return Integer.parseInt(json.substring(i, j));
        } catch (Exception e) {
            return 0;
        }
    }
}
