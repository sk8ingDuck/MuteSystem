package me.sk8ingduck.mutesystem;

import me.sk8ingduck.mutesystem.commands.Muteinfo;
import me.sk8ingduck.mutesystem.commands.Mute;
import me.sk8ingduck.mutesystem.commands.ClearMutes;
import me.sk8ingduck.mutesystem.commands.Unmute;
import me.sk8ingduck.mutesystem.listeners.Chat;
import me.sk8ingduck.mutesystem.listeners.PostLogin;
import me.sk8ingduck.mutesystem.utils.Config;
import me.sk8ingduck.mutesystem.utils.MuteRecord;
import me.sk8ingduck.mutesystem.utils.MySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.HashMap;

public final class MuteSystem extends Plugin {

    private HashMap<String, MuteRecord> mutes;
    private static MuteSystem bs;

    private Config config;

    private MySQL sql;
    @Override
    public void onEnable() {
        System.out.println("[MuteSystem] MuteSystem aktiviert!");
        bs = this;
        config = new Config();

        sql = new MySQL(config.getMySQLSetting("mysql.host"),
                config.getMySQLSetting("mysql.port"),
                config.getMySQLSetting("mysql.username"),
                config.getMySQLSetting("mysql.password"),
                config.getMySQLSetting("mysql.database"));
        mutes = new HashMap<>();

        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        pluginManager.registerCommand(this, new Mute("mute", "mutesystem.mute", ""));
        pluginManager.registerCommand(this, new Unmute("unmute", "mutesystem.unmute", ""));
        pluginManager.registerCommand(this, new Muteinfo("muteinfo", "mutesystem.muteinfo", ""));
        pluginManager.registerCommand(this, new ClearMutes("clearmutes", "mutesystem.clearmutes", "clearmute"));


        pluginManager.registerListener(this, new Chat());
        pluginManager.registerListener(this, new PostLogin());


        System.out.println("[MuteSystem] MuteSystem aktiviert!");
    }

    @Override
    public void onDisable() {
        System.out.println("[MuteSystem] MuteSystem deaktiviert!");
    }


    public static MuteSystem getBs() {
        return bs;
    }

    public Config getConfig() {
        return config;
    }

    public MySQL getSql() {
        return sql;
    }

    public HashMap<String, MuteRecord> getMutes() { return mutes; }
}
