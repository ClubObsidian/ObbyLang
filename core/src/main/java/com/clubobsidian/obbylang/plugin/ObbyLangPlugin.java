package com.clubobsidian.obbylang.plugin;

import java.io.File;
import java.util.logging.Logger;

import com.google.inject.Injector;

public interface ObbyLangPlugin {

	public Object getServer();
	public File getDataFolder();
	public Logger getLogger();
	public Injector getInjector();
	public boolean createObbyLangCommand();
	
}