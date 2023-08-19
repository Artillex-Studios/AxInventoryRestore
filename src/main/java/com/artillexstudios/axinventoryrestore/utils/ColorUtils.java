package com.artillexstudios.axinventoryrestore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    private static LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = null;
    private static LegacyComponentSerializer LEGACY_FORMATTER = null;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public ColorUtils() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        final String v = packageName.substring(packageName.lastIndexOf('.') + 1);

        if (v.contains("1_7") || v.contains("1_8") || v.contains("1_9") || v.contains("1_10") || v.contains("1_11") || v.contains("1_12") || v.contains("1_13") || v.contains("1_14") || v.contains("1_15")) {
            LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character('§').useUnusualXRepeatedCharacterHexFormat().build();
            LEGACY_FORMATTER = LegacyComponentSerializer.legacyAmpersand().toBuilder().useUnusualXRepeatedCharacterHexFormat().build();
            return;
        }

        LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character('§').useUnusualXRepeatedCharacterHexFormat().hexColors().build();
        LEGACY_FORMATTER = LegacyComponentSerializer.legacyAmpersand().toBuilder().useUnusualXRepeatedCharacterHexFormat().hexColors().build();
    }

    @NotNull
    public static String format(@NotNull String message) {
        message = message.replace('§', '&');
        message = toLegacy(MINI_MESSAGE.deserialize(message).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));
        message = LEGACY_FORMATTER.serialize(LEGACY_FORMATTER.deserialize(message));
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    @NotNull
    public static Component formatToComponent(@NotNull String message) {
        return toComponent(format(message));
    }

    public static @NotNull String toLegacy(@NotNull Component component) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    public static @NotNull Component toComponent(@NotNull String message) {
        return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
    }
}