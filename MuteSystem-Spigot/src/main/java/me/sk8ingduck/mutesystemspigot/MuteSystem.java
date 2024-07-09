package me.sk8ingduck.mutesystemspigot;

import me.sk8ingduck.mutesystemspigot.commands.*;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.config.SettingsConfig;
import me.sk8ingduck.mutesystemspigot.config.language.MessagesEnglishConfig;
import me.sk8ingduck.mutesystemspigot.config.language.MessagesGermanConfig;
import me.sk8ingduck.mutesystemspigot.listeners.Chat;
import me.sk8ingduck.mutesystemspigot.mysql.*;
import me.sk8ingduck.mutesystemspigot.utils.MuteRecord;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import me.sk8ingduck.mutesystemspigot.utils.WebSocket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Paths;
import java.util.HashMap;

public final class MuteSystem extends JavaPlugin {

	private static MuteSystem bs;
	private HashMap<String, MuteRecord> mutes;
	private SettingsConfig settingsConfig;
	private MessagesConfig msgsConfig;

	private Database sql;

	private WebSocket webSocket;
	private BukkitTask webSocketTask;

	public static MuteSystem getBs() {
		return bs;
	}

	@Override
	public void onEnable() {
		bs = this;
		settingsConfig = new SettingsConfig("settings.yml", getDataFolder());

		MessagesConfig germanConfig = new MessagesGermanConfig("messages_german.yml", getDataFolder());
		MessagesConfig englishConfig = new MessagesEnglishConfig("messages_english.yml", getDataFolder());

		if (settingsConfig.getLanguage().equalsIgnoreCase("german")) {
			msgsConfig = germanConfig;
		} else if (settingsConfig.getLanguage().equalsIgnoreCase("english")) {
			msgsConfig = englishConfig;
		}

		DatabaseDetails details = settingsConfig.getMySQLDetails();
		if (details.isUseMySQL()) {
			sql = new MySQL(details.getHost(), details.getPort(), details.getUsername(), details.getPassword(), details.getDatabase());
		} else {
			try {
				Util.sendMessageToConsole("§6[MuteSystem] Loading SQLite driver...");
				new SQLiteDriver(Paths.get("plugins/MuteSystem-Spigot/driver"));
				Util.sendMessageToConsole("§a[MuteSystem] Driver loaded successfully!");
			} catch (Exception e) {
				Util.sendMessageToConsole("§c[MuteSystem] Failed to load SQLite Driver! Error:");
				throw new RuntimeException(e);
			}
			sql = new SQLite("mutesystem.db", Paths.get("plugins/MuteSystem-Spigot", "database"));
		}
		mutes = new HashMap<>();


		getCommand("mute").setExecutor(new Mute());
		getCommand("tempmute").setExecutor(new Mute());
		getCommand("unmute").setExecutor(new Unmute());
		getCommand("muteinfo").setExecutor(new Muteinfo());
		getCommand("clearmutes").setExecutor(new ClearMutes());
		getCommand("mutetemplate").setExecutor(new Mutetemplate());
		getCommand("mutereload").setExecutor(new MuteReload());

		Bukkit.getPluginManager().registerEvents( new Chat(), this);

		Util.sendMessageToConsole("§a[MuteSystem] MuteSystem enabled!");

		if (settingsConfig.isWebinterfaceEnabled()) {
			webSocket = new WebSocket(settingsConfig.getWebinterfacePort());
			webSocketTask = Bukkit.getScheduler().runTaskAsynchronously(this, () -> webSocket.start());
		}
	}

	@Override
	public void onDisable() {
		Util.sendMessageToConsole("§c[MuteSystem] MuteSystem disabled!");
		if (sql != null)
			sql.close();

		if (webSocket != null)
			webSocket.stop();

		if (webSocketTask != null)
			webSocketTask.cancel();
	}

	public SettingsConfig getSettingsConfig() {
		return settingsConfig;
	}

	public MessagesConfig getMessagesConfig() {
		return msgsConfig;
	}

	public Database getSql() {
		return sql;
	}

	public void reloadConfigs() {
		settingsConfig.reload();

		if (settingsConfig.getLanguage().equalsIgnoreCase("german")) {
			msgsConfig = new MessagesGermanConfig("messages_german.yml", getDataFolder());
		} else if (settingsConfig.getLanguage().equalsIgnoreCase("english")) {
			msgsConfig = new MessagesEnglishConfig("messages_english.yml", getDataFolder());
		}
	}

	public HashMap<String, MuteRecord> getMutes() {
		return mutes;
	}
}
