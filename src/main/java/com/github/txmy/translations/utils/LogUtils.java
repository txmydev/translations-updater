package com.github.txmy.translations.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LogUtils {

    private static final String PREFIX = "&7[&atranslations-updater&7] &e";

    public static void log(String... messages) {
        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + message));
        }
    }

    public static void error(String... messages) {
        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&c" + message));
        }
    }

    public static String translate(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
