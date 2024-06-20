package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.database.impl.Base;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Converter3 {
    private final Base base;

    public Converter3(Base base) {
        this.base = base;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Migrating database... Don't stop the server while it's running!"));
        int success = 0;
        if (rename()) success++;
        if (success == 1 && insert()) success++;
        if (success == 2 && convertInventories()) success++;
        if (success == 3 && delete()) success++;

        service.shutdown();
        if (success == 4) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxInventoryRestore] Successful conversion!"));
        } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxInventoryRestore] Something went wrong while converting!"));
        }
    }

    public boolean rename() {
        final String sql = "ALTER TABLE AXIR_BACKUPS RENAME COLUMN inventory to inventoryold;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean insert() {
        final String sql = "ALTER TABLE AXIR_BACKUPS ADD inventory VARCHAR;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean delete() {
        final String sql = "ALTER TABLE AXIR_BACKUPS DROP COLUMN inventoryold;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private ExecutorService service;

    public boolean convertInventories() {
        service = Executors.newFixedThreadPool(5);

        int am = 0;
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM AXIR_BACKUPS;")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    am = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        final CountDownLatch latch = new CountDownLatch(am);
        final String sql = "SELECT inventoryold, id FROM AXIR_BACKUPS;";
        long time = System.currentTimeMillis();
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                PreparedStatement stmt2 = conn.prepareStatement("UPDATE AXIR_BACKUPS SET inventory = ? WHERE id = ?;");
                while (rs.next()) {
                    var stream = rs.getBinaryStream(1);
                    int id = rs.getInt(2);
                    service.submit(() -> {
                        insert(stmt2, latch, time, stream, id);
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                stmt2.close();
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted users!"));
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void insert(PreparedStatement stmt, CountDownLatch latch, long time, InputStream stream, int id) {
        try {
            ItemStack[] items;
            try {
                items = SerializationUtils.invFromBits(stream);
                stmt.setString(1, Serializers.ITEM_ARRAY.serialize(items));
            } catch (Exception ex) {
                final String sql = "DELETE FROM AXIR_BACKUPS WHERE id = ?;";
                try (Connection conn = base.getConnection(); PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                    stmt2.setInt(1, id);
                    stmt2.executeUpdate();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
                latch.countDown();
                return;
            }
            stmt.setInt(2, id);
            long processed = latch.getCount();
            if (processed % 1000 == 0) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] " + processed + " backups remaining (running: " + (System.currentTimeMillis() - time) + "ms)"));
            }
            stmt.executeUpdate();
            latch.countDown();
        } catch (SQLException ex) {
            ex.printStackTrace();
            latch.countDown();
        }
    }
}
