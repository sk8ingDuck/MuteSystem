package me.sk8ingduck.mutesystembungee.utils;

import me.sk8ingduck.mutesystembungee.MuteSystem;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Helper class for a mute record
 */
public class MuteRecord {

    private final String uuid;
    private final String mutedBy;
    private final String reason;
    private final LocalDateTime start;
    private final LocalDateTime end;

    private final boolean isUnmuted;
    private String unmutedBy;
    private String unmuteReason;
    private LocalDateTime unmuteTime;

    public MuteRecord(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end, boolean isUnmuted) {
        this.uuid = uuid;
        this.mutedBy = mutedBy;
        this.reason = reason;
        this.start = start;
        this.end = end;
        this.isUnmuted = isUnmuted;
    }

    public MuteRecord(String uuid, String mutedBy, String reason, LocalDateTime start, LocalDateTime end,
                      boolean isUnmuted, String unmutedBy, String unmuteReason, LocalDateTime unmuteTime) {
        this.uuid = uuid;
        this.mutedBy = mutedBy;
        this.reason = reason;
        this.start = start;
        this.end = end;
        this.isUnmuted = isUnmuted;
        this.unmutedBy = unmutedBy;
        this.unmuteReason = unmuteReason;
        this.unmuteTime = unmuteTime;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMutedBy() { return mutedBy; }

    public String getReason() {
        return reason;
    }

    public String getStart() {
        return start.format(TimeHelper.formatter);
    }

    public String getEnd() {
        if ((Duration.between(LocalDateTime.now(), end).toDays() / 365) > 100) {
            return MuteSystem.getBs().getMessagesConfig().getString("mutesystem.timeformat.permanent");
        }
        return end.format(TimeHelper.formatter);
    }

    public LocalDateTime getStartDate() {
        return start;
    }

    public LocalDateTime getEndDate() {
        return end;
    }

    public String getDuration() {
        return TimeHelper.getDifference(start, end);
    }

    public String getRemaining() {
        return TimeHelper.getDifference(LocalDateTime.now(), end);
    }

    public boolean isUnmuted() {
        return isUnmuted;
    }

    public String getUnmutedBy() { return unmutedBy; }

    public String getUnmuteReason() {
        return unmuteReason;
    }

    public String getUnmuteTime() {
        return unmuteTime.format(TimeHelper.formatter);
    }
}
