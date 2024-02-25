package me.sk8ingduck.mutesystem.config.language;

import me.sk8ingduck.mutesystem.config.MessagesConfig;

public class MessagesGermanConfig extends MessagesConfig {

	public MessagesGermanConfig(String name, String path) {
		super(name, path);
	}

	@Override
	public void loadMessages() {
		messages.put("mutesystem.prefix", "&4MuteSystem &7» ");
		messages.put("mutesystem.consolename", "CONSOLE");
		messages.put("mutesystem.playernotfound", "&cSpieler %PLAYER% nicht gefunden.");
		messages.put("mutesystem.alreadymuted", "&cSpieler %PLAYER% ist bereits gemutet.\n" +
				"Benutze /muteinfo %PLAYER%");
		messages.put("mutesystem.help",
				"&7&m-----------------&r&e« MuteSystem »&7&m-----------------\n\n" +
						"&7/mute &a<Spieler> [<Zeit>] <Grund> &8- &7Mute einen Spieler\n" +
						"&7/mute &a<Spieler> #<template-id> &8- &7Mute einen Spieler\n" +
						"&7/unmute &a<Spieler> &8- &7Entmute einen Spieler\n" +
						"&7/muteinfo &a<Spieler> &8-&7Zeige die Mute-Historie eines Spielers\n" +
						"&7/clearmutes &a<Spieler> &8- &7Lösche alle Mute-Einträge\n" +
						"&7/mutetemplate &alist &8- &7Zeige Mute-Vorlagen\n" +
						"&7/mutetemplate &aadd <Zeit> <Grund> &8- &7Mutevorlage hinzufügen\n" +
						"&7/mutetemplate &aedit <id> <Zeit> <Grund> &8- &7Mutevorlage bearbeiten\n" +
						"&7/mutetemplate &aremove <id> &8- &7Entferne eine Mutevorlage\n" +
						"&7/muteReload &8- &7Lade die Nachrichten aus der messages.yml neu\n\n" +
						"&7&m-----------------&r&e« MuteSystem »&7&m-----------------");
		messages.put("mutesystem.mute.syntax", "&cSyntax: /mute <Spieler> [<Zeit>] <Grund>");
		messages.put("mutesystem.mute.nopermission", "&cDu hast nicht genug Rechte!");
		messages.put("mutesystem.mute.insufficient_rank", "&cDu hast keine Rechte diesen Spieler zu muten.");

		messages.put("mutesystem.mute.successful",
				"&7Spieler &c%PLAYER% &7wurde gemutet! {muteBroadcast}");
		messages.put("mutesystem.unmute.syntax", "&cSyntax: /unmute <Spieler> [<Grund>]");
		messages.put("mutesystem.unmute.notmuted", "&cSpieler &e%PLAYER% &cist nicht gemutet.");
		messages.put("mutesystem.unmute.successful", "&7Spieler &e%PLAYER% &7wurde &7entmutet {unmuteBroadcast}");
		messages.put("mutesystem.muteinfo.syntax", "&cSyntax: /muteinfo <Spieler>");
		messages.put("mutesystem.muteinfo.nocurrentmute", "&cSpieler %PLAYER% ist aktuell nicht gemutet.");
		messages.put("mutesystem.unmute.youarenolongermuted", "&7Du wurdest von &e%PLAYER% &7entmutet." +
				"\n&7Grund: &e%REASON%");

		messages.put("mutesystem.muteinfo.currentmute", "&7Aktueller Mute von &e%PLAYER% {currentMute}");
		messages.put("mutesystem.muteinfo.nopastmute", "&cSpieler %PLAYER% hat keine vorherigen Mutes.");
		messages.put("mutesystem.muteinfo.pastmuteUnmuted", "&7Muteeintrag von &e%DATE% {pastMuteUnmuted}");
		messages.put("mutesystem.muteinfo.pastmuteNotunmuted", "&7Muteeintrag von &e%DATE% {pastMuteNotUnmuted}");

		messages.put("mutesystem.clearmutes.syntax", "&cSyntax: /clearmutes <Spieler>");
		messages.put("mutesystem.clearmutes.successful", "&4!! &7Mutes von &e%PLAYER% &7wurden von &e%UNMUTED_BY% &7gelöscht &4!!");
		messages.put("mutesystem.unmute.allmutesremoved", "&7Alle deine Mutes wurden von &e%PLAYER% &7entfernt :O");

		messages.put("mutesystem.template.syntax",
				"&cSyntaxfehler. Möglichkeiten:\n" +
						"&7/mutetemplate &alist &8- &7Liste vorhandener Mutevorlagen\n" +
						"&7/mutetemplate &aadd <Zeit> <Grund> &8- &7Mutevorlage hinzufügen\n" +
						"&7/mutetemplate &aedit <ID> <Zeit> <Grund> &8- &7Mutevorlage editieren\n" +
						"&7/mutetemplate &aremove <ID> &8- &7Mutevorlage entfernen");
		messages.put("mutesystem.template.list.header", "&7------------------- &eMutevorlagen &7-------------------");
		messages.put("mutesystem.template.list.content", "&7(&eID: %ID%&7) &8| &c%DURATION% &8| &e%REASON%");
		messages.put("mutesystem.template.list.footer", "&7------------------- &eMutevorlagen &7-------------------");
		messages.put("mutesystem.template.add.successful", "&aMutevorlage hinzugefügt!");
		messages.put("mutesystem.template.add.error", "&cMutevorlage nicht gefunden!");
		messages.put("mutesystem.template.edit.successful", "&aMutevorlage bearbeitet!");
		messages.put("mutesystem.template.remove.successful", "&aMutevorlage entfernt!");

		messages.put("mutesystem.mutemessage",
				"&cDu wurdest gemutet.\n" +
						"&eGrund: &c%REASON%\n" +
						"&eGemutet von: &c%MUTED_BY%\n" +
						"&eVerbleibende Zeit: &c%REMAINING_TIME%");

		messages.put("mutesystem.timeformat.years", " Jahre ");
		messages.put("mutesystem.timeformat.year", " Jahr ");
		messages.put("mutesystem.timeformat.days", " Tage ");
		messages.put("mutesystem.timeformat.day", " Tag ");
		messages.put("mutesystem.timeformat.hours", " Stunden ");
		messages.put("mutesystem.timeformat.hour", " Stunde ");
		messages.put("mutesystem.timeformat.minutes", " Minuten ");
		messages.put("mutesystem.timeformat.minute", " Minute ");
		messages.put("mutesystem.timeformat.seconds", " Sekunden ");
		messages.put("mutesystem.timeformat.second", " Sekunde ");
		messages.put("mutesystem.timeformat.permanent", "PERMANENT");
	}

