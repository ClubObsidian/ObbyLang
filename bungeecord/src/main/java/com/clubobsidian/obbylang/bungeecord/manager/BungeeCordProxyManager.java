package com.clubobsidian.obbylang.bungeecord.manager;

import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCordProxyManager extends ProxyManager<ProxiedPlayer> {

    @Override
    public void send(ProxiedPlayer player, String server) {
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(server);
        player.connect(serverInfo);
    }
}