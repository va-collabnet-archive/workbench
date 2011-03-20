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
package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public class AddConceptInArenaToRefset extends AbstractTask {
	
    private static final long serialVersionUID = -1488580246193922770L;

    private static final int dataVersion = 1;
    
    
    /** the refset we are adding to */
    private TermEntry refset = new TermEntry(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

    /** the value to be given to the new concept extension */
    private TermEntry memberType = new TermEntry(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refset);
        out.writeObject(this.memberType);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.refset = (TermEntry) in.readObject(); //from String
            this.memberType = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

   @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

   @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            Set<PositionBI> positionSet = new HashSet<PositionBI>();
            for (PathBI path : config.getEditingPathSet()) {
            	positionSet.add(tf.newPosition(path, Integer.MAX_VALUE));
            }
            
            I_HostConceptPlugins host = (I_HostConceptPlugins) 
                    worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            
            
            //get concept to add
            I_GetConceptData conceptToAdd = (I_GetConceptData) host.getTermComponent();
            if (conceptToAdd == null) {
                throw new TaskFailedException("There is no concept in the component view to clone...");
            }
            
            //get type value
            I_GetConceptData member = Terms.get().getConcept(memberType.ids);
            if (member == null) { 
                throw new TerminologyException("No member type has been selected");
            }
            
            //get refset 
            I_GetConceptData refsetConcept = Terms.get().getConcept(refset.ids);
            if (refsetConcept == null || refsetConcept.isCanceled()) { 
                throw new TerminologyException("A working refset has not been selected.");
            }
            
            TerminologyAmendmentBI ammender = 
                    Ts.get().getAmender(config.getEditCoordinate(), 
                    config.getViewCoordinate()); 
            
            //add to refset
            RefexAmendmentSpec refexSpec = 
                    new RefexAmendmentSpec(TkRefsetType.CID, 
                    conceptToAdd.getNid(), refsetConcept.getNid());
            refexSpec.with(RefexAmendmentSpec.RefexProperty.CNID1, 
                    member.getConceptNid());
            ammender.amendIfNotCurrent(refexSpec); 

            if (refsetConcept.isAnnotationStyleRefex()) {
                 Terms.get().addUncommitted(conceptToAdd);
            } else {
                 Terms.get().addUncommitted(refsetConcept);
            }

            return Condition.CONTINUE;
            
        } catch (TerminologyException e) {
        	throw new TaskFailedException(e);
        } catch (Exception e) {
        	 throw new TaskFailedException("Unable to add concept to refset. " + e.getMessage(), e);
		}
    }

   @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

   @Override
    public int[] getDataContainerIds() {
        return new int[] {};
    }
    
    public TermEntry getRefset() {
        return refset;
    }

    public void setRefset(TermEntry refset) {
        this.refset = refset;
    }

    public TermEntry getMemberType() {
        return memberType;
    }

    public void setMemberType(TermEntry memberType) {
        this.memberType = memberType;
    }

}
