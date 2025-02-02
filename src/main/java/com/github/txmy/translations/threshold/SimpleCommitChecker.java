package com.github.txmy.translations.threshold;

import com.github.txmy.translations.Credentials;
import com.github.txmy.translations.utils.HttpUtils;
import com.github.txmy.translations.utils.LogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static com.github.txmy.translations.executor.GitHubUpdaterExecutor.BASE_URL;

public class SimpleCommitChecker {

    private final Credentials credentials;

    private File lastCommitIdFile;
    private String lastCommitId;

    public SimpleCommitChecker(JavaPlugin plugin, Credentials credentials) {
        this.credentials = credentials;

        try {
            lastCommitIdFile = new File(plugin.getDataFolder(), ".data");
            if (!lastCommitIdFile.exists()) {
                if (lastCommitIdFile.getParentFile() != null)
                    lastCommitIdFile.getParentFile().mkdirs();
                lastCommitIdFile.createNewFile();
            }

            lastCommitId = Files.readString(lastCommitIdFile.toPath());
        } catch (IOException ex) {
            LogUtils.error("couldn't fetch last commit id from the internal file due to an error: " + ex.getLocalizedMessage());
        }
    }
    public void saveLastCommitId() throws IOException {
        if (!lastCommitIdFile.exists()) {
            if (lastCommitIdFile.getParentFile() != null) {
                lastCommitIdFile.getParentFile().mkdirs();
            }
            lastCommitIdFile.createNewFile();
        }

        Files.writeString(lastCommitIdFile.toPath(), lastCommitId, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public boolean needsUpdate() {
        try {
            JsonArray response = HttpUtils.getJsonArray(credentials, BASE_URL + credentials.repository() + "/commits");
            JsonObject lastCommit = response.get(0).getAsJsonObject();

            if (lastCommit.has("sha")) {
                String commitId = lastCommit.get("sha").getAsString();
                if (lastCommitId == null || !this.lastCommitId.equalsIgnoreCase(commitId)) {
                    this.lastCommitId = commitId;
                    return true;
                }

                return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }

    }
}
