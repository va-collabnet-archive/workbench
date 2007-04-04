package org.dwfa.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import java.security.NoSuchAlgorithmException;

import org.apache.derby.tools.ij;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringInputStream;
import org.dwfa.util.io.FileIO;


/**
 * Goal which executes derby sql commands to generate a 
 * database or perform other such tasks.
 * 
 * @goal run-derby
 * @phase process-resources
 */
public class Derby extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the source directory.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * 
	 * @parameter expression="${project.version}"
	 * @required
	 */
	private String version;

	/**
	 * @parameter
	 * @required
	 */
	private String dbName;
	
	/**
	 * @parameter
	 */
	private String[] sources;

	/**
	* List of source roots containing non-test code.
	* @parameter default-value="${project.compileSourceRoots}"
	* @required
	* @readonly
	*/
	private List sourceRoots;
	
	public void execute() throws MojoExecutionException, MojoFailureException {

		// calculate the SHA-1 hashcode for this mojo based on input
		Sha1HashCodeGenerator generator;
		String hashCode = "";
		try {
			generator = new Sha1HashCodeGenerator();
			generator.add(outputDirectory);
			generator.add(sourceDirectory);
			generator.add(version);
			generator.add(dbName);
			
			for(int i = 0; i < sources.length; i++) {
				generator.add(sources[i]);
			}
			Iterator iter = sourceRoots.iterator();
			
			while(iter.hasNext()) {
				generator.add(iter.next());
			}
			
			hashCode = generator.getHashCode();
			
			//System.out.println("HASH CODE=" + hashCode);
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		
		File goalFileDirectory = new File("target" + File.separator 
				+ "completed-mojos");
		File goalFile = new File(goalFileDirectory, hashCode);
		
		// check to see if this goal has been executed previously
		if(!goalFile.exists()) {
			// hasn't been executed previously
			try {
				
				File sqlSrcDir = new File(sourceDirectory.getParentFile(), "sql");
				File sqlTargetDir = new File(outputDirectory, "sql");
				sqlTargetDir.mkdirs();
				for (File f: sqlSrcDir.listFiles(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().endsWith(".sql");
					}})) {
					RegexReplace replacer = new RegexReplace("\\$\\{project.build.directory\\}", outputDirectory.getCanonicalPath().replace('\\', '/'));
					getLog().info("Transforming: " + f.getName());
					Reader is = new FileReader(f);
					String input = FileIO.readerToString(is);
					String sqlScript = replacer.execute(input);
					sqlScript = sqlScript.replace('/', File.separatorChar);
					FileIO.copyFile(new StringInputStream(sqlScript), new FileOutputStream(new File(sqlTargetDir, f.getName())), true);
					
				}
				File dbDir = new File(outputDirectory, dbName.replace('/', File.separatorChar));
				File dbErrLog = new File(dbDir.getParentFile(), "derbyErr.log");
				dbErrLog.getParentFile().mkdirs();
				FileWriter fw = new FileWriter(dbErrLog);
				fw.append("Created by DWFA derby plugin version: " + version + "\n");
				fw.close();
				System.getProperties().setProperty("derby.infolog.append", "true");
				System.getProperties().setProperty("derby.stream.error.file", dbErrLog.getCanonicalPath());
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				
				Connection conn = DriverManager.getConnection("jdbc:derby:directory:" + dbDir.getCanonicalPath() + 
						";create=true;");
				
				File[] sqlSources;
				if (sources == null) {
					sqlSources = sqlTargetDir.listFiles(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().endsWith(".sql");
						}});
				} else {
					sqlSources = new File[sources.length];
					for (int i = 0; i < sources.length; i++) {
						sqlSources[i] = new File(sqlTargetDir, sources[i]);
					}
				}
				for (File f: sqlSources) {
					getLog().info("Executing: " + f.getName());
					
					Reader is = new FileReader(f);
					String sqlScript = FileIO.readerToString(is);
					InputStream sqlIn = new StringInputStream(sqlScript);
					
					String inputEncoding = "US-ASCII";
					OutputStream sqlOut = new ByteArrayOutputStream();
					String outputEncoding = null;
					int errors = ij.runScript(conn, sqlIn, inputEncoding, sqlOut, outputEncoding);
					getLog().info(sqlOut.toString());
					if (errors > 0) {
						throw new MojoExecutionException("Execution errors: " + errors);
					}
				}
				conn.close();
				
				// create a new file to indicate this execution has completed
				goalFileDirectory.mkdirs();
				goalFile.createNewFile();
				
				try {
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				} catch (SQLException e) {
					getLog().info(e.getMessage());
				}
			} catch (ClassNotFoundException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (SQLException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (UnsupportedEncodingException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		} else {
			// skip execution as it has already been done previously
			getLog().info("Skipping goal - executed previously.");
		}
	}

}
