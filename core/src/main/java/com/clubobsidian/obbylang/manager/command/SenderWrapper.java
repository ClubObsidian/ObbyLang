package com.clubobsidian.obbylang.manager.command;

public abstract class SenderWrapper<T> {

	private T sender;
	public SenderWrapper(T sender)
	{
		this.sender = sender;
	}
	
	//ObbyLang
	public abstract boolean isPlayer();
	public abstract boolean isConsole();
	public abstract boolean isCommandBlock();
	
	public abstract Object asPlayer();
	public abstract Object asConsole();
	public abstract Object asCommandBlock();
	
	public abstract void sendMessage(String message); 
	public abstract void sendMessage(String[] messages);
	
	public T getOriginalSender()
	{
		return this.sender;
	}
}