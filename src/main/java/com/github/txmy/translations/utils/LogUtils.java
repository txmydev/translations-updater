package com.github.txmy.translations.utils;

import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.logging.Logger;

public class LogUtils {

    @Setter
    private static Logger logger;


    public static void log(String... messages) {
        for (String message : messages) {
            logger.info(ChatColor.stripColor(translate(message)));
        }
    }

    public static void error(String... messages) {
        for (String message : messages) {
            logger.severe(ChatColor.stripColor(translate(message)));
        }
    }

    public static String translate(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
