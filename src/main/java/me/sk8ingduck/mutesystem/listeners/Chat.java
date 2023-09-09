package me.sk8ingduck.mutesystem.listeners;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.utils.MuteRecord;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import me.sk8ingduck.mutesystem.utils.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.time.LocalDateTime;

public class Chat implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        if (event.getMessage().startsWith("/")) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String name = player.getName();

        //if for some reason the hashmap doesnt contain the muterecord (e.g. reload)
        if (!MuteSystem.getBs().getMutes().containsKey(player.getName()))
            UUIDFetcher.getUUID(name,
                    uuid -> MuteSystem.getBs().getSql().getMute(uuid.toString(), muteRecord ->
                            MuteSystem.getBs().getMutes().put(name, muteRecord)));

        MuteRecord muteRecord = MuteSystem.getBs().getMutes().get(name);

        if (muteRecord == null) return;

        if (LocalDateTime.now().minusSeconds(2).isAfter(muteRecord.getEndDate())) {
            MuteSystem.getBs().getMutes().put(name, null);
            return;
        }

        event.setCancelled(true);
        event.setMessage(null);

        Util.UUIDtoName(muteRecord.getMutedBy(), mutedByName ->
                player.sendMessage(MuteSystem.getBs().getMessagesConfig().get("mutesystem.mutemessage",
                        "%REASON%", muteRecord.getReason(),
                        "%MUTED_BY%", mutedByName,
                        "%REMAINING_TIME%", muteRecord.getRemaining())));
    }
}
