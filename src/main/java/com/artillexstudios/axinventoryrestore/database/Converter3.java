package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.database.impl.Base;
import com.artillexstudios.axinventoryrestore.utils.SQLUtils;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Converter3 {
    private final Base base;

    public Converter3(Base base) {
        this.base = base;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Migrating database... Don't stop the server while it's running!"));
        int success = 0;
        if (convertInventories()) success++;

        if (success == 1) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxInventoryRestore] Successful conversion!"));
        } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxInventoryRestore] Something went wrong while converting!"));
        }
    }

    public boolean convertInventories() {
        if (!SQLUtils.tableExist(base.getConnection(), "axinventoryrestore_uuids")) return true;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converting users..."));

        final String sql = "SELECT * FROM axinventoryrestore_uuids;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                final String sql2 = "INSERT INTO axir_users(uuid, name) VALUES (?,?);";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    int processed = 0;
                    while (rs.next()) {
                        stmt2.setString(1, rs.getString(1));
                        stmt2.setString(2, rs.getString(2));
                        if (processed % 100 == 0) {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted: " + processed));
                            stmt2.executeBatch();
                        }
                        processed++;
                        stmt2.addBatch();
                    }
                    stmt2.executeBatch();
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted users!"));
                }
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
