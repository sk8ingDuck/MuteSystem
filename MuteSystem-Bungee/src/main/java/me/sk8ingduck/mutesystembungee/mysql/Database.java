package me.sk8ingduck.mutesystembungee.mysql;


import me.sk8ingduck.mutesystembungee.utils.MuteRecord;
import me.sk8ingduck.mutesystembungee.utils.MuteTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Consumer;

public interface Database {

	void close();

	MuteRecord getMute(String uuid);

	void getMute(String uuid, Consumer<MuteRecord> muteRecord);

	ArrayList<MuteRecord> getPastMutes(String uuid);

	void getPastMutes(String uuid, Consumer<ArrayList<MuteRecord>> muteRecords);

	void mute(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end);

	void muteAsync(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end);

	void unmute(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
	            LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime);

	void unmuteAsync(String uuid, String mutedByUuid, String reason, LocalDateTime muteStart,
	                 LocalDateTime muteEnd, String unmutedByUuid, String unmuteReason, LocalDateTime unmuteTime);

	void clearMutes(String uuid);

	void clearMutesAsync(String uuid);

	ArrayList<MuteTemplate> getMuteTemplates();

	MuteTemplate getMuteTemplate(int id);

	void getMuteTemplateAsync(int id, Consumer<MuteTemplate> muteTemplate);

	void addMuteTemplate(String time, String reason);

	void editMuteTemplate(int id, String newTime, String newReason);

	void removeMuteTemplate(int id);
}