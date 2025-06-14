package com.artillexstudios.axinventoryrestore.hooks;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;

public class HookManager {
    private static AxShulkersHook axShulkersHook;

    public static void setupHooks() {
        reloadHooks();
    }

    public static void reloadHooks() {
        if (Bukkit.getPluginManager().getPlugin("AxShulkers") != null) {
            axShulkersHook = new AxShulkersHook();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxInventoryRestore] Hooked into AxShulkers!"));
        }
    }

    public static AxShulkersHook getAxShulkersHook() {
        return axShulkersHook;
    }
}
