/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.velocity.manager;

import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;
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