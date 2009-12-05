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

/**
 * This class represents and implementation of {@link AlertToDataConstraintFailureAbstractFactory} that
 * uses the {@code forCommit} flag on {@link AbstractConceptTest#test(org.dwfa.ace.api.I_GetConceptData, boolean)}
 * to determine the type of {@link AlertToDataConstraintFailureFactory} to return.
 * @author Matthew Edwards
 */
public class SimpleConstraintFailureChooser implements
        AlertToDataConstraintFailureAbstractFactory {

    private boolean isForCommit;

    public SimpleConstraintFailureChooser(boolean isForCommit) {
        this.isForCommit = isForCommit;
    }

    /**
     * @see AlertToDataConstraintFailureAbstractFactory#getFactory()
     */
    public AlertToDataConstraintFailureFactory getFactory() {
        if (isForCommit) {
            return new ErrorConstraintFailureFactory();
        } else {
            return new WarningConstraintFailureFactory();
        }
    }
}
