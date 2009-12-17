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
package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessIdEntries;

import com.sleepycat.je.DatabaseEntry;

public class IdCounter extends TermCounter implements I_ProcessIdEntries {

    public Object call() throws Exception {
        AceConfig.getVodb().iterateIdEntries(this);
        return null;
    }

    public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception {
        if (canceled) {
            throw new InterruptedException();
        }
        count++;
    }

}
