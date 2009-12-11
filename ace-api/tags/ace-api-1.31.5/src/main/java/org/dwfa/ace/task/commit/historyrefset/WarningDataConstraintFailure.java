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
import org.dwfa.ace.task.commit.AbortExtension;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;

import java.util.List;

public final class WarningDataConstraintFailure implements ConstraintFailure {

    WarningDataConstraintFailure(final I_GetConceptData concept, final I_ThinExtByRefVersioned refset,
                                      final List<AlertToDataConstraintFailure> failures) {

        AlertToDataConstraintFailure commitFailure = new AlertToDataConstraintFailure(
                AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
                "<html>For a concept to be part of an history refset it has to be retired.",
                concept);
        commitFailure.getFixOptions().add(new AbortExtension(refset, "Rollback history refset membership?"));
        failures.add(commitFailure);
    }
}
