package me.sk8ingduck.mutesystemspigot.utils;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;


public class WebSocket {

	private final int port;
	private ServerSocket serverSocket;
	private volatile boolean running = true;

	public WebSocket(int port) {
		this.port = port;
	}

	public void start() {
		try {
			serverSocket = new ServerSocket(port);

			while (running) {
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

					Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig()
									.get("mutesystem.mute.successful",
											"%PLAYER%", name,
											"%MUTED_BY%", mutedBy,
											"%REASON%", reason,
											"%TIME%", TimeHelper.getDifference(start, end)),
							"mutesystem.mute");

					Player p1 = Bukkit.getServer().getPlayer(name);
					if (p1 != null) {
						p1.sendMessage(TextComponent.toLegacyText(MuteSystem.getBs().getMessagesConfig()
								.get("mutesystem.mutemessage",
										"%REASON%", reason,
										"%MUTED_BY%", mutedBy,
										"%REMAINING_TIME%", TimeHelper.getDifference(start, end))));

						MuteSystem.getBs().getMutes().put(p1.getName(), new MuteRecord(p1.getUniqueId().toString(),
								mutedByUuid, reason, start, end, false));
					}
				} else if (params.length == 5 && params[0].equals("unmute")) {
					String name = params[1];
					String unmutedByUuid = params[2];
					String unmutedBy = params[3];
					String reason = params[4];

					Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig()
									.get("mutesystem.unmute.successful",
											"%PLAYER%", name,
											"%UNMUTED_BY%", unmutedBy,
											"%REASON%", reason),
							"mutesystem.unmute");

					Player p1 = Bukkit.getPlayer(name);
					if (p1 != null) {
						MuteSystem.getBs().getMutes().put(p1.getName(), null);
						p1.spigot().sendMessage(MuteSystem.getBs().getMessagesConfig()
								.get("mutesystem.unmute.youarenolongermuted",
										"%PLAYER%", unmutedBy,
										"%UNMUTED_BY%", unmutedBy,
										"%REASON%", reason));
					}
				} else {
					Util.sendMessageToConsole("§c[MuteManagement] Received invalid mute package.");
				}

				socket.close();
			}
		} catch (IOException e) {
			if (!running) {
				Util.sendMessageToConsole("§c[MuteSystem] Socket for Web Admin Panel stopped.");
			} else {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
