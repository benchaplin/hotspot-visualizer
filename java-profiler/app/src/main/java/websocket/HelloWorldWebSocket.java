package websocket;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class HelloWorldWebSocket {

    @OnWebSocketOpen
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session.getRemoteSocketAddress().toString());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);
        session.sendText("Hello, World!", Callback.from(() -> {
            System.out.println("Responded: Hello, World!");
            session.demand();
        }, Throwable::printStackTrace));
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Connection closed (code " + statusCode + "): " + reason);
    }

    @OnWebSocketError
    public void onError(Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }
}