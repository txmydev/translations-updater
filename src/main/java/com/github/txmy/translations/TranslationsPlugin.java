package com.github.txmy.translations;

import com.github.txmy.translations.command.MainCommand;
import com.github.txmy.translations.service.SimpleTranslationsUpdaterService;
import com.github.txmy.translations.service.TranslationsUpdaterService;
import com.github.txmy.translations.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TranslationsPlugin extends JavaPlugin {
    private static final String DEFAULT_TOKEN = "XXXXXXXXXXXXXXXXXXX";

    @Override
    public void onEnable() {
        LogUtils.log("Starting up...");
        LogUtils.log("Validating &fGitHub Token&e...");
        saveDefaultConfig();

        Credentials credentials = new Credentials(getConfig().getString("GITHUB_TOKEN", DEFAULT_TOKEN), getConfig().getString("GITHUB_REPOSITORY", "None"));
        if (credentials.token().equals(DEFAULT_TOKEN)) {
            LogUtils.log("&cThe github token hasn't been placed, please set it in config.yml.");
            return;
        }
        Settings settings = new Settings(getConfig().getBoolean("settings.run-on-startup", true),
                getConfig().getBoolean("settings.create-old-file-copies"),
                getConfig().getStringList("settings.commands-after-updating"));
        TranslationsUpdaterService service = new SimpleTranslationsUpdaterService(this, credentials, settings);

        Bukkit.getServicesManager().register(
                TranslationsUpdaterService.class,
                service,
                this,
                ServicePriority.Normal
        );

        if (settings.runOnStartup()) {
           service.update(false);
        }

        getCommand("tsu").setExecutor(new MainCommand(service, settings));
    }





}
