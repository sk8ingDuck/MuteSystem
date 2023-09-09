package me.sk8ingduck.mutesystem.config;

public class DBConfig extends Config {

	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String database;

	public DBConfig(String name, String path) {
		super(name, path);

		this.host = (String) getPathOrSet("mysql.host", "localhost", false);
		this.port = (int) getPathOrSet("mysql.port", 3306, false);
		this.username = (String) getPathOrSet("mysql.username", "root", false);
		this.password = (String) getPathOrSet("mysql.password", "pw", false);
		this.database = (String) getPathOrSet("mysql.database", "db", false);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}
}
