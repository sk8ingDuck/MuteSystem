package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.utils.TimeHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class Mutetemplate extends Command {

	public Mutetemplate(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

		if (args.length == 0) {
			sender.sendMessage(new TextComponent(config.get("mutesystem.template.syntax")));
			return;
		}
		String action = args[0];
		if (action.equalsIgnoreCase("list")) {
			sender.sendMessage(config.get("mutesystem.template.list.header", false));

			MuteSystem.getBs().getSql().getMuteTemplates().forEach(muteTemplate ->
					sender.sendMessage(config.get("mutesystem.template.list.content", false,
							"%ID%", String.valueOf(muteTemplate.getId()),
							"%DURATION%", String.valueOf(TimeHelper.formatTime(muteTemplate.getTime())),
							"%REASON%", muteTemplate.getReason())));

			sender.sendMessage(config.get("mutesystem.template.list.footer", false));
			return;
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
				sender.sendMessage(config.get("mutesystem.template.add.successful"));
				return;
			}
		} else if (action.equalsIgnoreCase("edit")) {
			if (args.length > 3) {
				StringBuilder reason = new StringBuilder();
				for (int i = 3; i < args.length; i++)
					reason.append(args[i]).append(" ");

				MuteSystem.getBs().getSql().editMuteTemplate(Integer.parseInt(args[1]), args[2], reason.toString());
				sender.sendMessage(config.get("mutesystem.template.edit.successful"));
				return;
			}
		} else if (action.equalsIgnoreCase("remove")) {
			if (args.length == 2) {
				MuteSystem.getBs().getSql().removeMuteTemplate(Integer.parseInt(args[1]));
				sender.sendMessage(config.get("mutesystem.template.remove.successful"));
				return;
			}
		}
		sender.sendMessage(config.get("mutesystem.template.syntax"));
	}
}