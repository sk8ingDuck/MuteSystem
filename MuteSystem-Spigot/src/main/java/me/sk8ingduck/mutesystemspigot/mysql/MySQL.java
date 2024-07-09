package me.sk8ingduck.mutesystemspigot.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.sk8ingduck.mutesystemspigot.utils.MuteRecord;
import me.sk8ingduck.mutesystemspigot.utils.MuteTemplate;
import me.sk8ingduck.mutesystemspigot.utils.Util;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class MySQL implements Database {
	private final HikariDataSource dataSource;

    public MySQL(String host, int port, String username, String password, String database) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true");
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
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

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "password VARCHAR(255), " +
                    "canBan BOOLEAN, " +
                    "canUnban BOOLEAN, " +
                    "canDeletePastBans BOOLEAN, " +
                    "canMute BOOLEAN, " +
                    "canUnmute BOOLEAN, " +
                    "canDeletePastMutes BOOLEAN, " +
                    "canEditUsers BOOLEAN);");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS muteTemplates(" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "time VARCHAR(8)," +
                    "reason VARCHAR(128))");

            stmt.close();
        } catch (SQLException e) {
            Util.sendMessageToConsole("§c[MuteSystem] MySQL Connection could not be established. Error:");
            e.printStackTrace();
        }
    }

    public void close() {
        dataSource.close();
    }

    public MuteRecord getMute(String uuid) {
        MuteRecord muteRecord = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM currentMutes WHERE UUID = ?")) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();

                String mutedBy = rs.getString("mutedBy");
                String reason = rs.getString("reason");

                if (LocalDateTime.now().minusSeconds(2).isAfter(end)) {
                    try (PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM currentMutes WHERE UUID = ?");
                         PreparedStatement insertStmt = con.prepareStatement("INSERT INTO pastMutes VALUES (?, ?, ?, ?, ?)")) {
                        deleteStmt.setString(1, uuid);
                        deleteStmt.executeUpdate();
                        insertStmt.setString(1, uuid);
                        insertStmt.setString(2, mutedBy);
                        insertStmt.setString(3, reason);
                        insertStmt.setTimestamp(4, Timestamp.valueOf(start));
                        insertStmt.setTimestamp(5, Timestamp.valueOf(end));
                        insertStmt.executeUpdate();
                    }
                } else {
                    muteRecord = new MuteRecord(uuid, mutedBy, reason, start, end, false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return muteRecord;
    }

    public void getMute(String uuid, Consumer<MuteRecord> muteRecord) {
        Util.pool.execute(() -> muteRecord.accept(getMute(uuid)));
    }

    public ArrayList<MuteRecord> getPastMutes(String uuid) {
        ArrayList<MuteRecord> pastMuteRecords = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT pastMutes.UUID, mutedBy, reason, startTime, " +
                     "endTime, (unmutedBy IS NOT NULL) isUnmuted, unmutedBy, unmuteReason, unmuteTime " +
                     "FROM pastMutes " +
                     "LEFT JOIN unmutes ON pastMutes.UUID = unmutes.UUID AND pastMutes.startTime = unmutes.muteTime " +
                     "WHERE pastMutes.uuid=?")) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String mutedBy = rs.getString("mutedBy");
                    String reason = rs.getString("reason");
                    LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
                    LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();
                    boolean isUnmuted = rs.getBoolean(6);
                    MuteRecord muteRecord = isUnmuted ?
                            new MuteRecord(uuid, mutedBy, reason, start, end, true,
                                    rs.getString("unmutedBy"), rs.getString("unmuteReason"),
                                    rs.getTimestamp("unmuteTime").toLocalDateTime()) :
                            new MuteRecord(uuid, mutedBy, reason, start, end, false);
                    pastMuteRecords.add(muteRecord);
                }
            }
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


    public void mute(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO " +
                     "currentMutes (UUID, mutedBy, reason, startTime, endTime) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, mutedBy);
            pstmt.setString(3, reason);
            pstmt.setTimestamp(4, Timestamp.valueOf(start));
            pstmt.setTimestamp(5, Timestamp.valueOf(end));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void muteAsync(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end) {
        Util.pool.execute(() -> mute(uuid, mutedBy, reason, start, end));
    }

    public void unmute(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
                       LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement deleteCurrentMutesStmt = conn.prepareStatement("DELETE FROM currentMutes WHERE UUID = ?");
             PreparedStatement insertPastMutesStmt = conn.prepareStatement("INSERT INTO pastMutes VALUES (?, ?, ?, ?, ?)");
             PreparedStatement insertUnmutesStmt = conn.prepareStatement("INSERT INTO unmutes VALUES (?, ?, ?, ?, ?)")) {

            conn.setAutoCommit(false);

            deleteCurrentMutesStmt.setString(1, uuid);
            deleteCurrentMutesStmt.executeUpdate();

            insertPastMutesStmt.setString(1, uuid);
            insertPastMutesStmt.setString(2, mutedByUuid);
            insertPastMutesStmt.setString(3, reason);
            insertPastMutesStmt.setTimestamp(4, Timestamp.valueOf(muteStart));
            insertPastMutesStmt.setTimestamp(5, Timestamp.valueOf(muteEnd));
            insertPastMutesStmt.executeUpdate();

            insertUnmutesStmt.setString(1, uuid);
            insertUnmutesStmt.setString(2, unmutedByUuid);
            insertUnmutesStmt.setString(3, unmuteReason);
            insertUnmutesStmt.setTimestamp(4, Timestamp.valueOf(muteStart));
            insertUnmutesStmt.setTimestamp(5, Timestamp.valueOf(unmuteTime));
            insertUnmutesStmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unmuteAsync(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
                            LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
        Util.pool.execute(() -> unmute(uuid, mutedByUuid, reason, muteStart, muteEnd, unmutedByUuid, unmuteReason, unmuteTime));
    }

    public void clearMutes(String uuid) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement deleteCurrentMutesStmt = con.prepareStatement("DELETE FROM currentMutes WHERE UUID = ?");
             PreparedStatement deletePastMutesStmt = con.prepareStatement("DELETE FROM pastMutes WHERE UUID = ?");
             PreparedStatement deleteUnmutesStmt = con.prepareStatement("DELETE FROM unmutes WHERE UUID = ?")) {

            deleteCurrentMutesStmt.setString(1, uuid);
            deletePastMutesStmt.setString(1, uuid);
            deleteUnmutesStmt.setString(1, uuid);

            deleteCurrentMutesStmt.executeUpdate();
            deletePastMutesStmt.executeUpdate();
            deleteUnmutesStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearMutesAsync(String uuid) {
        Util.pool.execute(() -> clearMutes(uuid));
    }

    public ArrayList<MuteTemplate> getMuteTemplates() {
        ArrayList<MuteTemplate> muteTemplates = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM muteTemplates ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                muteTemplates.add(new MuteTemplate(rs.getInt("id"),
                        rs.getString("time"),
                        rs.getString("reason")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return muteTemplates;
    }

    public MuteTemplate getMuteTemplate(int id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM muteTemplates WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MuteTemplate(rs.getInt("id"),
                            rs.getString("time"),
                            rs.getString("reason"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getMuteTemplateAsync(int id, Consumer<MuteTemplate> muteTemplate) {
        Util.pool.execute(() -> muteTemplate.accept(getMuteTemplate(id)));
    }

    public void addMuteTemplate(String time, String reason) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt
                     = con.prepareStatement("INSERT INTO muteTemplates (time, reason) VALUES (?, ?)")) {
            stmt.setString(1, time);
            stmt.setString(2, reason);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editMuteTemplate(int id, String newTime, String newReason) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement deleteTemplateStmt
                     = con.prepareStatement("UPDATE muteTemplates SET time = ?, reason= ? WHERE id = ?")) {
            deleteTemplateStmt.setString(1, newTime);
            deleteTemplateStmt.setString(2, newReason);
            deleteTemplateStmt.setInt(3, id);
            deleteTemplateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removeMuteTemplate(int id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement deleteTemplateStmt = con.prepareStatement("DELETE FROM muteTemplates WHERE id = ?")) {
            deleteTemplateStmt.setInt(1, id);
            deleteTemplateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
