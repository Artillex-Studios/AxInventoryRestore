package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;

public class MessageUtils {

    public static void sendMsgP(CommandSender p, String path) {
        p.sendMessage(ColorUtils.format(CONFIG.getString("prefix") + MESSAGES.getString(path)));
    }

    public static void sendMsgP(Player p, String path) {
        p.sendMessage(ColorUtils.format(CONFIG.getString("prefix") + MESSAGES.getString(path)));
    }

    public static void sendMsgP(Player p, String path, Map<String, String> replacements) {
        AtomicReference<String> message = new AtomicReference<>(MESSAGES.getString(path));
        replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));
        p.sendMessage(ColorUtils.format(CONFIG.getString("prefix") + message));
    }

    public static void sendMsgP(CommandSender p, String path, Map<String, String> replacements) {
        AtomicReference<String> message = new AtomicReference<>(MESSAGES.getString(path));
        replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));
        p.sendMessage(ColorUtils.format(CONFIG.getString("prefix") + message));
    }

    public static void sendMsg(Player p, String path) {
        p.sendMessage(ColorUtils.format(MESSAGES.getString(path)));
    }

    public static void sendListMsg(Player p, String path) {
        for (String m : MESSAGES.getStringList(path)) {
            p.sendMessage(ColorUtils.format(m));
        }
    }

    public static void sendListMsg(CommandSender p, String path) {
        for (String m : MESSAGES.getStringList(path)) {
            p.sendMessage(ColorUtils.format(m));
        }
    }
}