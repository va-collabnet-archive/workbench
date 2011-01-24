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
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.translation.LanguageUtil;

/**
 * The Class CreateNewContextualizedDescription.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class ContextualizeDescriptions extends AbstractTask {

	/** The issue repo prop. */
	private TermEntry concept;

	/** The issue repo prop. */
	private TermEntry languageRefset;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

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
		out.writeObject(concept);
		out.writeObject(languageRefset);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			concept=(TermEntry)in.readObject();
			languageRefset = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		I_TermFactory tf = Terms.get();

		try{
			I_GetConceptData conceptTarget = tf.getConcept(concept.ids);
			I_GetConceptData languageRefsetConcept = tf.getConcept(languageRefset.ids);

			I_GetConceptData preferred = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_GetConceptData synonym = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			I_GetConceptData acceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
			I_GetConceptData fsn = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			
			String lang="";
			if (languageRefsetConcept.getInitialText().toLowerCase().contains("spanish")){
				lang="es";
			}
			else{
				lang="en";
			}
				
			List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
					conceptTarget.getConceptNid(), languageRefsetConcept.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLang().toLowerCase().contains(lang)){
					if (description.getTypeId() == fsn.getConceptNid()) {
						description.contextualizeThisDescription(languageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					} else if (description.getTypeId() == synonym.getConceptNid()) {
						description.contextualizeThisDescription(languageRefsetConcept.getConceptNid(), 
								acceptable.getConceptNid());
					} else {
						description.contextualizeThisDescription(languageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					}
				}
			}

		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

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
		return new int[] {  };
	}

	public TermEntry getConcept() {
		return concept;
	}

	public void setConcept(TermEntry concept) {
		this.concept = concept;
	}

	public TermEntry getLanguageRefset() {
		return languageRefset;
	}

	public void setLanguageRefset(TermEntry languageRefset) {
		this.languageRefset = languageRefset;
	}


}