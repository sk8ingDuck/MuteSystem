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

import java.time.LocalDateTime;

public class Unmute extends Command {

    public Unmute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();
        if (args.length == 0) {
            sender.sendMessage(config.get("mutesystem.unmute.syntax", true));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", true,
                        "%PLAYER%", playerName));
                return;
            }
            MuteSystem.getBs().getSql().getMute(uuid, record -> {
                if (record == null) {
                    sender.sendMessage(config.get("mutesystem.unmute.notmuted", true,
                            "%PLAYER%", playerName));
                    return;
                }

                StringBuilder unmuteReason = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    unmuteReason.append(args[i]).append(" ");
                }


                if (sender instanceof ProxiedPlayer) {
                    UUIDFetcher.getUUID(sender.getName(), unmutedByUuid ->
                            unmute(playerName, uuid, record.getMutedBy(),
                                    record.getReason(), record.getStartDate(), record.getEndDate(), sender.getName(),
                                    unmutedByUuid, unmuteReason.toString(), LocalDateTime.now()));
                } else {
                    unmute(playerName, uuid, record.getMutedBy(), record.getReason(),
                            record.getStartDate(), record.getEndDate(), config.getString("mutesystem.consolename"),
                            config.getString("mutesystem.consolename"), unmuteReason.toString(), LocalDateTime.now());
                }
            });
        });
    }

    private void unmute(String playerName, String playerUuid, String mutedByUuid, String muteReason,
                        LocalDateTime start, LocalDateTime end, String unmutedByName, String unmutedByUuid,
                        String unmuteReason, LocalDateTime unmuteTime) {
        MuteSystem.getBs().getSql().unmuteAsync(playerUuid, mutedByUuid, muteReason, start, end, unmutedByUuid,
                unmuteReason, unmuteTime);

        Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.unmute.successful",
                        "%PLAYER%", playerName,
                        "%UNMUTED_BY%", unmutedByName,
                        "%REASON%", unmuteReason),
                "mutesystem.unmute");

        ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(playerName);
        if (p1 != null) {
            MuteSystem.getBs().getMutes().put(p1.getName(), null);
            p1.sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.unmute.youarenolongermuted",
                    "%PLAYER%", unmutedByName,
                    "%UNMUTED_BY", unmutedByName,
                    "%REASON%", unmuteReason));
        }
    }
}
