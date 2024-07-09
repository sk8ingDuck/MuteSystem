package me.sk8ingduck.mutesystembungee.config;

import me.sk8ingduck.mutesystembungee.mysql.DatabaseDetails;
import net.md_5.bungee.api.CommandSender;

import java.util.Map;
import java.util.stream.Collectors;

public class SettingsConfig extends Config {

	private final DatabaseDetails databaseDetails;
	private final boolean webinterfaceEnabled;
	private final int webinterfacePort;
	private String language;
	private Map<String, Integer> permissions;

	public SettingsConfig(String name, String path) {
		super(name, path, true);

		databaseDetails = new DatabaseDetails(
				((String) getPath("mutesystem.databaseType")).equalsIgnoreCase("mysql"),
				(String) getPath("mutesystem.mysql.host"),
				(int) getPath("mutesystem.mysql.port"),
				(String) getPath("mutesystem.mysql.username"),
				(String) getPath("mutesystem.mysql.password"),
				(String) getPath("mutesystem.mysql.database"));

		language = (String) getPath("mutesystem.language");
		webinterfaceEnabled = (boolean) getPath("mutesystem.webinterface.enabled");
		webinterfacePort = (int) getPath("mutesystem.webinterface.port");

		loadPermissions();
	}

	public DatabaseDetails getMySQLDetails() {
		return databaseDetails;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isWebinterfaceEnabled() {
		return webinterfaceEnabled;
	}

	public int getWebinterfacePort() {
		return webinterfacePort;
	}

	public int getMaxMuteDuration(CommandSender sender) {
		return permissions.entrySet().stream().filter(entry -> sender.hasPermission(entry.getKey()))
				.mapToInt(Map.Entry::getValue).max().orElse(0);
	}

	private void loadPermissions() {
		permissions = fileConfiguration.getSection("mutesystem.mute")
				.getKeys()
				.stream()
				.collect(Collectors.toMap(key -> "mutesystem.mute." + key,
						key -> fileConfiguration.getSection("mutesystem.mute").getInt(key)));
	}

	public void reload() {
		super.reload();
		language = (String) getPath("mutesystem.language");
	}
}
