package me.sk8ingduck.mutesystembungee.commands;

import me.sk8ingduck.mutesystembungee.MuteSystem;
import me.sk8ingduck.mutesystembungee.config.MessagesConfig;
import me.sk8ingduck.mutesystembungee.utils.UUIDFetcher;
import me.sk8ingduck.mutesystembungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Unmute extends Command implements TabExecutor {

    public Unmute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();
        if (args.length == 0) {
            sender.sendMessage(config.get("mutesystem.unmute.syntax"));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
                return;
            }
            MuteSystem.getBs().getSql().getMute(uuid, record -> {
                if (record == null) {
                    sender.sendMessage(config.get("mutesystem.unmute.notmuted", "%PLAYER%", playerName));
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
