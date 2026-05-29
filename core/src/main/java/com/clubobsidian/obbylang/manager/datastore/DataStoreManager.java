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

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.google.inject.Inject;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DataStoreManager implements RegisteredManager {

    private final File storesDir;
    private final Map<String, List<DataStore>> registeredStores = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "datastore-writer");
        thread.setDaemon(true);
        return thread;
    });

    @Inject
    private DataStoreManager(ObbyLangPlugin plugin) {
        this.storesDir = new File(plugin.getDataFolder(), "stores");
        this.storesDir.mkdirs();
    }

    public DataStore register(String owner, String name) {
        try {
            DataStore store = new DataStore(new File(this.storesDir, name + ".db"), this.executor);
            this.registeredStores
                .computeIfAbsent(owner, k -> new CopyOnWriteArrayList<>())
                .add(store);
            return store;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void unregister(String declaringClass) {
        List<DataStore> owned = this.registeredStores.remove(declaringClass);
        if (owned == null) {
            return;
        }
        for (DataStore store : owned) {
            store.close();
        }
    }
}
