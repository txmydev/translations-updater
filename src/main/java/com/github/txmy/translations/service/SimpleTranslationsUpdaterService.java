package com.github.txmy.translations.service;

import com.github.txmy.translations.Credentials;
import com.github.txmy.translations.Settings;
import com.github.txmy.translations.executor.FileTransferExecutor;
import com.github.txmy.translations.executor.GitHubUpdaterExecutor;
import com.github.txmy.translations.threshold.SimpleCommitChecker;
import com.github.txmy.translations.utils.LogUtils;
import com.google.common.collect.Maps;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;

public class SimpleTranslationsUpdaterService implements TranslationsUpdaterService {

    private final GitHubUpdaterExecutor gitHubUpdaterExecutor;
    private final FileTransferExecutor fileTransferExecutor;

    private final SimpleCommitChecker checker;

    public SimpleTranslationsUpdaterService(JavaPlugin plugin, Credentials credentials, Settings settings) {
        this.gitHubUpdaterExecutor = new GitHubUpdaterExecutor(credentials);
        this.fileTransferExecutor = new FileTransferExecutor(plugin.getDataFolder().toPath().getParent(), settings);

        this.checker = new SimpleCommitChecker(plugin, credentials);
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
    public Map<String, Boolean> update(boolean force) {

        if (!checker.needsUpdate() && !force) {
            LogUtils.log("Already using the latest version (Commit Checker)");
            return Maps.newHashMap();
        }

        Map<String, Boolean> map = fileTransferExecutor.withFiles(gitHubUpdaterExecutor.execute())
                .execute();
        StringBuilder builder = new StringBuilder();
        builder.append("\nFiles Updated:\n");
        map.forEach((file, result) -> {
            builder.append(" - ").append(file).append(": ").append(result ? "Success" : "Failure").append("\n");
        });
        LogUtils.log(builder.toString());

        try {
            checker.saveLastCommitId();
        } catch (IOException e) {
            LogUtils.error("error while saving last commit-id: ");
            e.printStackTrace();
        }

        return map;
    }
}
