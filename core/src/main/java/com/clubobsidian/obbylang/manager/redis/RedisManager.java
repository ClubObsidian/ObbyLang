package com.clubobsidian.obbylang.manager.redis;

import com.clubobsidian.obbylang.manager.RegisteredManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisManager implements RegisteredManager {

    private static RedisManager instance;

    public static RedisManager get() {
        if(instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }

    private Map<String, List<RedisClientWrapper>> wrappers;

    private RedisManager() {
        this.wrappers = new HashMap<>();
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