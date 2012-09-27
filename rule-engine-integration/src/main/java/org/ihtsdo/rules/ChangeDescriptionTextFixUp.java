/**
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
package org.ihtsdo.rules;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.I_Fixup;

/**
 * The Class ChangeDescriptionTextFixUp.
 */
public class ChangeDescriptionTextFixUp implements I_Fixup {
	
	/** The concept. */
	I_GetConceptData concept;
	
	/** The new text. */
	String newText;
	
	/** The part. */
	I_DescriptionPart part;

	/**
	 * Instantiates a new change description text fix up.
	 * 
	 * @param concept the concept
	 * @param part the part
	 * @param newText the new text
	 */
	public ChangeDescriptionTextFixUp(I_GetConceptData concept,
			I_DescriptionPart part, String newText) {
		super();
		this.concept = concept;
		this.part = part;
		this.newText = newText;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.commit.I_Fixup#fix()
	 */
	public void fix() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		part.setText(newText);
		tf.addUncommitted(concept);
		AceLog.getAppLog().info("Text changed in part: " + part);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Change text to '" + newText + "'";
	}

}
