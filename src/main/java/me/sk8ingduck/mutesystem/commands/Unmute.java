package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.utils.Config;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import me.sk8ingduck.mutesystem.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.LocalDateTime;

public class Unmute extends Command {

    Config config;

    public Unmute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        config = MuteSystem.getBs().getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.unmute.syntax", true)));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(new TextComponent(config.get("mutesystem.playernotfound", true)
                        .replaceAll("%PLAYER%", playerName)));
                return;
            }
            MuteSystem.getBs().getSql().getMute(uuid.toString(), record -> {
                if (record == null) {
                    sender.sendMessage(new TextComponent(config.get("mutesystem.unmute.notmuted", true)
                            .replaceAll("%PLAYER%", playerName)));
                    return;
                }

                StringBuilder unmuteReason = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    unmuteReason.append(args[i]).append(" ");
                }


                if (sender instanceof ProxiedPlayer) {
                    UUIDFetcher.getUUID(sender.getName(), unmutedByUuid ->
                            unmute(playerName, uuid.toString(), record.getMutedBy(),
                                    record.getReason(), record.getStartDate(), record.getEndDate(), sender.getName(),
                                    unmutedByUuid.toString(), unmuteReason.toString(), LocalDateTime.now()));
                } else {
                    unmute(playerName, uuid.toString(), record.getMutedBy(), record.getReason(),
                            record.getStartDate(), record.getEndDate(), config.get("mutesystem.consolename"),
                            config.get("mutesystem.consolename"), unmuteReason.toString(), LocalDateTime.now());
                }
            });
        });
    }

    private void unmute(String playerName, String playerUuid, String mutedByUuid, String muteReason,
                        LocalDateTime start, LocalDateTime end, String unmutedByName, String unmutedByUuid,
                        String unmuteReason, LocalDateTime unmuteTime) {
        MuteSystem.getBs().getSql().unmuteAsync(playerUuid, mutedByUuid, muteReason, start, end, unmutedByUuid,
                unmuteReason, unmuteTime);

        Util.broadcastMessage(config.get("mutesystem.unmute.successful")
                        .replaceAll("%PLAYER%", playerName)
                        .replaceAll("%UNMUTED_BY%", unmutedByName)
                        .replaceAll("%REASON%", unmuteReason),
                "mutesystem.unmute");

        ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(playerName);
        if (p1 != null) {
            MuteSystem.getBs().getMutes().put(p1.getName(), null);
            p1.sendMessage(new TextComponent(config.get("mutesystem.unmute.youarenolongermuted")
                    .replaceAll("%PLAYER%", unmutedByName)
                    .replaceAll("%UNMUTED_BY", unmutedByName)
                    .replaceAll("%REASON%", unmuteReason)));
        }
    }
}
