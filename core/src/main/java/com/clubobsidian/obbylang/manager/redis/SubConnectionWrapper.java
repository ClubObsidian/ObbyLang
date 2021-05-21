package com.clubobsidian.obbylang.manager.redis;

import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public class SubConnectionWrapper {

    private StatefulRedisPubSubConnection<String, String> connection;
    private RedisPubSubListener<String, String> listener;
    private String channel;

    public SubConnectionWrapper(StatefulRedisPubSubConnection<String, String> connection, RedisPubSubListener<String, String> listener, String channel) {
        this.connection = connection;
        this.listener = listener;
        this.channel = channel;
    }

    public StatefulRedisPubSubConnection<String, String> getConnection() {
        return this.connection;
    }

    public RedisPubSubListener<String, String> getListener() {
        return this.listener;
    }

    public String getChannel() {
        return this.channel;
    }
}