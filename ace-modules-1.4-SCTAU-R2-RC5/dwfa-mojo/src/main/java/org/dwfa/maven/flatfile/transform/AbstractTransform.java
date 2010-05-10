/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.maven.flatfile.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

import java.io.IOException;

public abstract class AbstractTransform implements I_ReadAndTransform {

	private String name;

	public void cleanup(Transform transformer) throws Exception {

   }
   private String lastTransform;

	private int columnId = -1;

    private I_ReadAndTransform chainedTransform; //

    public I_ReadAndTransform getChainedTransform() {
        return chainedTransform;
    }

	public String toString() {
		return getClass().getSimpleName() + ": " + name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastTransform() {
		return lastTransform;
	}
	protected String setLastTransform(String lastTransfrom) {
		this.lastTransform = lastTransfrom;
		return lastTransfrom;
	}
	public int getColumnId() {
		return columnId;
	}
	public void setColumnId(int columnId) {
		this.columnId = columnId;
	}

	public final void setup(Transform transformer) throws IOException, ClassNotFoundException {
		if (columnId == -1) {
			columnId = transformer.getNextColumnId();
		}
		setupImpl(transformer);
	}
	public abstract void setupImpl(Transform transformer) throws IOException, ClassNotFoundException;

    @Override public void setNamespace(final String namespace) {
        //do nothing
    }

}
