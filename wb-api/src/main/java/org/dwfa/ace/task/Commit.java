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
package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/db", type = BeanType.TASK_BEAN) })
public class Commit extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        List<String> extraGeneratorKeys = new ArrayList<String>();
        try {
            List<ChangeSetGeneratorBI> extraGeneratorList = 
            	(List<ChangeSetGeneratorBI>) process.readAttachement(ProcessAttachmentKeys.EXTRA_CHANGE_SET_GENERATOR_LIST.getAttachmentKey());

            if (extraGeneratorList != null) {
            	for (ChangeSetGeneratorBI generator: extraGeneratorList) {
            		UUID key = UUID.randomUUID();
            		extraGeneratorKeys.add(key.toString());
                    Ts.get().addChangeSetGenerator(key.toString(), generator);
            	}
            }

            Terms.get().commit();
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        } finally {
        	for (String key: extraGeneratorKeys) {
                Ts.get().removeChangeSetGenerator(key);
        	}
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
