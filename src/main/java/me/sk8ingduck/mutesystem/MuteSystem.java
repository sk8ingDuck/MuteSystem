package me.sk8ingduck.mutesystem;

import me.sk8ingduck.mutesystem.commands.Muteinfo;
import me.sk8ingduck.mutesystem.commands.Mute;
import me.sk8ingduck.mutesystem.commands.ClearMutes;
import me.sk8ingduck.mutesystem.commands.Unmute;
import me.sk8ingduck.mutesystem.config.DBConfig;
import me.sk8ingduck.mutesystem.config.MessagesConfig;
import me.sk8ingduck.mutesystem.listeners.Chat;
import me.sk8ingduck.mutesystem.listeners.PostLogin;
import me.sk8ingduck.mutesystem.utils.MuteRecord;
import me.sk8ingduck.mutesystem.utils.MySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.HashMap;

public final class MuteSystem extends Plugin {

    private HashMap<String, MuteRecord> mutes;
    private static MuteSystem bs;

    private MessagesConfig msgsConfig;

    private MySQL sql;
    @Override
    public void onEnable() {
        System.out.println("[MuteSystem] MuteSystem aktiviert!");
        bs = this;
        DBConfig dbConfig = new DBConfig("database.yml", "plugins/MuteSystem");
        msgsConfig = new MessagesConfig("messages.yml", "plugins/MuteSystem");

        sql = new MySQL(dbConfig.getHost(), dbConfig.getPort(), dbConfig.getUsername(), dbConfig.getPassword(), dbConfig.getDatabase());
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

    public MessagesConfig getMessagesConfig() {
        return msgsConfig;
    }

    public MySQL getSql() {
        return sql;
    }

    public HashMap<String, MuteRecord> getMutes() { return mutes; }
}
