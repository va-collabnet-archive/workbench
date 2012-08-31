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
package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class ResetWorkerSimulationClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ResetWorkerSimulationClockBeanInfo() {
        super();
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ResetWorkerSimulationClock.class);
        bd.setDisplayName("<html><font color='green'><center>Reset Worker<br>Simulation Clock");
        return bd;
    }
}