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
package org.dwfa.ace.task.developer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Very simple developer tool - can be used to grab a NID from a log and find
 * the concept if there is one
 * NOTE THIS IS NOT A GOOD IDEA FOR OTHER THAN DEVELOPER DEBUGGING
 * 
 * @author Dion
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/developer", type = BeanType.TASK_BEAN) })
public class ConceptFromNid extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_TermFactory termFactory;

    private String propName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();

    private String conceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(propName);
        out.writeObject(conceptPropName);
    }// End method writeObject

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            propName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }// End method complete

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        termFactory = Terms.get();

        try {

            String name = (String) process.getProperty(propName);

            int nid = Integer.parseInt(name);

            I_GetConceptData concept = termFactory.getConcept(nid);

            process.setProperty(conceptPropName, concept);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;

    }// End method evaluate

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }// End method getConditions

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getConceptPropName() {
        return conceptPropName;
    }

    public void setConceptPropName(String conceptPropName) {
        this.conceptPropName = conceptPropName;
    }
}// End class CreateRefsetMembersetPair
