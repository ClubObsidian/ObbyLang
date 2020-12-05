package com.clubobsidian.obbylang.manager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager implements RegisteredManager {

	private static DatabaseManager instance;	
	public static DatabaseManager get()
	{
		if(instance == null)
		{
			instance = new DatabaseManager();
		}
		return instance;
	}
	
	private Map<String, List<MySQLDatabase>> mysqlDatabases;
	private DatabaseManager()
	{
		this.mysqlDatabases = new HashMap<>();
	}
	
	public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password, int maxPoolSize)
	{
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql"))
		{
			return this.connect(declaringClass, type, "jdbc:mysql://" + ip + ":" + port + "/" + database + "?" + "user=" + username + "&password=" + password, maxPoolSize);
		}
		return null;
	}
	
	public Database connect(String declaringClass, String type, String connection, int maxPoolSize) //change to ip port username password
	{
		this.init(declaringClass);
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql"))
		{
			MySQLDatabase database = new MySQLDatabase(connection, maxPoolSize);
			this.mysqlDatabases.get(declaringClass).add(database);
			return database;
		}
		return null;
	}
	
	
	public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password)
	{
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql"))
		{
			return this.connect(declaringClass, type, "jdbc:mysql://" + ip + ":" + port + "/" + database + "?" + "user=" + username + "&password=" + password, 10);
		}
		return null;
	}
	
	public Database connect(String declaringClass, String type, String connection) //change to ip port username password
	{
		this.init(declaringClass);
		if(type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("sql"))
		{
			return this.connect(declaringClass, type, connection, 10);
		}
		return null;
	}
	
	public void unregister(String declaringClass)
	{
		this.init(declaringClass);
		for(MySQLDatabase database : this.mysqlDatabases.get(declaringClass))
		{
			database.getSource().close();
		}
		this.mysqlDatabases.keySet().remove(declaringClass);
	}
	
	private void init(String declaringClass)
	{
		if(this.mysqlDatabases.get(declaringClass) == null)
		{
			this.mysqlDatabases.put(declaringClass, new ArrayList<MySQLDatabase>());
		}
	}
	
	public abstract class Database {
		
		public Map<String, List<Object>> select(String query)
		{
			return this.select(query, new ArrayList<>());
		}
		
		public abstract Map<String, List<Object>> select(String query, List<Object> vars);
		
		public void execute(String query)
		{
			this.execute(query, new ArrayList<>());
		}
		
		public abstract boolean execute(String query, List<Object> vars);
	
		
	}
	
	private class MySQLDatabase extends Database 
	{
		private HikariDataSource source;
		
		public MySQLDatabase(String connection, int maxPoolSize)
		{
			this.source = new HikariDataSource();
			this.source.setMaximumPoolSize(maxPoolSize);
			this.source.setJdbcUrl(connection);
		}
		
		public HikariDataSource getSource()
		{
			return this.source;
		}

		@Override
		public Map<String, List<Object>> select(String query, List<Object> vars) 
		{
			Connection con = null;
			try 
			{
				con = this.source.getConnection();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				return null;
			}
			try 
			{
				PreparedStatement statement = con.prepareStatement(query);
				for(int i = 0; i < vars.size(); i++)
				{
					statement.setObject(i + 1, vars.get(i));
				}
				
				Map<String, List<Object>> selected = new HashMap<String, List<Object>>();
				ResultSet resultSet = statement.executeQuery();
				while(resultSet.next())
				{
					for(int i = 0; i < statement.getMetaData().getColumnCount(); i++)
					{
						String colName = statement.getMetaData().getColumnName(i + 1);
						if(!selected.containsKey(colName))
						{
							selected.put(colName, new ArrayList<>());
						}
						selected.get(colName).add(resultSet.getObject(colName));
					}
				}
				con.close();
				return selected;
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			
			try
			{
				con.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public boolean execute(String query, List<Object> vars) 
		{
			Connection con = null;
			try 
			{
				con = this.source.getConnection();
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
				return false;
			}
			try 
			{
				PreparedStatement statement = con.prepareStatement(query);
				for(int i = 0; i < vars.size(); i++)
				{
					statement.setObject(i + 1, vars.get(i));
				}
				statement.executeUpdate();
				con.close();
				return true;
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			return false;
		}
	}
}