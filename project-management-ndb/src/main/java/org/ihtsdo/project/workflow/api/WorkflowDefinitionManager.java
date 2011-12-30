package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.project.workflow.model.WorkflowDefinition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class WorkflowDefinitionManager {

	private static Map<String,WorkflowDefinition> wfDefCache = new HashMap<String,WorkflowDefinition>();

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

	public static void writeWfDefinition(WorkflowDefinition wfDefinition, String fileName){
		File outputFile = new File("sampleProcesses/" + fileName);
		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream(outputFile);
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
			wfDefCache.put(fileName, wfDefinition);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}
}
