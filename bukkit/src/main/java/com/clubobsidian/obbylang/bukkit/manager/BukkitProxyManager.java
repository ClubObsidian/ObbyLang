package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

public class BukkitProxyManager extends ProxyManager<Player> {

    @Override
    public void send(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(BukkitObbyLangPlugin.get(), "BungeeCord", out.toByteArray());
    }
}