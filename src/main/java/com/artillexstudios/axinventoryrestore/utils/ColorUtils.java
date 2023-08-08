package com.artillexstudios.axinventoryrestore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtils {

    public static Component deserialize(String txt) {
        final Component ct = LegacyComponentSerializer.legacyAmpersand().deserialize(txt);
        txt = MiniMessage.miniMessage().serialize(ct).replace("\\", "");
        return MiniMessage.miniMessage().deserialize(txt);
    }
}