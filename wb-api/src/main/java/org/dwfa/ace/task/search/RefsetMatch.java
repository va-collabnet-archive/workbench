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
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Test criteria that tests if a component is in a refset.
 * 
 * @author Dion McMurtrie
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RefsetMatch extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * refset concept the component must be a member of.
     */
    private TermEntry refset = new TermEntry(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refset);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.refset = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData refsetToMatch = AceTaskUtil.getConceptFromObject(refset);
            int refsetId = refsetToMatch.getConceptNid();

            I_TermFactory termFactory = Terms.get();

            boolean result = false;

            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                return isComponentInRefset(frameConfig, termFactory, ((I_GetConceptData) component).getConceptNid(),
                    refsetId);
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned description = (I_DescriptionVersioned) component;

                if (isComponentInRefset(frameConfig, termFactory, description.getDescId(), refsetId)) {
                    result = true;
                } else {
                    result = isComponentInRefset(frameConfig, termFactory, description.getConceptNid(), refsetId);
                }
            } else if (I_RelVersioned.class.isAssignableFrom(component.getClass())) {
                result = isComponentInRefset(frameConfig, termFactory, ((I_RelVersioned) component).getRelId(),
                    refsetId);
            }

            if (inverted) {
                return !result;
            } else {
                return result;
            }
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    private boolean isComponentInRefset(I_ConfigAceFrame frameConfig, I_TermFactory termFactory, int componentId,
            int refsetId) throws IOException, TerminologyException {
        List<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(componentId);

        for (I_ExtendByRef thinExtByRefVersioned : extensions) {
            List<I_ExtendByRefVersion> returnTuples = new ArrayList<I_ExtendByRefVersion>();
            thinExtByRefVersioned.addTuples(frameConfig.getAllowedStatus(), frameConfig.getViewPositionSetReadOnly(),
                returnTuples, frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());
            for (I_ExtendByRefVersion thinExtByRefTuple : returnTuples) {
                if (thinExtByRefTuple.getRefsetId() == refsetId) {
                    return true;
                }
            }
        }

        return false;
    }

    public TermEntry getRefset() {
        return refset;
    }

    public void setRefset(TermEntry refset) {
        this.refset = refset;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

}
