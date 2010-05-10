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
package org.dwfa.vodb.bind;

import java.util.logging.Level;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ThinExtRefsetIdSecondaryKeyCreator implements SecondaryKeyCreator {
    private ThinExtBinder fixedOnlyBinder = new ThinExtBinder(true);

    MemberAndSecondaryIdBinding memberAndSecondaryIdBinding = new MemberAndSecondaryIdBinding();

    public ThinExtRefsetIdSecondaryKeyCreator() {
        super();
    }

    public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
            DatabaseEntry resultEntry) throws DatabaseException {
        I_ThinExtByRefVersioned core = (I_ThinExtByRefVersioned) fixedOnlyBinder.entryToObject(dataEntry);

        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "Creating secondary RefsetId key (2) for m: " + core.getMemberId() + " refset: " + core.getRefsetId());
        }
        memberAndSecondaryIdBinding.objectToEntry(new MemberAndSecondaryId(core.getRefsetId(), core.getMemberId()),
            resultEntry);
        return true;
    }

}
