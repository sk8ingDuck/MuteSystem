package me.sk8ingduck.mutesystem.listeners;

import me.sk8ingduck.mutesystem.MuteSystem;
import me.sk8ingduck.mutesystem.utils.UUIDFetcher;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class PostLogin implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        String name = event.getPlayer().getName();
        if (MuteSystem.getBs().getMutes().containsKey(name)) return;

        // put current mute of each player in hashmap to reduce database accesses
        UUIDFetcher.getUUID(name,
                uuid -> MuteSystem.getBs().getSql().getMute(uuid.toString(),
                        muteRecord -> MuteSystem.getBs().getMutes().put(name, muteRecord)));
    }
}
