package edu.swer312.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.support.ui.FluentWait;

/**
 * Helpers for files the browser downloads. Every scenario gets its own download
 * folder, so parallel scenarios never see each other's files.
 */
public final class Downloads {

    /** Suffixes browsers use while a download is still in progress. */
    private static final List<String> PARTIAL_SUFFIXES = List.of(".crdownload", ".part", ".tmp");
    private static final Duration POLLING_INTERVAL = Duration.ofMillis(200);

    private Downloads() {
    }

    public static Path createFolder() throws IOException {
        return Files.createTempDirectory("downloads-");
    }

    /**
     * Waits until a completely downloaded, non-empty file matching the glob
     * (e.g. {@code graph.json} or {@code ai-search-*.png}) is present.
     */
    public static Path waitForFile(Path folder, String glob, Duration timeout) {
        return new FluentWait<>(folder)
                .withTimeout(timeout)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(UncheckedIOException.class)
                .withMessage(() -> "No downloaded file matching '" + glob + "' in " + folder
                        + ", found " + list(folder))
                .until(dir -> findFinished(dir, glob).orElse(null));
    }

    /**
     * Negative wait: true if no file appears in the folder during the whole period.
     * Implemented as a fluent wait for a file that is expected to time out (no sleep).
     */
    public static boolean staysEmpty(Path folder, Duration period) {
        try {
            new FluentWait<>(folder)
                    .withTimeout(period)
                    .pollingEvery(POLLING_INTERVAL)
                    .until(dir -> !list(dir).isEmpty());
            return false;
        } catch (TimeoutException stillEmpty) {
            return true;
        }
    }

    /** Reads a downloaded JSON file into nested maps and lists. */
    public static Map<String, Object> readJson(Path file) throws IOException {
        return new Json().toType(Files.readString(file), Json.MAP_TYPE);
    }

    /** @return the names of all files currently in the folder (finished or not). */
    public static List<String> list(Path folder) {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString()).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteFolder(Path folder) {
        if (folder == null || !Files.exists(folder)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(folder)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            // A browser may still hold a file open; a leftover temp folder is harmless.
        }
    }

    private static Optional<Path> findFinished(Path folder, String glob) {
        List<Path> matches = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, glob)) {
            stream.forEach(matches::add);
            for (Path file : matches) {
                String name = file.getFileName().toString();
                boolean partial = PARTIAL_SUFFIXES.stream().anyMatch(name::endsWith);
                if (!partial && Files.size(file) > 0) {
                    return Optional.of(file);
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
