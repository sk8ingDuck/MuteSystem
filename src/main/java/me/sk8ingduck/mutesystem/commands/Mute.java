package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.utils.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.LocalDateTime;

public class Mute extends Command {

    Config config;
    public Mute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        config = MuteSystem.getBs().getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.help")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.mute.syntax", true)));
            return;
        }

        String playerName = args[0];

        //get uuid async
        UUIDFetcher.getUUID(playerName, uuid -> {

            if (uuid == null) {
                sender.sendMessage(new TextComponent(config.get("mutesystem.playernotfound", true)
                        .replaceAll("%PLAYER%", playerName)));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid.toString(), muteRecord -> {
                if (muteRecord != null) {
                    sender.sendMessage(new TextComponent(config.get("mutesystem.alreadymuted", true)
                            .replaceAll("%PLAYER%", playerName)));
                    return;
                }

                LocalDateTime start = LocalDateTime.now();
                LocalDateTime end = TimeHelper.addTime(start, args[1]);

                if (args.length == 2 && end != null) {
                    sender.sendMessage(new TextComponent(config.get("mutesystem.mute.syntax", true)));
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(int i = (end == null) ? 1 : 2; i < args.length; i++){
                    sb.append(args[i]).append(" ");
                }

                String reason = sb.toString().trim();

                if (end == null) {
                    end = TimeHelper.addTime(start, "500y"); //permanent mute
                }



                if (sender instanceof ProxiedPlayer) {
                    LocalDateTime finalEnd = end;
                    UUIDFetcher.getUUID(sender.getName(), mutedByUuid -> mute(playerName, uuid.toString(),
                            sender.getName(), mutedByUuid.toString(), reason, start, finalEnd));
                } else {
                    mute(playerName, uuid.toString(), config.get("mutesystem.consolename"),
                            config.get("mutesystem.consolename"), reason, start, end);
                }
            });
        });
    }
    private void mute(String playerName, String playerUuid, String mutedByName,
                      String mutedByUuid, String reason, LocalDateTime start, LocalDateTime end) {
        MuteSystem.getBs().getSql().muteAsync(playerUuid, mutedByUuid, reason, start, end);

        Util.broadcastMessage(config.get("mutesystem.mute.successful")
                        .replaceAll("%PLAYER%", playerName)
                        .replaceAll("%MUTED_BY%", mutedByName)
                        .replaceAll("%REASON%", reason)
                        .replaceAll("%TIME%", TimeHelper.getDifference(start, end)),
                "mutesystem.mute");

        ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(playerName);
        if (p1 != null) {
            p1.sendMessage(new TextComponent(MuteSystem.getBs().getConfig().get("mutesystem.mutemessage")
                    .replaceAll("%REASON%", reason)
                    .replaceAll("%MUTED_BY%", mutedByName)
                    .replaceAll("%REMAINING_TIME%", TimeHelper.getDifference(start, end))));

            MuteSystem.getBs().getMutes().put(p1.getName(), new MuteRecord(playerUuid, mutedByUuid,
                    reason, start, end, false));
        }

    }
}
