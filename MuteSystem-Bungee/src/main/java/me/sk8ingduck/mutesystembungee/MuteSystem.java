package me.sk8ingduck.mutesystembungee;

import me.sk8ingduck.mutesystembungee.commands.*;
import me.sk8ingduck.mutesystembungee.config.MessagesConfig;
import me.sk8ingduck.mutesystembungee.config.SettingsConfig;
import me.sk8ingduck.mutesystembungee.config.language.MessagesEnglishConfig;
import me.sk8ingduck.mutesystembungee.config.language.MessagesGermanConfig;
import me.sk8ingduck.mutesystembungee.listeners.Chat;
import me.sk8ingduck.mutesystembungee.listeners.PostLogin;
import me.sk8ingduck.mutesystembungee.mysql.*;
import me.sk8ingduck.mutesystembungee.utils.MuteRecord;
import me.sk8ingduck.mutesystembungee.utils.TimeHelper;
import me.sk8ingduck.mutesystembungee.utils.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;

public final class MuteSystem extends Plugin {

	private static MuteSystem bs;
	private HashMap<String, MuteRecord> mutes;
	private SettingsConfig settingsConfig;
	private MessagesConfig msgsConfig;

	private Database sql;

	public static MuteSystem getBs() {
		return bs;
	}

	@Override
	public void onEnable() {
		bs = this;
		settingsConfig = new SettingsConfig("settings.yml", "plugins/MuteSystem");

		MessagesConfig germanConfig = new MessagesGermanConfig("messages_german.yml", "plugins/MuteSystem");
		MessagesConfig englishConfig = new MessagesEnglishConfig("messages_english.yml", "plugins/MuteSystem");

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
				new SQLiteDriver(Paths.get("plugins/MuteSystem/driver"));
				Util.sendMessageToConsole("§a[MuteSystem] Driver loaded successfully!");
			} catch (Exception e) {
				Util.sendMessageToConsole("§c[MuteSystem] Failed to load SQLite Driver! Error:");
				throw new RuntimeException(e);
			}
			sql = new SQLite("mutesystem.db", Paths.get("plugins/MuteSystem", "database"));
		}
		mutes = new HashMap<>();

		PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
		pluginManager.registerCommand(this, new Mute("mute", "mutesystem.mute", ""));
		pluginManager.registerCommand(this, new Unmute("unmute", "mutesystem.unmute", ""));
		pluginManager.registerCommand(this, new Muteinfo("muteinfo", "mutesystem.muteinfo", ""));
		pluginManager.registerCommand(this, new ClearMutes("clearmutes", "mutesystem.clearmutes", "clearmute"));
		pluginManager.registerCommand(this, new Mutetemplate("mutetemplate", "mutesystem.mutetemplate"));
		pluginManager.registerCommand(this, new MuteReload("mutereload", "mutesystem.mutereload", "mreload", "muter", "reloadmute", "rmute"));


		pluginManager.registerListener(this, new Chat());
		pluginManager.registerListener(this, new PostLogin());


		Util.sendMessageToConsole("§a[MuteSystem] MuteSystem activated!");

		if (settingsConfig.isWebinterfaceEnabled())
			listen(settingsConfig.getWebinterfacePort());
	}

	@Override
	public void onDisable() {
		Util.sendMessageToConsole("§c[MuteSystem] MuteSystem disabled!");
		sql.close();
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
			msgsConfig = new MessagesGermanConfig("messages_german.yml", "plugins/MuteSystem");
		} else if (settingsConfig.getLanguage().equalsIgnoreCase("english")) {
			msgsConfig = new MessagesEnglishConfig("messages_english.yml", "plugins/MuteSystem");
		}
	}

	public HashMap<String, MuteRecord> getMutes() {
		return mutes;
	}


	private void listen(int port) {
		getProxy().getScheduler().runAsync(this, () -> {
			try {
				ServerSocket serverSocket = new ServerSocket(port);

				while (true) {
					Socket socket = serverSocket.accept();

					InputStream inputStream = socket.getInputStream();
					byte[] header = new byte[2];
					inputStream.read(header);
					int dataLength = ((header[0] & 0xff) << 8) | (header[1] & 0xff);
					byte[] data = new byte[dataLength];
					inputStream.read(data);

					String[] params = new String(data).split(",");
					if (params.length == 6 && params[0].equals("mute")) {
						String name = params[1];
						String mutedByUuid = params[2];
						String mutedBy = params[3];
						String reason = params[4];
						LocalDateTime start = LocalDateTime.now();
						LocalDateTime end = LocalDateTime.parse(params[5], TimeHelper.formatter);

						Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mute.successful",
										"%PLAYER%", name,
										"%MUTED_BY%", mutedBy,
										"%REASON%", reason,
										"%TIME%", TimeHelper.getDifference(start, end)),
								"mutesystem.mute");

						ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(name);
						if (p1 != null) {
							p1.sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mutemessage",
									"%REASON%", reason,
									"%MUTED_BY%", mutedBy,
									"%REMAINING_TIME%", TimeHelper.getDifference(start, end)));


							MuteSystem.getBs().getMutes().put(p1.getName(), new MuteRecord(p1.getUniqueId().toString(), mutedByUuid,
									reason, start, end, false));
						}
					} else if (params.length == 5 && params[0].equals("unmute")) {
						String name = params[1];
						String unmutedByUuid = params[2];
						String unmutedBy = params[3];
						String reason = params[4];

						Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.unmute.successful",
								"%PLAYER%", name,
								"%UNMUTED_BY%", unmutedBy,
								"%REASON%", reason),
								"mutesystem.unmute");

						ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(name);
						if (p1 != null) {
							MuteSystem.getBs().getMutes().put(p1.getName(), null);
							p1.sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.unmute.youarenolongermuted",
									"%PLAYER%", unmutedBy,
									"%UNMUTED_BY", unmutedBy,
									"%REASON%", reason));
						}
					} else {
						Util.sendMessageToConsole("§c[MuteManagement] Received invalid mute package.");
					}

					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
