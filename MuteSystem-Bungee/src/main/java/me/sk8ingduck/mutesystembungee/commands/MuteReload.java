package me.sk8ingduck.mutesystembungee.commands;

import me.sk8ingduck.mutesystembungee.MuteSystem;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteReload extends Command {


	public MuteReload(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		MuteSystem.getBs().reloadConfigs();
		sender.sendMessage(new TextComponent("§aConfig reloaded!"));
	}
}