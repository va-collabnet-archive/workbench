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
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.api.Precedence;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
		@Spec(directory = "search", type = BeanType.TASK_BEAN) })
		public class SearchLangRefsetStatus extends AbstractSearchTest {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	/**
	 * Status concept for the term component to test.
	 */
	private TermEntry langRefsetTerm = new TermEntry(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
	private TermEntry statusTerm = new TermEntry(ArchitectonicAuxiliary.Concept.STATUS.getUids());

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(this.statusTerm);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			this.statusTerm = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {
			I_GetConceptData langRefsetConcept = AceTaskUtil.getConceptFromObject(langRefsetTerm);
			I_GetConceptData statusToMatch = AceTaskUtil.getConceptFromObject(statusTerm);

			I_GetConceptData conceptToTest;
			if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
				conceptToTest = (I_GetConceptData) component;
			} else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
				I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
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
			
			I_GetConceptData currentStatus = promotionRefset.getPromotionStatus(conceptToTest.getConceptNid(), config);

//			List<? extends I_ExtendByRef> members = tf.getAllExtensionsForComponent(conceptToTest.getConceptNid());
//			I_ExtendByRefVersion lastTuple = null;
//			for (I_ExtendByRef promotionMember : members) {
//				if (promotionMember.getRefsetId() == promotionRefset.getRefsetId()) {
//					List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(
//							config.getAllowedStatus(), 
//							config.getViewPositionSetReadOnly(), 
//							Precedence.TIME, 
//							config.getConflictResolutionStrategy());
//					if (tuples != null && !tuples.isEmpty()) {
//						for (I_ExtendByRefVersion loopTuple : tuples) {
//							if (lastTuple == null || lastTuple.getTime() < loopTuple.getTime()) {
//								lastTuple = loopTuple;
//							}
//						}
//					}
//				}
//			}
//			
//			if (lastTuple == null) {
//				return applyInversion(false);
//			}
			
			if (currentStatus == null) {
				return applyInversion(false);
			}
			
			if (currentStatus.getConceptNid() == statusToMatch.getConceptNid()) {
				return applyInversion(true);
			} else {
				return applyInversion(false);
			}

		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
	}

	public TermEntry getStatusTerm() {
		return statusTerm;
	}

	public void setStatusTerm(TermEntry statusTerm) {
		this.statusTerm = statusTerm;
	}

	public TermEntry getLangRefsetTerm() {
		return langRefsetTerm;
	}

	public void setLangRefsetTerm(TermEntry langRefsetTerm) {
		this.langRefsetTerm = langRefsetTerm;
	}

}
