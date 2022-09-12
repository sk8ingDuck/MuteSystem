package me.sk8ingduck.mutesystem.utils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class MySQL {
    private Connection con;

    public MySQL(String host, String port, String username, String password, String database) {
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/",
                    username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setup(database);
    }

    private void setup(String database) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database);
            stmt.executeUpdate("USE " + database);

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS currentMutes(" +
                    "UUID VARCHAR(40) PRIMARY KEY, " +
                    "mutedBy VARCHAR(40), " +
                    "reason VARCHAR(255), " +
                    "startTime DATETIME, " +
                    "endTime DATETIME)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS pastMutes(" +
                    "UUID VARCHAR(40), " +
                    "mutedBy VARCHAR(40), " +
                    "reason VARCHAR(255), " +
                    "startTime DATETIME, " +
                    "endTime DATETIME, " +
                    "PRIMARY KEY(UUID, startTime))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS unmutes(" +
                    "UUID VARCHAR(40), " +
                    "unmutedBy VARCHAR(40), " +
                    "unmuteReason VARCHAR(255), " +
                    "muteTime DATETIME, " +
                    "unmuteTime DATETIME, " +
                    "PRIMARY KEY(UUID, muteTime))");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private MuteRecord getMute(String uuid) {
        MuteRecord muteRecord = null;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM currentMutes WHERE UUID = '" + uuid + "'");
            while (rs.next()) {
                LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();

                String mutedBy = rs.getString("mutedBy");
                String reason = rs.getString("reason");

                if (LocalDateTime.now().minusSeconds(2).isAfter(end)) {
                    Statement deleteStmt = con.createStatement();
                    deleteStmt.executeUpdate("DELETE FROM currentMutes WHERE UUID = '" + uuid + "'");
                    deleteStmt.executeUpdate("INSERT INTO pastMutes VALUES(" +
                            "'" + uuid + "', " +
                            "'" + mutedBy + "', " +
                            "'" + reason + "', " +
                            "'" + start + "', " +
                            "'" + end + "')");
                    deleteStmt.close();
                } else {
                    muteRecord = new MuteRecord(uuid, mutedBy, reason, start, end, false);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return muteRecord;
    }

    public void getMute(String uuid, Consumer<MuteRecord> muteRecord) {
        Util.pool.execute(() -> muteRecord.accept(getMute(uuid)));
    }
    private ArrayList<MuteRecord> getPastMutes(String uuid) {
        ArrayList<MuteRecord> pastMuteRecords = new ArrayList<>();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " +
                    "pastmutes.uuid, " +
                    "mutedBy, " +
                    "reason, " +
                    "startTime, " +
                    "endTime, " +
                    "(unmutedBy IS NOT NULL) isunmuted, " +
                    "unmutedBy, " +
                    "unmuteReason, " +
                    "unmuteTime " +
                    "FROM `pastmutes` LEFT JOIN unmutes " +
                    "ON pastmutes.UUID = unmutes.UUID AND pastmutes.startTime = unmutes.muteTime " +
                    "WHERE pastmutes.uuid='" + uuid + "'");

            while (rs.next()) {
                String mutedBy = rs.getString("mutedBy");
                String reason = rs.getString("reason");

                LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();

                boolean isunmuted = rs.getBoolean("isunmuted");
                if (!isunmuted) {
                    MuteRecord muteRecord = new MuteRecord(uuid, mutedBy, reason, start, end, false);
                    pastMuteRecords.add(muteRecord);
                } else {
                    String unmutedBy = rs.getString("unmutedBy");
                    String unmuteReason = rs.getString("unmuteReason");
                    LocalDateTime unmuteTime = rs.getTimestamp("unmuteTime").toLocalDateTime();

                    MuteRecord muteRecord = new MuteRecord(uuid, mutedBy, reason, start, end,
                            true, unmutedBy, unmuteReason, unmuteTime);
                    pastMuteRecords.add(muteRecord);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!pastMuteRecords.isEmpty()) {
            pastMuteRecords.sort(Comparator.comparing(MuteRecord::getStartDate));
            return pastMuteRecords;
        }
        return null;
    }

    public void getPastMutes(String uuid, Consumer<ArrayList<MuteRecord>> muteRecords) {
        Util.pool.execute(() -> muteRecords.accept(getPastMutes(uuid)));
    }


    private void mute(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO currentMutes VALUES(" +
                    "'" + uuid + "', " +
                    "'" + mutedBy + "', " +
                    "'" + reason + "', " +
                    "'" + start + "', " +
                    "'" + end + "')");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void muteAsync(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end) {
        Util.pool.execute(() -> mute(uuid, mutedBy, reason, start, end));
    }
    private void unmute(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart, LocalDateTime muteEnd,
                       String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM currentMutes WHERE UUID = '" + uuid + "'");
            stmt.executeUpdate("INSERT INTO pastMutes VALUES(" +
                    "'" + uuid + "', " +
                    "'" + mutedByUuid + "', " +
                    "'" + reason + "', " +
                    "'" + muteStart + "', " +
                    "'" + muteEnd + "')");

            stmt.executeUpdate("INSERT INTO unmutes VALUES(" +
                    "'" + uuid + "', " +
                    "'" + unmutedByUuid + "', " +
                    "'" + unmuteReason + "', " +
                    "'" + muteStart + "', " +
                    "'" + unmuteTime + "')");

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unmuteAsync(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
                            LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
        Util.pool.execute(() -> unmute(uuid, mutedByUuid, reason, muteStart, muteEnd, unmutedByUuid, unmuteReason, unmuteTime));
    }

    private void clearMutes(String uuid) {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM currentMutes WHERE UUID = '" + uuid + "'");
            stmt.executeUpdate("DELETE FROM pastMutes WHERE UUID = '" + uuid + "'");
            stmt.executeUpdate("DELETE FROM unmutes WHERE UUID = '" + uuid + "'");

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearMutesAsync(String uuid) {
        Util.pool.execute(() -> clearMutes(uuid));
    }
}
