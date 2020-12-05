package com.clubobsidian.obbylang.manager.http;

import java.io.IOException;

import org.jsoup.Jsoup;

public class HttpManager {

	private static HttpManager instance;
	
	public static HttpManager get()
	{
		if(instance == null)
		{
			instance = new HttpManager();
		}
		return instance;
	}
	
	public String get(String url)
	{
		return this.get(url, "wget");
	}
	
	public String get(String url, String userAgent)
	{
		return this.get(url, userAgent, true);
	}
	
	public String get(String url, String userAgent, boolean ignoreContentType)
	{
		try 
		{
			return Jsoup.connect(url).userAgent(userAgent).ignoreContentType(ignoreContentType).execute().body();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public String getHTML(String url)
	{
		try 
		{
			return Jsoup.connect(url).userAgent("wget").get().html();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
}