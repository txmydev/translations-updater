package com.github.txmy.translations.command;

import com.github.txmy.translations.Settings;
import com.github.txmy.translations.service.TranslationsUpdaterService;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static com.github.txmy.translations.utils.LogUtils.translate;

@AllArgsConstructor
public class MainCommand implements CommandExecutor {

    private TranslationsUpdaterService service;
    private Settings settings;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("tsu.command")) {
            sender.sendMessage(translate("&cError: You don't have permission to perform this action."));
            return true;
        }

        String subcommand = args.length < 1 ? "" : args[0];
        switch (subcommand) {
            case "forceupdate":
                service.update(true);
                sender.sendMessage(translate("&aSuccess! Force updated the translations from the GitHub repo, check console for any errors."));

                settings.commandsAfterUpdate().forEach(afterCommand -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), afterCommand));
                break;
            default:
                sender.sendMessage(translate("&cerror: unknown sub-command, available: /tsu forceupdate"));
                break;
        }


        return true;
    }
}
