/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.bukkit.manager.plugin.protocollib;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtocolLibManager implements RegisteredManager {

    private final Map<String, List<PacketAdapter>> scripts = new HashMap<>();
    private final ScriptManager scriptManager;

    public ProtocolLibManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    private void init(String declaringClass) {
        if(this.scripts.get(declaringClass) == null) {
            this.scripts.put(declaringClass, new ArrayList<>());
        }
    }

    public PacketAdapter register(String declaringClass, ScriptObjectMirror script, PacketType packetType) {
        return this.register(declaringClass, script, new PacketType[]{packetType});
    }

    public PacketAdapter register(String declaringClass, ScriptObjectMirror script, PacketType[] packetTypes) {
        this.init(declaringClass);
        CompiledScript compiledScript = this.scriptManager.getScript(declaringClass);

        boolean isServer = packetTypes[0].isServer();

        PacketAdapter adapter = null;
        if(isServer) {
            adapter = new PacketAdapter(BukkitObbyLangPlugin.get(), packetTypes) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    script.call(compiledScript, event);
                }
            };
        } else {
            adapter = new PacketAdapter(BukkitObbyLangPlugin.get(), packetTypes) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    script.call(compiledScript, event);
                }
            };
        }

        this.scripts.get(declaringClass).add(adapter);
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
        return adapter;
    }

    public PacketType packetType(String side, String packet) {
        Class<?> sideClass = null;
        if(side.equalsIgnoreCase("server")) {
            sideClass = PacketType.Play.Server.class;
        } else if(side.equalsIgnoreCase("client")) {
            sideClass = PacketType.Play.Client.class;
        }

        if(sideClass == null) {
            return null;
        }

        try {
            Object obj = sideClass.getDeclaredField(packet).get(null);
            if(obj == null) {
                return null;
            }
            return (PacketType) obj;
        } catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(PacketAdapter packetAdapter : this.scripts.get(declaringClass)) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
        }
        this.scripts.keySet().remove(declaringClass);
    }

    public boolean unregister(String declaringClass, PacketAdapter adapter) {
        this.init(declaringClass);
        List<PacketAdapter> adapterList = this.scripts.get(declaringClass);
        for(PacketAdapter packetAdapter : adapterList) {
            if(packetAdapter.equals(adapter)) {
                ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
                adapterList.remove(adapter);
                return true;
            }
        }
        return false;
    }
}