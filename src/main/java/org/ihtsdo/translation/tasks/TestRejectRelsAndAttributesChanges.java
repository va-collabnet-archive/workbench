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
package org.ihtsdo.translation.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lucene.SearchResult;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
		public class TestRejectRelsAndAttributesChanges extends AbstractConceptTest {

	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
	throws TaskFailedException {
		try {
			I_ConfigAceFrame config =  getFrameConfig();
			ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

			I_GetConceptData snomedRoot = getConceptSafe(Terms.get(), SNOMED.Concept.ROOT.getUids());
			if (snomedRoot == null)
				return alertList;
			if (!snomedRoot.isParentOfOrEqualTo(concept, getFrameConfig().getAllowedStatus(), getFrameConfig()
					.getDestRelTypes(), getFrameConfig().getViewPositionSetReadOnly(), getFrameConfig().getPrecedence(),
					getFrameConfig().getConflictResolutionStrategy())) {
				return alertList;
			}

			boolean notAllowedChangeDetected = false;

			for (I_RelTuple loopRelTuple : concept.getSourceRelTuples(null, 
					null, config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy())) {
				if (loopRelTuple.getTime() == Long.MAX_VALUE) {
					notAllowedChangeDetected = true;
				}
			}

			if (concept.getConceptAttributeTuples(
					config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next().getTime() 
					==  Long.MAX_VALUE) {
				notAllowedChangeDetected = true;
			}

			if (notAllowedChangeDetected) {
				alertList.add(new AlertToDataConstraintFailure(
						(AlertToDataConstraintFailure.ALERT_TYPE.ERROR), "<html>Change not allowed", concept));
			}

			return alertList;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}
}
