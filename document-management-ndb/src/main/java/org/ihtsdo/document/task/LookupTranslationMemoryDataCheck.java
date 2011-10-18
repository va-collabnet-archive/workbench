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
package org.ihtsdo.document.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.document.TranslationMemoryResultsPanel;


/**
 * The Class LookupTranslationMemoryDataCheck.
 */
@BeanList(specs = {@Spec(directory = "tasks/documents tasks", type = BeanType.TASK_BEAN)})

public class LookupTranslationMemoryDataCheck extends AbstractConceptTest {

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
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.commit.AbstractConceptTest#test(org.dwfa.ace.api.I_GetConceptData, boolean)
	 */
	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
			boolean forCommit) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = LocalVersionedTerminology.get().getActiveAceFrameConfig();
			String query = "";
			for (I_DescriptionVersioned desc: concept.getDescriptions()) {
				query = query + desc.getLastTuple();
				//TODO check! ::.getTuples(config.getConflictResolutionStrategy()).iterator().next().getText();
			}
			JPanel resultsPanel = new TranslationMemoryResultsPanel(query);

			JPanel signpostPanel = config.getSignpostPanel();
			Component[] components = signpostPanel.getComponents();
			for (int i = 0; i < components.length; i++) {
				signpostPanel.remove(components[i]);
			}
			signpostPanel.setLayout(new BorderLayout());       
			signpostPanel.add(resultsPanel, BorderLayout.CENTER);
			config.setShowSignpostPanel(true);
			signpostPanel.revalidate();
			return new ArrayList<AlertToDataConstraintFailure>();
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

}
