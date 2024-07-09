package me.sk8ingduck.mutesystemspigot.commands;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.config.MessagesConfig;
import me.sk8ingduck.mutesystemspigot.utils.UUIDFetcher;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Unmute implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();
        if (args.length == 0) {
            Util.sendMessage(sender, config.get("mutesystem.unmute.syntax"));
            return true;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                Util.sendMessage(sender, config.get("mutesystem.playernotfound", "%PLAYER%", playerName));
                return;
            }
            MuteSystem.getBs().getSql().getMute(uuid, record -> {
                if (record == null) {
                    Util.sendMessage(sender, config.get("mutesystem.unmute.notmuted", "%PLAYER%", playerName));
                    return;
                }

                StringBuilder unmuteReason = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    unmuteReason.append(args[i]).append(" ");
                }


                if (sender instanceof Player) {
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
        return true;
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

        Player p1 = Bukkit.getPlayer(playerName);
        if (p1 != null) {
            MuteSystem.getBs().getMutes().put(p1.getName(), null);
            p1.spigot().sendMessage(MuteSystem.getBs().getMessagesConfig()
                    .get("mutesystem.unmute.youarenolongermuted",
                    "%PLAYER%", unmutedByName,
                    "%UNMUTED_BY", unmutedByName,
                    "%REASON%", unmuteReason));
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
        }

        return suggestions;
    }
}
