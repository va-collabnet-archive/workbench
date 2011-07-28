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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile/cap", type = BeanType.TASK_BEAN) })
public class SetNewCapUserParentConceptForPathConcept extends AbstractSetNewCapUserParentConcept {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private String parentConceptForPathPropName = ProcessAttachmentKeys.PARENT_CONCEPT_FOR_PATH.getAttachmentKey();
    private String newProfilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(parentConceptForPathPropName);
        out.writeObject(newProfilePropName);
   }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	parentConceptForPathPropName = (String) in.readObject();
            newProfilePropName = (String) in.readObject();
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected void setupInput(I_EncodeBusinessProcess process)  throws IllegalArgumentException,
    	IntrospectionException, IllegalAccessException, InvocationTargetException 
    {
        parentIds = new LinkedList<Integer>();

        try {
            I_ConfigAceFrame newConfig = (I_ConfigAceFrame) process.getProperty(newProfilePropName);
	    	I_GetConceptData parentNode = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PATH.getPrimoridalUid());

	    	String[] potentialParentConcepts = generatePotentialParentConcepts(parentNode, newConfig.getViewCoordinate());
	    	
	        instruction = getInstruction();
	        parentConceptList = new JComboBox(potentialParentConcepts);
	        parentConceptList.setSelectedIndex(initialIndex);
	    } catch (Exception e) {
	    	throw new IllegalArgumentException(e.getMessage());
	    }
    }

	protected void readInput(I_EncodeBusinessProcess process) {

		try {
            int index = parentConceptList.getSelectedIndex();
            process.setProperty(parentConceptForPathPropName, parentIds.get(index).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	protected JLabel getInstruction() {
		return new JLabel("Parent of path");
	}
	
    public String getParentConceptForPathPropName() {
        return parentConceptForPathPropName;
    }

    public void setParentConceptForPathPropName(String prop) {
        this.parentConceptForPathPropName = prop;
    }

    public String getNewProfilePropName() {
        return newProfilePropName;
    }

    public void setNewProfilePropName(String newProfilePropName) {
        this.newProfilePropName = newProfilePropName;
    }
}
