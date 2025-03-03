package profiler;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Runner {
    public static void compileAndRunJavaFile(Session session, Path javaFile) {
        try {
            // Compile the Java file
            System.out.println("Compiling java file: " + javaFile.getFileName());
            Process compileProcess =
                    new ProcessBuilder("javac", javaFile.toAbsolutePath().toString()).start();
            String compileErrors = readProcessErrorStream(compileProcess);

            if (!compileProcess.waitFor(10, TimeUnit.SECONDS)) {
                session.sendText("ERROR: Compilation timed out", Callback.NOOP);
                compileProcess.destroy();
                return;
            }

            if (compileProcess.exitValue() != 0) {
                session.sendText("ERROR: Compilation failed:\n" + compileErrors, Callback.NOOP);
                return;
            }

            // Run the compiled Java program with JFR
            String className = javaFile.getFileName().toString().replace(".java", "");
            String jfrFileName = "hotspot.jfr";
            Path jfrFile = Paths.get(jfrFileName);

            System.out.println("Running the java file with JFR...");
            System.out.println("    (command: java -XX:+FlightRecorder -XX:StartFlightRecording=filename="
                            + jfrFile.toAbsolutePath() + " " + className + ")");
            Process runProcess = new ProcessBuilder(
                    "java", "-XX:+FlightRecorder",
                    "-XX:StartFlightRecording:filename=" + jfrFile.toAbsolutePath() + ",settings=profile",
                    className
            ).start();

            // TODO: Set timeout for execution?

            // Parse the JFR file and send messages for hotspot events
            JFRReader.parseJFRFile(session, jfrFile);
        } catch (Exception e) {
            System.out.println("Error compiling/running: " + e.getMessage());
            session.sendText("ERROR: " + e.getMessage(), Callback.NOOP);
        }
    }

    private static String readProcessErrorStream(Process process) {
        try (InputStream errorStream = process.getErrorStream()) {
            return new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Failed to read error stream: " + e.getMessage();
        }
    }
}
