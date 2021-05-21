package com.clubobsidian.obbylang.plugin;

import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.google.inject.Injector;

import java.io.File;
import java.util.logging.Logger;

public interface ObbyLangPlugin {

    public Object getServer();

    public File getDataFolder();

    public Logger getLogger();

    public Injector getInjector();

    public boolean createObbyLangCommand();

    public ObbyLangPlatform getPlatform();

}