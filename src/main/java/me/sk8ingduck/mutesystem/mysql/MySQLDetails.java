package me.sk8ingduck.mutesystem.mysql;

public class MySQLDetails {

	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String database;

	public MySQLDetails(String host, int port, String username, String password, String database) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
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
