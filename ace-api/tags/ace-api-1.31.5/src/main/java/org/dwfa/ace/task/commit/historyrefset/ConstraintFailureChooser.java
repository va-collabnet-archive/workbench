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
package org.dwfa.ace.task.commit.historyrefset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;

import java.util.List;

/**
 * Chooses an instance of <code>ConstraintFailure</code> which will then constrain the concept supplied and
 * add any failures and/or fixes into the supplied list.
 * @author ssahayam
 */
public interface ConstraintFailureChooser {

    /**
     * Given a concept, refset and a list of failures, choose a <code>ConstraintFailure</code> implementation to
     * validate the concept supplied.
     * @param forCommit Where this constraint is for commit or not.
     * @param concept The concept to constrain.
     * @param refset The referenceset under constraint.
     * @param failures The list to which any failures should be added.
     */
    void constrain(boolean forCommit, I_GetConceptData concept, I_ThinExtByRefVersioned refset,
                  List<AlertToDataConstraintFailure> failures);
}
