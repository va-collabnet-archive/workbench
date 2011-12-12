package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.ihtsdo.project.workflow.model.WorkflowDefinition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class WorkflowDefinitionManager {

	public static WorkflowDefinition readWfDefinition(File file){

		XStream xStream = new XStream(new DomDriver());
		WorkflowDefinition wfDef=(WorkflowDefinition)xStream.fromXML(file);
		return wfDef;

	}

	public static void writeWfDefinition(WorkflowDefinition wfDefinition, File outputFile){

		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream(outputFile);
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}
}
