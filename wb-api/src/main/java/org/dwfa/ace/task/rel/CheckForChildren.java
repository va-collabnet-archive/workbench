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
package org.dwfa.ace.task.rel;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

@BeanList(specs = { @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN) })
public class CheckForChildren extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private WizardBI wizard;

    private transient Condition returnCondition;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       // I_GetConceptData newConcept = null;
        try {
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());

            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());

            I_GetConceptData concept = (I_GetConceptData) host.getTermComponent();

            if (concept == null) {
                throw new TaskFailedException("There is no concept in the arena...");
            }

            //ComponentBI component = (ComponentBI) concept;

            //ConceptVersionBI conceptVer = (ConceptVersionBI) component;

            if(concept.getRelsIncoming().size() != 0){
            	JPanel wizardPanel = wizard.getWizardPanel();
            	if (SwingUtilities.isEventDispatchThread()) {
                  wizard.setWizardPanelVisible(true);
               } else {
               try {
                  SwingUtilities.invokeAndWait(new Runnable() {

                  @Override
                  public void run() {
                     wizard.setWizardPanelVisible(true);
                  }
               });
               } catch (InterruptedException ex) {
                  throw new TaskFailedException(ex);
               } catch (InvocationTargetException ex) {
                  throw new TaskFailedException(ex);
               }
               }

            	Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }

            	wizardPanel.add(new JLabel("Please remove the children of the concept before retiring the concept"));

            	returnCondition = Condition.ITEM_CANCELED;
            } else{
            	returnCondition = Condition.CONTINUE;
            }

            host.unlink();
            return returnCondition;

        } catch (IOException e) {
        	throw new TaskFailedException(e);
        }
    }



    public Collection<Condition> getConditions() {
    	return AbstractTask.CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
