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

import junit.framework.Assert;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.easymock.internal.MocksControl;
import org.junit.Before;
import org.junit.Test;

public class AlertToDataConstraintFailureFactoryTest {

    private I_GetConceptData mockConceptData;
    private MocksControl mocksControl;

    @Before
    public void setup() {
        mocksControl = new MocksControl(MocksControl.MockType.DEFAULT);
        mockConceptData = mocksControl.createMock(I_GetConceptData.class);
    }

    @Test
    public void testGetErrorAlertType() throws Exception {
        String message = "test alert with alert type 'ERROR'";
        AlertToDataConstraintFailureAbstractFactory factoryChooser = new SimpleConstraintFailureChooser(true);
        AlertToDataConstraintFailureFactory factory = factoryChooser.getFactory();
        AlertToDataConstraintFailure alert = factory.createAlertToDataConstraintFailure(message, mockConceptData);
        Assert.assertEquals(alert.getAlertType(), ALERT_TYPE.ERROR);
        Assert.assertEquals(alert.getConceptWithAlert(), mockConceptData);
        Assert.assertEquals(alert.getAlertMessage(), message);
    }

    @Test
    public void testGetWarningAlertType() throws Exception {
        String message = "test alert with alert type 'WARNING'";
        AlertToDataConstraintFailureAbstractFactory factoryChooser = new SimpleConstraintFailureChooser(false);
        AlertToDataConstraintFailureFactory factory = factoryChooser.getFactory();
        AlertToDataConstraintFailure alert = factory.createAlertToDataConstraintFailure(message, mockConceptData);
        Assert.assertEquals(alert.getAlertType(), ALERT_TYPE.WARNING);
        Assert.assertEquals(alert.getConceptWithAlert(), mockConceptData);
        Assert.assertEquals(alert.getAlertMessage(), message);
    }
}
