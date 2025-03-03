package profiler;

import jdk.jfr.consumer.*;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.nio.file.Path;

public class JFRReader {
    public static void parseJFRFile(Session session, Path jfrFile) throws IOException {
        try (EventStream eventStream = EventStream.openFile(jfrFile)) {

            // Capture Execution Samples with Line Numbers
            eventStream.onEvent("jdk.Compilation", event -> {
                RecordedMethod method = event.getValue("method");
                if (method != null) {
                    String className = method.getType().getName();
                    String methodName = method.getName();
                    System.out.println("JIT compiled: " + className + "." + methodName);
                }
            });

            eventStream.start();
        }
    }
}
