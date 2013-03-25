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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * The Class TestUsingLibrary.
 */
@BeanList(specs = {
		@Spec(directory = "tasks/rules tasks", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })

public class TestUsingRealtimeContext extends AbstractConceptTest {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (!(objDataVersion == 1)) {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.commit.AbstractConceptTest#test(org.dwfa.ace.api.I_GetConceptData, boolean)
	 */
	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
			boolean forCommit) throws TaskFailedException {
		List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
		try {
			ViewCoordinate myVc = new ViewCoordinate(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
			myVc.setRelationshipAssertionType(RelAssertionType.STATED);
			ConceptVersionBI archAux = Ts.get().getConceptVersion(myVc, ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getPrimoridalUid());
			ConceptVersionBI refAux = Ts.get().getConceptVersion(myVc, RefsetAuxiliary.Concept.REFSET_AUXILIARY.getPrimoridalUid());
			ConceptVersionBI prjAux = Ts.get().getConceptVersion(myVc, ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.getPrimoridalUid());
			ConceptVersionBI conceptv = Ts.get().getConceptVersion(myVc, concept.getConceptNid());
			
			if (RulesLibrary.rulesDisabled ||
					RefsetAuxiliary.Concept.COMMIT_RECORD.getUids().contains(concept.getPrimUuid()) ||
					RefsetAuxiliary.Concept.CONFLICT_RECORD.getUids().contains(concept.getPrimUuid()) ||
					conceptv.isKindOf(archAux) ||
					conceptv.isKindOf(refAux) ||
					conceptv.isKindOf(prjAux)) {
				return alertList;
			}
			
			I_TermFactory tf = Terms.get();
			alertList =  RulesLibrary.checkConcept(concept, 
					tf.getConcept(RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT.getUids()), true, 
					getFrameConfig(), INFERRED_VIEW_ORIGIN.CONSTRAINT_NORMAL_FORM).getAlertList();
			return alertList;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

}
