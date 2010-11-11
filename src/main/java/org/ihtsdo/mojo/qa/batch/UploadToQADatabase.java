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
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.tk.api.Precedence;

/**
 * The <codebatchQACheck</code> class iterates through the concepts from a viewpoint and preforms QA
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author ALO
 * @goal upload-to-qa-db
 * @phase process-resources
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
	 * @required
	 */
	private File executionDetails;
	
	/**
	 * Execution details xml file.
	 * 
	 * @parameter
	 * @required
	 */
	private File executionXml;
	
	/**
	 * Findings csv/txt file.
	 * 
	 * @parameter
	 * @required
	 */
	private File findings;
	
	/**
	 * Rules csv/txt file.
	 * 
	 * @parameter
	 * @required
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

	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			validateParamenters();
			loadToDatabase();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}


	private void loadToDatabase() {
		// TODO connect to db, read files, and load to db
		
	}


	private void validateParamenters() throws Exception {
		if (!executionDetails.exists() || !executionXml.exists() || !findings.exists() || !rules.exists()) {
			throw new Exception("File not found...");
		}
		// TODO: validate connection data
	}

}
