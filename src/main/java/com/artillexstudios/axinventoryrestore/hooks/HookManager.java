package com.artillexstudios.axinventoryrestore.hooks;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class HookManager {
    private static AxShulkersHook axShulkersHook;

    public static void setupHooks() {
        reloadHooks();
    }

    public static void reloadHooks() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AxShulkers");
        if (plugin != null && plugin.getDescription().getVersion().startsWith("1.")) {
            axShulkersHook = new AxShulkersHook();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxInventoryRestore] Hooked into AxShulkers!"));
        }
    }

    public static AxShulkersHook getAxShulkersHook() {
        return axShulkersHook;
    }
}
