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

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;

/**
 * @author kec
 * 
 */
public class SetBranchForCondition implements PropertyChangeListener {

    private int originId;
    private Condition condition;
    private I_EncodeBusinessProcess process;

    /**
     * @param originId
     * @param condition
     * @param process
     */
    public SetBranchForCondition(int originId, Condition condition, I_EncodeBusinessProcess process) {
        super();
        this.originId = originId;
        this.condition = condition;
        this.process = process;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        int oldId = ((Integer) evt.getOldValue()).intValue();
        int newId = ((Integer) evt.getNewValue()).intValue();
        if (oldId != -1) {
            this.process.removeBranch(this.process.getTask(originId), this.process.getTask(oldId), this.condition);
        }
        if (newId != -1) {
            this.process.addBranch(this.process.getTask(originId), this.process.getTask(newId), this.condition);
        }
    }

}
