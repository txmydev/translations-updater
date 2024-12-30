package com.github.txmy.translations.threshold;


import com.github.txmy.translations.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// Class utilized to save in a hidden file last update in the plugin data folder
public class SimpleThresholdChecker {

    private final Path path;
    private long lastUpdate;

    public SimpleThresholdChecker(Path parentPath) {
        this.path = parentPath.resolve(".checker");

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }

            lastUpdate = readLast();
        } catch (IOException ex) {
            LogUtils.error("error while creating checker file: " + ex.getLocalizedMessage());
        }

    }

    public boolean hasCooldown(Duration threshold) {
        return lastUpdate + threshold.toMillis() > System.currentTimeMillis();
    }

    public long readLast() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String l = reader.lines().collect(Collectors.joining());
            try {
                return Long.parseLong(l);
            } catch (NumberFormatException ex) {
                return 0L;
            }
        }
    }

    public void saveNow() {
        try {
            Files.writeString(path, (this.lastUpdate = System.currentTimeMillis()) + "", StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LogUtils.error("error while saving checker file: " + e.getLocalizedMessage());
        }
    }


}
