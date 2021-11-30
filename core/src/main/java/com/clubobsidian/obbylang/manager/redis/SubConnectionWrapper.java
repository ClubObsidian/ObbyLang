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

import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public class SubConnectionWrapper {

    private final StatefulRedisPubSubConnection<String, String> connection;
    private final RedisPubSubListener<String, String> listener;
    private final String channel;

    public SubConnectionWrapper(StatefulRedisPubSubConnection<String, String> connection,
                                RedisPubSubListener<String, String> listener, String channel) {
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