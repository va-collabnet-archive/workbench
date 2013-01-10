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
package org.ihtsdo.translation.tasks.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class SearchLangRefsetDescTypeAcceptability.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
		@Spec(directory = "search", type = BeanType.TASK_BEAN) })
		public class SearchLangRefsetDescTypeAcceptability extends AbstractSearchTest {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Status concept for the term component to test.
	 */ 
	private TermEntry langRefsetTerm = new TermEntry(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN_US.getUids());
	
	/** The desc type term. */
	private TermEntry descTypeTerm ;
	
	/** The acceptability term. */
	private TermEntry acceptabilityTerm ;

	/**
	 * Write object.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(this.descTypeTerm);
		out.writeObject(this.acceptabilityTerm);
	}
	
	/**
	 * Instantiates a new search lang refset desc type acceptability.
	 *
	 * @throws ValidationException the validation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SearchLangRefsetDescTypeAcceptability() throws ValidationException, IOException{
	}
	
	/**
	 * Read object.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			this.descTypeTerm = (TermEntry) in.readObject();
			this.acceptabilityTerm = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.search.AbstractSearchTest#test(org.dwfa.ace.api.I_AmTermComponent, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	@Override
	public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {
			 //descTypeTerm = new TermEntry(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getUUIDs());
			 //acceptabilityTerm =   new TermEntry(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getUUIDs());
			 if(langRefsetTerm == null){
				 return false;
			 }
			 if(descTypeTerm == null){
				 return false;
			 }
			 if(acceptabilityTerm == null){
				 return false;
			 }
			 
			I_GetConceptData langRefsetConcept = AceTaskUtil.getConceptFromObject(langRefsetTerm);
			I_GetConceptData descTypeToMatch = AceTaskUtil.getConceptFromObject(descTypeTerm);
			I_GetConceptData acceptabilityToMatch = AceTaskUtil.getConceptFromObject(acceptabilityTerm);

			I_GetConceptData conceptToTest;
			I_DescriptionVersioned desc = null;
			if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
				desc = (I_DescriptionVersioned) component;
				conceptToTest = Terms.get().getConcept(desc.getConceptNid());
			} else {
				return applyInversion(false);
			}

			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("### testing status for: " + conceptToTest);
			}

			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			LanguageMembershipRefset langRefset;
			try {
				langRefset = new LanguageMembershipRefset(langRefsetConcept, config);
			} catch (Exception e) {
				return applyInversion(false);
			}
			PromotionRefset promotionRefset = langRefset.getPromotionRefset(config);

			if (promotionRefset == null) {
				return applyInversion(false);
			}

			ContextualizedDescription contextDesc = new ContextualizedDescription(desc.getNid(), conceptToTest.getNid(),
					langRefset.getRefsetId());
			
			boolean passes = false;
			
			if ( (descTypeToMatch == null || descTypeToMatch.getConceptNid() == contextDesc.getTypeId()) && 
					(acceptabilityToMatch == null || acceptabilityToMatch.getNid() == contextDesc.getAcceptabilityId())) {
				passes = true;
			} 
			
			return applyInversion(passes);

		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	/**
	 * Gets the lang refset term.
	 *
	 * @return the lang refset term
	 */
	public TermEntry getLangRefsetTerm() {
		return langRefsetTerm;
	}

	/**
	 * Sets the lang refset term.
	 *
	 * @param langRefsetTerm the new lang refset term
	 */
	public void setLangRefsetTerm(TermEntry langRefsetTerm) {
		this.langRefsetTerm = langRefsetTerm;
	}

	/**
	 * Gets the desc type term.
	 *
	 * @return the desc type term
	 */
	public TermEntry getDescTypeTerm() {
		return descTypeTerm;
	}

	/**
	 * Sets the desc type term.
	 *
	 * @param descTypeTerm the new desc type term
	 */
	public void setDescTypeTerm(TermEntry descTypeTerm) {
		this.descTypeTerm = descTypeTerm;
	}

	/**
	 * Gets the acceptability term.
	 *
	 * @return the acceptability term
	 */
	public TermEntry getAcceptabilityTerm() {
		return acceptabilityTerm;
	}

	/**
	 * Sets the acceptability term.
	 *
	 * @param acceptabilityTerm the new acceptability term
	 */
	public void setAcceptabilityTerm(TermEntry acceptabilityTerm) {
		this.acceptabilityTerm = acceptabilityTerm;
	}

}
