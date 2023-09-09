package me.sk8ingduck.mutesystem.utils;

public class MuteTemplate {
	private final int id;
	private final String time;
	private final String reason;

	public MuteTemplate(int id, String time, String reason) {
		this.id = id;
		this.time = time;
		this.reason = reason;
	}

	public int getId() {
		return id;
	}

	public String getTime() {
		return time;
	}

	public String getReason() {
		return reason;
	}
}
