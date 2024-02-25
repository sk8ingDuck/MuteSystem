package me.sk8ingduck.mutesystem.config.language;

import me.sk8ingduck.mutesystem.config.MessagesConfig;

import java.util.LinkedHashMap;

public class MessagesEnglishConfig extends MessagesConfig {

	public MessagesEnglishConfig(String name, String path) {
		super(name, path);
	}

	@Override
	public void loadMessages() {
		messages = new LinkedHashMap<>();
		messages.put("mutesystem.prefix", "&4MuteSystem &7» ");
		messages.put("mutesystem.consolename", "CONSOLE");
		messages.put("mutesystem.playernotfound", "&cPlayer %PLAYER% not found.");
		messages.put("mutesystem.alreadymuted", "&cPlayer %PLAYER% is already muted.\n" +
				"Use /muteinfo %PLAYER%");
		messages.put("mutesystem.help",
				"&7&m-----------------&r&e« MuteSystem »&7&m-----------------\n\n" +
						"&7/mute &a<Player> [<Time>] <Reason> &8- &7Mute a player\n" +
						"&7/mute &a<Player> #<template-id> &8- &7Mute a player\n" +
						"&7/unmute &a<Player> &8- &7Unmute a player\n" +
						"&7/muteinfo &a<Player> &8- &7View player's mute history\n" +
						"&7/clearmutes &a<Player> &8- &7Clear all mute entries\n" +
						"&7/mutetemplate &alist &8- &7Show mute templates\n" +
						"&7/mutetemplate &aadd <time> <reason> &8- &7Add mute template\n" +
						"&7/mutetemplate &aedit <time> <reason> &8- &7Edit mute template\n" +
						"&7/mutetemplate &aremove <id> &8- &7Remove mute template\n" +
						"&7/muteReload &8- &7Reload messages from config\n\n" +
						"&7&m-----------------&r&e« MuteSystem »&7&m-----------------");
		messages.put("mutesystem.mute.syntax", "&cSyntax: /mute <Player> [<Time>] <Reason>");
		messages.put("mutesystem.mute.nopermission", "&cYou don't have enough permissions!");
		messages.put("mutesystem.mute.successful", "&7Player &c%PLAYER% &7was muted! {muteBroadcast}");
		messages.put("mutesystem.mute.insufficient_rank", "&cYou don't have permission to mute this player.");

		messages.put("mutesystem.unmute.syntax", "&cSyntax: /unmute <Player> [<Reason>]");
		messages.put("mutesystem.unmute.notunmuted", "&cPlayer &e%PLAYER% &cis not muted.");
		messages.put("mutesystem.unmute.successful", "&7Player &e%PLAYER% &7was unmuted! {unmuteBroadcast}");
		messages.put("mutesystem.unmute.youarenolongermuted", "&7You were unmuted by &e%PLAYER%" +
				"\n&7Reason: &e%REASON%");
		messages.put("mutesystem.muteinfo.syntax", "&cSyntax: /muteinfo <Player>");
		messages.put("mutesystem.muteinfo.nocurrentmute", "&cPlayer %PLAYER% is not muted currently.");
		messages.put("mutesystem.muteinfo.currentmute", "&7Player %PLAYER% &cis currently muted! {currentMute}");
		messages.put("mutesystem.muteinfo.nopastmute", "&cPlayer %PLAYER% has no previous mutes.");
		messages.put("mutesystem.muteinfo.pastmuteUnmuted", "&7Mute from &e%DATE% {pastMuteUnmuted}");
		messages.put("mutesystem.muteinfo.pastmuteNotunmuted", "&7Mute from &e%DATE% {pastMuteNotUnmuted}");

		messages.put("mutesystem.clearmutes.syntax", "&cSyntax: /clearmutes <Player>");
		messages.put("mutesystem.clearmutes.successful", "&4!! &7Mute entries of &e%PLAYER% &7were cleared by &e%UNMUTED_BY% &4!!");
		messages.put("mutesystem.unmute.allmutesremoved", "&7All your mute entries have been cleared by &e%PLAYER% &7:O");

		messages.put("mutesystem.template.syntax",
				"&cSyntax error. Possibilities:\n" +
						"&7/mutetemplate &alist &8- &7list existing mute templates\n" +
						"&7/mutetemplate &aadd <time> <reason> &8- &7add mute template\n" +
						"&7/mutetemplate &aedit <id> <time> <reason> &8- &7edit mute template\n" +
						"&7/mutetemplate &aremove <id> &8- &7remove mute template");
		messages.put("mutesystem.template.list.header", "&7------------------- &eMute Templates &7-------------------");
		messages.put("mutesystem.template.list.content", "&7(&eID: %ID%&7) &8| &c%DURATION% &8| &e%REASON%");
		messages.put("mutesystem.template.list.footer", "&7------------------- &eMute Templates &7-------------------");
		messages.put("mutesystem.template.add.successful", "&aMute Template added!");
		messages.put("mutesystem.template.add.error", "&cMute template not found!");
		messages.put("mutesystem.template.edit.successful", "&aMute Template edited!");
		messages.put("mutesystem.template.remove.successful", "&aMute Template removed!");

		messages.put("mutesystem.mutemessage",
				"&cYou have been muted.\n" +
						"&eReason: &c%REASON%\n" +
						"&eMuted by: &c%MUTED_BY%\n" +
						"&eRemaining time: &c%REMAINING_TIME%");

		messages.put("mutesystem.timeformat.years", " Years ");
		messages.put("mutesystem.timeformat.year", " Year ");
		messages.put("mutesystem.timeformat.days", " Days ");
		messages.put("mutesystem.timeformat.day", " Day ");
		messages.put("mutesystem.timeformat.hours", " Hours ");
		messages.put("mutesystem.timeformat.hour", " Hour ");
		messages.put("mutesystem.timeformat.minutes", " Minutes ");
		messages.put("mutesystem.timeformat.minute", " Minute ");
		messages.put("mutesystem.timeformat.seconds", " Seconds ");
		messages.put("mutesystem.timeformat.second", " Second ");
		messages.put("mutesystem.timeformat.permanent", "PERMANENT");
	}

