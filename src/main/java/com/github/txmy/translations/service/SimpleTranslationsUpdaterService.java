package com.github.txmy.translations;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class SimpleTranslationsUpdaterService implements TranslationsUpdaterService {

    private final Credentials credentials;
    private final Settings settings;

    private final GitHubUpdaterExecutor gitHubUpdaterExecutor;
    private final FileTransferExecutor fileTransferExecutor;

    public SimpleTranslationsUpdaterService(JavaPlugin plugin, Credentials credentials, Settings settings) {
        this.credentials = credentials;
        this.settings = settings;

        this.gitHubUpdaterExecutor = new GitHubUpdaterExecutor(credentials);
        this.fileTransferExecutor = new FileTransferExecutor(plugin.getDataFolder().toPath().getParent(), settings);
    }

    @Override
    public GitHubUpdaterExecutor getGitHubUpdaterExecutor() {
        return gitHubUpdaterExecutor;
    }

    @Override
    public FileTransferExecutor getFileTransferExecutor() {
        return fileTransferExecutor;
    }

    @Override
    public Map<String, Boolean> update() {
        return fileTransferExecutor.withFiles(gitHubUpdaterExecutor.execute())
                .execute();
    }
}
