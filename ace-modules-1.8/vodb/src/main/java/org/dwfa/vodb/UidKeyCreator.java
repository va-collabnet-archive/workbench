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
package org.dwfa.vodb;

import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.UuidBinding;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryMultiKeyCreator;

public class UidKeyCreator implements SecondaryMultiKeyCreator {

    private UuidBinding uuidBinding;
    private ThinIdVersionedBinding idBinding;

    public UidKeyCreator(UuidBinding uuidBinding, ThinIdVersionedBinding idBinding) {
        super();
        this.uuidBinding = uuidBinding;
        this.idBinding = idBinding;
    }

    @SuppressWarnings("unchecked")
    public void createSecondaryKeys(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
            Set results) throws DatabaseException {
        Set<DatabaseEntry> keySet = results;
        I_IdVersioned id = (I_IdVersioned) idBinding.entryToObject(dataEntry);
        for (I_IdPart p : id.getVersions()) {
            if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
                UUID secondaryId = (UUID) p.getSourceId();
                DatabaseEntry entry = new DatabaseEntry();
                uuidBinding.objectToEntry(secondaryId, entry);
                keySet.add(entry);
            }
        }
    }
}
