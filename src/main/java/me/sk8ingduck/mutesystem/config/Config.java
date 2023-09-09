package me.sk8ingduck.mutesystem.config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	private final File file;
	protected Configuration fileConfiguration;

	public Config(String name, String path) {
		File folder = new File(path);
		if (!folder.exists()) folder.mkdir();

		file = new File(path, name);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			fileConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void save() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(fileConfiguration, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void reload() {
		try {
			fileConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Object getPathOrSet(String path, Object defaultValue) {
		return getPathOrSet(path, defaultValue, true);
	}
	public Object getPathOrSet(String path, Object defaultValue, boolean translateColors) {
		if (fileConfiguration.get(path) == null) {
			fileConfiguration.set(path, defaultValue);
			save();
		}

		return translateColors ? translateColors(fileConfiguration.get(path)) : fileConfiguration.get(path);
	}

	private Object translateColors(Object value) {
		if (value instanceof String) {
			return hex((String) value);
		}
		return value;
	}
	public static String hex(String message) {
		if (message == null) return null;
		Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			String hexCode = message.substring(matcher.start(), matcher.end());
			String replaceSharp = hexCode.replace('#', 'x');

			char[] ch = replaceSharp.toCharArray();
			StringBuilder builder = new StringBuilder();
			for (char c : ch) {
				builder.append("&").append(c);
			}

			message = message.replace(hexCode, builder.toString());
			matcher = pattern.matcher(message);
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}