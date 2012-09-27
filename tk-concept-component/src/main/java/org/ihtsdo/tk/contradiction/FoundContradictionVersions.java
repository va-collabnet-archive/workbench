/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
	 package org.ihtsdo.tk.contradiction;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentVersionBI;

// TODO: Auto-generated Javadoc
/**
 * The Class FoundContradictionVersions.
 */
public class FoundContradictionVersions {
	
	/** The result. */
	private ContradictionResult result;
	
	/** The versions. */
	private Collection<? extends ComponentVersionBI> versions;
	
	/**
	 * Instantiates a new found contradiction versions.
	 *
	 * @param contradictionResult the contradiction result
	 * @param componentVersions the component versions
	 */
	public FoundContradictionVersions(ContradictionResult contradictionResult, Collection<? extends ComponentVersionBI> componentVersions) {
		result = contradictionResult;
		versions = componentVersions;
	}
	
	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public ContradictionResult getResult() {
		return result;
	}
	
	/**
	 * Gets the versions.
	 *
	 * @return the versions
	 */
	public Collection<? extends ComponentVersionBI> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("ContradictionFinder findings: ");
		
		if (versions.size() == 0 && result == ContradictionResult.NONE) {
			buffer.append("\nThere are no versions in conflict");
		} else if (result == ContradictionResult.ERROR || 
				   versions.size() == 0 || 
				   result == ContradictionResult.NONE) {
			buffer.append("\nThere was an odd result that requires investigation for this concept");
		} else {
			buffer.append("\nThe result of the inspection was: ");

			switch (result) {
			case CONTRADICTION:
				buffer.append("Contradcition");
				break;
			case DUPLICATE_EDIT:
				buffer.append("Duplicate edit of a concept");
				break;
			case DUPLICATE_NEW:
				buffer.append("Duplicate newly created concepts");
				break;
			case SINGLE_MODELER_CHANGE:
				buffer.append("Single modeler change");
				break;
			}				

			buffer.append("\nWith the following Versions: ");
			
			int counter = 0;
			for ( ComponentVersionBI v : versions) {
				buffer.append("\n" + counter++ + ": " + v.toString());
			}
		}
		
		return buffer.toString();
	}
}
