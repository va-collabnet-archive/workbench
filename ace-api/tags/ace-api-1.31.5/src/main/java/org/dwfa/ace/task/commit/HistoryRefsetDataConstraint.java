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
/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.commit;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.historyrefset.ConstraintFailureChooser;
import org.dwfa.ace.task.commit.historyrefset.DefaultConstraintFailureChooser;
import org.dwfa.ace.task.commit.historyrefset.HistoryRefsetDataValidatorImpl;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Verifies data contraints that apply to a HistoryReferenceSet.
 * Currently these checks are:
 * <p/>
 * <ol>
 *    <li>The source concept of this referenceset must be retired.
 * </ol>
 * @author ssahayam
 */
@BeanList(specs = {
        @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class HistoryRefsetDataConstraint extends AbstractConceptTest {

    private static final long serialVersionUID              = 6855477436530159022L;
    private static final int DATA_VERSION                   = 1;

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
    }

    private void readObject(final ObjectInputStream in) throws IOException {
        int objDataVersion = in.readInt();
        if (objDataVersion != DATA_VERSION) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(final I_GetConceptData concept, final boolean forCommit)
            throws TaskFailedException {
        try {
            return new HistoryRefsetDataValidatorImpl(getTermFactory(), getConstraintFailureChooser(), forCommit).
                    validate(concept);
        } catch (Exception e) {
            logWarning(e);
            throw new TaskFailedException(e);
        }
    }

    private ConstraintFailureChooser getConstraintFailureChooser() {
        return new DefaultConstraintFailureChooser();
    }

    private void logWarning(final Exception e) {
        getLogger().warning("An exception was thrown while running validation on association refset. "
                            + e.getMessage());
    }

    private I_TermFactory getTermFactory() {
        return LocalVersionedTerminology.get();
    }
}
