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
/*
 * Created on Mar 21, 2005
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

/**
 * @author kec
 * 
 */
public class SetCurrentTaskId implements PropertyChangeListener {

    I_EncodeBusinessProcess process;

    /**
     * @param process
     */
    public SetCurrentTaskId(I_EncodeBusinessProcess process) {
        super();
        this.process = process;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        int currentTaskId = ((Integer) evt.getNewValue()).intValue();
        this.process.setCurrentTaskId(currentTaskId);
    }
}
