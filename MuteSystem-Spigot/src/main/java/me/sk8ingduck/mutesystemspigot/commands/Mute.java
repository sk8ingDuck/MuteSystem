package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.utils.MuteRecord;
import me.sk8ingduck.mutesystemspigot.utils.TimeHelper;
import me.sk8ingduck.mutesystemspigot.utils.UUIDFetcher;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mute implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length == 0 ||(args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            Util.sendMessage(sender, config.get("mutesystem.help", false));
            return true;
        }

        if (args.length < 2) {
            Util.sendMessage(sender, config.get("mutesystem.mute.syntax"));
            return true;
        }

        String playerName = args[0];

        //get uuid async
        UUIDFetcher.getUUID(playerName, uuid -> {

            if (uuid == null) {
                Util.sendMessage(sender, config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid, muteRecord -> {
                if (muteRecord != null) {
                    Util.sendMessage(sender, config.get("mutesystem.alreadymuted", "%PLAYER%", playerName));
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
                    Util.sendMessage(sender, config.get("mutesystem.mute.syntax"));
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
                        Util.sendMessage(sender, config.get("mutesystem.mute.nopermission"));
                        return;
                    }
                }

                //mute
                if (sender instanceof Player) {
                    if (!Util.canMute((Player) sender, uuid)) {
                        Util.sendMessage(sender, config.get("mutesystem.mute.insufficient_rank"));
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
        return true;
    }

    private void handleTemplateMute(CommandSender sender, MessagesConfig config,
                                    String uuid, String playerName, int templateId) {
        MuteSystem.getBs().getSql().getMuteTemplateAsync(templateId, muteTemplate -> {
            if (muteTemplate == null) {
                Util.sendMessage(sender, config.get("mutesystem.template.add.error"));
                return;
            }
            LocalDateTime start = LocalDateTime.now();

            LocalDateTime end = TimeHelper.addTime(start, muteTemplate.getTime());
            if (!sender.hasPermission("mutesystem.mutesystem.permanent")) {
                long duration = Duration.between(LocalDateTime.now(), end).getSeconds();
                if (MuteSystem.getBs().getSettingsConfig().getMaxMuteDuration(sender) < duration) {
                    Util.sendMessage(sender, config.get("mutesystem.mute.nopermission"));
                    return;
                }
            }

            if (sender instanceof Player) {
                if (!Util.canMute((Player) sender, uuid)) {
                    Util.sendMessage(sender, config.get("mutesystem.mute.insufficient_rank"));
                    return;
                }

                UUIDFetcher.getUUID(sender.getName(), mutedByUuid -> mute(playerName, uuid,
                        sender.getName(), mutedByUuid, muteTemplate.getReason(), start, end));
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
                        "%DURATION%", TimeHelper.getDifference(start, end)),
                "mutesystem.mute");

        Player p1 = Bukkit.getPlayer(playerName);
        if (p1 != null) {
            p1.spigot().sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mutemessage",
                    "%REASON%", reason,
                    "%MUTED_BY%", mutedByName,
                    "%REMAINING_TIME%", TimeHelper.getDifference(start, end)));


            MuteSystem.getBs().getMutes().put(p1.getName(), new MuteRecord(playerUuid, mutedByUuid,
                    reason, start, end, false));
        }

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
