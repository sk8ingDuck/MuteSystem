package me.sk8ingduck.mutesystem.config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	private final String name;
	private final File file;
	protected Configuration fileConfiguration;
	protected boolean copyDefault;
	public Config(String name, String path, boolean copyDefault) {
		this.name = name;
		this.copyDefault = copyDefault;

		File folder = new File(path);
		if (!folder.exists()) folder.mkdir();

		file = new File(path, name);

		if (!file.exists() || (copyDefault && !containsComments(file))) {
			try {
				if (file.exists()) {
					Files.move(file.toPath(), new File(path, "old_" + name).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				}

				if (!copyDefault) {
					file.createNewFile();
				} else {
					// Copy file from resources
					InputStream resource = getClass().getClassLoader().getResourceAsStream(name);
					if (resource == null) {
						throw new FileNotFoundException("Resource file " + name + " not found");
					}
					try (ReadableByteChannel inputChannel = Channels.newChannel(resource);
					     FileOutputStream output = new FileOutputStream(file);
					     WritableByteChannel outputChannel = Channels.newChannel(output)) {
						ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
						while (inputChannel.read(buffer) != -1) {
							((Buffer) buffer).flip();
							outputChannel.write(buffer);
							buffer.clear();
						}
					}
					ProxyServer.getInstance().getLogger().info("§c[MuteSystem] Recreated " +
							"config file " + name + " with default values because of plugin update!");
					ProxyServer.getInstance().getLogger().info("§c[MuteSystem] Old config file " +
							"was saved as old_" + name);
				}

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

	private boolean containsComments(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith("#")) {
					return true;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	public static String hex(String string) {
		if (string == null || string.isEmpty()) return "";
		Pattern pattern = Pattern.compile("(#|&#)[a-fA-F0-9]{6}");
		for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {
			String color = string.substring(matcher.start(), matcher.end());
			if (color.startsWith("&#")) {
				color = "#" + color.substring(2); // Convert '&#123456' to '#123456'
			}
			string = string.replace(matcher.group(), ChatColor.of(color) + "");
		}
		string = ChatColor.translateAlternateColorCodes('&', string);
		return string;
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

	public Object getPath(String path) {
		return getPath(path, false);
	}


	public Object getPath(String path, boolean translateColors) {
		if (fileConfiguration.get(path) == null) {
			recreateFromResources();
			ProxyServer.getInstance().getLogger()
					.info("§c[MuteSystem] Recreated config file " + name + " because Path '" + path + "' was null!");
			ProxyServer.getInstance().getLogger()
					.info("§c[MuteSystem] Old config file was saved as old_" + name);
		}
		return translateColors ? translateColors(fileConfiguration.get(path)) : fileConfiguration.get(path);
	}

	private void recreateFromResources() {
		try {
			if (file.exists()) {
				Files.move(file.toPath(), new File(file.getParent(), "old_" + file.getName()).toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}

			// Copy file from resources
			String name = file.getName();
			InputStream resource = getClass().getClassLoader().getResourceAsStream(name);
			if (resource == null) {
				throw new FileNotFoundException("Resource file " + name + " not found");
			}
			try (ReadableByteChannel inputChannel = Channels.newChannel(resource);
			     FileOutputStream output = new FileOutputStream(file);
			     WritableByteChannel outputChannel = Channels.newChannel(output)) {
				ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
				while (inputChannel.read(buffer) != -1) {
					buffer.flip();
					outputChannel.write(buffer);
					buffer.clear();
				}
			}
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

		return getPath(path, translateColors);
	}

	private Object translateColors(Object value) {
		if (value instanceof String) {
			return hex((String) value);
		}
		return value;
	}
}
