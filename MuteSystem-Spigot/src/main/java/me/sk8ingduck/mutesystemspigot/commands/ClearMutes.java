package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.utils.UUIDFetcher;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClearMutes implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length != 1) {
            Util.sendMessage(sender, config.get("mutesystem.clearmutes.syntax"));
            return true;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                Util.sendMessage(sender, config.get("mutesystem.playernotfound", "%PLAYER%", args[0]));
                return;
            }


            String unmutedBy = sender instanceof Player
                    ? sender.getName()
                    : config.getString("mutesystem.consolename");

            MuteSystem.getBs().getSql().clearMutesAsync(uuid);
            Util.broadcastMessage(config.get("mutesystem.clearmutes.successful",
                            "%PLAYER%", playerName,
                            "%UNMUTED_BY%", unmutedBy),
                    "mutesystem.clearmutes");

            Player p1 = Bukkit.getPlayer(playerName);
            if (p1 != null) {
                MuteSystem.getBs().getMutes().put(p1.getName(), null);
                p1.spigot().sendMessage(config.get("mutesystem.unmute.allmutesremoved",
                        "%PLAYER%", unmutedBy));
            }
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
