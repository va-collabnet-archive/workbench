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
package org.ihtsdo.mojo.qa.batch;

import java.io.File;
import java.sql.DriverManager;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mysql.jdbc.Connection;
/**
 * The <codebatchQACheck</code> class iterates through the concepts from a viewpoint and preforms QA
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author ALO
 * @goal test-db
 * @phase compile
 */
public class TestDatabase extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

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

	private String runId;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			validateParamenters();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}





	private void validateParamenters() throws Exception {

		// validate connection data
		Class.forName("com.mysql.jdbc.Driver");
		con= (com.mysql.jdbc.Connection)DriverManager.getConnection(url,username,password);

		con.close();
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
