package com.github.txmy.translations.service;

import com.github.txmy.translations.Credentials;
import com.github.txmy.translations.Settings;
import com.github.txmy.translations.executor.FileTransferExecutor;
import com.github.txmy.translations.executor.GitHubUpdaterExecutor;
import com.github.txmy.translations.threshold.SimpleThresholdChecker;
import com.github.txmy.translations.utils.LogUtils;
import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleTranslationsUpdaterService implements TranslationsUpdaterService {

    private static final Duration THRESHOLD = Duration.ofMinutes(5L);

    private final GitHubUpdaterExecutor gitHubUpdaterExecutor;
    private final FileTransferExecutor fileTransferExecutor;

    private final SimpleThresholdChecker checker;

    public SimpleTranslationsUpdaterService(JavaPlugin plugin, Credentials credentials, Settings settings) {
        this.gitHubUpdaterExecutor = new GitHubUpdaterExecutor(credentials);
        this.fileTransferExecutor = new FileTransferExecutor(plugin.getDataFolder().toPath().getParent(), settings);

        this.checker = new SimpleThresholdChecker(plugin.getDataFolder().toPath());
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
        if (checker.hasCooldown(THRESHOLD) && !force) {
            LogUtils.log("won't update, cooldown still active.");
            return Maps.newHashMap();
        }

        CompletableFuture.runAsync(checker::saveNow);

        Map<String, Boolean> map = fileTransferExecutor.withFiles(gitHubUpdaterExecutor.execute())
                .execute();

        StringBuilder builder = new StringBuilder();
        builder.append(LogUtils.translate("performed update, results: "));
        AtomicBoolean first = new AtomicBoolean(true);
        map.forEach((file, result) -> {
            builder.append(ChatColor.WHITE).append(first.getAndSet(false) ? "" : ",").append(result ? ChatColor.GREEN.toString() : ChatColor.DARK_RED.toString())
                    .append(file);


        });
        LogUtils.log(builder.toString());

        return map;
    }
}
