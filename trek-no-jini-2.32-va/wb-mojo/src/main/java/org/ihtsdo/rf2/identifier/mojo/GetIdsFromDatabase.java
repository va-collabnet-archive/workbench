/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
/**
 * Gets ids from SCTID_IDENTIFIER table
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Alejandro Rodriguez
 * @goal get-ids-from-db
 * @phase compile
 */
public class GetIdsFromDatabase extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Ids csv/txt output file.
	 * 
	 * @parameter
	 */
	private String idsOutputStr;

	/**
	 * dbConnection JDBC URL
	 * 
	 * @parameter
	 * @required
	 */
	
	private String url;

	/**
	 * dbConnection JDBC username
	 * 
	 * @parameter
	 * @required
	 */
	private String username;

	/**
	 * dbConnection JDBC password
	 * 
	 * @parameter
	 * @required
	 */
	private String password;

	private Connection con;
	
	private File ids;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			validateParameters();
			loadFromDatabase();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	private void loadFromDatabase() throws SQLException, IOException {

		PrintWriter idsPw=new PrintWriter(ids);
		Statement stmt=(Statement) con.createStatement();
		ResultSet rs=stmt.executeQuery("Select Partition_ID,Namespace_ID,Release_ID,SCTID,CODE from SCTID_IDENTIFIER ");
		//Write data to file
		while (rs.next()){
			idsPw.print(rs.getString(1) + "\t");
			idsPw.print(rs.getString(2) + "\t");
			idsPw.print(rs.getString(3) + "\t");
			idsPw.print(rs.getString(4) + "\t");
			idsPw.print(rs.getString(5) + "\r\n" );
		}
		rs.close();
		stmt.close();
		con.close();
	}

	
	private void validateParameters() throws Exception {
		File idOutput = new File(outputDirectory, "generated-resources/ids-output/");
		if (!idOutput.exists()) {
			idOutput.mkdirs();
		}

		if (idsOutputStr == null || idsOutputStr.isEmpty()) {
			ids = new File(idOutput, "idsOutput.txt");
		} else {
			ids = new File(idOutput, idsOutputStr);
		}

		// validate connection data
		Class.forName("com.mysql.jdbc.Driver");
		con= (com.mysql.jdbc.Connection)DriverManager.getConnection(url,username,password);

	}


	/**
	 * @param outputDirectory the outputDirectory to set
	 */
	protected void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @param url the url to set
	 */
	protected void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param username the username to set
	 */
	protected void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password the password to set
	 */
	protected void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param con the con to set
	 */
	protected void setCon(Connection con) {
		this.con = con;
	}


}
