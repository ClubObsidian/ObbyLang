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

package com.clubobsidian.obbylang.manager.redis;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisManager implements RegisteredManager {

    private final Map<String, List<RedisClientWrapper>> wrappers = new ConcurrentHashMap<>();

    @Inject
    private RedisManager() {
    }

    public RedisClientWrapper create(String declaringClass, String ip, int port, String password) {
        return this.create(declaringClass, ip, port, password, false);
    }

    public RedisClientWrapper create(String declaringClass, String ip, int port, String password, boolean pingBefore) {
        try {
            return this.create(declaringClass, "redis://" + URLEncoder.encode(password, "UTF-8") + "@" + ip + ":" + port + "/0", pingBefore);
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RedisClientWrapper create(String declaringClass, String connection) {
        return this.create(declaringClass, connection, false);
    }

    public RedisClientWrapper create(String declaringClass, String connection, boolean pingBefore) {
        RedisClientWrapper wrapper = new RedisClientWrapper(declaringClass, connection, pingBefore);
        this.init(declaringClass);
        this.wrappers.get(declaringClass).add(wrapper);
        return wrapper;
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(RedisClientWrapper w : this.wrappers.get(declaringClass)) {
            w.unload();
        }

        this.wrappers.remove(declaringClass);
    }

    private void init(String declaringClass) {
        if(this.wrappers.get(declaringClass) == null) {
            this.wrappers.put(declaringClass, new ArrayList<>());
        }
    }
}