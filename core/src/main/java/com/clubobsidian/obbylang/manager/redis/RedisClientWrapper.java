package com.clubobsidian.obbylang.manager.redis;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class RedisClientWrapper {


    private final String declaringClass;
    private final RedisClient client;
    private final boolean pingBefore;
    private final List<SubConnectionWrapper> registerConnections = new CopyOnWriteArrayList<>();
    private final Map<String, StatefulRedisConnection<String, String>> pubCons = new ConcurrentHashMap<>();

    public RedisClientWrapper(String declaringClass, String connection, boolean pingBefore) {
        this.declaringClass = declaringClass;
        this.client = this.createRedisClient(connection);
        this.pingBefore = pingBefore;
    }

    public void register(final ScriptObjectMirror script, final String registeredChannel) {
        StatefulRedisPubSubConnection<String, String> con = this.client.connectPubSub();

        RedisPubSubListener<String, String> listener = new RedisPubSubListener<String, String>() {
            @Override
            public void message(String channel, String message) {
                if(channel.equalsIgnoreCase(registeredChannel)) {
                    CompiledScript owner = ScriptManager.get().getScript(declaringClass);
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
        client.getResources().eventBus().get().subscribe(event ->
        {
            String eventMessage = this.declaringClass + " " + event.getClass().getName();
            ObbyLang.get().getPlugin().getLogger().log(Level.SEVERE, eventMessage);
        });
        return client;
    }
}