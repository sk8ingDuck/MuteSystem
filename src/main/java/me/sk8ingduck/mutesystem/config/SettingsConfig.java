package me.sk8ingduck.mutesystem.config;

import net.md_5.bungee.api.CommandSender;
import java.util.HashMap;

public class SettingsConfig extends Config {

	private String language;
	private final boolean webinterfaceEnabled;
	private final int webinterfacePort;
	private final HashMap<String, Integer> permissions;
	public SettingsConfig(String name, String path) {
		super(name, path);

		language = (String) getPathOrSet("mutesystem.language", "german", false);

		webinterfaceEnabled = (boolean) getPathOrSet("mutesystem.webinterface.enabled", true);
		webinterfacePort = (int) getPathOrSet("mutesystem.webinterface.port", 42070);

		permissions = new HashMap<>();
		permissions.put("mutesystem.mute.supporter", 24 * 60 * 60);
		permissions.put("mutesystem.mute.srsupporter", 7 * 24 * 60 * 60);
		permissions.put("mutesystem.mute.moderator", 30 * 24 * 60 * 60);
		permissions.put("mutesystem.mute.srmoderator", 365 * 24 * 60 * 60);
		permissions.forEach((permission, defaultValue) ->
				permissions.put(permission, (int) getPathOrSet(permission, defaultValue)));
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

	public boolean canMute(CommandSender sender, long muteDuration) {
		return permissions.entrySet().stream()
				.anyMatch(entry -> sender.hasPermission(entry.getKey()) && muteDuration <= entry.getValue());
	}

	public void reload() {
		super.reload();
		language = (String) getPathOrSet("mutesystem.language", "german", false);
	}
}



