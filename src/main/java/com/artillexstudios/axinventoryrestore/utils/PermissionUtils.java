package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PermissionUtils {

    public static boolean hasPermission(@NotNull Player player, @NotNull String permission) {
        return player.hasPermission("axinventoryrestore.admin")
                || player.hasPermission("axir.admin")
                || player.hasPermission("axinventoryrestore." + permission)
                || player.hasPermission("axir." + permission)
                || player.hasPermission("axir.*")
                || player.hasPermission("axinventoryrestore.*");
    }

    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission("axinventoryrestore.admin")
                || sender.hasPermission("axir.admin")
                || sender.hasPermission("axinventoryrestore." + permission)
                || sender.hasPermission("axir." + permission)
                || sender.hasPermission("axir.*")
                || sender.hasPermission("axinventoryrestore.*");
    }
}
