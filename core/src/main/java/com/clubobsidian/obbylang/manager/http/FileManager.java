package com.clubobsidian.obbylang.manager.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileManager {

	private static FileManager instance;
	
	public static FileManager get()
	{
		if(instance == null)
		{
			instance = new FileManager();
		}
		return instance;
	}
	
	public boolean download(String url, String file, boolean deleteIfExists)
	{
		return this.download(url, new File(file), deleteIfExists);
	}
	
	public boolean download(String url, File file, boolean deleteIfExists)
	{
		return this.download(url, null, file, deleteIfExists);
	}
	
	public boolean download(String url, String agent, String file)
	{
		return this.download(url, agent, new File(file));
	}
	
	public boolean download(String url, String agent, File file)
	{
		return this.download(url, agent, file, false);
	}
	
	public boolean download(String url, String agent, File file, boolean deleteIfExists)
	{
		if(file.exists())
			file.delete();
		
		URL downloadUrl;
		try 
		{
			downloadUrl = new URL(url);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			return false;
		}
		try 
		{
			URLConnection con = downloadUrl.openConnection();
			con.setRequestProperty("User-Agent", agent);
			InputStream is = con.getInputStream();
			Files.copy(is, Paths.get(file.toURI()));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean download(String url, String agent, String file, boolean deleteIfExists)
	{
		return this.download(url, agent, new File(file), deleteIfExists);
	}
}