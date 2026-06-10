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

package com.clubobsidian.obbylang.manager.datastore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.openjdk.nashorn.api.scripting.JSObject;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

public class DataStore {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS kv (key TEXT PRIMARY KEY, value TEXT NOT NULL)";
    private static final String SELECT_VALUE = "SELECT value FROM kv WHERE key=?";
    private static final String UPSERT = "INSERT OR REPLACE INTO kv (key, value) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM kv WHERE key=?";
    private static final String EXISTS = "SELECT 1 FROM kv WHERE key=?";
    private static final String SELECT_KEYS = "SELECT key FROM kv";

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Connection connection;
    private final BlockingQueue<Runnable> writeQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private final Gson gson = new GsonBuilder().disableInnerClassSerialization().create();

    DataStore(File file, ExecutorService executor) throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        config.setOpenMode(SQLiteOpenMode.FULLMUTEX);

        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        dataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());

        this.connection = dataSource.getConnection();
        this.executor = executor;
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(CREATE_TABLE);
        }
    }

    public void set(String key, String value) {
        this.cache.put(key, value);
        boolean addedToQueue = this.writeQueue.offer(() -> {
            try (PreparedStatement ps = this.connection.prepareStatement(UPSERT)) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        if (addedToQueue) {
            this.executor.submit(this::drainQueue);
        }
    }

    public void delete(String key) {
        this.cache.remove(key);
        boolean addedToQueue = this.writeQueue.offer(() -> {
            try (PreparedStatement ps = this.connection.prepareStatement(DELETE)) {
                ps.setString(1, key);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        if (addedToQueue) {
            this.executor.submit(this::drainQueue);
        }
    }

    public void setList(String key, Object array) {
        Collection<Object> values = array instanceof JSObject
                ? ((JSObject) array).values()
                : array instanceof Collection
                ? (Collection<Object>) array
                : Collections.singleton(array);
        List<String> list = new ArrayList<>();
        for (Object v : values) {
            if (v != null) {
                list.add(v.toString());
            } else {
                list.add(null);
            }
        }
        this.set(key, this.gson.toJson(list));
    }

    public String get(String key) {
        String cached = this.cache.get(key);
        if (cached != null) {
            return cached;
        }
        try (PreparedStatement ps = this.connection.prepareStatement(SELECT_VALUE)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object get(String key, JSObject fn) {
        String value = this.get(key);
        if (value == null) {
            return null;
        }
        return fn.call(null, value);
    }

    public boolean has(String key) {
        String cached = this.cache.get(key);
        if (cached != null) {
            return true;
        }
        try (PreparedStatement ps = this.connection.prepareStatement(EXISTS)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> keys() {
        Set<String> result = new LinkedHashSet<>();
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_KEYS)) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.addAll(this.cache.keySet());
        return new ArrayList<>(result);
    }

    public List<String> getList(String key) {
        String json = this.get(key);
        if (json == null) {
            return null;
        }
        return this.gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public List<Object> getList(String key, JSObject jsObj) {
        List<String> raw = this.getList(key);
        if (raw == null) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        for (String elem : raw) {
            result.add(jsObj.call(null, elem));
        }
        return result;
    }

    private void drainQueue() {
        Runnable task;
        while ((task = this.writeQueue.poll()) != null) {
            task.run();
        }
    }

    private void drain() {
        CompletableFuture<Void> sentinel = new CompletableFuture<>();
        this.writeQueue.offer(() -> sentinel.complete(null));
        this.executor.submit(this::drainQueue);
        try {
            sentinel.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean close() {
        this.drain();
        try {
            this.connection.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
