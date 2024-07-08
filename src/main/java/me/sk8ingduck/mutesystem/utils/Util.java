package me.sk8ingduck.mutesystem.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.WeightNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Util {

    /* Executor pool for async methods */
    public static final ExecutorService pool = Executors.newCachedThreadPool();
    static boolean luckPermsEnabled = ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null;

    /**
     * Helper method to broadcast a message
     * @param message the message to be broadcasted
     * @param permission the permission needed to recieve the message
     */
    public static void broadcastMessage(BaseComponent[] message, String permission) {
        ProxyServer.getInstance().getPlayers().stream().filter(player -> player.hasPermission(permission))
                .forEach(player -> player.sendMessage(message));
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }

    /**
     * Try to resolve an uuid to a name async
     *
     * @param uuid the uuid
     */
    public static String UUIDtoName(String uuid) {
        if (uuid == null || uuid.equals("")) {
            return null;
        }
        String name;
        try {
            name = UUIDFetcher.getName(UUID.fromString(uuid));
        } catch (IllegalArgumentException ex) {
            name = uuid;
        }
        return name;
    }


    /**
     * Check if a player can mute another player.
     * @param muted the player who should be muted.
     * @param muter the player who is executing the mute command.
     * @return true if the muter can mute the muted player.
     */
    public static boolean canMute(ProxiedPlayer muter, String muted) {
        if (!luckPermsEnabled) return true;

        LuckPerms luckPerms = LuckPermsProvider.get();
        User muterUser = luckPerms.getUserManager().getUser(muter.getUniqueId());

        if (muterUser == null) return true;

        User mutedUser;
        try {
            UUID mutedUUID = UUID.fromString(muted);
            mutedUser = luckPerms.getUserManager().getUser(mutedUUID);
            if (mutedUser == null) mutedUser = luckPerms.getUserManager().loadUser(mutedUUID).get();
        } catch (IllegalArgumentException ex) {
            mutedUser = luckPerms.getUserManager().getUser(muted);
        } catch (ExecutionException | InterruptedException e) {
            return true;
        }

        if (mutedUser == null) return true;

        Group mutedGroup = luckPerms.getGroupManager().getGroup(mutedUser.getPrimaryGroup());
        Group muterGroup = luckPerms.getGroupManager().getGroup(muterUser.getPrimaryGroup());

        if (muterGroup == null || mutedGroup == null) return true;

        int maxWeightMuted = mutedGroup.getNodes(NodeType.WEIGHT).stream()
                .mapToInt(WeightNode::getWeight)
                .max()
                .orElse(0);

        int maxWeightMuter = muterGroup.getNodes(NodeType.WEIGHT).stream()
                .mapToInt(WeightNode::getWeight)
                .max()
                .orElse(0);

        return maxWeightMuted <= maxWeightMuter;
    }

    public static void sendMessageToConsole(String message) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(message));
    }
}
