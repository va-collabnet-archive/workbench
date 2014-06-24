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

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * The Class SimpleKindOfComputer.
 */
public class SimpleKindOfComputer {

	/**
	 * Checks if is kind of.
	 * 
	 * @param descendant
	 *            the descendant
	 * @param parent
	 *            the parent
	 * @return true, if is kind of
	 */
	public boolean isKindOf(UUID descendant, UUID parent) throws ContradictionException {
		boolean result = false;
		ConceptVersionBI parentConcept = null;
		ConceptVersionBI subtypeConcept = null;
		try {
			if (parent != null && descendant != null) {
				parentConcept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), parent);
				subtypeConcept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), descendant);
				if (parentConcept == null || subtypeConcept == null) {
					result = false;
				}
				if (Terms.get().hasPath(parentConcept.getConceptNid())) {
					result = isConceptOriginInPath(parentConcept, subtypeConcept);
				} else {
					result = subtypeConcept.isKindOf(parentConcept);
				}
			} else {
				return false;
			}
		} catch (java.lang.AssertionError e) {
			System.out.println("Error retrieving concepts in iParentOf: " + parent + ", " + subtypeConcept);
			System.out.println(e.getMessage());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	private boolean isConceptOriginInPath(ConceptVersionBI path, ConceptVersionBI concept) {
		try {
			return concept.getConceptAttributes().getPrimordialVersion().getPathNid() == path.getConceptNid();
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		
		return false;
	}

}
