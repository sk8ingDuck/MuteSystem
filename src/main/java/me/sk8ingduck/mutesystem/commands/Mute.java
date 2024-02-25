package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.utils.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mute extends Command implements TabExecutor {

    public Mute(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length == 0 ||(args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage(config.get("mutesystem.help", false));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(config.get("mutesystem.mute.syntax"));
            return;
        }

        String playerName = args[0];

        //get uuid async
        UUIDFetcher.getUUID(playerName, uuid -> {

            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid, muteRecord -> {
                if (muteRecord != null) {
                    sender.sendMessage(config.get("mutesystem.alreadymuted", "%PLAYER%", playerName));
                    return;
                }

                LocalDateTime start = LocalDateTime.now();

                /* Mute Template */
                Pattern pattern = Pattern.compile("^#([1-9]\\d*)$"); //Regex for #<number>
                Matcher matcher = pattern.matcher(args[1]);
                if (args.length == 2 && matcher.matches()) {
                    int templateId = Integer.parseInt(matcher.group(1));
                    handleTemplateMute(sender, config, uuid, playerName, templateId);
                    return;
                }

                LocalDateTime end = TimeHelper.addTime(start, args[1]);

                //No reason speicified
                if (args.length == 2 && end != null) {
                    sender.sendMessage(config.get("mutesystem.mute.syntax"));
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
                    if (MuteSystem.getBs().getSettingsConfig().getMaxMuteDuration(sender) < duration) {
                        sender.sendMessage(config.get("mutesystem.mute.nopermission"));
                        return;
                    }
                }

                //mute
                if (sender instanceof ProxiedPlayer) {
                    if (!Util.canMute((ProxiedPlayer) sender, uuid)) {
                        sender.sendMessage(config.get("mutesystem.mute.insufficient_rank"));
                        return;
                    }

                    LocalDateTime finalEnd = end;
                    UUIDFetcher.getUUID(sender.getName(), mutedByUuid -> mute(playerName, uuid,
                            sender.getName(), mutedByUuid, reason, start, finalEnd));
                } else {
                    mute(playerName, uuid, config.getString("mutesystem.consolename"),
                            config.getString("mutesystem.consolename"), reason, start, end);
                }
            });
        });
    }

    private void handleTemplateMute(CommandSender sender, MessagesConfig config,
                                    String uuid, String playerName, int templateId) {
        MuteSystem.getBs().getSql().getMuteTemplateAsync(templateId, muteTemplate -> {
            if (muteTemplate == null) {
                sender.sendMessage(config.get("mutesystem.template.add.error"));
                return;
            }
            LocalDateTime start = LocalDateTime.now();

            LocalDateTime end = TimeHelper.addTime(start, muteTemplate.getTime());
            if (!sender.hasPermission("mutesystem.mutesystem.permanent")) {
                long duration = Duration.between(LocalDateTime.now(), end).getSeconds();
                if (MuteSystem.getBs().getSettingsConfig().getMaxMuteDuration(sender) < duration) {
                    sender.sendMessage(config.get("mutesystem.mute.nopermission"));
                    return;
                }
            }

            if (sender instanceof ProxiedPlayer) {
                if (!Util.canMute((ProxiedPlayer) sender, uuid)) {
                    sender.sendMessage(config.get("mutesystem.mute.insufficient_rank"));
                    return;
                }

                UUIDFetcher.getUUID(sender.getName(), bannedByUuid -> mute(playerName, uuid,
                        sender.getName(), bannedByUuid, muteTemplate.getReason(), start, end));
            } else {
                mute(playerName, uuid, config.getString("mutesystem.consolename"),
                        config.getString("mutesystem.consolename"), muteTemplate.getReason(), start, end);
            }
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
        } else if (args.length == 2) {
            String[] timeUnits = {"s", "m", "h", "d", "w", "y"};
            for (String unit : timeUnits) {
                suggestions.add(args[1] + unit);
            }
            suggestions.add("#<ID>");
        }

        return suggestions;
    }
}
