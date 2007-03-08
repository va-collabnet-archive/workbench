package org.dwfa.mojo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal query-snomed
 * 
 * @phase generate-resources
 */
public class QuerySnomed extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources"
	 * @required
	 */
	File outputDirectory;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	* Project instance, used to add new source directory to the build.
	* @parameter default-value="${project}"
	* @required
	* @readonly
	*/
	private MavenProject project;
		
	private Connection conn;
	public void setup() throws Exception {
		
		Properties p = System.getProperties();
		p.put("derby.storage.tempDirectory", "tmp-derby");
		p.put("derbyderby.stream.error.file", "derby-error.txt");
		
		//Remove the locks, remove the tmp directory, then jar
		//Can create a jar file as jar -cvf0 snomed-db.jar snomed-db
		//String dbSpec = "jar:(src/main/resources/snomed-db.jar)snomed-db";
		//Unfortunately, with SNOMED, threw java.util.zip.ZipException: zip file too large
		String dbSpec = "src/main/resources/snomed-db";

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		String connectionString = "jdbc:derby:" + dbSpec ;
		getLog().info(connectionString);
		conn=DriverManager.getConnection(connectionString);
		conn.setAutoCommit(true);

		conn.commit();
	    getLog().info("Opened database");
	    Statement s = conn.createStatement();
	    
//	  Execute query
	    ResultSet result = s.executeQuery
	    	("select count(*) from CONCEPTS");

//	     While more rows exist, print them
	    while (result.next() )
	    {
	    	// Use the getInt method to obtain emp. id
	    	System.out.println ("Concept count: " + result.getInt(1));
	    }
	    conn.close();
	}

	
	public void execute() throws MojoExecutionException {
		getLog().info("I'm about to query the database");
		try {
			setup();
		} catch (SQLException e) {
			while (e != null) {
				e.printStackTrace();
				e = e.getNextException();
			}
			e.printStackTrace();
		} catch (Exception e) {
			throw new MojoExecutionException("Could not execute: ", e);
		}
	}
}
