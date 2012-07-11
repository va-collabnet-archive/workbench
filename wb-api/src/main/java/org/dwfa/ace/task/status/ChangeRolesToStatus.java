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
package org.dwfa.ace.task.status;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

@BeanList(specs = { @Spec(directory = "tasks/ide/status", type = BeanType.TASK_BEAN) })
public class ChangeRolesToStatus extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private TermEntry newStatus = new TermEntry(ArchitectonicAuxiliary.Concept.RETIRED.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newStatus);
        out.writeObject(activeConceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            newStatus = (TermEntry) in.readObject();
            activeConceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_GetConceptData concept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            if (config.getEditingPathSet().size() == 0) {
                throw new TaskFailedException("You must select at least one editing path. ");
            }

            I_GetConceptData newStatusConcept = Terms.get().getConcept(newStatus.ids);
           
            ViewCoordinate vc = config.getViewCoordinate();
            
            Collection<? extends RelationshipChronicleBI> relsOut = concept.getRelationshipsSource();
            
            //get rels that are NOT isa
            for (RelationshipChronicleBI rel : relsOut) {
                for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                    if (!(vc.getIsaTypeNids().contains(relv.getTypeNid())) && 
                            relv.getCharacteristicNid() != SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()) {
                    	//change status
                    	for (PathBI editPath : config.getEditingPathSet()) {
                    		Set<I_RelPart> partsToAdd = new HashSet<I_RelPart>();
	                    	I_RelPart newPart = (I_RelPart) relv.makeAnalog(
	                    			newStatusConcept.getConceptNid(), 
	                    			Long.MAX_VALUE,
                                                config.getEditCoordinate().getAuthorNid(),
                                                config.getEditCoordinate().getModuleNid(),
	                    			editPath.getConceptNid());
                    	}

                    }
                }
            }
            
            Terms.get().addUncommittedNoChecks(concept);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        this.activeConceptPropName = propName;
    }

    public TermEntry getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TermEntry newStatus) {
        this.newStatus = newStatus;
    }

   /* public TermEntry getRelType() {
        return relType;
    }

    public void setRelType(TermEntry relType) {
        this.relType = relType;
    }*/

}
