package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.utils.UUIDFetcher;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Muteinfo implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

		if (args.length != 1) {
			Util.sendMessage(sender, config.get("mutesystem.muteinfo.syntax"));
			return true;
		}

		String playerName = args[0];

		UUIDFetcher.getUUID(playerName, uuid -> {
			if (uuid == null) {
				Util.sendMessage(sender, config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
				return;
			}

			MuteSystem.getBs().getSql().getMute(uuid, currentMuteRecord -> {
				if (currentMuteRecord == null) {
					Util.sendMessage(sender, config.get("mutesystem.muteinfo.nocurrentmute", "%PLAYER%", playerName));
				} else {
					Util.sendMessage(sender, config.get("mutesystem.muteinfo.currentmute",
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
						Util.sendMessage(sender, config.get("mutesystem.muteinfo.nopastmute", "%PLAYER%", playerName));
						return;
					}

					pastMuteRecords.sort((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));

					//run async because of UUID to Name fetching
					Bukkit.getScheduler().runTaskAsynchronously(MuteSystem.getBs(), () -> {
						pastMuteRecords.stream()
								.sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
								.forEach(muteRecord -> {
									if (!muteRecord.isUnmuted()) {
										Util.sendMessage(sender, config.get("mutesystem.muteinfo.pastmuteNotunmuted",
												"%PLAYER%", playerName,
												"%DATE%", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(muteRecord.getStartDate()),
												"%REASON%", muteRecord.getReason(),
												"%MUTED_BY%", Util.UUIDtoName(muteRecord.getMutedBy()),
												"%MUTE_START%", muteRecord.getStart(),
												"%MUTE_END%", muteRecord.getEnd(),
												"%DURATION%", muteRecord.getDuration()));
									} else {
										Util.sendMessage(sender, config.get("mutesystem.muteinfo.pastmuteUnmuted",
												"%PLAYER%", playerName,
												"%DATE%", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(muteRecord.getStartDate()),
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
		return true;
	}


	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())
						&& !player.getName().equals(sender.getName())) {
					suggestions.add(player.getName());
				}
			}
		}

		return suggestions;
	}

}
