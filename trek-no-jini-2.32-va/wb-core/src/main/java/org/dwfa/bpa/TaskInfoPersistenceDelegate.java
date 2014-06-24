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
 * Created on Jan 9, 2006
 */
package org.dwfa.bpa;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.util.Collection;

public class TaskInfoPersistenceDelegate extends DefaultPersistenceDelegate {

    public TaskInfoPersistenceDelegate() {
        super();
    }

    /**
     * @see java.beans.DefaultPersistenceDelegate#initialize(java.lang.Class,
     *      java.lang.Object, java.lang.Object, java.beans.Encoder)
     */
    @Override
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);

        TaskInfo ti = (TaskInfo) oldInstance;

        Collection<Branch> branches = ti.getBranches();
        for (Branch b : branches) {
            out.writeStatement(new Statement(oldInstance, "addBranch", new Object[] { b }));
        }

        Collection<ExecutionRecord> records = ti.getExecutionRecords();
        for (ExecutionRecord r : records) {
            out.writeStatement(new Statement(oldInstance, "addExecutionRecord", new Object[] { r }));
        }
    }

}
