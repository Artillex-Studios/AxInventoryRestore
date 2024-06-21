package com.artillexstudios.axinventoryrestore.database.impl;

import com.artillexstudios.axinventoryrestore.AxInventoryRestore;
import org.h2.jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static com.artillexstudios.axinventoryrestore.AxInventoryRestore.CONFIG;

public class H2 extends Base {
    private H2Connection conn;

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public String getType() {
        return "H2";
    }

    @Override
    public void setup() {
        try {
            conn = new H2Connection("jdbc:h2:./" + AxInventoryRestore.getInstance().getDataFolder() + "/data;mode=MySQL;TRACE_LEVEL_FILE=2", new Properties(), null, null, false);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.setup();
    }

    @Override
    public void disable() {
        final String sql = "SHUTDOWN COMPACT;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (CONFIG.getBoolean("compact-database", true))
                stmt.executeUpdate();
            conn.realClose();
        } catch (Exception ignored) {}
    }

    private static class H2Connection extends JdbcConnection {

        public H2Connection(String s, Properties properties, String s1, Object o, boolean b) throws SQLException {
            super(s, properties, s1, o, b);
        }

        @Override
        public synchronized void close() {
        }

        public synchronized void realClose() throws SQLException {
            super.close();
        }
    }
}
