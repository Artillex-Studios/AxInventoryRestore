package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {

    @NotNull
    public static Location deserializeLocation(@NotNull String loc) {
        String[] l = loc.replace(" ", "").split(";");
        if (l.length == 6)
            return new Location(Bukkit.getWorld(l[0]), Double.parseDouble(l[1]), Double.parseDouble(l[2]), Double.parseDouble(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]));
        return new Location(Bukkit.getWorld(l[0]), Double.parseDouble(l[1]), Double.parseDouble(l[2]), Double.parseDouble(l[3]));
    }

    @NotNull
    public static String serializeLocation(@NotNull Location loc, boolean exact) {

        final String world = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        if (exact) {
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            return String.format("%s;%.3f;%.3f;%.3f;%.3f;%.3f", world, x, y, z, yaw, pitch);
        }

        return String.format("%s;%.3f;%.3f;%.3f", world, x, y, z);
    }
}
