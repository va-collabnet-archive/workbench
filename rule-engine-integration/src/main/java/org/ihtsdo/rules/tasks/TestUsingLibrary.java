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
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
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


/**
 * The Class TestUsingLibrary.
 */
@BeanList(specs = {
	 @Spec(directory = "tasks/rules tasks", type = BeanType.TASK_BEAN)
    /*, @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
     @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)*/ })
		
public class TestUsingLibrary extends AbstractConceptTest {

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
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			return RulesLibrary.checkConcept(concept, 
					tf.getConcept(RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT.getUids()), false, config).getAlertList();
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

}
