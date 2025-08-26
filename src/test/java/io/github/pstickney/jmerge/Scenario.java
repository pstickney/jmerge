package io.github.pstickney.jmerge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    private String folder;
    private String base;
    private String overlay;
    private String expected;
    private String config;

    public static String readResource(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    public static Stream<Scenario> discover(String dir, String type) throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get(Scenario.class.getResource(String.format("%s/%s", dir, type)).toURI()))) {
            return paths
                .filter(Files::isDirectory)
                .map(path -> {
                    String folder = path.getFileName().toString();
                    Path configPath = path.resolve("config." + type);
                    Path basePath = path.resolve("base." + type);
                    Path overlayPath = path.resolve("overlay." + type);
                    Path expectedPath = path.resolve("expected." + type);
                    String baseContent, overlayContent, expectedContent;
                    String configContent = null;
                    if (Files.exists(configPath)) {
                        try {
                            configContent = readResource(configPath);
                        } catch (IOException ignored) {
                        }
                    }
                    try {
                        baseContent = readResource(basePath);
                        overlayContent = readResource(overlayPath);
                        expectedContent = readResource(expectedPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return new Scenario(folder, baseContent, overlayContent, expectedContent, configContent);
                }).collect(Collectors.toList()).stream();
        } catch (Exception e) {
            throw new IOException("Failed to discover scenarios under " + dir, e);
        }
    }

    @Override
    public String toString() {
        return folder;
    }
}
