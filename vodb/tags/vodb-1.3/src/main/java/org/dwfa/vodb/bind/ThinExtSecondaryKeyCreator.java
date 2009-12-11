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

import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ThinExtSecondaryKeyCreator implements SecondaryKeyCreator {
    public enum KEY_TYPE {
        REFSET_ID, COMPONENT_ID
    };

    private static ThinExtBinder fixedOnlyBinder = new ThinExtBinder(true);

    private KEY_TYPE keyType;

    public static class MemberAndSecondaryId {
        int memberId;

        int secondaryId;

        public MemberAndSecondaryId() {
            super();
        }

        public MemberAndSecondaryId(int secondaryId, int memberId) {
            super();
            this.memberId = memberId;
            this.secondaryId = secondaryId;
        }

        public int getSecondaryId() {
            return secondaryId;
        }

        public void setSecondaryId(int c1Id) {
            this.secondaryId = c1Id;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int relId) {
            this.memberId = relId;
        }
    }

    public static class MemberAndSecondaryIdBinding extends TupleBinding {

        public MemberAndSecondaryId entryToObject(TupleInput ti) {
            return new MemberAndSecondaryId(ti.readInt(), ti.readInt());
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            MemberAndSecondaryId id = (MemberAndSecondaryId) obj;
            to.writeInt(id.getSecondaryId());
            to.writeInt(id.getMemberId());
        }

    }

    MemberAndSecondaryId memberAndSecondaryId = new MemberAndSecondaryId();

    MemberAndSecondaryIdBinding memberAndSecondaryIdBinding = new MemberAndSecondaryIdBinding();

    public ThinExtSecondaryKeyCreator(KEY_TYPE keyType) {
        super();
        this.keyType = keyType;
    }

    public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
        DatabaseEntry resultEntry) throws DatabaseException {
        ThinExtByRefVersioned core = (ThinExtByRefVersioned) fixedOnlyBinder.entryToObject(dataEntry);
        
        
        switch (keyType) {
        case REFSET_ID:
            return createSecondaryKey(core.getMemberId(), core.getRefsetId(), resultEntry);
        case COMPONENT_ID:
            return createSecondaryKey(core.getMemberId(), core.getComponentId(), resultEntry);

        default:
            throw new RuntimeException("Can't handle keytype:" + keyType);
        }
    }

    public synchronized boolean createSecondaryKey(int memberId, int secondaryId, DatabaseEntry resultEntry)
            throws DatabaseException {

        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Creating secondary key (2) for " + keyType + " m: " + memberId + " s: " + secondaryId);
        }
        memberAndSecondaryId.setSecondaryId(secondaryId);
        memberAndSecondaryId.setMemberId(memberId);
        memberAndSecondaryIdBinding.objectToEntry(memberAndSecondaryId, resultEntry);
        return true;
    }

}
