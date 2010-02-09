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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForFullySpecifiedName extends AbstractConceptTest {

	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
			boolean forCommit) throws TaskFailedException {
		try {
			ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
			for (I_DescriptionVersioned desc : concept.getDescriptions()) {
				descriptions.add(desc);
			}
			for (I_DescriptionVersioned desc : concept
					.getUncommittedDescriptions()) {
				descriptions.add(desc);
			}
			return testDescriptions(concept, descriptions, forCommit);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	private List<AlertToDataConstraintFailure> testDescriptions(
			I_GetConceptData concept,
			ArrayList<I_DescriptionVersioned> descriptions, boolean forCommit)
			throws Exception {
		ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
		boolean found = false;
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type_aux = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		for (I_DescriptionVersioned desc : descriptions) {
			for (I_DescriptionPart part : desc.getVersions()) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					if (part.getTypeId() == fully_specified_description_type_aux
							.getConceptId()) {
						found = true;
						if (part.getText().matches(".*\\(\\?+\\).*")) {
							alertList
									.add(new AlertToDataConstraintFailure(
											AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
											"<html>Unedited semantic tag",
											concept));
							return alertList;
						}
					}
				}
			}
		}
		// This might work once we get the SNOMED version of FSN down
		// if (!found) {
		// alertList.add(new AlertToDataConstraintFailure(
		// AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
		// "<html>No fully Specified name", concept));
		// }
		return alertList;
	}

}
