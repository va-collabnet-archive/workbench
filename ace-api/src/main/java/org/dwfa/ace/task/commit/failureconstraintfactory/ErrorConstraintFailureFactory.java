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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;

/**
 * This class is used to create instances of
 * {@link AlertToDataConstraintFailure} objects with alert type
 * {@link ALERT_TYPE.ERROR}
 * 
 * @author Matthew Edwards
 */
public final class ErrorConstraintFailureFactory implements AlertToDataConstraintFailureFactory {

    private ALERT_TYPE alertType;

    /**
     * Package Private Constructor to prevent instantiaton other than by
     * {@link AlertToDataConstraintFailureFactoryChooser}
     */
    ErrorConstraintFailureFactory() {
        alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
    }

    /**
     * Creates an instance of {@link AlertToDataConstraintFailure} with the
     * alert type of {@link ALERT_TYPE.ERROR}
     * 
     * @see AlertToDataConstraintFailureFactory
     *      #createAlertToDataConstraintFailure(java.lang.String,
     *      org.dwfa.ace.api.I_GetConceptData)
     */
    public AlertToDataConstraintFailure createAlertToDataConstraintFailure(String message,
            I_GetConceptData conceptWithAlert) {
        AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType, message, conceptWithAlert);
        Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO, message);
        return alert;
    }
}
