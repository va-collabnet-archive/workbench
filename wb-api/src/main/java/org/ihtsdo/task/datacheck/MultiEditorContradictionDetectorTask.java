/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.task.datacheck;

import java.util.Collection;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.bdb.MultiEditorContradictionDetector;

/**
 *
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/multi-editor", type = BeanType.TASK_BEAN)})
public class MultiEditorContradictionDetectorTask extends AbstractTask {

    // 
    /**
     * AbstractTask.evaluate(..) will check for contradictions based on the COMMIT_RECORD refset.
     *
     * @param process
     * @param worker
     * @return
     * @throws TaskFailedException
     */
    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

         MultiEditorContradictionDetector mecd;
         // mecd = new MultiEditorContradictionDetector(commitRecRefsetNid, vc);

        return Condition.CONTINUE;
    }

    /**
     * AbstractTask.complete(..) does not perform any additional processing.
     *
     * @param process
     * @param worker
     * @throws TaskFailedException
     */
    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do
    }

    /**
     * AbstractTask.getConditions() returns CONTINUE_CONDITION.
     *
     * @return
     */
    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }
}
