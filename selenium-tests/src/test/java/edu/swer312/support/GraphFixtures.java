package edu.swer312.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonException;

/** Access to the graph JSON fixtures in {@code src/test/resources/graphs}. */
public final class GraphFixtures {

    private static final String FOLDER = "/graphs/";

    private GraphFixtures() {
    }

    /**
     * Copies a fixture to a temporary file, because a browser file input needs a real
     * path on disk. The caller deletes the file when the upload has finished.
     */
    public static Path copyToTempFile(String name) throws IOException {
        Path tempFile = Files.createTempFile("graph-" + name + "-", ".json");
        try (InputStream in = open(name)) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    /**
     * Number of nodes the application should show after loading this fixture, or
     * empty when the fixture is not a valid graph document (malformed JSON, missing
     * {@code graph.nodes}). In that case the application is expected to show an alert.
     */
    public static OptionalInt expectedNodeCount(String name) throws IOException {
        String text;
        try (InputStream in = open(name)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            Map<String, Object> document = new Json().toType(text, Json.MAP_TYPE);
            if (document != null
                    && document.get("graph") instanceof Map<?, ?> graph
                    && graph.get("nodes") instanceof List<?> nodes) {
                return OptionalInt.of(nodes.size());
            }
        } catch (JsonException malformed) {
            // Deliberately invalid fixture: fall through.
        }
        return OptionalInt.empty();
    }

    private static InputStream open(String name) {
        InputStream in = GraphFixtures.class.getResourceAsStream(FOLDER + name + ".json");
        if (in == null) {
            throw new IllegalArgumentException("Unknown graph fixture: " + name + ".json");
        }
        return in;
    }
}
