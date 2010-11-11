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
 * @goal perform-qa
 * @phase process-resources
 */
public class BatchQACheck extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * The uuid for the tested path.
	 * 
	 * @parameter 
	 * @required
	 */
	private String test_path_uuid;

	/**
	 * The time for the test in yyyy.mm.dd hh:mm:ss zzz format
	 * 
	 * @parameter
	 */
	private String test_time;

	/**
	 * The uuid for the context.
	 * 
	 * @parameter 
	 * @required
	 */
	private String context_uuid;

	/** The vodb directory. */
	private File vodbDirectory;

	/** The read only. */
	private boolean readOnly = false;

	/** The cache size. */
	private Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	private DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The tf. */
	private I_TermFactory tf;
	
	private File executionXmlOutput;
	private File findingsOutput;
	private File rulesOutput;
	private File executionDetailsOutput;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			executionXmlOutput = new File(outputDirectory, "executionXmlOutput.xml");
			findingsOutput = new File(outputDirectory, "findingsOutput.txt");
			rulesOutput = new File(outputDirectory, "rulesOutput.txt");
			executionDetailsOutput = new File(outputDirectory, "executionDetailsOutput.txt");
			validateParamenters();
			openDb();
			exportExecutionDescriptor();
			performQA();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	private void exportExecutionDescriptor() throws Exception {
		I_GetConceptData context = tf.getConcept(UUID.fromString(context_uuid));
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		List<RulesDeploymentPackageReference> kbPackages = contextHelper.getPackagesForContext(context);
		for (RulesDeploymentPackageReference loopPackage : kbPackages) {
			// TODO: write to executionXmlOutput 
			loopPackage.getName();
			loopPackage.getUrl();
			for (Rule loopRule : loopPackage.getRules()) {
				loopRule.getName();
				String ruleUid = (String) loopRule.getMetaData().get("UUID");
				String description = (String) loopRule.getMetaData().get("DESCRIPTION");
				String ditaUid = (String) loopRule.getMetaData().get("DITA_UID");
				Integer severity = (Integer) loopRule.getMetaData().get("SEVERITY");
				I_GetConceptData roleInContext = contextHelper.getRoleInContext(ruleUid, context); // only for xml
				// write rule in executionXmlOutput
				// write rule in rulesOutput
			}
		}
		// TODO: close xml

	}

	private void performQA() throws Exception {
		I_GetConceptData context = tf.getConcept(UUID.fromString(context_uuid));
		tf.iterateConcepts(new PerformQA(context, findingsOutput, config));
	}

	private void validateParamenters() throws Exception {
		UUID.fromString(test_path_uuid);
		UUID.fromString(context_uuid);
		DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss zzz");
		if (test_time != null && !test_time.isEmpty()) {
			df.parse(test_time);
		}
	}

	private void openDb() throws Exception {
		vodbDirectory = new File("generated-resources/berkeley-db");
		if (!vodbDirectory.exists()) {
			throw new Exception("Database can't be found in expected location...");
		}
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	private I_ConfigAceFrame getTestConfig() throws TerminologyException, IOException, ParseException {
		I_ConfigAceFrame config = null;
		config = tf.newAceFrameConfig();
		DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss zzz");
		config.addViewPosition(tf.newPosition(
				tf.getPath(new UUID[] {UUID.fromString(test_path_uuid)}), 
				tf.convertToThinVersion(df.parse(test_time).getTime())));
		config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
		config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
		config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

		//			I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
		//	        newDbProfile.setUsername("username");
		//	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		//	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		//	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
		//	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
		//	        config.setDbConfig(newDbProfile);

		config.setPrecedence(Precedence.TIME);

		return config;
	}

}
