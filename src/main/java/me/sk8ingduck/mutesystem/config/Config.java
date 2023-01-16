package me.sk8ingduck.mutesystem.config;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

	private final File file;
	private final Configuration fileConfiguration;

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
			ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Object getPathOrSet(String path, Object defaultValue) {
		if (fileConfiguration.get(path) == null) {
			fileConfiguration.set(path, defaultValue);
			save();
			return defaultValue instanceof String ? ((String) defaultValue).replaceAll("&", "§") : defaultValue;
		}

		Object value = fileConfiguration.get(path);

		return value instanceof String ? ((String) value).replaceAll("&", "§") : value;
	}
}
