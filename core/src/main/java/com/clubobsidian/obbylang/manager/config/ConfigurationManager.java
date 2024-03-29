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

package com.clubobsidian.obbylang.manager.config;

import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.wrappy.Configuration;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private final Path directory;
    private final File tempConfigFolder;

    @Inject
    protected ConfigurationManager(ObbyLangPlugin plugin) {
        this.directory = Paths.get(plugin.getDataFolder().getPath(), "config");
        this.tempConfigFolder = new File(plugin.getDataFolder(), "tempconfig");
        this.init();
    }

    private void init() {
        try {
            Files.createDirectories(this.directory);
        } catch(IOException e) {
            e.printStackTrace();
        }

        if(this.tempConfigFolder.exists()) {
            try {
                FileUtils.deleteDirectory(tempConfigFolder);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        this.tempConfigFolder.mkdirs();
    }

    public Configuration load(File file) {
        return Configuration.load(file);
    }

    public Configuration load(String fileName) {
        return Configuration.load(new File(this.directory.toFile(), fileName));
    }

    public Configuration load(String url, String fileName) {
        return this.load(url, fileName, true);
    }

    public Configuration load(String url, String fileName, boolean overwrite) {
        return this.load(url, fileName, new HashMap<>(), overwrite);
    }

    public Configuration load(String url, String fileName, Map<String, String> requestProperties, boolean overwrite) {
        File saveTo = new File(this.directory.toFile(), fileName);
        try {
            //https://stackoverflow.com/questions/4571346/how-to-encode-url-to-avoid-special-characters-in-java
            String decodedURL = URLDecoder.decode(url, "UTF-8");
            URL u = new URL(decodedURL);
            URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), u.getRef());
            u = new URL(uri.toASCIIString());
            return Configuration.load(u, saveTo, requestProperties, overwrite);
        } catch(MalformedURLException | UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Configuration loadRaw(String url, String fileName) {
        return this.loadRaw(url, fileName, true);
    }

    public Configuration loadRaw(String url, String fileName, boolean overwrite) {
        return this.loadRaw(url, fileName, new HashMap<>(), overwrite);
    }

    public Configuration loadRaw(String url, String fileName, Map<String, String> requestProperties, boolean overwrite) {
        File saveTo = new File(this.directory.toFile(), fileName);
        try {
            URL u = new URL(url);
            return Configuration.load(u, saveTo, requestProperties, overwrite);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}