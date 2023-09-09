package me.sk8ingduck.mutesystem.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Util {

    /* Executor pool for async methods */
    public static final ExecutorService pool = Executors.newCachedThreadPool();

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


}
