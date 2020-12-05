package com.clubobsidian.obbylang.manager.config;

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

import org.apache.commons.io.FileUtils;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.wrappy.Configuration;

public class ConfigurationManager {

	private static ConfigurationManager instance;
	
	public static ConfigurationManager get()
	{
		if(instance == null)
		{
			instance = new ConfigurationManager();
		}
		return instance;
	}
	
	
	private Path directory;
	private File tempConfigFolder;
	protected ConfigurationManager()
	{
		this.init();
	}
	
	private void init()
	{
		this.directory = Paths.get(ObbyLang.get().getPlugin().getDataFolder().getPath(), "config");
		try 
		{
			Files.createDirectories(this.directory);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		this.tempConfigFolder = new File(ObbyLang.get().getPlugin().getDataFolder().getPath(), "tempconfig");
		if(this.tempConfigFolder.exists())
		{
			try 
			{
				FileUtils.deleteDirectory(tempConfigFolder);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		this.tempConfigFolder.mkdirs();
	}
	
	public Configuration load(String fileName)
	{
		return Configuration.load(new File(this.directory.toFile(), fileName));
	}
	
	public Configuration load(String url, String fileName)
	{
		return this.load(url, fileName, true);
	}
	
	public Configuration load(String url, String fileName, boolean overwrite)
	{
		return this.load(url, fileName, new HashMap<>(), overwrite);
	}
	
	public Configuration load(String url, String fileName, Map<String,String> requestProperties, boolean overwrite)
	{
		File saveTo = new File(this.directory.toFile(), fileName);
		try 
		{
			//https://stackoverflow.com/questions/4571346/how-to-encode-url-to-avoid-special-characters-in-java
			String decodedURL = URLDecoder.decode(url, "UTF-8");
			URL u = new URL(decodedURL);
			URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), u.getRef());
			u = new URL(uri.toASCIIString());
			
			return Configuration.load(u, saveTo, requestProperties, overwrite);
		} 
		catch (MalformedURLException | UnsupportedEncodingException | URISyntaxException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}