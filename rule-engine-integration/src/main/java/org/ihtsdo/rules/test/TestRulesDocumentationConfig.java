package org.ihtsdo.rules.test;

import java.net.URL;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.ihtsdo.rules.RulesLibrary;

public class TestRulesDocumentationConfig extends TestCase {
	
	public void testStateFull() {
		try {
			URL url = RulesLibrary.getDocumentationUrlForRuleUUID(UUID.fromString("e6e3fbd0-11f7-11e0-ac64-0800200c9a66"));
			System.out.println("--------------------- Returned URL: " + url.toString());
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
