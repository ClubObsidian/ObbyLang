package com.clubobsidian.obbylang.plugin;

import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.google.inject.Injector;

import java.io.File;
import java.util.logging.Logger;

public interface ObbyLangPlugin {

    Object getServer();

    File getDataFolder();

    Logger getLogger();

    Injector getInjector();

    boolean createObbyLangCommand();

    ObbyLangPlatform getPlatform();

}