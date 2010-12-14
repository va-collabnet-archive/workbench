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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The <codebatchQACheck</code> class iterates through the concepts from a
 * viewpoint and preforms QA
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
	 * The uuid for database.
	 * 
	 * @parameter
	 * @required
	 */
	private String database_uuid;

	/**
	 * The uuid for the tested path.
	 * 
	 * @parameter
	 * @required
	 */
	private String test_path_uuid;

	/**
	 * The name for the execution.
	 * 
	 * @parameter
	 * @required
	 */
	private String execution_name;

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
	
	
	private File executionDetailsOutput;
	private File executionXmlOutput;
	private File findingsOutput;
	private File rulesOutput;

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

	UUID executionUUID;
	DateFormat df;
	Calendar executionDate ;
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss zzz");
			executionDate = new GregorianCalendar();
			executionUUID = UUID.randomUUID();
			validateParamenters();
			openDb();
			RulesContextHelper contextHelper = new RulesContextHelper(config);
			cleanKbFileCache();
			exportExecutionDescriptor(contextHelper);
			performQA(executionUUID, contextHelper);
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	private void cleanKbFileCache() {
		File rules = new File("rules");
		for (File loopFile : rules.listFiles()) {
			if (loopFile.getName().endsWith(".bkb") || loopFile.getName().endsWith(".pkg")) {
				loopFile.delete();
			}
		}
		
	}

	private void exportExecutionDescriptor(RulesContextHelper contextHelper) throws Exception {
		
		FileOutputStream ruleFos = new FileOutputStream(rulesOutput);
		OutputStreamWriter ruleOsw = new OutputStreamWriter(ruleFos, "UTF-8");
		PrintWriter rulePw = new PrintWriter(ruleOsw);

		FileOutputStream executionXmlOs = new FileOutputStream(executionXmlOutput); 
		
		// Rules header
		rulePw.println(  "rule uuid" + "\t" + "name" + "\t" + "description" + "\t" + "severity" + "\t" 
				+ "package name" + "\t" + "package url" + "\t" + "dita uid" + "\t" + "rule code");

		try {


			I_GetConceptData context = tf.getConcept(UUID.fromString(context_uuid));
			contextHelper.getKnowledgeBaseForContext(context, config, true);

		
			// Create the execution XMLexecutionPw
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();

			DOMImplementation impl = docBuilder.getDOMImplementation();
			
			Document document = impl.createDocument(null, "description", null);
			Element rootElement = document.getDocumentElement();

			List<RulesDeploymentPackageReference> kbPackages = contextHelper.getPackagesForContext(context);
			for (RulesDeploymentPackageReference loopPackage : kbPackages) {
				Element packageElement = document.createElement("package");
				packageElement.setAttribute("name", loopPackage.getName());
				packageElement.setAttribute("url", loopPackage.getUrl());
				rootElement.appendChild(packageElement);

				for (Rule loopRule : loopPackage.getRules()) {
					String ruleUid = (String) loopRule.getMetaData().get("UUID");
					String description = (String) loopRule.getMetaData().get("DESCRIPTION");
					String ditaUid = (String) loopRule.getMetaData().get("DITA_UID");
					Integer severity = (Integer) loopRule.getMetaData().get("SEVERITY");
					String ruleCode = (String) loopRule.getMetaData().get("RULE_CODE");
					I_GetConceptData roleInContext = contextHelper.getRoleInContext(ruleUid, context); // STATUS

					// Write to rules file
					rulePw.print(ruleUid + "\t");
					rulePw.print(loopRule.getName() + "\t");
					rulePw.print(description + "\t");
//					if (roleInContext != null) {
//						rulePw.print(roleInContext.toUserString() + "\t");// Status
//					} else {
//						rulePw.print("default" + "\t");// Status
//					}
					rulePw.print(severity + "\t");
					rulePw.print(loopPackage.getName() + "\t");
					rulePw.print(loopPackage.getUrl() + "\t");
					rulePw.print(ditaUid + "\t");
					rulePw.print(ruleCode);
//					rulePw.print(df.format(executionDate.getTime()));
					rulePw.println();

					// Generate XML components
					Element ruleElement = document.createElement("rule");
					ruleElement.setAttribute("name", loopRule.getName());

					if(description != null){
						Element desciptionElement = document.createElement("description");
						desciptionElement.appendChild(document.createTextNode(description));
						ruleElement.appendChild(desciptionElement);
					}

					if(ruleUid != null){
						Element UUIDElement = document.createElement("UUID");
						UUIDElement.appendChild(document.createTextNode(ruleUid));
						ruleElement.appendChild(UUIDElement);
					}

					Element statusElement = document.createElement("status");
					if (roleInContext != null) {
						statusElement.appendChild(document.createTextNode(roleInContext.toUserString()));
					} else {
						statusElement.appendChild(document.createTextNode("default"));
					}
					ruleElement.appendChild(statusElement);

					Element severityElement = document.createElement("severity");
					severityElement.appendChild(document.createTextNode("Severity: " + severity));
					ruleElement.appendChild(severityElement);

					if(ditaUid != null){
						Element dtiaUidElement = document.createElement("ditaUid");
						dtiaUidElement.appendChild(document.createTextNode(ditaUid));
						ruleElement.appendChild(dtiaUidElement);
					}
						

					Element lastExecutionElement = document.createElement("lastExecution");
					lastExecutionElement.appendChild(document.createTextNode(df.format(executionDate.getTime())));

					packageElement.appendChild(ruleElement);
				}
			}

			// Serialize the document onto System.out
		      TransformerFactory xformFactory = TransformerFactory.newInstance();  
		      Transformer idTransform = xformFactory.newTransformer();
		      Source input = new DOMSource(document);
		      Result output = new StreamResult(executionXmlOs);
		      idTransform.transform(input, output);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			executionXmlOs.flush();
			executionXmlOs.close();
			rulePw.flush();
			rulePw.close();
		}
	}

	private void performQA(UUID executionUUID, RulesContextHelper contextHelper) throws Exception {
		I_GetConceptData context = tf.getConcept(UUID.fromString(context_uuid));

		// Add results to output file
		FileOutputStream findingFos = new FileOutputStream(findingsOutput);
		OutputStreamWriter findingOsw = new OutputStreamWriter(findingFos, "UTF-8");
		PrintWriter findingPw = new PrintWriter(findingOsw);
		//TODO: add header titles
		findingPw.println("uuid" + "\t" + "database Uid" + "\t" + "path Uid" + "\t" + "run Id" + "\t" + "rule uid" + "\t" + "component uuid"  + "\t" + "details" + "\t" + "component name");
		Long start = Calendar.getInstance().getTimeInMillis();
		tf.iterateConcepts(new PerformQA(context, findingPw, config, executionUUID, contextHelper, database_uuid, test_path_uuid));
		findingPw.flush();
		findingPw.close();
		Long end = Calendar.getInstance().getTimeInMillis();
		
		FileOutputStream executionFos = new FileOutputStream(executionDetailsOutput);
		OutputStreamWriter executionOsw = new OutputStreamWriter(executionFos, "UTF-8");
		PrintWriter executionPw = new PrintWriter(executionOsw);
		// Execution header
		executionPw.println("uuid" + "\t" + "database Uid" + "\t" + "path Uid" + "\t" + "name" + "\t" + "view point" + "\t" + "start time" + "\t" + "end time" + "\t" + "context");
		// Write Execution file
		executionPw.print(executionUUID + "\t");
		executionPw.print(database_uuid + "\t");
		executionPw.print(test_path_uuid + "\t");
		executionPw.print(execution_name + "\t");
		executionPw.print(test_time + "\t");
		executionPw.print(df.format(start) + "\t");
		executionPw.print(df.format(end) + "\t");
		executionPw.print(context.toUserString());
		executionPw.println();

		executionPw.flush();
		executionPw.close();
	}

	private void validateParamenters() throws Exception {
		File qaOutput = new File(outputDirectory, "generated-resources/qa-output/");
		if (!qaOutput.exists()) {
			qaOutput.mkdirs();
		}
		
		if (executionXmlOutputStr == null || executionXmlOutputStr.isEmpty()) {
			executionXmlOutput = new File(qaOutput, "executionXmlOutput.xml");
		} else {
			executionXmlOutput = new File(qaOutput, executionXmlOutputStr);
		}
		if (findingsOutputStr == null || findingsOutputStr.isEmpty()) {
			findingsOutput = new File(qaOutput, "findingsOutput.txt");
		} else {
			findingsOutput = new File(qaOutput, findingsOutputStr);
		}
		if (rulesOutputStr == null || rulesOutputStr.isEmpty()) {
			rulesOutput = new File(qaOutput, "rulesOutput.txt");
		} else {
			rulesOutput = new File(qaOutput, rulesOutputStr);
		}
		if (executionDetailsOutputStr == null || executionDetailsOutputStr.isEmpty()) {
			executionDetailsOutput = new File(qaOutput, "executionDetailsOutput.txt");
		} else {
			executionDetailsOutput = new File(qaOutput, executionDetailsOutputStr);
		}
		UUID.fromString(test_path_uuid);
		UUID.fromString(context_uuid);
		DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss");
		if (test_time != null && !test_time.isEmpty()) {
			df.parse(test_time);
		}
	}

	private void openDb() throws Exception {
//		vodbDirectory = new File("generated-resources/berkeley-db");
//		if (!vodbDirectory.exists()) {
//			throw new Exception("Database can't be found in expected location...");
//		}
//		dbSetupConfig = new DatabaseSetupConfig();
//		System.out.println("Opening database");
//		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	private I_ConfigAceFrame getTestConfig() throws TerminologyException, IOException, ParseException {
		I_ConfigAceFrame config = null;
		config = tf.newAceFrameConfig();
		DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss");
		config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString(test_path_uuid) }), tf.convertToThinVersion(df.parse(test_time).getTime())));
		
		// Addes inferred promotion template to catch the context relationships [ testing
		config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("cb0f6c0d-ebf3-5d84-9e12-d09a937cbffd") }), Integer.MAX_VALUE));
		
		config.addEditingPath(tf.getPath(new UUID[] { UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }));
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
		config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
		config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
		config.getDestRelTypes().add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));;
		config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
		config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
		config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

		// I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
		// newDbProfile.setUsername("username");
		// newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		// newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		// newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
		// newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
		// config.setDbConfig(newDbProfile);

		config.setPrecedence(Precedence.TIME);

		return config;
	}

}
