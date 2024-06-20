package com.artillexstudios.axinventoryrestore.listeners;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.listeners.impl.ContainerCloseListener;
import com.artillexstudios.axinventoryrestore.listeners.impl.DeathListener;
import com.artillexstudios.axinventoryrestore.listeners.impl.EnderChestCloseListener;
import com.artillexstudios.axinventoryrestore.listeners.impl.JoinListener;
import com.artillexstudios.axinventoryrestore.listeners.impl.QuitListener;
import com.artillexstudios.axinventoryrestore.listeners.impl.WorldChangeListener;
import org.bukkit.plugin.PluginManager;

public class RegisterListeners {
    private final AxInventoryRestore main = AxInventoryRestore.getInstance();
    private final PluginManager plm = main.getServer().getPluginManager();

    public void register() {
        plm.registerEvents(new EnderChestCloseListener(), main);
        plm.registerEvents(new DeathListener(), main);
        plm.registerEvents(new JoinListener(), main);
        plm.registerEvents(new QuitListener(), main);
        plm.registerEvents(new WorldChangeListener(), main);
        plm.registerEvents(new ContainerCloseListener(), main);
    }
}
