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
package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class IsKindOf extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the term component to test.
     */
    private TermEntry parentTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.parentTerm);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.parentTerm = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData parent = AceTaskUtil.getConceptFromObject(parentTerm);

            I_GetConceptData potChild;
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                potChild = (I_GetConceptData) component;
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
                potChild = Terms.get().getConcept(desc.getConceptNid());
            } else {
                return applyInversion(false);
            }
            if (parent.getConceptNid() == potChild.getConceptNid()) {
                return applyInversion(true);
            }

            return applyInversion(parent.isParentOf(potChild, frameConfig.getAllowedStatus(),
                frameConfig.getDestRelTypes(), frameConfig.getViewPositionSetReadOnly(),
                frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()));
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public TermEntry getParentTerm() {
        return parentTerm;
    }

    public void setParentTerm(TermEntry parentTerm) {
        this.parentTerm = parentTerm;
    }

}
