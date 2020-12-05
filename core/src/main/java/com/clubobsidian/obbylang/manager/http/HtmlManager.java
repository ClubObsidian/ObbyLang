package com.clubobsidian.obbylang.manager.http;

import java.io.IOException;

import org.jsoup.Jsoup;

public class HtmlManager {

	private static HtmlManager instance;
	
	public static HtmlManager get()
	{
		if(instance == null)
		{
			instance = new HtmlManager();
		}
		return instance;
	}
	
	public String get(String url)
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