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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
		@Spec(directory = "search", type = BeanType.TASK_BEAN) })
		public class SearchLangRefsetDescTypeAcceptability extends AbstractSearchTest {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	/**
	 * Status concept for the term component to test.
	 */
	private TermEntry langRefsetTerm;
	private TermEntry descTypeTerm;
	private TermEntry acceptabilityTerm;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(this.descTypeTerm);
		out.writeObject(this.acceptabilityTerm);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			this.descTypeTerm = (TermEntry) in.readObject();
			this.acceptabilityTerm = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {
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

	public TermEntry getLangRefsetTerm() {
		return langRefsetTerm;
	}

	public void setLangRefsetTerm(TermEntry langRefsetTerm) {
		this.langRefsetTerm = langRefsetTerm;
	}

	public TermEntry getDescTypeTerm() {
		return descTypeTerm;
	}

	public void setDescTypeTerm(TermEntry descTypeTerm) {
		this.descTypeTerm = descTypeTerm;
	}

	public TermEntry getAcceptabilityTerm() {
		return acceptabilityTerm;
	}

	public void setAcceptabilityTerm(TermEntry acceptabilityTerm) {
		this.acceptabilityTerm = acceptabilityTerm;
	}

}
