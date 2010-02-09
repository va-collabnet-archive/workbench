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

import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.TaskFailedException;

public abstract class AbstractExtensionTest extends AbstractDataConstraintTest {

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
    public final List<AlertToDataConstraintFailure> test(I_Transact component, 
    		boolean forCommit) throws TaskFailedException {
        if (I_GetExtensionData.class.isAssignableFrom(component.getClass())) {
            I_GetExtensionData extension = (I_GetExtensionData) component;
            try {
                return test(extension.getExtension(), forCommit);
            } catch (IOException e) {
                throw new TaskFailedException(e);
            }
        } else if (I_ThinExtByRefVersioned.class.isAssignableFrom(component.getClass())) {
            return test((I_ThinExtByRefVersioned) component, forCommit);
        } 
        return new ArrayList<AlertToDataConstraintFailure>();
    }
    
    public abstract List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension, 
    		boolean forCommit) throws TaskFailedException;
}
