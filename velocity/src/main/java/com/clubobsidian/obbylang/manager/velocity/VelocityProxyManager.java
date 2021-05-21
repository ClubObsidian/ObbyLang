package com.clubobsidian.obbylang.manager.velocity;

import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class VelocityProxyManager extends ProxyManager<Player> {

    @Override
    public void send(Player player, String server) {
        Optional<RegisteredServer> serverInfo = VelocityObbyLangPlugin
                .get()
                .getServer()
                .getServer(server);
        serverInfo.ifPresent(info -> {
            player.createConnectionRequest(info).fireAndForget();
        });
    }
}