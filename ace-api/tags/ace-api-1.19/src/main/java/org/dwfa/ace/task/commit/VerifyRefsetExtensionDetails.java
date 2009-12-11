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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ExtensionValidator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class VerifyRefsetExtensionDetails extends AbstractExtensionTest{

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    
    private I_TermFactory termFactory = null;
    
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

    
        public List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension, 
    		boolean forCommit)
            throws TaskFailedException {
    	
    	ExtensionValidator ev = new ExtensionValidator();
    	
    	return ev.validate(extension.getComponentId(), extension.getTypeId(), forCommit);
    	
    }//End method test
    
}//End class VerifyRefsetExtensionDetails
