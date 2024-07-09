package me.sk8ingduck.mutesystemspigot.listeners;

import me.sk8ingduck.mutesystemspigot.MuteSystem;
import me.sk8ingduck.mutesystemspigot.utils.MuteRecord;
import me.sk8ingduck.mutesystemspigot.utils.UUIDFetcher;
import me.sk8ingduck.mutesystemspigot.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.LocalDateTime;

public class Chat implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();
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

		Bukkit.getScheduler().runTaskAsynchronously(MuteSystem.getBs(), () -> {
			player.spigot().sendMessage(MuteSystem.getBs().getMessagesConfig()
					.get("mutesystem.mutemessage",
							"%REASON%", muteRecord.getReason(),
							"%MUTED_BY%", Util.UUIDtoName(muteRecord.getMutedBy()),
							"%REMAINING_TIME%", muteRecord.getRemaining()));
		});
	}
}
