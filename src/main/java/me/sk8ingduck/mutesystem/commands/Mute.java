package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.utils.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mute extends Command {

    public Mute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length == 0 ||(args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage(config.get("mutesystem.help"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(config.get("mutesystem.mute.syntax", true));
            return;
        }

        String playerName = args[0];

        //get uuid async
        UUIDFetcher.getUUID(playerName, uuid -> {

            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", true,
                        "%PLAYER%", playerName));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid.toString(), muteRecord -> {
                if (muteRecord != null) {
                    sender.sendMessage(config.get("mutesystem.alreadymuted", true,
                            "%PLAYER%", playerName));
                    return;
                }

                LocalDateTime start = LocalDateTime.now();

                /* Mute Template */
                Pattern pattern = Pattern.compile("^#([1-9]\\d*)$"); //Regex for #<number>
                Matcher matcher = pattern.matcher(args[1]);
                if (args.length == 2 && matcher.matches()) {
                    int templateId = Integer.parseInt(matcher.group(1));
                    MuteSystem.getBs().getSql().getMuteTemplateAsync(templateId, muteTemplate -> {
                        if (muteTemplate == null) {
                            sender.sendMessage(config.get("mutesystem.template.add.error"));
                            return;
                        }

                        LocalDateTime end = TimeHelper.addTime(start, muteTemplate.getTime());
                        if (!sender.hasPermission("mutesystem.mute.permanent")) {
                            long duration = Duration.between(LocalDateTime.now(), end).getSeconds();
                            if (!MuteSystem.getBs().getSettingsConfig().canMute(sender, duration)) {
                                sender.sendMessage(config.get("mutesystem.mute.nopermission", true));
                                return;
                            }
                        }

                        if (sender instanceof ProxiedPlayer) {
                            UUIDFetcher.getUUID(sender.getName(), mutedByUuid -> mute(playerName, uuid.toString(),
                                    sender.getName(), mutedByUuid.toString(), muteTemplate.getReason(), start, end));
                        } else {
                            mute(playerName, uuid.toString(), config.getString("mutesystem.consolename"),
                                    config.getString("mutesystem.consolename"), muteTemplate.getReason(), start, end);
                        }
                    });
                    return;
                }

                LocalDateTime end = TimeHelper.addTime(start, args[1]);

                //No reason speicified
                if (args.length == 2 && end != null) {
                    sender.sendMessage(config.get("mutesystem.mute.syntax", true));
                    return;
                }

                //reason
                StringBuilder sb = new StringBuilder();
                for(int i = (end == null) ? 1 : 2; i < args.length; i++){
                    sb.append(args[i]).append(" ");
                }
                String reason = sb.toString().trim();

                //permanent or not
                if (end == null) {
                    end = TimeHelper.addTime(start, "500y"); //permanent mute
                }

                if (!sender.hasPermission("mutesystem.mute.permanent")) {
                    long duration = Duration.between(LocalDateTime.now(), end).getSeconds();
                    if (!MuteSystem.getBs().getSettingsConfig().canMute(sender, duration)) {
                        sender.sendMessage(config.get("mutesystem.mute.nopermission",  true));
                        return;
                    }
                }

                //mute
                if (sender instanceof ProxiedPlayer) {
                    LocalDateTime finalEnd = end;
                    UUIDFetcher.getUUID(sender.getName(), mutedByUuid -> mute(playerName, uuid.toString(),
                            sender.getName(), mutedByUuid.toString(), reason, start, finalEnd));
                } else {
                    mute(playerName, uuid.toString(), config.getString("mutesystem.consolename"),
                            config.getString("mutesystem.consolename"), reason, start, end);
                }
            });
        });
    }

    private void mute(String playerName, String playerUuid, String mutedByName,
                     String mutedByUuid, String reason, LocalDateTime start, LocalDateTime end) {
        MuteSystem.getBs().getSql().muteAsync(playerUuid, mutedByUuid, reason, start, end);

        Util.broadcastMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mute.successful",
                                "%PLAYER%", playerName,
                        "%MUTED_BY%", mutedByName,
                        "%REASON%", reason,
                        "%TIME%", TimeHelper.getDifference(start, end)),
                "mutesystem.mute");

        ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(playerName);
        if (p1 != null) {
            p1.sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mutemessage",
                    "%REASON%", reason,
                    "%MUTED_BY%", mutedByName,
                    "%REMAINING_TIME%", TimeHelper.getDifference(start, end)));


            MuteSystem.getBs().getMutes().put(p1.getName(), new MuteRecord(playerUuid, mutedByUuid,
                    reason, start, end, false));
        }

    }
}
