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

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.bind.ThinImageBinder;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ConceptKeyForImageCreator implements SecondaryKeyCreator {
    ThinImageBinder imageBinder = new ThinImageBinder();
    EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public boolean createSecondaryKey(SecondaryDatabase secDb,
            DatabaseEntry keyEntry, DatabaseEntry dataEntry,
            DatabaseEntry resultEntry) throws DatabaseException {
        I_ImageVersioned image =
                (I_ImageVersioned) imageBinder.entryToObject(dataEntry);
        intBinder.objectToEntry(image.getConceptId(), resultEntry);
        return true;
    }

}
