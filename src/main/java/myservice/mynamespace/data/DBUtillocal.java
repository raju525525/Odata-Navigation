package myservice.mynamespace.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DBUtillocal {
	private static Connection connection;
	static String collectionName;

	static Properties prop = new Properties();
	static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	static InputStream stream = loader.getResourceAsStream("/application.properties");

	public static Connection getConnection() {
		try {
			try {
				prop.load(stream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Class.forName(prop.getProperty("jdbc.driver"));
			try {
				connection = DriverManager.getConnection(prop.getProperty("jdbc.url"),
						prop.getProperty("jdbc.username"), prop.getProperty("jdbc.password"));
			} catch (SQLException ex) {
				// log an exception. fro example:
				ex.printStackTrace();
				System.out.println("Failed to create the database connection.");
			}
		} catch (ClassNotFoundException ex) {
			// log an exception. for example:
			System.out.println("Driver not found.");
		}
		return connection;
	}

	public static void Close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String readCollectionNames(Integer n) {
		if (n == 1) {
			collectionName = prop.getProperty("appconfig.collection");
		}
		if (n == 2) {
			collectionName = prop.getProperty("dataconf.collection");
		}
		if (n == 3) {
			collectionName = prop.getProperty("floorplan.collection");
		}
		return collectionName;

	}

	public static void main(String[] args) {
		List drivers = Collections.list(DriverManager.getDrivers());
		for (int i = 0; i < drivers.size(); i++) {
			Driver driver = (Driver) drivers.get(i);
			String driverName = driver.getClass().getName();
			System.out.println("Driver " + i + ":::" + driverName);
		}
	}

}
