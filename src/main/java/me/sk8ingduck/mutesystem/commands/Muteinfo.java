package me.sk8ingduck.mutesystem.commands;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.utils.MuteRecord;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import me.sk8ingduck.mutesystem.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.atomic.AtomicInteger;

public class Muteinfo extends Command {

    public Muteinfo(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessagesConfig config = MuteSystem.getBs().getMessagesConfig();

        if (args.length != 1) {
            sender.sendMessage(config.get("mutesystem.muteinfo.syntax", true));
            return;
        }

        String playerName = args[0];

        UUIDFetcher.getUUID(playerName, uuid -> {
            if (uuid == null) {
                sender.sendMessage(config.get("mutesystem.playernotfound", true,
                        "%PLAYER%", playerName));
                return;
            }

            MuteSystem.getBs().getSql().getMute(uuid, currentMuteRecord -> {
                if (currentMuteRecord == null) {
                    sender.sendMessage(config.get("mutesystem.muteinfo.nocurrentmute",
                            true, "%PLAYER%", playerName));
                } else {
                    Util.UUIDtoName(currentMuteRecord.getMutedBy(), mutedByName
                            -> sender.sendMessage(config.get("mutesystem.muteinfo.currentmute",
                            "%PLAYER%", playerName,
                            "%REASON%", currentMuteRecord.getReason(),
                            "%MUTED_BY%", mutedByName,
                            "%MUTE_START%", currentMuteRecord.getStart(),
                            "%MUTE_END%", currentMuteRecord.getEnd(),
                            "%DURATION%", currentMuteRecord.getDuration(),
                            "%REMAINING_TIME%", currentMuteRecord.getRemaining())));
                }
            });


            MuteSystem.getBs().getSql().getPastMutes(uuid, pastMuteRecords -> {
                if (pastMuteRecords == null || pastMuteRecords.isEmpty()) {
                    sender.sendMessage(config.get("mutesystem.muteinfo.nopastmute", true,
                            "%PLAYER%", playerName));
                    return;
                }

                AtomicInteger index = new AtomicInteger(0);
                for (MuteRecord muteRecord : pastMuteRecords) {

                    if (!muteRecord.isUnmuted()) {
                        Util.UUIDtoName(muteRecord.getMutedBy(), mutedByName ->
                                sender.sendMessage(config.get("mutesystem.muteinfo.pastmuteNotunmuted",
                                        "%INDEX%", String.valueOf(index.incrementAndGet()),
                                        "%PLAYER%", playerName,
                                        "%REASON%", muteRecord.getReason(),
                                        "%MUTED_BY%", mutedByName,
                                        "%MUTE_START%", muteRecord.getStart(),
                                        "%MUTE_END%", muteRecord.getEnd(),
                                        "%DURATION%", muteRecord.getDuration())));
                    } else {
                        Util.UUIDtoName(muteRecord.getMutedBy(), mutedByName ->
                                Util.UUIDtoName(muteRecord.getUnmutedBy(), unmutedByName ->
                                        sender.sendMessage(config.get("mutesystem.muteinfo.pastmuteUnmuted",
                                                "%INDEX%", String.valueOf(index.incrementAndGet()),
                                                "%PLAYER%", playerName,
                                                "%REASON%", muteRecord.getReason(),
                                                "%MUTED_BY%", mutedByName,
                                                "%MUTE_START%", muteRecord.getStart(),
                                                "%MUTE_END%", muteRecord.getEnd(),
                                                "%DURATION%", muteRecord.getDuration(),
                                                "%UNMUTED_BY%", unmutedByName,
                                                "%UNMUTE_REASON%", muteRecord.getUnmuteReason(),
                                                "%UNMUTE_TIME%", muteRecord.getUnmuteTime()))));
                    }
                }
            });
        });

    }


}