	@Override
	public void loadTextComponents() {
		if (fileConfiguration.getSection("textcomponents").getKeys().isEmpty()) {
			fileConfiguration.set("textcomponents.muteBroadcast",
					"&6[INFO] {hovertext: &7Muted by &c%MUTED_BY%\n" +
					"&7Reason: &c%REASON%\n" +
					"&7Duration: &c%DURATION%}");

			fileConfiguration.set("textcomponents.unmuteBroadcast",
					"&6[INFO] {hovertext: &7Unmuted by &c%UNMUTED_BY%\n" +
					"&7Reason: &c%REASON%}");

			fileConfiguration.set("textcomponents.currentMute",
					"&6[INFO] {hovertext: &8------- &eCurrent mute of &e%PLAYER% &8-------\n" +
					"&7Reason: &e%REASON%\n" +
					"&7Muted by: &e%MUTED_BY%\n" +
					"&7Muted on: &e%MUTE_START%\n" +
					"&7Muted until: &e%MUTE_END%\n" +
					"&7Mute duration: &e%DURATION%\n" +
					"&7Remaining time: &e%REMAINING_TIME%\n" +
					"&8------- &eCurrent mute of &e%PLAYER% &8-------}");

			fileConfiguration.set("textcomponents.pastMuteUnmuted",
					"&6[INFO] {hovertext: &8------- &ePrevious mute of &e%PLAYER% &8-------\n" +
					"&7Reason: &e%REASON%\n" +
					"&7Muted by: &e%MUTED_BY%\n" +
					"&7Muted on: &e%MUTE_START%\n" +
					"&7Muted until: &e%MUTE_END%\n" +
					"&7Mute duration: &e%DURATION%\n" +
					"&7Unmuted: &aYES\n" +
					"&7Unmuted by: &e%UNMUTED_BY%\n" +
					"&7Unmute reason: &e%UNMUTE_REASON%\n" +
					"&7Unmute time: &e%UNMUTE_TIME%\n" +
					"&8------- &ePrevious mute of &e%PLAYER% &8-------}");

			fileConfiguration.set("textcomponents.pastMuteNotUnmuted",
					"&6[INFO] {hovertext: &8------- &ePrevious mute of &e%PLAYER% &8-------\n" +
					"&7Reason: &e%REASON%\n" +
					"&7Muted by: &e%MUTED_BY%\n" +
					"&7Muted on: &e%MUTE_START%\n" +
					"&7Muted until: &e%MUTE_END%\n" +
					"&7Mute duration: &e%DURATION%\n" +
					"&7Unmuted: &cNO\n" +
					"&8------- &ePrevious mute of &e%PLAYER% &8-------}");

			super.save();
		}

		fileConfiguration.getSection("textcomponents")
				.getKeys()
				.forEach(key -> messages.put("textcomponents." + key, fileConfiguration.getString("textcomponents." + key)));
	}
}
