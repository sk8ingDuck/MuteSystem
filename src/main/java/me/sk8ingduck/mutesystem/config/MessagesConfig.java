package me.sk8ingduck.mutesystem.config;

import java.util.HashMap;

public class MessagesConfig extends Config {

	private final HashMap<String, String> messages;

	public MessagesConfig(String name, String path) {
		super(name, path);

		this.messages = new HashMap<>();

		messages.put("mutesystem.prefix", "&4MuteSystem &7» ");
		messages.put("mutesystem.consolename", "CONSOLE");
		messages.put("mutesystem.playernotfound", "&cSpieler %PLAYER% nicht gefunden.");
		messages.put("mutesystem.alreadymuted", "&cSpieler %PLAYER% ist bereits gemutet. Benutze /muteinfo %PLAYER%");
		messages.put("mutesystem.help", """
				&7----------------- &eMuteSystem &7-----------------
				&7/mute &a<Spieler> [<Zeit>] <Grund> &8- &7Mute einen Spieler
				&7/unmute &a<Spieler> &8- &7Entmute einen Spieler
				&7/muteinfo &a<Spieler> &8- &7Mutehistorie von Spieler ansehen
				&7/clearmutes &a<Spieler> &8- &7Lösche alle Muteeinträge
				&7----------------- &eMuteSystem &7-----------------""");

		messages.put("mutesystem.mute.syntax", "&cSyntax: /mute <Spieler> [<Zeit>] <Grund>");
		messages.put("mutesystem.mute.successful", """
				&7Spieler &c%PLAYER% &7wurde von &c%MUTED_BY% &7gemutet.
				&7Grund: &c%REASON%
				&7Zeit: &c%TIME%""");

		messages.put("mutesystem.unmute.syntax", "&cSyntax: /unmute <Spieler> [<Grund>]");
		messages.put("mutesystem.unmute.notmuted", "&cSpieler &e%PLAYER% &cist nicht gemutet.");
		messages.put("mutesystem.unmute.successful", "&7Spieler &e%PLAYER% &7wurde von &e%UNMUTED_BY% &7entmutet." +
				"\n&7Grund: &e%REASON%");
		messages.put("mutesystem.unmute.youarenolongermuted", "&7Spieler &e%PLAYER% &7hat dich entmutet." +
				"\n&7Grund: &e%REASON%");

		messages.put("mutesystem.muteinfo.syntax", "&cSyntax: /muteinfo <Spieler>");
		messages.put("mutesystem.muteinfo.nocurrentmute", "&cSpieler %PLAYER% ist aktuell nicht gemutet.");
		messages.put("mutesystem.muteinfo.currentmute", """
				&8------- &eaktueller Mute von &e%PLAYER% &8-------
				&7Grund: &e%REASON%
				&7Gemutet von: &e%MUTED_BY%
				&7Gemutet am: &e%MUTE_START%
				&7Gemutet bis: &e%MUTE_END%
				&7Mutedauer: &e%DURATION%
				&7Verbleibende Zeit: &e%REMAINING_TIME%
				&8------- &eaktueller Mute von &e%PLAYER% &8-------""");
		messages.put("mutesystem.muteinfo.nopastmute", "&cSpieler %PLAYER% hat keine früheren Mutes.");

		messages.put("mutesystem.muteinfo.pastmuteUnmuted", """
				&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------
				&7Grund: &e%REASON%
				&7Gemutet von: &e%MUTED_BY%
				&7Gemutet am: &e%MUTE_START%
				&7Gemutet bis: &e%MUTE_END%
				&7Mutedauer: &e%DURATION%
				&7Wurde entmutet: &aJA
				&7Entmutet von: &e%UNMUTED_BY%
				&7Entmutegrund: &e%UNMUTE_REASON%
				&7Entmutenzeit: &e%UNMUTE_TIME%
				&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------""");
		messages.put("mutesystem.muteinfo.pastmuteNotunmuted", """
				&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------
				&7Grund: &e%REASON%
				&7Gemutet von: &e%MUTED_BY%
				&7Gemutet am: &e%MUTE_START%
				&7Gemutet bis: &e%MUTE_END%
				&7Mutedauer: &e%DURATION%
				&7Wurde entmutet: &cNEIN
				&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------""");

		messages.put("mutesystem.clearmutes.syntax", "&cSyntax: /clearmutes <Spieler>");
		messages.put("mutesystem.clearmutes.successful", "&4!! &7Muteeinträge von &e%PLAYER% &7wurden von &e%UNMUTED_BY% &7gelöscht &4!!");
		messages.put("mutesystem.unmute.allmutesremoved", "&7Alle deine Mutes wurden von &e%PLAYER% &7entfernt :O");

		messages.put("mutesystem.mutemessage", """
				&cDu wurdest gemutet.
				&eGrund: &c%REASON%
				&eGemutet von: &c%MUTED_BY%
				&eVerbleibende Zeit: &c%REMAINING_TIME%""");

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

		messages.forEach((messagePath, message) -> messages.put(messagePath, (String) getPathOrSet(messagePath, message)));
	}

	public String get(String path) {
		return get(path, true);
	}

	public String get(String path, boolean prefix) {
		return ((prefix ? messages.get("mutesystem.prefix") : "") + messages.get(path));
	}

}
