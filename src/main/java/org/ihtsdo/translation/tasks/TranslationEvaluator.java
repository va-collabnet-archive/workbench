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
package org.ihtsdo.translation.tasks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


/**
 * The Class TranslationEvaluator.
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class TranslationEvaluator extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		// Nothing to do...

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

			I_GetConceptData concept = config.getHierarchySelection();

			I_TermFactory termFactory = Terms.get();

			I_GetConceptData refsetsParent = termFactory
			.getConcept(new UUID[] {UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da")});
			
			if (refsetsParent.isParentOf(concept)) {
				process.writeAttachment("DLG_MSG", "True");
			} else {
				process.writeAttachment("DLG_MSG", "False");
			}
			
			boolean fsnTranslated = false;
			boolean preferredTerTranslated = false;
			
			I_GetConceptData fully_specified_description_type_aux = termFactory
			.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
					.getUids());
			
			I_GetConceptData preferred_description_type_aux = termFactory
			.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			
			long lastVersion = Long.MIN_VALUE;
			for (I_DescriptionVersioned<?> description : concept.getDescriptions()) {
				I_DescriptionPart lastPart = description.getMutableParts().iterator().next();
				for (I_DescriptionPart part : description.getMutableParts()) {
					if (part.getTime() > lastVersion) {
						lastPart = part;
						lastVersion = part.getTime();
					}
				}
				
				if (lastPart.getTime() == fully_specified_description_type_aux.getConceptNid() &&
						lastPart.getLang().equals("es")) {
					fsnTranslated = true;
				}
				
				if (lastPart.getTime() == preferred_description_type_aux.getConceptNid() &&
						lastPart.getLang().equals("es")) {
					preferredTerTranslated = true;
				}
			}
			
			if (fsnTranslated && preferredTerTranslated) {
				process.writeAttachment("DLG_MSG", "Traducido!");
			} else {
				process.writeAttachment("DLG_MSG", "No traducido...");
			}
			
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
