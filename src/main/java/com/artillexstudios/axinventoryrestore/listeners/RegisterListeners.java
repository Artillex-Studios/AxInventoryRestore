package com.artillexstudios.axinventoryrestore.listeners;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.listeners.impl.DeathListener;
import org.bukkit.plugin.PluginManager;

public class RegisterListeners {
    private final AxInventoryRestore main = AxInventoryRestore.getInstance();
    private final PluginManager plm = main.getServer().getPluginManager();

    public void register() {
        plm.registerEvents(new DeathListener(), main);
    }
}
