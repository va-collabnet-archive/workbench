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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
/**
 * The <codebatchQACheck</code> class iterates through the concepts from a viewpoint and preforms QA
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author ALO
 * @goal upload-to-qa-db
 * @phase compile
 */
public class UploadToQADatabase extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Execution details csv/txt file.
	 * 
	 * @parameter
	 */
	private String executionDetailsOutputStr;

	/**
	 * Execution details xml file.
	 * 
	 * @parameter
	 */
	private String executionXmlOutputStr;

	/**
	 * Findings csv/txt file.
	 * 
	 * @parameter
	 */
	private String findingsOutputStr;

	/**
	 * Rules csv/txt file.
	 * 
	 * @parameter
	 */
	private String rulesOutputStr;

	/**
	 * Execution details csv/txt file.
	 * 
	 * @parameter
	 */
	private File executionDetails;

	/**
	 * Execution details xml file.
	 * 
	 * @parameter
	 */
	private File executionXml;

	/**
	 * Findings csv/txt file.
	 * 
	 * @parameter
	 */
	private File findings;

	/**
	 * Rules csv/txt file.
	 * 
	 * @parameter
	 */
	private File rules;

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

	/**
	 * backup folder
	 * 
	 * @parameter expression="folder"
	 * @required
	 */
	private File backupFolder;

	private Connection con;

	private String runId;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			validateParamenters();
			loadToDatabase();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	private void loadToDatabase() throws SQLException, IOException {
		loadExecutionInfo();
		loadFindings();
		loadRules();
		ruleSeverityCtrl();
		if (!runId.equals("")){

			java.sql.CallableStatement cStmt = con.prepareCall("{call qa_controller(?)}");

			cStmt.setString(1, runId);

			cStmt.execute();
		}
	}

	private void ruleSeverityCtrl() {     

		try {
			ResultSet rs = con.createStatement().executeQuery("select rule_uid,name from qa_rule_imp r where not exists(select * from qa_severity s where s.severity_uid=r.severity_uid)");

			while (rs.next()){
				System.out.println("[WARNING] RULE WITHOUT SEVERITY: " + rs.getString(1) + " - " + rs.getString(2));
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadRules() throws SQLException {      
		Statement statement = (com.mysql.jdbc.Statement)con.createStatement();
		statement.execute("SET UNIQUE_CHECKS=0; ");
		statement.execute("ALTER TABLE qa_rule_imp DISABLE KEYS");


		con.createStatement().execute("TRUNCATE TABLE qa_rule_imp");
		// Define the query we are going to execute
		String statementText = "LOAD DATA LOCAL INFILE '" + rules.getAbsolutePath() + "' " +
		"INTO TABLE qa_rule_imp IGNORE 1 LINES " +
		"(rule_uid, name, description, severity_uid,package_name,package_url,DITA_documentation_link_UID,rule_code)";

		statement.execute(statementText);

		statement.execute("ALTER TABLE qa_rule_imp ENABLE KEYS");
		statement.execute("SET UNIQUE_CHECKS=1; ");
	}


	private void loadFindings() throws SQLException {      
		Statement statement = (com.mysql.jdbc.Statement)con.createStatement();
		statement.execute("SET UNIQUE_CHECKS=0; ");
		statement.execute("ALTER TABLE qa_finding DISABLE KEYS");


		con.createStatement().execute("TRUNCATE TABLE qa_finding");
		// Define the query we are going to execute
		String statementText = "LOAD DATA LOCAL INFILE '" + findings.getAbsolutePath() + "' " +
		"INTO TABLE qa_finding IGNORE 1 LINES " +
		"(finding_uid, database_uid, path_uid, run_id, rule_uid, qa_component_uid, detail, qa_component_name)";

		statement.execute(statementText);

		statement.execute("ALTER TABLE qa_finding ENABLE KEYS");
		statement.execute("SET UNIQUE_CHECKS=1; ");
	}

	private void loadExecutionInfo() throws IOException, SQLException {  


		BufferedReader inputFileReader = new BufferedReader(new FileReader(executionDetails));
		inputFileReader.readLine();
		String currentLine = inputFileReader.readLine();
		runId = "";
		String name= "";
		String db="";
		String path="";
		String vp="";
		String st="";
		String et="";
		String context="";
		String xmlExec="";
		String pathName="";
		if (currentLine != null) {
			String[] lineParts = currentLine.split("\t");

			runId = lineParts[0];
			db= lineParts[1];
			path = lineParts[2];
			name = lineParts[3];
			vp = lineParts[4];
			st= lineParts[5];
			et = lineParts[6];
			context = lineParts[7];
			pathName = lineParts[8];

		}

		inputFileReader.close();

		InputStreamReader fi = new  InputStreamReader(new FileInputStream(executionXml),Charset.forName("ISO-8859-1"));

		CharBuffer target =null;
		if (executionXml.length()>65535){
			target = CharBuffer.allocate(65535);
		}else{
			target = CharBuffer.allocate((int) executionXml.length());
		}
		int count = fi.read(target);
		if (count>-1){
			target.rewind();
			xmlExec=target.toString();
		}

		fi.close();

		String statementText="INSERT INTO qa_database (database_uid, name) select distinct '" + 
		db + "','" + name + "' from dual where not exists (select 0 from " +
		"qa_database where database_uid='" + db + "')";

		PreparedStatement statement = (com.mysql.jdbc.PreparedStatement)con.clientPrepareStatement(statementText);

		statement.execute();

		statement.close();

		statementText = "INSERT INTO qa_run (run_id, database_uid, path_uid,name, viewpoint_time, start_time, end_time,context_name,context_configuration,run_configuration,path_name)" +
		" values (?,?,?,?,?,?,?,?,?,?,?) " ;

		//		String statementText = "INSERT INTO QA_RUN (run_id, database_uid, path_uid,name,context_name,context_configuration,run_configuration)" +
		//		" values (?,?,?,?,?,?,?) " ;

		statement = (com.mysql.jdbc.PreparedStatement)con.clientPrepareStatement(statementText);

		statement.setString(1, runId);

		statement.setString(2, db);
		statement.setString(3, path);
		statement.setString(4, name);
		statement.setString(5, vp);
		statement.setString(6, st);
		statement.setString(7, et);
		statement.setString(8, context);
		statement.setString(9, xmlExec);
		statement.setString(10, xmlExec);
		statement.setString(11, pathName);

		//		statement.setString(5, context);
		//		statement.setString(6, xmlExec);
		//		statement.setString(7, xmlExec);

		statement.execute();

		statement.close();
	}




	private void validateParamenters() throws Exception {
		File qaOutput = new File(outputDirectory, "generated-resources/qa-output/");
		if (!qaOutput.exists()) {
			qaOutput.mkdirs();
		}

		if (executionXmlOutputStr == null || executionXmlOutputStr.isEmpty()) {
			executionXml = new File(qaOutput, "executionXmlOutput.xml");
		} else {
			executionXml = new File(qaOutput, executionXmlOutputStr);
		}
		if (findingsOutputStr == null || findingsOutputStr.isEmpty()) {
			findings = new File(qaOutput, "findingsOutput.txt");
		} else {
			findings = new File(qaOutput, findingsOutputStr);
		}
		if (rulesOutputStr == null || rulesOutputStr.isEmpty()) {
			rules = new File(qaOutput, "rulesOutput.txt");
		} else {
			rules = new File(qaOutput, rulesOutputStr);
		}
		if (executionDetailsOutputStr == null || executionDetailsOutputStr.isEmpty()) {
			executionDetails = new File(qaOutput, "executionDetailsOutput.txt");
		} else {
			executionDetails = new File(qaOutput, executionDetailsOutputStr);
		}
		if (!executionDetails.exists() || !executionXml.exists() || !findings.exists() || !rules.exists()) {
			throw new Exception("File not found...");
		}
		if (backupFolder.exists() &&  !backupFolder.isDirectory()){
			throw new Exception("Backup folder parameter is not folder");

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
	 * @param executionDetails the executionDetails to set
	 */
	protected void setExecutionDetails(File executionDetails) {
		this.executionDetails = executionDetails;
	}


	/**
	 * @param executionXml the executionXml to set
	 */
	protected void setExecutionXml(File executionXml) {
		this.executionXml = executionXml;
	}


	/**
	 * @param findings the findings to set
	 */
	protected void setFindings(File findings) {
		this.findings = findings;
	}


	/**
	 * @param rules the rules to set
	 */
	protected void setRules(File rules) {
		this.rules = rules;
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
	 * @param backupFolder the backupFolder to set
	 */
	protected void setBackupFolder(File backupFolder) {
		this.backupFolder = backupFolder;
	}


	/**
	 * @param con the con to set
	 */
	protected void setCon(Connection con) {
		this.con = con;
	}


}
