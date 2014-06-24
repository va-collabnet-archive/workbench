/*
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
package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The Class WorkflowDefinitionManager.
 */
public class WorkflowDefinitionManager {

	/** The wf def cache. */
	private static Map<String,WorkflowDefinition> wfDefCache = new HashMap<String,WorkflowDefinition>();

	/**
	 * Read wf definition.
	 *
	 * @param fileName the file name
	 * @return the workflow definition
	 */
	public static WorkflowDefinition readWfDefinition(String fileName){
		if (wfDefCache.containsKey(fileName)) {
			return wfDefCache.get(fileName);
		} else {
			File file = new File("sampleProcesses/" + fileName);
			XStream xStream = new XStream(new DomDriver());
			WorkflowDefinition wfDef=(WorkflowDefinition)xStream.fromXML(file);
			wfDefCache.put(fileName, wfDef);
			return wfDef;
		}

	}

	/**
	 * Write wf definition.
	 *
	 * @param wfDefinition the wf definition
	 */
	public static void writeWfDefinition(WorkflowDefinition wfDefinition){
		File outputFile = new File("sampleProcesses/" + wfDefinition.getName() + ".wfd");
		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream(outputFile);
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
			wfDefCache.put(wfDefinition.getName() + ".wfd", wfDefinition);
		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (UnsupportedEncodingException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}
}
