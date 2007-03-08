package org.dwfa.mojo;

import java.sql.DriverManager;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal openbase-snomed
 * 
 * @phase generate-resources
 */
public class GenerateSnomedOpenBase extends GenerateSnomedJDBC {

	public void setup() throws Exception {

		Class.forName("com.openbase.jdbc.ObDriver");
  		String url = "jdbc:openbase://g5-1.informatics.com/snomed";		// Set here your hostname and the database name 
	    getLog().info("OpenBase url: " + url);
		setConn(DriverManager.getConnection(url, "snomed", "sdo"));
		getConn().setAutoCommit(false);
		createTables();
		getConn().commit();
	    getLog().info("Opened OpenBase database");
	}

	public String longDataType() {
		return "longlong";
	}
}
