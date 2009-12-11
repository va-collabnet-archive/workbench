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
package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
		@Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class DifferencePreferredName extends AbstractDifferenceDescription {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	protected int[] getAllowedTypes() throws IOException, TerminologyException {
		return new int[] { ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
				.localize().getNid() };
	}

}
