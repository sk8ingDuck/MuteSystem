package me.sk8ingduck.mutesystem.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config {

    private final HashMap<String, String> msgCache;
    private final String prefix;

    public Config() {
        msgCache = new HashMap<>();
        setupConfig();
        loadMessages();
        prefix = msgCache.get("mutesystem.prefix");
    }

    private void setupConfig() {
        File ord = new File("plugins/MuteSystem");
        if (!ord.exists())
            ord.mkdir();
        File messages = new File("plugins/MuteSystem/messages.yml");
        File database = new File("plugins/MuteSystem/mysql.yml");

        try {
            if (!messages.exists())
                messages.createNewFile();
            if (!database.exists())
                database.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(messages);
            Configuration sql = ConfigurationProvider.getProvider(YamlConfiguration.class).load(database);
            try {
                if (!sql.contains("mysql.username")) {
                    sql.set("mysql.host", "localhost");
                    sql.set("mysql.port", "3306");
                    sql.set("mysql.username", "root");
                    sql.set("mysql.password", "password");
                    sql.set("mysql.database", "db");
                }
                if (!cfg.contains("mutesystem.prefix")) {
                    cfg.set("mutesystem.prefix", "&4MuteSystem &7» ");
                    cfg.set("mutesystem.consolename", "CONSOLE");
                    cfg.set("mutesystem.playernotfound", "&cSpieler %PLAYER% nicht gefunden.");
                    cfg.set("mutesystem.alreadymuted", "&cSpieler %PLAYER% ist bereits gemutet. Benutze /muteinfo %PLAYER%");
                    cfg.set("mutesystem.help", "&7----------------- &eMuteSystem &7-----------------\n&7/mute &a<Spieler> [<Zeit>] <Grund> &8- &7Mute einen Spieler\n&7/unmute &a<Spieler> &8- &7Entmute einen Spieler\n&7/muteinfo &a<Spieler> &8- &7Mutehistorie von Spieler ansehen\n&7/clearmutes &a<Spieler> &8- &7Lösche alle Muteeinträge\n&7----------------- &eMuteSystem &7-----------------");

                    cfg.set("mutesystem.mute.syntax", "&cSyntax: /mute <Spieler> [<Zeit>] <Grund>");
                    cfg.set("mutesystem.mute.successful", "&7Spieler &c%PLAYER% &7wurde von &c%MUTED_BY% &7gemutet.\n&7Grund: &c%REASON%\n&7Zeit: &c%TIME%");

                    cfg.set("mutesystem.unmute.syntax", "&cSyntax: /unmute <Spieler> [<Grund>]");
                    cfg.set("mutesystem.unmute.notmuted", "&cSpieler &e%PLAYER% &cist nicht gemutet.");
                    cfg.set("mutesystem.unmute.successful", "&7Spieler &e%PLAYER% &7wurde von &e%UNMUTED_BY% &7entmutet.\n&7Grund: &e%REASON%");
                    cfg.set("mutesystem.unmute.youarenolongermuted", "&7Spieler &e%PLAYER% &7hat dich entmutet.\n&7Grund: &e%REASON%");

                    cfg.set("mutesystem.muteinfo.syntax", "&cSyntax: /muteinfo <Spieler>");
                    cfg.set("mutesystem.muteinfo.nocurrentmute", "&cSpieler %PLAYER% ist aktuell nicht gemutet.");
                    cfg.set("mutesystem.muteinfo.currentmute", "&8------- &eaktueller Mute von &e%PLAYER% &8-------\n&7Grund: &e%REASON%\n&7Gemutet von: &e%MUTED_BY%\n&7Gemutet am: &e%MUTE_START%\n&7Gemutet bis: &e%MUTE_END%\n&7Mutedauer: &e%DURATION%\n&7Verbleibende Zeit: &e%REMAINING_TIME%\n&8------- &eaktueller Mute von &e%PLAYER% &8-------");
                    cfg.set("mutesystem.muteinfo.nopastmute", "&cSpieler %PLAYER% hat keine früheren Mutes.");

                    cfg.set("mutesystem.muteinfo.pastmuteUnmuted", "&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------\n&7Grund: &e%REASON%\n&7Gemutet von: &e%MUTED_BY%\n&7Gemutet am: &e%MUTE_START%\n&7Gemutet bis: &e%MUTE_END%\n&7Mutedauer: &e%DURATION%\n&7Wurde entmutet: &aJA\n&7Entmutet von: &e%UNMUTED_BY%\n&7Entmutegrund: &e%UNMUTE_REASON%\n&7Entmutenzeit: &e%UNMUTE_TIME%\n&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------");
                    cfg.set("mutesystem.muteinfo.pastmuteNotunmuted", "&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------\n&7Grund: &e%REASON%\n&7Gemutet von: &e%MUTED_BY%\n&7Gemutet am: &e%MUTE_START%\n&7Gemutet bis: &e%MUTE_END%\n&7Mutedauer: &e%DURATION%\n&7Wurde entmutet: &cNEIN\n&8------- &eFrüherer Mute von von &e%PLAYER% &7(&e%INDEX%&7) &8-------");

                    cfg.set("mutesystem.clearmutes.syntax", "&cSyntax: /clearmutes <Spieler>");
                    cfg.set("mutesystem.clearmutes.successful", "&4!! &7Muteeinträge von &e%PLAYER% &7wurden von &e%UNMUTED_BY% &7gelöscht &4!!");
                    cfg.set("mutesystem.unmute.allmutesremoved", "&7Alle deine Mutes wurden von &e%PLAYER% &7entfernt :O");

                    cfg.set("mutesystem.mutemessage", "&cDu wurdest gemutet.\n&eGrund: &c%REASON%\n&eGemutet von: &c%MUTED_BY%\n&eVerbleibende Zeit: &c%REMAINING_TIME%");

                    cfg.set("mutesystem.timeformat.years", " Jahre ");
                    cfg.set("mutesystem.timeformat.year", " Jahr ");
                    cfg.set("mutesystem.timeformat.days", " Tage ");
                    cfg.set("mutesystem.timeformat.day", " Tag ");
                    cfg.set("mutesystem.timeformat.hours", " Stunden ");
                    cfg.set("mutesystem.timeformat.hour", " Stunde ");
                    cfg.set("mutesystem.timeformat.minutes", " Minuten ");
                    cfg.set("mutesystem.timeformat.minute", " Minute ");
                    cfg.set("mutesystem.timeformat.seconds", " Sekunden ");
                    cfg.set("mutesystem.timeformat.second", " Sekunde ");
                    cfg.set("mutesystem.timeformat.permanent", "PERMANENT");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cfg, messages);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(sql, database);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadMessages() {
        try {
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("plugins/MuteSystem/messages.yml"));
            cacheConfigRecursive(cfg, "mutesystem");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Put the config values in a hashmap to reduce config accesses and therefore increase performance
     * @param config the config
     * @param root the root path
     */
    public void cacheConfigRecursive(Configuration config, String root) {
        if (config.getString(root) != null && !config.getString(root).isEmpty()) {
            msgCache.put(root, config.getString(root));
        } else {
            Collection<String> keys = config.getSection(root).getKeys();
            keys.forEach(key -> cacheConfigRecursive(config, root + "." + key)); //recursively get child of section
        }
    }

    public String get(String path) {
        return get(path, false);
    }

    public String get(String path, boolean prefix) {
        String output = "";
        if (prefix) output += this.prefix;
        output += msgCache.get(path);
        return output.replaceAll("&", "§");
    }

    public String getMySQLSetting(String setting) {
        String str = "";
        File msgs = new File("plugins/MuteSystem/mysql.yml");
        try {
            Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(msgs);
            str = cfg.getString(setting);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
