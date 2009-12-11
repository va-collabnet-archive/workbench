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
package org.dwfa.ace.task.refset;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/refset", type = BeanType.TASK_BEAN) })
public class RunMemberRefsetCalculation extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {


		try {
			I_TermFactory tf = LocalVersionedTerminology.get();

			MemberRefsetCalculator cal = new MemberRefsetCalculator();
			System.out.println("Setting outputdirectory to: "+ new File(".").getAbsolutePath());
			cal.setOutputDirectory(new File("."));
			
			int nid = ConceptConstants.AU_CT_EDIT_PATH.localize().getNid();
			cal.setPathConcept(tf.getConcept(nid));
			cal.run();
			
		} catch (Exception e) {
			throw new TaskFailedException("Member refset calculation run failed", e);
		}

		return Condition.CONTINUE;

	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

}
