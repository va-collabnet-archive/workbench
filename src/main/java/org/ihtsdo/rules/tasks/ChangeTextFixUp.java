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
package org.ihtsdo.rules.tasks;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.I_Fixup;

/**
 * The Class ChangeTextFixUp.
 */
public class ChangeTextFixUp implements I_Fixup {
	
	/** The concept. */
	I_GetConceptData concept;
	
	/** The desc. */
	I_DescriptionVersioned desc;
	
	/** The part. */
	I_DescriptionPart part;
	
	/**
	 * Instantiates a new change text fix up.
	 * 
	 * @param concept the concept
	 * @param desc the desc
	 * @param part the part
	 */
	public ChangeTextFixUp(I_GetConceptData concept,
			I_DescriptionVersioned desc, I_DescriptionPart part) {
		super();
		this.concept = concept;
		this.desc = desc;
		this.part = part;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.commit.I_Fixup#fix()
	 */
	public void fix() throws Exception {
        I_TermFactory tf = LocalVersionedTerminology.get();
		part.setText(part.getText() + " corregido");
		tf.addUncommitted(concept);
		AceLog.getAppLog().info("Changed part: " + part);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "remove " + part.getText();
	}

}
