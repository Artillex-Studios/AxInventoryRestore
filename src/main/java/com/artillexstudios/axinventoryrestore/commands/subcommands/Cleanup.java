package com.artillexstudios.axinventoryrestore.commands.subcommands;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.MESSAGEUTILS;

public enum Cleanup {
    INSTANCE;

    public void execute(CommandSender sender) {
        CompletableFuture.runAsync(() -> AxInventoryRestore.getDatabase().cleanup()).thenRun(() -> MESSAGEUTILS.sendLang(sender, "cleaned-up"));
    }
}
