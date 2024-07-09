package me.sk8ingduck.mutesystemspigot.config;

import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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

	private final File file;
	private FileConfiguration fileConfiguration;
	protected boolean copyDefault;
	public Config(String name, File path, boolean copyDefault) {
		this.copyDefault = copyDefault;

		if (!path.exists()) path.mkdir();

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
					Util.sendMessageToConsole("§c[MuteSystem] Recreated config file " + name
							+ " with default values because of plugin update!");
					Util.sendMessageToConsole("§c[MuteSystem] Old config file was saved as old_" + name);
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		fileConfiguration = new YamlConfiguration();

		try {
			fileConfiguration.load(file);;
		} catch (IOException | InvalidConfigurationException e) {
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

	public static String hex(String message) {
		if (message == null) return null;
		Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			String hexCode = message.substring(matcher.start(), matcher.end());
			String replaceSharp = hexCode.replace('#', 'x');

			char[] ch = replaceSharp.toCharArray();
			StringBuilder builder = new StringBuilder("");
			for (char c : ch) {
				builder.append("&").append(c);
			}

			message = message.replace(hexCode, builder.toString());
			matcher = pattern.matcher(message);
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	protected FileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	protected void save() {
		try {
			fileConfiguration.save(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void reload() {
		try {
			fileConfiguration.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	public Object getPath(String path) {
		return getPath(path, false);
	}


	public Object getPath(String path, boolean translateColors) {
		if (fileConfiguration.get(path) == null) {
			recreateFromResources();
			Util.sendMessageToConsole("§c[MuteSystem] Recreated config file " + file.getName()
					+ " because Path '" + path + "' was null!");
			Util.sendMessageToConsole("§c[MuteSystem] Old config file was saved as old_" + file.getName());
		}
		return translateColors ? translateColors(fileConfiguration.get(path)) : fileConfiguration.get(path);
	}

	private void recreateFromResources() {
		try {
			if (file.exists()) {
				Files.move(file.toPath(), new File(file.getParent(),
						"old_" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
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
			fileConfiguration = new YamlConfiguration();

			try {
				fileConfiguration.load(file);;
			} catch (IOException | InvalidConfigurationException e) {
				throw new RuntimeException(e);
			}

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