	@Override
	public void loadTextComponents() {
		if (fileConfiguration.getSection("textcomponents").getKeys().isEmpty()) {
			fileConfiguration.set("textcomponents.muteBroadcast",
					"&6[INFO] {hovertext: &7Gemutet von &c%MUTED_BY%\n" +
					"&7Grund: &c%REASON%\n" +
					"&7Dauer: &c%DURATION%}");

			fileConfiguration.set("textcomponents.unmuteBroadcast",
					"&6[INFO] {hovertext: &7Entmutet von &c%UNMUTED_BY%\n" +
					"&7Grund: &c%REASON%}");

			fileConfiguration.set("textcomponents.currentMute",
					"&6[INFO] {hovertext: &8------- &eAktueller Mute von &e%PLAYER% &8-------\n" +
					"&7Grund: &e%REASON%\n" +
					"&7Gemutet von: &e%MUTED_BY%\n" +
					"&7Gemutet am: &e%MUTE_START%\n" +
					"&7Gemutet bis: &e%MUTE_END%\n" +
					"&7Mutedauer: &e%DURATION%\n" +
					"&7Verbleibende Zeit: &e%REMAINING_TIME%\n" +
					"&8------- &eAktueller Mute von &e%PLAYER% &8-------}");

			fileConfiguration.set("textcomponents.pastMuteUnmuted",
					"&6[INFO] {hovertext: &8------- &eFrüherer Mute von &e%PLAYER% &7(&e%INDEX%&7) &8-------\n" +
					"&7Grund: &e%REASON%\n" +
					"&7Gemutet von: &e%MUTED_BY%\n" +
					"&7Gemutet am: &e%MUTE_START%\n" +
					"&7Gemutet bis: &e%MUTE_END%\n" +
					"&7Mutedauer: &e%DURATION%\n" +
					"&7Entmutet: &aJA\n" +
					"&7Entmutet von: &e%UNMUTED_BY%\n" +
					"&7Entmute Grund: &e%UNMUTE_REASON%\n" +
					"&7Entmute Zeit: &e%UNMUTE_TIME%\n" +
					"&8------- &eFrüherer Mute von &e%PLAYER% &7(&e%INDEX%&7) &8-------}");

			fileConfiguration.set("textcomponents.pastMuteNotUnmuted",
					"&6[INFO] {hovertext: &8------- &eFrüherer Mute von &e%PLAYER% &7(&e%INDEX%&7) &8-------\n" +
					"&7Grund: &e%REASON%\n" +
					"&7Gemutet von: &e%MUTED_BY%\n" +
					"&7Gemutet am: &e%MUTE_START%\n" +
					"&7Gemutet bis: &e%MUTE_END%\n" +
					"&7Mutedauer: &e%DURATION%\n" +
					"&7Entmutet: &cNEIN\n" +
					"&8------- &eFrüherer Mute von &e%PLAYER% &7(&e%INDEX%&7) &8-------}");

			super.save();
		}

		fileConfiguration.getSection("textcomponents")
				.getKeys()
				.forEach(key -> messages.put("textcomponents." + key, fileConfiguration.getString("textcomponents." + key)));
	}
}
