package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ItemBuilder {
    private final YamlDocument file;
    private final String section;
    private @NotNull ItemStack item = new ItemStack(Material.RED_BANNER);
    private final Map<String, String> replacements;

    public ItemBuilder(Config file, String section, Map<String, String> replacements) {
        this.file = file.getBackingDocument();
        this.section = section;
        this.replacements = replacements;

        createItem();
    }

    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }

    private void createItem() {
        setMaterial();
        setName();
        setLore();
        setAmount();
        setColor();
        setGlow();
        setEnchantments();
    }

    private void setName() {
        if (!file.isString(section + ".name")) return;

        AtomicReference<String> message = new AtomicReference<>(file.getString(section + ".name"));
        replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.format(String.valueOf(message)));

        item.setItemMeta(meta);
    }

    private void setLore() {
        if (file.getStringList(section + ".lore") == null) return;
        if (!item.hasItemMeta()) return;

        ArrayList<String> lore = new ArrayList<>();

        for (String txt : file.getStringList(section + ".lore")) {
            AtomicReference<String> message = new AtomicReference<>(txt);
            replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));
            lore.add(ColorUtils.format(String.valueOf(message)));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    private void setMaterial() {
        if (!file.isString(section + ".material")) return;

        Material material = Material.getMaterial(file.getString(section + ".material"));
        if (material == null) {
            Bukkit.getLogger().warning("Error while creating item: " + section + ". Invalid material. Defaulting to RED_BANNER");
            return;
        }

        item = new ItemStack(material);
    }

    private void setAmount() {
        if (!file.isInt(section + ".amount")) return;

        int amount = file.getInt(section + ".amount");

        item.setAmount(amount);
    }

    private void setColor() {
        if (!item.getType().toString().startsWith("LEATHER_")) return;
        if (!file.isString(section + ".color")) return;

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
        String[] rgb = file.getString(section + ".color").replace(" ", "").split(",");
        leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

        item.setItemMeta(leatherArmorMeta);
    }

    private void setGlow() {
        if (!file.isBoolean(section + ".glow")) return;

        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
    }

    private void setEnchantments() {
        if (!file.isSection(section + ".enchantments")) return;

        ItemMeta meta = item.getItemMeta();

        for (String ench : file.getStringList(section + ".enchantments")) {
            String[] ench2 = ench.split(":");
            Enchantment ench3 = null;
            for (Enchantment str : Enchantment.values()) {
                if (("minecraft:" + ench2[0]).equalsIgnoreCase(str.getKey().toString())) {
                    ench3 = str;
                    meta.addEnchant(str, Integer.parseInt(ench2[1]), true);
                    break;
                }
            }
            if (ench3 == null) {
                Bukkit.getLogger().warning("Invalid enchantment: " + ench2[0]);
            }
        }

        item.setItemMeta(meta);
    }
}
