package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import com.artillexstudios.axinventoryrestore.database.impl.Base;
import com.artillexstudios.axinventoryrestore.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Converter3 {
    private final Base base;

    public Converter3(Base base) {
        this.base = base;

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Migrating database... Don't stop the server while it's running!"));
        int success = 0;
        if (insertInventory()) success++;
        if (success == 1 && insertWorld()) success++;
        if (success == 2 && convert()) success++;
        if (success == 3 && deleteInventory()) success++;
        if (success == 4 && deleteWorld()) success++;

        service.shutdown();
        if (success == 5) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxInventoryRestore] Successful conversion! Optimizing database & starting the server.."));
        } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxInventoryRestore] Something went wrong while converting!"));
        }
        base.disable();
        AxInventoryRestore.getDB().setup();
    }

    public boolean insertWorld() {
        final String sql = "ALTER TABLE axir_backups ADD worldId INT AFTER world;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteWorld() {
        final String sql = "ALTER TABLE axir_backups DROP COLUMN world;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean insertInventory() {
        final String sql = "ALTER TABLE axir_backups ADD inventoryId INT AFTER inventory;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventory() {
        final String sql = "ALTER TABLE axir_backups DROP COLUMN inventory;";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private ExecutorService service;
    private AtomicInteger progress;

    public boolean convert() {
        service = Executors.newFixedThreadPool(5);

        int am = 0;
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM axir_backups;")) {
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
        progress = new AtomicInteger(am);
        final String sql = "SELECT inventory, id, world FROM axir_backups;";
        long time = System.currentTimeMillis();
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                PreparedStatement stmt2 = conn.prepareStatement("UPDATE axir_backups SET inventoryId = ?, worldId = ? WHERE id = ?;");
                while (rs.next()) {
                    var stream = rs.getBinaryStream(1);
                    int id = rs.getInt(2);
                    String world = rs.getString(3);
                    service.submit(() -> {
                        insertInventory(stmt2, latch, time, stream, id, world);
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                stmt.executeBatch();
                stmt2.close();
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] Converted users!"));
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void insertInventory(PreparedStatement stmt, CountDownLatch latch, long time, InputStream stream, int id, String world) {
        try {
            ItemStack[] items;
            try {
                items = SerializationUtils.invFromBits(stream);
                stmt.setInt(1, storeItems(Serializers.ITEM_ARRAY.serialize(items)));
                stmt.setInt(2, storeWorld(world));
            } catch (Exception ex) {
                final String sql = "DELETE FROM axir_backups WHERE id = ?;";
                try (Connection conn = base.getConnection(); PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                    stmt2.setInt(1, id);
                    stmt2.executeUpdate();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
                latch.countDown();
                return;
            }
            stmt.setInt(3, id);

            int processed = progress.getAndDecrement();
            if (processed % 1000 == 0) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF6600[AxInventoryRestore] " + processed + " backups remaining (running: " + (System.currentTimeMillis() - time) + "ms)"));
                stmt.executeBatch();
            }
//            stmt.executeUpdate();
            stmt.addBatch();
            latch.countDown();
        } catch (SQLException ex) {
            ex.printStackTrace();
            latch.countDown();
        }
    }

    private final HashMap<byte[], Integer> cache = new HashMap<>();

    private int storeItems(byte[] items) {
        var res = cache.get(items);
        if (res != null) return res;

        final String sql = "INSERT INTO axir_storage(inventory) VALUES (?);";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBytes(1, items);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    cache.put(items, id);
                    return id;
                }
            }
        } catch (SQLException ex2) {
            ex2.printStackTrace();
        }

        throw new RuntimeException("Failed to save inventory!");
    }

    private int storeWorld(String world) {
        final String sql = "INSERT INTO axir_worlds(name) VALUES (?);";
        try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, world);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException ex) {
            final String sql0 = "SELECT id FROM axir_worlds WHERE name = ?";
            try (Connection conn = base.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql0)) {
                stmt.setString(1, world);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }

        throw new RuntimeException("Failed to save world!");
    }
}
