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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

@BeanList(specs = { @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN) })
public class SetActiveConceptToPrimitive extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(activeConceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
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
            ConceptChronicleBI cc = (ConceptChronicleBI) concept;
            ConAttrChronicleBI ca = cc.getConAttrs();
            ConAttrVersionBI cv = (ConAttrVersionBI) ca;
            
            if(cv.isDefined() == true){
        		//make analog
	            for (PathBI ep: config.getEditingPathSet()) {
					AnalogBI newAnalog = cv.makeAnalog(
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
							config.getDbConfig().getUserConcept().getNid(),
							ep.getConceptNid(), 
							Long.MAX_VALUE);
					
					I_ConceptAttributePart newAnalogAttr = (I_ConceptAttributePart) newAnalog;
					newAnalogAttr.setDefined(false);
					
					cv.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), 
							config.getDbConfig().getUserConcept().getNid(),
							ep.getConceptNid(), 
							Long.MAX_VALUE);
					I_GetConceptData retireConcept = Terms.get().getConceptForNid(cv.getNid());
    	            Terms.get().addUncommitted(retireConcept);
					}
	            Terms.get().addUncommitted(concept);
        	} else{
        		return Condition.CONTINUE;
        	}
           return Condition.CONTINUE;
        }catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
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
}
