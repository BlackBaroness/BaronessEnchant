package ru.baronessdev.free.enchant.logging;

import org.bukkit.ChatColor;

public class Logger {

    public static void log(LogType type, String s) {
        System.out.println(
                ChatColor.LIGHT_PURPLE + "[BaronessEnchant] " +
                        getPrefix(type) + " " + s
        );
    }

    private static String getPrefix(LogType type) {
        switch (type) {
            case INFO:
                return ChatColor.YELLOW + "[INFO]" + ChatColor.WHITE;
            case ERROR:
                return ChatColor.DARK_RED + "[ERROR]" + ChatColor.RED;
            default:
                return "";
        }
    }
}
