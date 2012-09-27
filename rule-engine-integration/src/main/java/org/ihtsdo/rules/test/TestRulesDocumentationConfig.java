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
package org.ihtsdo.rules.test;

import java.net.URL;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.rules.RulesLibrary;

/**
 * The Class TestRulesDocumentationConfig.
 */
public class TestRulesDocumentationConfig extends TestCase {
	
	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			URL url = RulesLibrary.getDocumentationUrlForRuleUUID(UUID.fromString("e6e3fbd0-11f7-11e0-ac64-0800200c9a66"));
			AceLog.getAppLog().info("--------------------- Returned URL: " + url.toString());
		} catch (ConfigurationException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
}
