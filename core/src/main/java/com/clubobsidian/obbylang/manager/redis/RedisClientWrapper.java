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

package com.clubobsidian.obbylang.manager.redis;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class RedisClientWrapper {

    private final ScriptManager scriptManager;
    private final ObbyLangPlugin plugin;
    private final String declaringClass;
    private final RedisClient client;
    private final boolean pingBefore;
    private final List<SubConnectionWrapper> registerConnections = new CopyOnWriteArrayList<>();
    private final Map<String, StatefulRedisConnection<String, String>> pubCons = new ConcurrentHashMap<>();

    public RedisClientWrapper(ScriptManager scriptManager,
                              ObbyLangPlugin plugin,
                              String declaringClass,
                              String connection,
                              boolean pingBefore) {
        this.scriptManager = scriptManager;
        this.plugin = plugin;
        this.declaringClass = declaringClass;
        this.client = this.createRedisClient(connection);
        this.pingBefore = pingBefore;
    }

    public void register(final ScriptObjectMirror script, final String registeredChannel) {
        StatefulRedisPubSubConnection<String, String> con = this.client.connectPubSub();

        RedisPubSubListener<String, String> listener = new RedisPubSubListener<>() {
            @Override
            public void message(String channel, String message) {
                if(channel.equalsIgnoreCase(registeredChannel)) {
                    CompiledScript owner = scriptManager.getScript(declaringClass);
                    script.call(owner, message);
                }
            }

            @Override
            public void message(String pattern, String channel, String message) {
            }

            @Override
            public void subscribed(String channel, long count) {
            }

            @Override
            public void psubscribed(String pattern, long count) {
            }

            @Override
            public void unsubscribed(String channel, long count) {
            }

            @Override
            public void punsubscribed(String pattern, long count) {
            }
        };

        con.addListener(listener);
        con.sync().subscribe(registeredChannel);

        this.registerConnections.add(new SubConnectionWrapper(con, listener, registeredChannel));
    }

    @Deprecated
    public void publish(final String channel, final String message) {
        publishAsync(channel, message);
    }

    public void publishAsync(final String channel, final String message) {
        if(this.pubCons.get(channel) == null) {
            this.pubCons.put(channel, this.client.connect());
        }

        this.pubCons.get(channel).async().publish(channel, message);
    }

    public void publishSync(final String channel, final String message) {
        if(this.pubCons.get(channel) == null) {
            this.pubCons.put(channel, this.client.connect());
        }

        this.pubCons.get(channel).sync().publish(channel, message);
    }

    protected void unload() {
        for(SubConnectionWrapper wrapper : this.registerConnections) {
            wrapper.getConnection().removeListener(wrapper.getListener());
            wrapper.getConnection().async().unsubscribe(wrapper.getChannel());
        }

        for(StatefulRedisConnection<String, String> con : this.pubCons.values()) {
            con.closeAsync();
        }

        this.client.shutdownAsync();
    }

    private RedisClient createRedisClient(String connection) {
        RedisClient client = RedisClient.create(connection);
        client.setOptions(ClientOptions
                .builder()
                .autoReconnect(true)
                .pingBeforeActivateConnection(this.pingBefore)
                .build());
        client.getResources().eventBus().get().subscribe(event -> {
            String eventMessage = this.declaringClass + " " + event.getClass().getName();
            this.plugin.getLogger().log(Level.SEVERE, eventMessage);
        });
        return client;
    }
}