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
package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.LocalVersionedTerminology;

public class IdIterator implements I_ProcessIds {

    private BufferedWriter output = null;

    public IdIterator(BufferedWriter output) throws IOException {
        this.output = output;
    }

    public void processId(I_IdVersioned idv) throws Exception {
		List<I_IdTuple> tuples = idv.getTuples();
				
		for (I_IdTuple tuple: tuples) {			
			String status = LocalVersionedTerminology.get().getUids(tuple.getIdStatus()).iterator().next().toString();
			output.write(tuple.getUIDs().iterator().next().toString() + "\t" + status);
		}
		
		output.newLine();	
	}

}
