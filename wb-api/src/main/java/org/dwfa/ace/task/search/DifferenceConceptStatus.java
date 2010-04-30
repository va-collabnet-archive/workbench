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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;

public class DifferenceConceptStatus extends AbstractSearchTest {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {

            if (frameConfig.getViewPositionSet().size() < 2) {
                // Cannot be a difference if there are not two or more paths to
                // compare...
                return applyInversion(false);
            }
            I_GetConceptData conceptToTest;
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                conceptToTest = (I_GetConceptData) component;
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
                conceptToTest = Terms.get().getConcept(desc.getConceptId());
            } else {
                return applyInversion(false);
            }

            I_ConceptAttributeTuple firstTuple = null;
            boolean firstPass = true;
            for (I_Position p : frameConfig.getViewPositionSet()) {
                Set<I_Position> positions = new HashSet<I_Position>();
                positions.add(p);
                PositionSetReadOnly positionSet = new PositionSetReadOnly(positions);
                List<? extends I_ConceptAttributeTuple> tuples = conceptToTest.getConceptAttributeTuples(
                    frameConfig.getAllowedStatus(), positionSet, 
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());
                if (firstPass) {
                    if (tuples.size() > 0) {
                        firstTuple = tuples.get(0);
                    }
                    firstPass = false;
                } else {
                    if (tuples.size() > 0) {
                        if (firstTuple == null) {
                            return applyInversion(true);
                        }
                        return (applyInversion(firstTuple.getStatusId() != tuples.get(0).getStatusId()));
                    } else {
                        if (firstTuple != null) {
                            return (applyInversion(true));
                        }
                    }
                }
            }
            return applyInversion(false);
        } catch (IOException ex) {
            throw new TaskFailedException(ex);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }
}
