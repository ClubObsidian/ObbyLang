package com.clubobsidian.obbylang.discord.manager.command;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;


public class DiscordCommandManager extends CommandManager {

	private Map<String, DiscordCommand> registeredCommands;
	public DiscordCommandManager()
	{
		this.registeredCommands = new ConcurrentHashMap<>();
	}
	
	@Override
	protected boolean registerCommand(CommandWrapper<?> wrapper) 
	{
		if(wrapper.getCommand() instanceof DiscordCommand)
		{
			String commandName = wrapper.getCommandName();
			DiscordCommand cmd = (DiscordCommand) wrapper.getCommand();
			this.removeCommand(wrapper);
			this.registeredCommands.put(commandName, cmd);
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean removeCommand(CommandWrapper<?> wrapper) 
	{
		if(wrapper.getCommand() instanceof DiscordCommand)
		{
			String commandName = wrapper.getCommandName();
			super.unregister(wrapper.getCommandName());
			return this.registeredCommands.remove(commandName) != null;
		}
		
		return false;
	}
	
	public Map<String, DiscordCommand> getRegisteredCommands()
	{
		return this.registeredCommands;
	}
	
	@SubscribeEvent
	public void onEvent(MessageReceivedEvent event) 
	{
		
		String message = event.getMessage().getContentRaw();
		String label = message;
		String[] args = new String[0];
		if(message.contains(" "))
		{
			String[] split = message.split(" ");
			label = split[0];
			
			if(split.length > 1)
			{
				args = Arrays.copyOfRange(split, 1, split.length);
			}
		}
		
		DiscordCommand command = this.registeredCommands.get(label);
		if(command != null)
		{
			User user = event.getAuthor();
			MessageChannel channel = event.getChannel();
			command.onCommand(user, channel, args);
		}
	}
}