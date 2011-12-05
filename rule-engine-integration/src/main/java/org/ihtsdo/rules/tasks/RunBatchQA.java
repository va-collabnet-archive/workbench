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
package org.ihtsdo.rules.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
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

import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The Class TestUsingLibrary.
 */
@BeanList(specs = {
		@Spec(directory = "tasks/rules tasks", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })

		public class RunBatchQA extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	private File executionDetailsOutput;
	private File executionXmlOutput;
	private File findingsOutput;
	private File rulesOutput;
	private UUID executionUUID;
	private DateFormat df;
	private Calendar executionDate;
	private I_ConfigAceFrame config;

	private HashMap<String, String> allRules;

	private String database_uuid;
	private String test_path_uuid;
	private String execution_name;
	private String test_time;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}


	@Override
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
			executionDate = new GregorianCalendar();
			executionUUID = UUID.randomUUID();
			
			allRules=new HashMap<String,String>();
			config = Terms.get().getActiveAceFrameConfig();
			RulesContextHelper contextHelper = new RulesContextHelper(config);
			
			exportExecutionDescriptor(contextHelper);
			performQA(executionUUID, contextHelper);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		return null;
	}

	@Override
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Condition> getConditions() {
		// TODO Auto-generated method stub
		return null;
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
			I_GetConceptData context = Terms.get().getConcept(RefsetAuxiliary.Concept.BATCH_QA_CONTEXT.getUids());
			contextHelper.getKnowledgeBaseForContext(context, config, true);


			// Create the execution XMLexecutionPw
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();

			DOMImplementation impl = docBuilder.getDOMImplementation();

			Document document = impl.createDocument(null, "description", null);
			Element rootElement = document.getDocumentElement();
			
			HashSet<String> rules;
			List<RulesDeploymentPackageReference> kbPackages = contextHelper.getPackagesForContext(context);
			for (RulesDeploymentPackageReference loopPackage : kbPackages) {
				Element packageElement = document.createElement("package");
				packageElement.setAttribute("name", loopPackage.getName());
				packageElement.setAttribute("url", loopPackage.getUrl());
				rootElement.appendChild(packageElement);

				rules=new HashSet<String>();
				for (Rule loopRule : loopPackage.getRules()) {
					String ruleUid = (String) loopRule.getMetaData().get("UUID");
					if (ruleUid != null && !ruleUid.equals("null") && rules.contains(ruleUid)){
						System.out.println("DUPLICATED RULE UUID:" + ruleUid +  " Name:" + (String) loopRule.getMetaData().get("DESCRIPTION"));
						//throw new Exception("DUPLICATED RULE UUID:" + ruleUid +  " Name:" + (String) loopRule.getMetaData().get("DESCRIPTION"));
					}else{
						rules.add(ruleUid);
					}
					String description = (String) loopRule.getMetaData().get("DESCRIPTION");
					if (!allRules.containsKey(ruleUid)){
						allRules.put(ruleUid, description);
					}
					String ditaUid = (String) loopRule.getMetaData().get("DITA_UID");
					String severityUid = (String) loopRule.getMetaData().get("SEVERITY");
					String ruleCode = (String) loopRule.getMetaData().get("RULE_CODE");
					I_GetConceptData roleInContext = contextHelper.getRoleInContext(ruleUid, context); // STATUS

					// Write to rules file
					rulePw.print(ruleUid + "\t");
					rulePw.print(loopRule.getName() + "\t");
					rulePw.print(description + "\t");
					if (roleInContext != null) {
						//rulePw.print(roleInContext.toUserString() + "\t");// Status
						System.out.println("*+*+ " + ruleUid + " - " + description + " - " + roleInContext.toUserString());
					} else {
						//rulePw.print("default" + "\t");// Status
						System.out.println("*+*+ " + ruleUid + " - " + description + " - Default");
					}
					rulePw.print(severityUid + "\t");
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
					severityElement.appendChild(document.createTextNode("Severity: " + severityUid));
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
			// Add batch duplicate fsn rule
//			rulePw.print("d4d60d70-0733-11e1-be50-0800200c9a66" + "\t");
//			rulePw.print("FSN should be unique (batch check)" + "\t");
//			rulePw.print("FSN should be unique (batch check)" + "\t");
//			System.out.println("*+*+ " + "d4d60d70-0733-11e1-be50-0800200c9a66" + " - " + "FSN should be unique (batch check)" + " - Default");
//			rulePw.print("f9545a20-12cf-11e0-ac64-0800200c9a66" + "\t");
//			rulePw.print("Batch Rule" + "\t");
//			rulePw.print("Batch Rule" + "\t");
//			rulePw.print("" + "\t");
//			rulePw.print(4);
//			rulePw.println();
			executionXmlOs.flush();
			executionXmlOs.close();
			rulePw.flush();
			rulePw.close();
		}
	}

	private void performQA(UUID executionUUID, RulesContextHelper contextHelper) throws Exception {
		I_TermFactory tf = Terms.get();
		System.out.println("Starting concept iteration...");
		I_GetConceptData context = tf.getConcept(RefsetAuxiliary.Concept.BATCH_QA_CONTEXT.getUids());

		// Add results to output file
		FileOutputStream findingFos = new FileOutputStream(findingsOutput);
		OutputStreamWriter findingOsw = new OutputStreamWriter(findingFos, "UTF-8");
		PrintWriter findingPw = new PrintWriter(findingOsw);
		//TODO: add header titles
		findingPw.println("uuid" + "\t" + "database Uid" + "\t" + "path Uid" + "\t" + "run Id" + "\t" + "rule uid" + "\t" + "component uuid"  + "\t" + "details" + "\t" + "component name");
		Long start = Calendar.getInstance().getTimeInMillis();
		tf.iterateConcepts(new PerformQA(context, findingPw, config, executionUUID, contextHelper, database_uuid, test_path_uuid,allRules));
		findingPw.flush();
		findingPw.close();
		Long end = Calendar.getInstance().getTimeInMillis();

		FileOutputStream executionFos = new FileOutputStream(executionDetailsOutput);
		OutputStreamWriter executionOsw = new OutputStreamWriter(executionFos, "UTF-8");
		PrintWriter executionPw = new PrintWriter(executionOsw);
		// Execution header
		executionPw.println("uuid" + "\t" + "database Uid" + "\t" + "path Uid" + "\t" + "name" + "\t" + "view point" + "\t" + "start time" + "\t" + "end time" + "\t" + "context"+ "\t" + "path name");
		// Write Execution file
		executionPw.print(executionUUID + "\t");
		executionPw.print(database_uuid + "\t");
		executionPw.print(test_path_uuid + "\t");
		executionPw.print(execution_name + "\t");
		executionPw.print(test_time + "\t");
		executionPw.print(df.format(start) + "\t");
		executionPw.print(df.format(end) + "\t");
		executionPw.print(context.toUserString() + "\t");
		executionPw.print(tf.getConcept(UUID.fromString(test_path_uuid)).toUserString());
		executionPw.println();

		executionPw.flush();
		executionPw.close();
		System.out.println("Concept iteration finished...");
	}

}
