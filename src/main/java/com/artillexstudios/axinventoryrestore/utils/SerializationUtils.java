package com.artillexstudios.axinventoryrestore.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Deprecated(forRemoval = true)
public class SerializationUtils {

    @NotNull
    public static String invToBase64(ItemStack[] stack) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Nullable
    public static ItemStack[] invFromBase64(@NotNull String data) {

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack[]) dataInput.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ByteArrayInputStream invToBits(ItemStack[] stack) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(stack);

            boos.flush();
            boos.close();

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static ItemStack[] invFromBits(@NotNull InputStream stream) {

        try {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(stream)) {
                return (ItemStack[]) dataInput.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
