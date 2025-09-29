package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.guis.MainGui;
import com.artillexstudios.axinventoryrestore.queue.Priority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum View {
    INSTANCE;

    public void execute(Player sender, String player) {
        AxInventoryRestore.getThreadedQueue().submit(() -> {
            UUID uuid = AxInventoryRestore.getDatabase().getUUID(player);
            if (uuid == null) {
                MESSAGEUTILS.sendLang(sender, "errors.unknown-player", Map.of("%number%", "1"));
                return;
            }

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            Scheduler.get().runAt(sender.getLocation(), task ->
                new MainGui(uuid, sender, Optional.ofNullable(name).orElse(player)).open());
        }, Priority.HIGH);
    }
}
