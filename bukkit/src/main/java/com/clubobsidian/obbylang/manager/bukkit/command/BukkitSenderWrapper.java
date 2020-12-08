package com.clubobsidian.obbylang.manager.bukkit.command;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.manager.message.MessageManager;

public class BukkitSenderWrapper extends SenderWrapper<CommandSender> implements CommandSender {

	public BukkitSenderWrapper(CommandSender sender) 
	{
		super(sender);
	}

	@Override
	public Object asCommandBlock() 
	{
		if(this.isCommandBlock())
		{
			return (CommandBlock) this.getOriginalSender();
		}
		return null;
	}

	@Override
	public Object asConsole() 
	{
		if(this.isConsole())
		{
			return (ConsoleCommandSender) this.getOriginalSender();
		}
		return null;
	}

	@Override
	public Object asPlayer() 
	{
		if(this.isPlayer())
		{
			return (Player) this.getOriginalSender();
		}
		return null;
	}

	@Override
	public boolean isCommandBlock() 
	{
		return this.getOriginalSender() instanceof CommandBlock;
	}

	@Override
	public boolean isConsole() 
	{
		return this.getOriginalSender() instanceof ConsoleCommandSender;
	}

	@Override
	public boolean isPlayer() 
	{
		return this.getOriginalSender() instanceof Player;
	}

	@Override
	public void sendMessage(String message) 
	{
		this.getOriginalSender().sendMessage(MessageManager.get().color(message));
	}

	@Override
	public void sendMessage(String[] messages) 
	{
		for(String message : messages)
		{
			this.sendMessage(message);
		}
	}
	
	@Override
	public boolean isPermissionSet(String name) 
	{
		return this.getOriginalSender().isPermissionSet(name);
	}

	@Override
	public boolean isPermissionSet(Permission perm) 
	{
		return this.getOriginalSender().isPermissionSet(perm);
	}

	@Override
	public boolean hasPermission(String name) 
	{
		return this.getOriginalSender().hasPermission(name);
	}

	@Override
	public boolean hasPermission(Permission perm) 
	{
		return this.getOriginalSender().hasPermission(perm);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) 
	{
		return this.getOriginalSender().addAttachment(plugin, name, value);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) 
	{
		return this.getOriginalSender().addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) 
	{
		return this.getOriginalSender().addAttachment(plugin, name, value, ticks);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int ticks) 
	{
		return this.getOriginalSender().addAttachment(plugin, ticks);
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) 
	{
		this.getOriginalSender().removeAttachment(attachment);
	}

	@Override
	public void recalculatePermissions() 
	{
		this.getOriginalSender().recalculatePermissions();
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() 
	{
		return this.getOriginalSender().getEffectivePermissions();
	}

	@Override
	public boolean isOp() 
	{
		return this.getOriginalSender().isOp();
	}

	@Override
	public void setOp(boolean value) 
	{
		this.getOriginalSender().setOp(value);
	}

	@Override
	public Server getServer() 
	{
		return this.getOriginalSender().getServer();
	}

	@Override
	public String getName() 
	{
		return this.getOriginalSender().getName();
	}

	@Override
	public Spigot spigot()
	{
		return this.getOriginalSender().spigot();
	}
}