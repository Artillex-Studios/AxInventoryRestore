package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("axinventoryrestore.admin")) {
            MessageUtils.sendMsgP(sender, "errors.no-permission");
            return true;
        }

        if (args.length == 1 && args[0].equals("reload")) {

            AxInventoryRestore.getAbstractConfig().reloadConfig();
            AxInventoryRestore.getAbstractMessages().reloadConfig();

            MessageUtils.sendMsgP(sender, "reloaded");
            return true;
        }

        if (args.length == 1 && args[0].equals("cleanup")) {

            MessageUtils.sendMsgP(sender, "cleaned-up");
            return true;
        }

        if (args.length == 1) {
            new MainGui(Bukkit.getOfflinePlayer(args[0]), (Player) sender).openMainGui();
            return true;
        }

        if (args.length == 2 && args[0].equals("save")) {
            final String cause = AxInventoryRestore.MESSAGES.getString("manual-created-by").replace("%player%", sender.getName());

            if (args[1].equals("*")) {
               for (Player player : Bukkit.getOnlinePlayers()) {
                   AxInventoryRestore.getDB().saveInventory(player, "MANUAL", cause);
               }

               MessageUtils.sendMsgP(sender, "manual-backup-all");
               return true;
            }

            if (Bukkit.getPlayer(args[1]) == null) {
                MessageUtils.sendMsgP(sender, "errors.player-offline");
                return true;
            }

            AxInventoryRestore.getDB().saveInventory(Bukkit.getPlayer(args[1]), "MANUAL", cause);
            MessageUtils.sendMsgP(sender, "manual-backup", Map.of("%player%", Bukkit.getPlayer(args[1]).getName()));
            return true;
        }

        MessageUtils.sendListMsg(sender, "help");
        return true;
    }
}
