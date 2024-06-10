package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.events.WebHooks;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.utils.PermissionUtils;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.DISCORD;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGES;
import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (args.length == 1 && args[0].equals("reload")) {
            if (!PermissionUtils.hasPermission(sender, "reload")) {
                MESSAGEUTILS.sendLang(sender, "errors.no-permission");
                return true;
            }

            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff[AxInventoryRestore] &#66aaffReloading configuration..."));
            if (!CONFIG.reload()) {
                MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "config.yml"));
                return true;
            }
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fconfig.yml&#00FF00!"));

            if (!MESSAGES.reload()) {
                MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "messages.yml"));
                return true;
            }
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fmessages.yml&#00FF00!"));
            if (!DISCORD.reload()) {
                MESSAGEUTILS.sendLang(sender, "reload-fail", Collections.singletonMap("%file%", "discord.yml"));
                return true;
            }
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╠ &#00FF00Reloaded &fdiscord.yml&#00FF00!"));
            WebHooks.reload();

            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00aaff╚ &#00FF00Successful reload!"));
            MESSAGEUTILS.sendLang(sender, "reloaded");
            return true;
        }

        if (args.length == 1 && args[0].equals("cleanup")) {
            if (!PermissionUtils.hasPermission(sender, "cleanup")) {
                MESSAGEUTILS.sendLang(sender, "errors.no-permission");
                return true;
            }

            MESSAGEUTILS.sendLang(sender, "cleaned-up");
            return true;
        }

        if (args.length == 1) {
            if (!PermissionUtils.hasPermission(sender, "view")) {
                MESSAGEUTILS.sendLang(sender, "errors.no-permission");
                return true;
            }

            final UUID uuid = AxInventoryRestore.getDB().getUUID(args[0]);
            if (uuid == null) {
                MESSAGEUTILS.sendLang(sender, "errors.unknown-player");
                return true;
            }

            final Integer userId = AxInventoryRestore.getDB().getUserId(uuid);
            if (userId == null) {
                MESSAGEUTILS.sendLang(sender, "errors.unknown-player");
                return true;
            }

            if (!(sender instanceof Player)) {
                MESSAGEUTILS.sendLang(sender, "errors.not-player");
                return true;
            }

            new MainGui(uuid, (Player) sender, args[0]).openMainGui();
            return true;
        }

        if (args.length == 2 && args[0].equals("save")) {
            if (!PermissionUtils.hasPermission(sender, "manualbackup")) {
                MESSAGEUTILS.sendLang(sender, "errors.no-permission");
                return true;
            }

            final String cause = MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

            if (args[1].equals("*")) {
               for (Player pl : Bukkit.getOnlinePlayers()) {
                   AxInventoryRestore.getDB().saveInventory(pl, "MANUAL", cause);
               }

               MESSAGEUTILS.sendLang(sender, "manual-backup-all");
               return true;
            }

            if (Bukkit.getPlayer(args[1]) == null) {
                MESSAGEUTILS.sendLang(sender, "errors.player-offline");
                return true;
            }

            AxInventoryRestore.getDB().saveInventory(Bukkit.getPlayer(args[1]), "MANUAL", cause);

            MESSAGEUTILS.sendLang(sender, "manual-backup", Map.of("%player%", Bukkit.getPlayer(args[1]).getName()));
            return true;
        }
;
        for (String m : MESSAGES.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
        return true;
    }
}
