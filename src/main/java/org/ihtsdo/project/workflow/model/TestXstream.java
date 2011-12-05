package org.ihtsdo.project.workflow.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.bpa.BusinessProcess;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class TestXstream {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WfState state1 = new WfState();
		state1.setId(UUID.randomUUID());
		state1.setName("Assigned");

		WfState state2 = new WfState();
		state2.setId(UUID.randomUUID());
		state2.setName("Delivered");

		WfRole role1 = new WfRole("Translator", UUID.randomUUID());
		WfRole role2 = new WfRole("Project Manager", UUID.randomUUID());

//				WfAction action = new WfAction();
//				action.setConsequence(state2);
				List<WfState> states = new ArrayList<WfState>();
				states.add(state1);
				states.add(state2);
				List<WfRole> roles = new ArrayList<WfRole>();
				roles.add(role2);
				roles.add(role1);

		WorkflowDefinition wfdf=new WorkflowDefinition();
		wfdf.setName("testWfDefinition");
		wfdf.setRoles(roles);
		wfdf.setStates(states);
		writeWfDefinition(wfdf);
		
		WorkflowDefinition wfdf2=readWfDefinition(new File("sampleProcesses/"  +  wfdf.getName() + ".wfd"));
		
//		XStream xstream = new XStream(new JettisonMappedXmlDriver());
//		//xstream.setMode(XStream.NO_REFERENCES);
//		//xstream.alias("action", WfAction.class);
//		System.out.println("JSON Len: " + xstream.toXML(role2).length());
//		System.out.println(xstream.toXML(role2));
//
//		XStream xStream = new XStream(new DomDriver());
//		System.out.println("XML Len: " + xStream.toXML(role2).length());
//		System.out.println(xStream.toXML(role2));


	}
	public static WorkflowDefinition readWfDefinition(File file){

		XStream xStream = new XStream(new DomDriver());
		WorkflowDefinition wfDef=(WorkflowDefinition)xStream.fromXML(file);
		return wfDef;

	}

	public static void writeWfDefinition(WorkflowDefinition wfDefinition){

		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream("sampleProcesses/" +  wfDefinition.getName() + ".wfd");
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
