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
package org.dwfa.ace.task.commit.failureconstraintfactory;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;

/**
 * This interface is used as an implementation of the Factory Pattern for
 * creating AlertToDataConstraintFailure objects.
 * 
 * @author Matthew Edwards
 */
public interface AlertToDataConstraintFailureFactory {

    /**
     * Delegates instance creation of AlertToDataConstraintFailure objects to
     * the subclass.
     * Each Subclass is responsible for creating it's {@code
     * AlertToDataConstraintFailure} objects.
     * 
     * @param message String containing the message for the
     *            {@link AlertToDataConstraintFailure}
     * @param conceptWithAlert I_GetConceptData that the
     *            {@link AlertToDataConstraintFailure} is for
     * @return an instantiated {@link AlertToDataConstraintFailure} object
     */
    AlertToDataConstraintFailure createAlertToDataConstraintFailure(String message, I_GetConceptData conceptWithAlert);
}
