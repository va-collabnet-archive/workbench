/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.api.cs;

import java.io.IOException;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.TerminologyException;

/**
 * Validator for concept attributes.
 * 
 * @author Dion McMurtrie
 */
public class ConceptAttributeValidator extends SimpleValidator {

	private boolean timeLenient = false;
	private String failureReport;
	
	@Override
	protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
			throws IOException, TerminologyException {
		
		termFactory = tf;
		failureReport = "";
		
		UniversalAceConceptAttributes conceptAttributes = bean.getConceptAttributes();
		if (conceptAttributes == null) {
			//for some reason this gets passed beans that have no conceptAttributes.
			//have to assume that this means it matches?
			AceLog.getEditLog().warning("UniversalAceBean has no conceptAttributes");
			return true;
		}
		
		int startParts = 0;
		I_ConceptAttributeVersioned thinConAttr = tf.getConcept(bean.getConceptAttributes().getConId()).getConceptAttributes();
		for (UniversalAceConceptAttributesPart part : conceptAttributes.getVersions()) {

			if (part.getTime() != Long.MAX_VALUE) {
				startParts++;
				boolean match = false;
				for (I_ConceptAttributePart conceptAttributePart : thinConAttr.getVersions()) {
					if (conceptAttributePart.getConceptStatus() == getNativeId(part.getConceptStatus())
							&& conceptAttributePart.getPathId() == getNativeId(part.getPathId())
							&& (timeLenient || conceptAttributePart.getVersion() == part.getTime())) {
						match = true;
						break;
					}
				}
					
				if (!match) {
					failureReport += "concept does not contain a concept attribute part match. \nnewPart was " + part + ", \nexisting versions " + thinConAttr.getVersions();
					return false; // test 2
				}	
			}
			
		}
		if (startParts != thinConAttr.getVersions().size()) {
			failureReport += "number of concept attribute parts is different for " + bean + " and " + thinConAttr.getVersions();
			return false; // test 3
		}

		// passed all tests for all descriptions
		return true;
	}

	@Override
	public String getFailureReport() {
		return failureReport;
	}
	
	public boolean isTimeLenient() {
		return timeLenient;
	}

	public void setTimeLenient(boolean timeLenient) {
		this.timeLenient = timeLenient;
	}

}
