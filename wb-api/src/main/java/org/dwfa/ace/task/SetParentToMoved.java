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
package org.dwfa.ace.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.tk.api.WizardBI;




@BeanList(specs = { @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN) })
public class SetParentToMoved extends AbstractTask{

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    
    private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();

   

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(relParentPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                relParentPropName = (String) in.readObject();
            } 
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

   
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
    	
    	I_GetConceptData parentConcept;
		try {
			parentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.getUids());
			process.setProperty(relParentPropName, parentConcept);  
			} catch (TerminologyException e) {
				throw new TaskFailedException(e);
			} catch (IOException e) {
				throw new TaskFailedException(e);
			} catch (IntrospectionException e) {
				throw new TaskFailedException(e);
			} catch (IllegalAccessException e) {
				throw new TaskFailedException(e);
			} catch (InvocationTargetException e) {
				throw new TaskFailedException(e);
			}
        return Condition.CONTINUE;
    }
       
    public String getRelParentPropName() {
        return relParentPropName;
    }

    public void setRelParentPropName(String newStatusPropName) {
        this.relParentPropName = newStatusPropName;
    }
    
    public Collection<Condition> getConditions() {
    	return AbstractTask.CONTINUE_CONDITION;
    }

}
