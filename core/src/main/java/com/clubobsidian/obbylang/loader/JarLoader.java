package com.clubobsidian.obbylang.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader extends URLClassLoader {

    public JarLoader() {
        super(new URL[0]);
    }

    public boolean addFile(File file) {
        try {
            super.addURL(file.toURI().toURL());
            return true;
        } catch(MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }
}