package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum View {
    INSTANCE;

    public void execute(Player sender, String player) {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            UUID uuid = getUUID(sender, player);
            if (uuid == null) return;

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            Scheduler.get().runAt(sender.getLocation(), task ->
                new MainGui(uuid, sender, Optional.ofNullable(name).orElse(player)).open());
        }, Priority.HIGH);
    }

    @Nullable
    private UUID getUUID(Player sender, String value) {
        UUID uuid = tryParseUUID(value);
        if (uuid != null) return uuid;
        uuid = AxInventoryRestore.getDatabase().getUUID(value);
        if (uuid != null) return uuid;

        MESSAGEUTILS.sendLang(sender, "errors.unknown-player", Map.of("%number%", "name not found"));
        return null;
    }

    @Nullable
    private UUID tryParseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
