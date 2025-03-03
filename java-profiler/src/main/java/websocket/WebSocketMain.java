package websocket;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

public class WebSocketMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        // Create a ContextHandler with the given context path.
        ContextHandler contextHandler = new ContextHandler("/ctx");
        server.setHandler(contextHandler);

        // Create a WebSocketUpgradeHandler that implicitly creates a ServerWebSocketContainer.
        WebSocketUpgradeHandler webSocketHandler = WebSocketUpgradeHandler.from(server, contextHandler, container ->
        {
            // Configure the ServerWebSocketContainer.
            container.setMaxTextMessageSize(128 * 1024);

            // Map a request URI to a WebSocket endpoint
            container.addMapping("/ws/run", (rq, rs, cb) -> new RunnerEndpoint());
        });
        contextHandler.setHandler(webSocketHandler);

        // Starting the Server will start the ContextHandler and the WebSocketUpgradeHandler,
        // which would run the configuration of the ServerWebSocketContainer.
        server.start();
        System.out.println("Jetty WebSocket Server started on ws://localhost:8080");
    }
}