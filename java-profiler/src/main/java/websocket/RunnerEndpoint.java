package websocket;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import profiler.Runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

@WebSocket
public class RunnerEndpoint {

    @OnWebSocketOpen
    public void onConnect(Session session) {
        session.setIdleTimeout(Duration.ofSeconds(60));
        System.out.println("CLIENT CONNECTED: " + session.getRemoteSocketAddress().toString());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        String trimmedMsg = message.trim();
        System.out.println("MESSAGE RECEIVED: {" + trimmedMsg + "}");

        Optional<String> errorMsg = validateRunnableJava(trimmedMsg);

        if (errorMsg.isPresent()) {
            System.out.println("RESPONDING: {" + trimmedMsg + "} is invalid (" + errorMsg.get() + ")");
            session.sendText(errorMsg.get(), Callback.from(session::demand, Throwable::printStackTrace));
        } else {
            System.out.println("{" + trimmedMsg + "} is valid, proceeding to compile and run");
            Runner.compileAndRunJavaFile(session, Paths.get(trimmedMsg));
            System.out.println("RESPONDING COMPLETE");
            session.sendText("Complete", Callback.from(session::demand, Throwable::printStackTrace));
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("CONNECTION CLOSED (code " + statusCode + "): " + reason);
    }

    @OnWebSocketError
    public void onError(Throwable error) {
        System.err.println("WEBSOCKET ERROR: " + error.getMessage());
    }

    // Validates input is a .java file
    private Optional<String> validateRunnableJava(String filePath) {
        System.out.println("Validating {" + filePath + "} is a java file");
        Path file = Paths.get(filePath);

        if (Files.notExists(file)) {
            return Optional.of(String.format("File {%s} does not exist", filePath));
        } else if (!file.getFileName().toString().endsWith(".java")) {
            return Optional.of(String.format("File {%s} is not a .java file", file.getFileName()));
        }

        return Optional.empty();
    }
}