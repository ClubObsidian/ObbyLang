package com.clubobsidian.obbylang.manager.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.database.type.sql.MySQLDatabase;

public class DatabaseManager implements RegisteredManager {

	private static DatabaseManager instance;	

	public static DatabaseManager get() {
		if(instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}
	
	private Map<String, List<Database>> databases;

	private DatabaseManager() {
		this.databases = new HashMap<>();
	}
	
	public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password, int maxPoolSize) {
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql")) {
			return this.connect(declaringClass, type, "jdbc:mysql://" + ip + ":" + port + "/" + database + "?" + "user=" + username + "&password=" + password, maxPoolSize);
		}
		return null;
	}
	
	public Database connect(String declaringClass, String type, String connection, int maxPoolSize) {
		this.init(declaringClass);
		Database database = null;
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql")) {
			database = new MySQLDatabase(connection, maxPoolSize);
		}
		if(database != null) {
			this.databases.get(declaringClass).add(database);
		}
		return database;
	}

	public Database connect(String declaringClass, String type, String connection) {
		this.init(declaringClass);
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql")) {
			return this.connect(declaringClass, type, connection, 10);
		}
		return null;
	}

	public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password) {
		StringBuilder sb = new StringBuilder();
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql")) {
			sb.append("jdbc:mysql://");
			sb.append(ip);
			sb.append(":");
			sb.append(port);
			sb.append("/");
			sb.append(database);
			sb.append("?user=");
			sb.append(username);
			sb.append("&password=");
			sb.append(password);
		} else if(type.equalsIgnoreCase("influx") || type.equalsIgnoreCase("influxdb")) {

		}
		return sb.length() > 0 ? this.connect(declaringClass, type,  sb.toString()) : null;
	}

	
	public void unregister(String declaringClass) {
		this.init(declaringClass);
		for(Database database : this.databases.get(declaringClass)) {
			database.close();
		}
		this.databases.keySet().remove(declaringClass);
	}
	
	private void init(String declaringClass) {
		if(this.databases.get(declaringClass) == null) {
			this.databases.put(declaringClass, new CopyOnWriteArrayList<>());
		}
	}
}