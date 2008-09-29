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

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would 
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin. 
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

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
