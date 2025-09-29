package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.events.WebHooks;
import com.artillexstudios.axinventoryrestore.hooks.HookManager;
import com.artillexstudios.axinventoryrestore.schedulers.AutoBackupScheduler;
import com.artillexstudios.axinventoryrestore.utils.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum Reload {
    INSTANCE;

    public void execute(CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff[AxInventoryRestore] &#66aaffReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fconfig.yml&#00FF00!"));

        if (!MESSAGES.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "messages.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fmessages.yml&#00FF00!"));
        if (!DISCORD.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "discord.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fdiscord.yml&#00FF00!"));
        WebHooks.reload();

        AxInventoryRestore.setDebugMode(CONFIG.getBoolean("debug", false));
        DateUtils.reload();
        AutoBackupScheduler.start();
        HookManager.reloadHooks();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╚ &#00FF00Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reloaded");
    }
}
