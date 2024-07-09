package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.utils.TimeHelper;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Mutetemplate implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

		if (args.length == 0) {
			Util.sendMessage(sender, config.get("mutesystem.template.syntax"));
			return true;
		}
		String action = args[0];
		if (action.equalsIgnoreCase("list")) {
			Util.sendMessage(sender, config.get("mutesystem.template.list.header", false));

			MuteSystem.getBs().getSql().getMuteTemplates().forEach(muteTemplate ->
					Util.sendMessage(sender, config.get("mutesystem.template.list.content", false,
							"%ID%", String.valueOf(muteTemplate.getId()),
							"%DURATION%", String.valueOf(TimeHelper.formatTime(muteTemplate.getTime())),
							"%REASON%", muteTemplate.getReason())));

			Util.sendMessage(sender, config.get("mutesystem.template.list.footer", false));
			return true;
		} else if (action.equalsIgnoreCase("add")) {
			if (args.length > 2) {
				StringBuilder reason = new StringBuilder();
				for (int i = 2; i < args.length; i++)
					reason.append(args[i]).append(" ");

				if (args[1].equalsIgnoreCase("perma")
						|| args[1].equalsIgnoreCase("permanent")
						|| args[1].equalsIgnoreCase("p"))
					args[1] = "500y";

				MuteSystem.getBs().getSql().addMuteTemplate(args[1], reason.toString());
				Util.sendMessage(sender, config.get("mutesystem.template.add.successful"));
				return true;
			}
		} else if (action.equalsIgnoreCase("edit")) {
			if (args.length > 3) {
				StringBuilder reason = new StringBuilder();
				for (int i = 3; i < args.length; i++)
					reason.append(args[i]).append(" ");

				MuteSystem.getBs().getSql().editMuteTemplate(Integer.parseInt(args[1]), args[2], reason.toString());
				Util.sendMessage(sender, config.get("mutesystem.template.edit.successful"));
				return true;
			}
		} else if (action.equalsIgnoreCase("remove")) {
			if (args.length == 2) {
				MuteSystem.getBs().getSql().removeMuteTemplate(Integer.parseInt(args[1]));
				Util.sendMessage(sender, config.get("mutesystem.template.remove.successful"));
				return true;
			}
		}
		Util.sendMessage(sender, config.get("mutesystem.template.syntax"));
		return true;
	}
}