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
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.conflict.detector.RelTupleConflictComparator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionBI;

public abstract class AbstractDifferenceRels extends AbstractSearchTest {

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
                conceptToTest = Terms.get().getConcept(desc.getConceptNid());
            } else {
                return applyInversion(false);
            }

            TreeSet<I_RelTuple> firstSet = null;
            for (PositionBI p : frameConfig.getViewPositionSet()) {
                Set<PositionBI> positionSet = new HashSet<PositionBI>();
                positionSet.add(p);
                PositionSetReadOnly viewSet = new PositionSetReadOnly(positionSet);
                List<? extends I_RelTuple> tuples = getTuplesToCompare(frameConfig, conceptToTest, viewSet);
                if (firstSet == null) {
                    firstSet = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
                    firstSet.addAll(tuples);
                } else {
                    int firstSetSize = firstSet.size();
                    if (firstSetSize != tuples.size()) {
                        return applyInversion(true);
                    }
                    firstSet.addAll(tuples);
                    if (firstSet.size() != firstSetSize) {
                        return applyInversion(true);
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

    protected abstract List<? extends I_RelTuple> getTuplesToCompare(I_ConfigAceFrame frameConfig,
            I_GetConceptData conceptToTest, PositionSetReadOnly viewSet) throws IOException, TerminologyException;

}
