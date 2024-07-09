package me.sk8ingduck.mutesystembungee.commands;

import me.sk8ingduck.mutesystembungee.MuteSystem;
import me.sk8ingduck.mutesystembungee.config.MessagesConfig;
import me.sk8ingduck.mutesystembungee.utils.UUIDFetcher;
import me.sk8ingduck.mutesystembungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Muteinfo extends Command implements TabExecutor {

	public Muteinfo(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

		if (args.length != 1) {
			sender.sendMessage(config.get("mutesystem.muteinfo.syntax"));
			return;
		}

		String playerName = args[0];

		UUIDFetcher.getUUID(playerName, uuid -> {
			if (uuid == null) {
				sender.sendMessage(config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
				return;
			}

			MuteSystem.getBs().getSql().getMute(uuid, currentMuteRecord -> {
				if (currentMuteRecord == null) {
					sender.sendMessage(config.get("mutesystem.muteinfo.nocurrentmute", "%PLAYER%", playerName));
				} else {
					sender.sendMessage(config.get("mutesystem.muteinfo.currentmute",
							"%PLAYER%", playerName,
							"%REASON%", currentMuteRecord.getReason(),
							"%MUTED_BY%", Util.UUIDtoName(currentMuteRecord.getMutedBy()),
							"%MUTE_START%", currentMuteRecord.getStart(),
							"%MUTE_END%", currentMuteRecord.getEnd(),
							"%DURATION%", currentMuteRecord.getDuration(),
							"%REMAINING_TIME%", currentMuteRecord.getRemaining()));
				}

				//run async because of UUID to Name fetching
				MuteSystem.getBs().getSql().getPastMutes(uuid, pastMuteRecords -> {
					if (pastMuteRecords == null || pastMuteRecords.isEmpty()) {
						sender.sendMessage(config.get("mutesystem.muteinfo.nopastmute", "%PLAYER%", playerName));
						return;
					}

					pastMuteRecords.sort((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));

					//run async because of UUID to Name fetching
					ProxyServer.getInstance().getScheduler().runAsync(MuteSystem.getBs(), () -> {
						pastMuteRecords.stream()
								.sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
								.forEach(muteRecord -> {
									if (!muteRecord.isUnmuted()) {
										sender.sendMessage(config.get("mutesystem.muteinfo.pastmuteNotunmuted",
												"%PLAYER%", playerName,
												"%DATE%", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(muteRecord.getStartDate()),
												"%REASON%", muteRecord.getReason(),
												"%MUTED_BY%", Util.UUIDtoName(muteRecord.getMutedBy()),
												"%MUTE_START%", muteRecord.getStart(),
												"%MUTE_END%", muteRecord.getEnd(),
												"%DURATION%", muteRecord.getDuration()));
									} else {
										sender.sendMessage(config.get("mutesystem.muteinfo.pastmuteUnmuted",
												"%PLAYER%", playerName,
												"%REASON%", muteRecord.getReason(),
												"%MUTED_BY%", Util.UUIDtoName(muteRecord.getMutedBy()),
												"%MUTE_START%", muteRecord.getStart(),
												"%MUTE_END%", muteRecord.getEnd(),
												"%DURATION%", muteRecord.getDuration(),
												"%UNMUTED_BY%", Util.UUIDtoName(muteRecord.getUnmutedBy()),
												"%UNMUTE_REASON%", muteRecord.getUnmuteReason(),
												"%UNMUTE_TIME%", muteRecord.getUnmuteTime()));
									}
								});
					});
				});
			});
		});
	}


	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())
						&& !player.getName().equals(sender.getName())) {
					suggestions.add(player.getName());
				}
			}
		}

		return suggestions;
	}

}
