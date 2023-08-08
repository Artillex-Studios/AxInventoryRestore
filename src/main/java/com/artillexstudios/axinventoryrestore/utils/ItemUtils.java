package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemUtils {

    public static String itemTo64(ItemStack stack) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static ItemStack itemFrom64(String data) {

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ItemStack(Material.AIR);
    }

    @Nullable
    public static String readData(ItemStack it, String key) {

        final ItemMeta meta = it.getItemMeta();

        final NamespacedKey nKey = new NamespacedKey(AxInventoryRestore.getInstance(), key);

        return meta.getPersistentDataContainer().get(nKey, PersistentDataType.STRING);
    }
}
