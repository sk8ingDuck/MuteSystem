package me.sk8ingduck.mutesystem.mysql;

public class DatabaseDetails {

	private final boolean useMySQL;
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String database;

	public DatabaseDetails(boolean useMySQL, String host, int port, String username, String password, String database) {
		this.useMySQL = useMySQL;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	public boolean isUseMySQL() {
		return useMySQL;
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
