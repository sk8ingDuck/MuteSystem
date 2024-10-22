package me.sk8ingduck.mutesystembungee.commands;

import me.sk8ingduck.mutesystembungee.MuteSystem;
import me.sk8ingduck.mutesystembungee.config.MessagesConfig;
import me.sk8ingduck.mutesystembungee.utils.UUIDFetcher;
import me.sk8ingduck.mutesystembungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class ClearMutes extends Command implements TabExecutor {

    public ClearMutes(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.clearmutes.syntax")));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", "%PLAYER%", args[0]));
                return;
            }


            String unmutedBy = sender instanceof ProxiedPlayer
                    ? sender.getName()
                    : config.getString("mutesystem.consolename");

            MuteSystem.getBs().getSql().clearMutesAsync(uuid);
            Util.broadcastMessage(config.get("mutesystem.clearmutes.successful",
                            "%PLAYER%", playerName,
                            "%UNMUTED_BY%", unmutedBy),
                    "mutesystem.clearmutes");

            ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(playerName);
            if (p1 != null) {
                MuteSystem.getBs().getMutes().put(p1.getName(), null);
                p1.sendMessage(config.get("mutesystem.unmute.allmutesremoved",
                        "%PLAYER%", unmutedBy));
            }
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
