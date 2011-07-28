/*
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
package org.dwfa.ace.task.profile.cap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JList;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile/cap", type = BeanType.TASK_BEAN) })
public class SetNewCapUserPathsForView extends AbstractSetNewCapUserPaths {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private String pathsForViewPropName = ProcessAttachmentKeys.PATHS_FOR_VIEW.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathsForViewPropName);
        out.writeObject(newProfilePropName);
   }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	pathsForViewPropName = (String) in.readObject();
            newProfilePropName = (String) in.readObject();
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

	protected JLabel getInstruction() {
		return new JLabel("Select view:");
	}
	
	protected String getPropName() {
		return getPathsForViewPropName();
	}
	
	protected UUID getParentNode() {
		try {
			return ArchitectonicAuxiliary.Concept.PATH.getPrimoridalUid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
    public String getPathsForViewPropName() {
        return pathsForViewPropName;
    }

    public void setPathsForViewPropName(String prop) {
        this.pathsForViewPropName = prop;
    }


    public String getNewProfilePropName() {
        return newProfilePropName;
    }

    public void setNewProfilePropName(String prop) {
        this.newProfilePropName = prop;
    }
}
