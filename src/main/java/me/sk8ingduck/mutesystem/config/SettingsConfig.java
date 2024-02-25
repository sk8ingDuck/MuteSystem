package me.sk8ingduck.mutesystem.config;

import me.sk8ingduck.mutesystem.mysql.MySQLDetails;
import net.md_5.bungee.api.CommandSender;

import java.util.Map;
import java.util.stream.Collectors;

public class SettingsConfig extends Config {

	private final MySQLDetails mySQLDetails;
	private final boolean webinterfaceEnabled;
	private final int webinterfacePort;
	private String language;
	private Map<String, Integer> permissions;

	public SettingsConfig(String name, String path) {
		super(name, path, true);

		mySQLDetails = new MySQLDetails(
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

	public MySQLDetails getMySQLDetails() {
		return mySQLDetails;
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
		permissions = fileConfiguration.getSection("mutesystem.ban")
				.getKeys()
				.stream()
				.collect(Collectors.toMap(key -> "mutesystem.ban." + key,
						key -> fileConfiguration.getSection("mutesystem.ban").getInt(key)));
	}

	public void reload() {
		super.reload();
		language = (String) getPath("mutesystem.language");
	}
}
