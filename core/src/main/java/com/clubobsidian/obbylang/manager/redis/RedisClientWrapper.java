package com.clubobsidian.obbylang.manager.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import com.clubobsidian.obbylang.manager.script.ScriptManager;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class RedisClientWrapper {


	private String declaringClass;
	private RedisClient client;
	private boolean pingBefore;
	private List<SubConnectionWrapper> registerConnections;
	private Map<String, StatefulRedisConnection<String, String>> publishConnections;
	public RedisClientWrapper(String declaringClass, String connection, boolean pingBefore)
	{
		this.declaringClass = declaringClass;
		this.client = this.createRedisClient(connection);
		this.pingBefore = pingBefore;
		this.registerConnections = new ArrayList<>();
		this.publishConnections = new HashMap<>();
	}
	
	public void register(final ScriptObjectMirror script, final String registeredChannel)
	{
		StatefulRedisPubSubConnection<String, String> con = this.client.connectPubSub();
		
		RedisPubSubListener<String,String> listener = new RedisPubSubListener<String, String>() 
		{
			@Override
			public void message(String channel, String message) 
			{
				if(channel.equalsIgnoreCase(registeredChannel))
				{
					CompiledScript owner = ScriptManager.get().getScript(declaringClass);
					script.call(owner, message);
				}
			}

			@Override
			public void message(String pattern, String channel, String message) {}

			@Override
			public void subscribed(String channel, long count) {}

			@Override
			public void psubscribed(String pattern, long count) {}

			@Override
			public void unsubscribed(String channel, long count) {}

			@Override
			public void punsubscribed(String pattern, long count) {}
		};
		
		con.addListener(listener);
		con.sync().subscribe(registeredChannel);
		
		this.registerConnections.add(new SubConnectionWrapper(con, listener, registeredChannel));
	}
	
	@Deprecated
	public void publish(final String channel, final String message)
	{
		publishAsync(channel, message);
	}
	
	public void publishAsync(final String channel, final String message)
	{
		if(this.publishConnections.get(channel) == null)
		{
			this.publishConnections.put(channel, this.client.connect());
		}
		
		this.publishConnections.get(channel).async().publish(channel, message);
	}
	
	public void publishSync(final String channel, final String message)
	{
		if(this.publishConnections.get(channel) == null)
		{
			this.publishConnections.put(channel, this.client.connect());
		}
		
		this.publishConnections.get(channel).sync().publish(channel, message);
	}
	
	protected void unload()
	{
		for(SubConnectionWrapper wrapper : this.registerConnections)
		{
			wrapper.getConnection().removeListener(wrapper.getListener());
			wrapper.getConnection().async().unsubscribe(wrapper.getChannel());
		}
		
		for(StatefulRedisConnection<String, String> con : this.publishConnections.values())
		{
			con.closeAsync();
		}
		
		this.client.shutdownAsync();
	}
	
	private RedisClient createRedisClient(String connection)
	{
		RedisClient client = RedisClient.create(connection);
		client.setOptions(ClientOptions
				.builder()
				.autoReconnect(true)
				.pingBeforeActivateConnection(this.pingBefore)
				.build());
		return client;
	}
}