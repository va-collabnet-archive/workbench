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
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 * The Class CreateNewContextualizedDescription.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class ContextualizeAllDescriptions extends AbstractTask {

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
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			
			I_GetConceptData preferred = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_GetConceptData synonym = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			I_GetConceptData acceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
			I_GetConceptData fsn = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			Set<PositionBI> savedViewPositionSet = config.getViewPositionSet();
			Set<PathBI> savedEditPathSet = config.getEditingPathSet();

			config.getViewPositionSet().clear();
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), Integer.MAX_VALUE));
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_ES_PATH.getUids()), Integer.MAX_VALUE));
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_SE_PATH.getUids()), Integer.MAX_VALUE));

			config.getEditingPathSet().clear();
			config.getEditingPathSet().add(tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
			
			config.getDescTypes().clear();
			config.getDescTypes().add(preferred.getConceptNid());
			config.getDescTypes().add(synonym.getConceptNid());
			config.getDescTypes().add(fsn.getConceptNid());
			
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			tf.setActiveAceFrameConfig(config);

			I_GetConceptData englishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
			I_GetConceptData englishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.EN.getUids());
			String englishLCode=ArchitectonicAuxiliary.getLanguageCode(englishLanguageCodeConcept.getUids());

			LanguageMembershipRefset englishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						englishLanguageRefsetConcept, englishLCode, config);

			I_GetConceptData spanishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_ES.getUids());
			I_GetConceptData spanishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.ES.getUids());
			String spanishLCode=ArchitectonicAuxiliary.getLanguageCode(spanishLanguageCodeConcept.getUids());

			LanguageMembershipRefset spanishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						spanishLanguageRefsetConcept, spanishLCode, config);

			I_GetConceptData swedishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_SV_SE.getUids());
			I_GetConceptData swedishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.SV_SE.getUids());
			String swedishLCode=ArchitectonicAuxiliary.getLanguageCode(swedishLanguageCodeConcept.getUids());

			LanguageMembershipRefset swedishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						swedishLanguageRefsetConcept, swedishLCode, config);

			I_GetConceptData snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			I_IntSet isaType = tf.newIntSet();
			isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

			tf.iterateConcepts(new PutsDescriptionsInLanguageRefset( snomedRoot,  config, 
					 isaType,  englishLanguageRefsetConcept,
					 spanishLanguageRefsetConcept, swedishLanguageRefsetConcept,
					 preferred,  acceptable,  fsn,
					 synonym));
			
			tf.commit();
			
//			config.getEditingPathSet().clear();
//			config.getEditingPathSet().addAll(savedEditPathSet);
//
//			config.getViewPositionSet().clear();
//			config.getViewPositionSet().addAll(savedViewPositionSet);
//
//			tf.setActiveAceFrameConfig(config);

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

}