package me.sk8ingduck.mutesystembungee.listeners;

import me.sk8ingduck.mutesystembungee.MuteSystem;
import me.sk8ingduck.mutesystembungee.utils.UUIDFetcher;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class PostLogin implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        MuteSystem ms = MuteSystem.getBs();
        String name = event.getPlayer().getName();
        if (ms.getMutes().containsKey(name)) return;

        // put current mute of each player in hashmap to reduce database accesses
        UUIDFetcher.getUUID(name, uuid -> ms.getSql().getMute(uuid, muteRecord -> ms.getMutes().put(name, muteRecord)));
    }
}
