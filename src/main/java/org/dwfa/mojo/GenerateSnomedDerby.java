package org.dwfa.mojo;

import java.io.File;
import java.sql.DriverManager;


/**
 * Goal which touches a timestamp file.
 * 
 * @goal derby-snomed
 * 
 * @phase generate-resources
 */

public class GenerateSnomedDerby extends GenerateSnomedJDBC {

	public void setup() throws Exception {
		File dbDir = new File(getSourceDirectory().getAbsolutePath()
				+ "/../resources/snomed-db");
		getLog().info("Deleting dbdir: " + dbDir.delete());

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		String connectionString = "jdbc:derby:" + dbDir.getAbsolutePath() + ";create=true";
		getLog().info(connectionString);
		setConn(DriverManager.getConnection(connectionString));
		getConn().setAutoCommit(false);
		createTables();
		getConn().commit();
	    getLog().info("Opened derby database");
	}

	public String longDataType() {
		return "BIGINT";
	}

}
