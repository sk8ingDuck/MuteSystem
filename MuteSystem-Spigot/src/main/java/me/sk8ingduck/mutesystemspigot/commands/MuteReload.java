package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MuteReload implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		MuteSystem.getBs().reloadConfigs();
		sender.sendMessage("§aConfig reloaded!");
		return true;
	}
}