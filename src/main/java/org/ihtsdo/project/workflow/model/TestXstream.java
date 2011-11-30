package org.ihtsdo.project.workflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

//		WfAction action = new WfAction();
//		action.setConsequence(state2);
//		List<WfState> states = new ArrayList<WfState>();
//		states.add(state1);
//		action.setInitialStates(states);
//		List<WfRole> roles = new ArrayList<WfRole>();
//		roles.add(role2);
//		roles.add(role1);
//		action.setPermissions(roles);
//		action.setId(UUID.randomUUID());
//		action.setName("My action");

		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		//xstream.setMode(XStream.NO_REFERENCES);
		//xstream.alias("action", WfAction.class);
		System.out.println("JSON Len: " + xstream.toXML(role2).length());
		System.out.println(xstream.toXML(role2));
		
		XStream xStream = new XStream(new DomDriver());
		System.out.println("XML Len: " + xStream.toXML(role2).length());
		System.out.println(xStream.toXML(role2));
		
		

	}

}
