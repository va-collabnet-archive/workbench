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
 * Created on Jul 26, 2005
 */
package org.dwfa.bpa.tasks.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/flow tasks", type = BeanType.TASK_BEAN) })
public class RandomBranch extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private static SecureRandom randomGenerator = new SecureRandom();

    /**
     * Branch frequency as an integer between 0 - 100 %.
     */
    private int branchFrequency = 0;

    /**
     * @return Returns the relativeTimeInMins.
     */
    public Integer getBranchFrequency() {
        return new Integer(branchFrequency);
    }

    public void setBranchFrequency(Integer relativeTime) {
        Integer oldValue = new Integer(branchFrequency);
        this.branchFrequency = relativeTime.intValue();
        this.firePropertyChange("relativeTimeInMins", oldValue, relativeTime);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.branchFrequency);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.branchFrequency = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        if (this.branchFrequency == 100) {
            return Condition.TRUE;
        }
        if (this.branchFrequency == 0) {
            return Condition.FALSE;
        }
        if ((randomGenerator.nextFloat() * 100) <= this.branchFrequency) {
            return Condition.TRUE;
        }
        return Condition.FALSE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
