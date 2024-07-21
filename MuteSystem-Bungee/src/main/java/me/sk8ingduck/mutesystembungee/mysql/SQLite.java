package me.sk8ingduck.mutesystembungee.mysql;

import me.sk8ingduck.mutesystembungee.utils.MuteRecord;
import me.sk8ingduck.mutesystembungee.utils.MuteTemplate;
import me.sk8ingduck.mutesystembungee.utils.Util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SQLite implements Database {
	private final ExecutorService pool = Executors.newCachedThreadPool();
	private Connection connection;

	public SQLite(String fileName, Path path) {
		try {
			// Load the SQLite JDBC driver (if necessary)
			Class.forName("org.sqlite.JDBC");

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			File databaseFile = path.resolve(fileName).toFile();
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
			initializeDatabase();
		} catch (Exception e) {
			Util.sendMessageToConsole("§c[MuteSystem] SQLite Connection could not be established. Error:");
			e.printStackTrace();
		}
	}

	private void initializeDatabase() {
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS currentMutes(" +
					"UUID TEXT PRIMARY KEY, " +
					"mutedBy TEXT, " +
					"reason TEXT, " +
					"startTime DATETIME, " +
					"endTime DATETIME)");

			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS pastMutes(" +
					"UUID TEXT, " +
					"mutedBy TEXT, " +
					"reason TEXT, " +
					"startTime DATETIME, " +
					"endTime DATETIME, " +
					"PRIMARY KEY(UUID, startTime))");

			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS unmutes(" +
					"UUID TEXT, " +
					"unmutedBy TEXT, " +
					"unmuteReason TEXT, " +
					"muteTime DATETIME, " +
					"unmuteTime DATETIME, " +
					"PRIMARY KEY(UUID, muteTime))");

			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS muteTemplates(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"time TEXT, " +
					"reason TEXT)");

		} catch (SQLException e) {
			Util.sendMessageToConsole("§c[MuteSystem] Error initializing the SQLite database.");
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			Util.sendMessageToConsole("§c[MuteSystem] Error closing the SQLite database connection.");
			e.printStackTrace();
		}
	}

	public MuteRecord getMute(String uuid) {
		MuteRecord muteRecord = null;
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM currentMutes WHERE UUID = ?")) {
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
				LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();

				String mutedBy = rs.getString("mutedBy");
				String reason = rs.getString("reason");

				if (LocalDateTime.now().minusSeconds(2).isAfter(end)) {
					try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM " +
							"currentMutes WHERE UUID = ?");
					     PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO " +
							     "pastMutes(UUID, mutedBy, reason, startTime, endTime) VALUES (?, ?, ?, ?, ?)")) {
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
		pool.execute(() -> muteRecord.accept(getMute(uuid)));
	}

	public ArrayList<MuteRecord> getPastMutes(String uuid) {
		ArrayList<MuteRecord> pastMuteRecords = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT pastMutes.UUID, mutedBy, reason, " +
				"startTime, endTime, (unmutedBy IS NOT NULL) AS isUnmuted, unmutedBy, unmuteReason, unmuteTime " +
				"FROM pastMutes LEFT JOIN unmutes ON pastMutes.UUID = unmutes.UUID " +
				"AND pastMutes.startTime = unmutes.muteTime WHERE pastMutes.uuid=?")) {
			stmt.setString(1, uuid);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String mutedBy = rs.getString("mutedBy");
					String reason = rs.getString("reason");
					LocalDateTime start = rs.getTimestamp("startTime").toLocalDateTime();
					LocalDateTime end = rs.getTimestamp("endTime").toLocalDateTime();
					boolean isUnmuted = rs.getBoolean("isUnmuted");
					MuteRecord muteRecord = isUnmuted
									? new MuteRecord(uuid, mutedBy, reason, start, end, true,
									rs.getString("unmutedBy"), rs.getString("unmuteReason"),
									rs.getTimestamp("unmuteTime").toLocalDateTime())
									: new MuteRecord(uuid, mutedBy, reason, start, end, false);
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
		pool.execute(() -> muteRecords.accept(getPastMutes(uuid)));
	}

	public void mute(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end) {
		try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO " +
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
		pool.execute(() -> mute(uuid, mutedBy, reason, start, end));
	}

	public void unmute(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
	                   LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
		try (PreparedStatement deleteCurrentMutesStmt = connection.prepareStatement("DELETE FROM " +
				"currentMutes WHERE UUID = ?");
		     PreparedStatement insertPastMutesStmt = connection.prepareStatement("INSERT INTO " +
				     "pastMutes(UUID, mutedBy, reason, startTime, endTime) VALUES (?, ?, ?, ?, ?)");
		     PreparedStatement insertUnmutesStmt = connection.prepareStatement("INSERT INTO " +
				     "unmutes(UUID, unmutedBy, unmuteReason, muteTime, unmuteTime) VALUES (?, ?, ?, ?, ?)")) {

			connection.setAutoCommit(false);

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

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unmuteAsync(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
	                        LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime) {
		pool.execute(() -> unmute(uuid, mutedByUuid, reason, muteStart, muteEnd, unmutedByUuid, unmuteReason, unmuteTime));
	}

	public void clearMutes(String uuid) {
		try (PreparedStatement deleteCurrentMutesStmt = connection.prepareStatement("DELETE FROM currentMutes WHERE UUID = ?");
		     PreparedStatement deletePastMutesStmt = connection.prepareStatement("DELETE FROM pastMutes WHERE UUID = ?");
		     PreparedStatement deleteUnmutesStmt = connection.prepareStatement("DELETE FROM unmutes WHERE UUID = ?")) {

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
		pool.execute(() -> clearMutes(uuid));
	}

	public ArrayList<MuteTemplate> getMuteTemplates() {
		ArrayList<MuteTemplate> muteTemplates = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM muteTemplates ORDER BY id");
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
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM muteTemplates WHERE id = ?")) {
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
		pool.execute(() -> muteTemplate.accept(getMuteTemplate(id)));
	}

	public void addMuteTemplate(String time, String reason) {
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO " +
				"muteTemplates (time, reason) VALUES (?, ?)")) {
			stmt.setString(1, time);
			stmt.setString(2, reason);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void editMuteTemplate(int id, String newTime, String newReason) {
		try (PreparedStatement stmt = connection.prepareStatement("UPDATE " +
				"muteTemplates SET time = ?, reason = ? WHERE id = ?")) {
			stmt.setString(1, newTime);
			stmt.setString(2, newReason);
			stmt.setInt(3, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeMuteTemplate(int id) {
		try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM muteTemplates WHERE id = ?")) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}