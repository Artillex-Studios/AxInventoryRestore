package com.artillexstudios.axinventoryrestore.utils;

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
}
