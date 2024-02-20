package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import me.sk8ingduck.mutesystem.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ClearMutes extends Command {

    public ClearMutes(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.clearmutes.syntax", true)));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", true,
                        "%PLAYER%", args[0]));
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
}
