package com.clubobsidian.obbylang.util;

public enum ChatColor {

	AQUA('b'),
	BLACK('0'),
	BLUE('9'),
	DARK_AQUA('3'),
	DARK_BLUE('1'),
	DARK_GRAY('8'),
	DARK_GREEN('2'),
	DARK_PURPLE('5'),
	DARK_RED('4'),
	GOLD('6'),
	GRAY('7'),
	GREEN('a'),
	LIGHT_PURPLE('d'),
	RED('c'),
	WHITE('f'),
	YELLOW('e'),
	//Formatting
	BOLD('l', true),
	ITALIC('o', true),
	MAGIC('k', true),
	RESET('r', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true);
	
	public static final char FORMATTING_CODE = '\u00A7';
	
	private char colorCode;
	private boolean formatting;
	private ChatColor(char colorCode)
	{
		this(colorCode, false);
	}
	
	private ChatColor(char colorCode, boolean formatting)
	{
		this.colorCode = colorCode;
		this.formatting = formatting;
	}
	
	public char getColorCode()
	{
		return this.colorCode;
	}
	
	public boolean isColor()
	{
		return !this.isFormatting();
	}
	
	public boolean isFormatting()
	{
		return this.formatting;
	}
	
	public static String translateAlternateColorCodes(char translate, String message)
	{
		char[] chars = message.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			if(chars[i] == translate)
			{
				if(i + 1 < chars.length)
				{
					for(ChatColor color : ChatColor.values())
					{
						if(chars[i + 1] == color.getColorCode())
						{
							chars[i] = ChatColor.FORMATTING_CODE;
						}
					}
				}
			}
		}
		return String.valueOf(chars);
	}
	
	public static String translateAlternateColorCodes(String message)
	{
		return translateAlternateColorCodes('&', message);
	}
	
	@Override
	public String toString()
	{
		return ChatColor.FORMATTING_CODE + "" + this.getColorCode();
	}
}