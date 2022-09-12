package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.utils.MuteRecord;
import me.sk8ingduck.mutesystem.utils.Config;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import me.sk8ingduck.mutesystem.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.atomic.AtomicInteger;

public class Muteinfo extends Command {

    Config config;

    public Muteinfo(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        config = MuteSystem.getBs().getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.syntax", true)));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(new TextComponent(config.get("mutesystem.playernotfound", true)
                        .replaceAll("%PLAYER%", playerName)));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid.toString(), currentMuteRecord -> {
                if (currentMuteRecord == null) {
                    sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.nocurrentmute",
                            true).replaceAll("%PLAYER%", playerName)));
                } else {
                    Util.UUIDtoName(currentMuteRecord.getMutedBy(), mutedByName
                            -> sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.currentmute")
                            .replaceAll("%PLAYER%", playerName)
                            .replaceAll("%REASON%", currentMuteRecord.getReason())
                            .replaceAll("%MUTED_BY%", mutedByName)
                            .replaceAll("%MUTE_START%", currentMuteRecord.getStart())
                            .replaceAll("%MUTE_END%", currentMuteRecord.getEnd())
                            .replaceAll("%DURATION%", currentMuteRecord.getDuration())
                            .replaceAll("%REMAINING_TIME%", currentMuteRecord.getRemaining()))));
                }
            });


            MuteSystem.getBs().getSql().getPastMutes(uuid.toString(), pastMuteRecords -> {
                if (pastMuteRecords == null || pastMuteRecords.isEmpty()) {
                    sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.nopastmute", true)
                            .replaceAll("%PLAYER%", playerName)));
                    return;
                }

                AtomicInteger index = new AtomicInteger(0);
                for (MuteRecord muteRecord : pastMuteRecords) {

                    if (!muteRecord.isUnmuted()) {
                        Util.UUIDtoName(muteRecord.getMutedBy(), mutedByName ->
                                sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.pastmuteNotunmuted")
                                        .replaceAll("%INDEX%", String.valueOf(index.incrementAndGet()))
                                        .replaceAll("%PLAYER%", playerName)
                                        .replaceAll("%REASON%", muteRecord.getReason())
                                        .replaceAll("%MUTED_BY%", mutedByName)
                                        .replaceAll("%MUTE_START%", muteRecord.getStart())
                                        .replaceAll("%MUTE_END%", muteRecord.getEnd())
                                        .replaceAll("%DURATION%", muteRecord.getDuration()))));
                    } else {
                        Util.UUIDtoName(muteRecord.getMutedBy(), mutedByName ->
                                Util.UUIDtoName(muteRecord.getUnmutedBy(), unmutedByName ->
                                        sender.sendMessage(new TextComponent(config.get("mutesystem.muteinfo.pastmuteUnmuted")
                                                .replaceAll("%INDEX%", String.valueOf(index.incrementAndGet()))
                                                .replaceAll("%PLAYER%", playerName)
                                                .replaceAll("%REASON%", muteRecord.getReason())
                                                .replaceAll("%MUTED_BY%", mutedByName)
                                                .replaceAll("%MUTE_START%", muteRecord.getStart())
                                                .replaceAll("%MUTE_END%", muteRecord.getEnd())
                                                .replaceAll("%DURATION%", muteRecord.getDuration())
                                                .replaceAll("%UNMUTED_BY%", unmutedByName)
                                                .replaceAll("%UNMUTE_REASON%", muteRecord.getUnmuteReason())
                                                .replaceAll("%UNMUTE_TIME%", muteRecord.getUnmuteTime())))
                                ));
                    }
                }
            });
        });

    }


}
