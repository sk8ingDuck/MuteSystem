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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
     * @param uuid the uuid
     * @param acceptor the name acceptor
     */
    public static void UUIDtoName(String uuid, Consumer<String> acceptor) {
        if (uuid == null || uuid.equals("")) {
            acceptor.accept(null);
        } else {
            try {
                UUIDFetcher.getName(UUID.fromString(uuid), acceptor);
            } catch (IllegalArgumentException ex) {
                acceptor.accept(uuid);
            }
        }
    }

    /**
     * Check if a player can ban another player
     * @param banner the player who wants to ban
     * @param banned the player who will be banned
     * @return true if the banner can ban the banned player
     */
    public static boolean canMute(ProxiedPlayer banner, String banned) {
        if (!luckPermsEnabled) return true;

        LuckPerms luckPerms = LuckPermsProvider.get();
        User bannerUser = luckPerms.getUserManager().getUser(banner.getUniqueId());

        if (bannerUser == null) return true;

        User bannedUser;
        try {
            UUID bannedUUID = UUID.fromString(banned);
            bannedUser = luckPerms.getUserManager().getUser(bannedUUID);
            if (bannedUser == null) bannedUser = luckPerms.getUserManager().loadUser(bannedUUID).get();
        } catch (IllegalArgumentException ex) {
            bannedUser = luckPerms.getUserManager().getUser(banned);
        } catch (ExecutionException | InterruptedException e) {
            return true;
        }

        if (bannedUser == null) return true;

        Group bannedGroup = luckPerms.getGroupManager().getGroup(bannedUser.getPrimaryGroup());
        Group bannerGroup = luckPerms.getGroupManager().getGroup(bannerUser.getPrimaryGroup());

        if (bannerGroup == null || bannedGroup == null) return true;

        int maxWeightBanned = bannedGroup.getNodes(NodeType.WEIGHT).stream()
                .mapToInt(WeightNode::getWeight)
                .max()
                .orElse(0);

        int maxWeightBanner = bannerGroup.getNodes(NodeType.WEIGHT).stream()
                .mapToInt(WeightNode::getWeight)
                .max()
                .orElse(0);

        return maxWeightBanned <= maxWeightBanner;
    }
}
