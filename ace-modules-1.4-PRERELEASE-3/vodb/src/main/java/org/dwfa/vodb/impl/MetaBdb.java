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
package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.I_StoreMetadata;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class MetaBdb implements I_StoreMetadata {

    private Database metaInfoDb; // change set info, version info...
    private TupleBinding stringBinder = TupleBinding.getPrimitiveBinding(String.class);

    public MetaBdb(Environment env, DatabaseConfig metaInfoDbConfig) throws DatabaseException {
        super();
        metaInfoDb = env.openDatabase(null, "metaInfoDb", metaInfoDbConfig);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#getProperty(java.lang.String)
     */
    public String getProperty(String key) throws IOException {
        DatabaseEntry propKey = new DatabaseEntry();
        DatabaseEntry propValue = new DatabaseEntry();

        stringBinder.objectToEntry(key, propKey);
        try {
            if (metaInfoDb.get(BdbEnv.transaction, propKey, propValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                return (String) stringBinder.entryToObject(propValue);
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#getProperties()
     */
    public Map<String, String> getProperties() throws IOException {
        try {
            Cursor concCursor = metaInfoDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            HashMap<String, String> propMap = new HashMap<String, String>();
            while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                try {
                    String key = (String) stringBinder.entryToObject(foundKey);
                    String value = (String) stringBinder.entryToObject(foundData);
                    propMap.put(key, value);
                } catch (Exception e) {
                    concCursor.close();
                    throw new ToIoException(e);
                }
            }
            concCursor.close();
            return Collections.unmodifiableMap(propMap);
        } catch (Exception e) {
            throw new ToIoException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#setProperty(java.lang.String,
     * java.lang.String)
     */
    public void setProperty(String key, String value) throws IOException {
        DatabaseEntry propKey = new DatabaseEntry();
        DatabaseEntry propValue = new DatabaseEntry();
        stringBinder.objectToEntry(key, propKey);
        stringBinder.objectToEntry(value, propValue);
        try {
            metaInfoDb.put(BdbEnv.transaction, propKey, propValue);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void close() throws DatabaseException {
        if (metaInfoDb != null) {
            metaInfoDb.close();
        }
    }

    public void sync() throws DatabaseException {
        if (metaInfoDb != null) {
            if (!metaInfoDb.getConfig().getReadOnly()) {
                metaInfoDb.sync();
            }
        }
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        // nothing to do

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
