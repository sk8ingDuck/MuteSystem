package me.sk8ingduck.mutesystemspigot.mysql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class SQLiteDriver {
	private static final String SQLITE_VERSION = "3.45.1.0";
	private static final String SQLITE_SHA256 = "f5f5404fa5a60f9e0b15e7bea2ea2d137e255f01babd0bfcb9dafcd2e3bf9cd2";
	private final Path dataDirectory;

	public SQLiteDriver(Path dataDirectory) throws Exception {
		this.dataDirectory = dataDirectory;

		if (ReflectUtil.getClass("org.sqlite.JDBC") != null) {
			return;
		}
		if (!Files.exists(dataDirectory)) {
			Files.createDirectories(dataDirectory);
		}
		if (loadCache()) {
			return;
		}
		downloadAndLoad();
	}

	private String getSQLiteLibraryName() {
		return "sqlite-jdbc-" + SQLITE_VERSION + ".jar";
	}

	private File getSQLiteLibraryFile() {
		return new File(dataDirectory.toFile(), getSQLiteLibraryName());
	}

	private String getSQLiteDownloadUrl() {
		return "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/"
				.concat(SQLITE_VERSION)
				.concat("/")
				.concat(getSQLiteLibraryName());
	}

	public boolean loadCache() throws Exception {
		File libraryFile = getSQLiteLibraryFile();
		if (libraryFile.exists()) {
			if (getSha256(libraryFile).equals(SQLITE_SHA256)) {
				try {
					ReflectUtil.addFileLibrary(libraryFile);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	public void downloadAndLoad() throws Exception {
		File libraryFile = getSQLiteLibraryFile();
		Path path = new DownloadTask(getSQLiteDownloadUrl(), libraryFile.toPath()).call();
		if (!getSha256(path.toFile()).equals(SQLITE_SHA256)) {
			throw new RuntimeException("Checksum failed!");
		}
		try {
			ReflectUtil.addFileLibrary(libraryFile);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Calculate the SHA-256 checksum
	private String getSha256(File file) throws Exception {
		try (FileInputStream fis = new FileInputStream(file);
		     ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			byte[] buff = new byte[1024];
			int n;
			while ((n = fis.read(buff)) > 0) {
				baos.write(buff, 0, n);
			}
			final byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
			StringBuilder sb = new StringBuilder();
			for (byte aByte : digest) {
				String temp = Integer.toHexString((aByte & 0xFF));
				if (temp.length() == 1) {
					sb.append("0");
				}
				sb.append(temp);
			}
			return sb.toString();
		}
	}
}