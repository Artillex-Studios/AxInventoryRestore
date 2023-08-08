package com.artillexstudios.axinventoryrestore.commands;

import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (args.length == 1 && args[0].equals("reload")) {
            if (!sender.hasPermission("axinventoryrestore.reload")) {
                MessageUtils.sendMsgP(sender, "errors.no-permission");
                return true;
            }

            MessageUtils.sendMsgP(sender, "reloaded");
            return true;
        }

        if (args.length == 1) {
            new MainGui(Bukkit.getOfflinePlayer(args[0]), (Player) sender).openMainGui();
            return true;
        }

        MessageUtils.sendMsgP(sender, "usage");
        return true;
    }
}
