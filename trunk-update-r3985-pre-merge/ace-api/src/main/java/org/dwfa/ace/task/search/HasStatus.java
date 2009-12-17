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
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
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
public class HasStatus extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Status concept for the term component to test.
     */
    private TermEntry statusTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.statusTerm);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.statusTerm = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData statusToMatch = AceTaskUtil.getConceptFromObject(statusTerm);

            I_GetConceptData conceptToTest;
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                conceptToTest = (I_GetConceptData) component;
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
                conceptToTest = LocalVersionedTerminology.get().getConcept(desc.getConceptId());
            } else {
                return applyInversion(false);
            }

            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("### testing status for: " + conceptToTest);
            }

            List<I_ConceptAttributeTuple> attributeTuples = conceptToTest.getConceptAttributeTuples(null,
                frameConfig.getViewPositionSet());

            for (I_ConceptAttributeTuple tuple : attributeTuples) {
                I_GetConceptData statusToTest = LocalVersionedTerminology.get().getConcept(tuple.getConceptStatus());
                if (statusToMatch.isParentOfOrEqualTo(statusToTest, frameConfig.getAllowedStatus(),
                    frameConfig.getDestRelTypes(), frameConfig.getViewPositionSet(), false)) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("    status check " + statusToTest + "true for " + conceptToTest);
                        AceLog.getAppLog().info("Status OK: " + conceptToTest.getUids());
                    }
                    return applyInversion(true);
                }
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("    status check false " + statusToTest + " for " + conceptToTest);
                }
            }
            return applyInversion(false);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public TermEntry getStatusTerm() {
        return statusTerm;
    }

    public void setStatusTerm(TermEntry statusTerm) {
        this.statusTerm = statusTerm;
    }

}
