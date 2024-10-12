package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.events.WebHooks;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import com.artillexstudios.axinventoryrestore.schedulers.AutoBackupScheduler;
import com.artillexstudios.axinventoryrestore.utils.BackupLimiter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

@Command({"axinventoryrestore", "axir", "axinvrestore", "invrestore", "inventoryrestore"})
public class Commands {

    @DefaultFor({"~", "~ help"})
    public void help(@NotNull CommandSender sender) {
        for (String m : MESSAGES.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }

    @Subcommand("view")
    @CommandPermission("axinventoryrestore.view")
    @AutoComplete("@offlinePlayers")
    public void view(Player sender, String player) {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            final UUID uuid = AxInventoryRestore.getDB().getUUID(player);
            if (uuid == null) {
                MESSAGEUTILS.sendLang(sender, "errors.unknown-player", Map.of("%number%", "1"));
                return;
            }

            final Integer userId = AxInventoryRestore.getDB().getUserId(uuid);
            if (userId == null) {
                MESSAGEUTILS.sendLang(sender, "errors.unknown-player", Map.of("%number%", "2"));
                return;
            }

            final String name = Bukkit.getOfflinePlayer(uuid).getName();
            Scheduler.get().runAt(sender.getLocation(), t -> {
                new MainGui(uuid, sender, name == null ? player : name).openMainGui();
            });
        }, Priority.HIGH);
    }

    @Subcommand("reload")
    @CommandPermission("axinventoryrestore.reload")
    public void reload(CommandSender sender) {
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

        AutoBackupScheduler.start();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╚ &#00FF00Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reloaded");
    }

    @Subcommand("cleanup")
    @CommandPermission("axinventoryrestore.cleanup")
    public void cleanup(CommandSender sender) {
        CompletableFuture.runAsync(() -> AxInventoryRestore.getDB().cleanup()).thenRun(() -> MESSAGEUTILS.sendLang(sender, "cleaned-up"));
    }

    @Subcommand("save")
    @CommandPermission("axinventoryrestore.manualbackup")
    public void save(CommandSender sender, Player player) {
        final String cause = MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

        AxInventoryRestore.getDB().saveInventory(player, "MANUAL", cause);
        BackupLimiter.tryLimit(player.getUniqueId(), "manual", "MANUAL");
        MESSAGEUTILS.sendLang(sender, "manual-backup", Map.of("%player%", player.getName()));
    }

    @Subcommand("saveall")
    @CommandPermission("axinventoryrestore.manualbackup")
    public void saveall(CommandSender sender) {
        final String cause = MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

        for (Player pl : Bukkit.getOnlinePlayers()) {
            AxInventoryRestore.getDB().saveInventory(pl, "MANUAL", cause);
            BackupLimiter.tryLimit(pl.getUniqueId(), "manual", "MANUAL");
        }

        MESSAGEUTILS.sendLang(sender, "manual-backup-all");
    }
}
