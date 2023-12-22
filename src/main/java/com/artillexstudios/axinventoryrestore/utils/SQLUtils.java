package com.artillexstudios.axinventoryrestore.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class SQLUtils {

    public static boolean tableExist(Connection connection, String table) {
        try {
            final DatabaseMetaData dmb = connection.getMetaData();
            final ResultSet tables = dmb.getTables(null, null, table.toUpperCase(), null);
            if (tables.next()) return true;

            final ResultSet tables2 = dmb.getTables(null, null, table, null);
            if (tables2.next()) return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
