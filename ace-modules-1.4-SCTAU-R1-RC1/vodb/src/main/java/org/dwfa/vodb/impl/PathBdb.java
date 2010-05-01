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
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.I_StorePaths;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessPathEntries;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class PathBdb implements I_StoreInBdb, I_StorePaths {

    private Database pathDb;
    private PathBinder pathBinder = new PathBinder();
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public PathBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        pathDb = env.openDatabase(null, "pathDb", dbConfig);
    }

    public void close() throws DatabaseException {
        if (pathDb != null) {
            pathDb.close();
        }
    }

    public void sync() throws DatabaseException {
        if (pathDb != null) {
            if (!pathDb.getConfig().getReadOnly()) {
                pathDb.sync();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StorePaths#writePath(org.dwfa.ace.api.I_Path)
     */
    public void writePath(I_Path p) throws DatabaseException {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(p.getConceptId(), key);
        pathBinder.objectToEntry(p, value);
        pathDb.put(BdbEnv.transaction, key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StorePaths#getPath(int)
     */
    public I_Path getPath(int nativeId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting path : " + nativeId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry pathKey = new DatabaseEntry();
        DatabaseEntry pathValue = new DatabaseEntry();
        intBinder.objectToEntry(nativeId, pathKey);
        if (pathDb.get(BdbEnv.transaction, pathKey, pathValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Got path: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return (I_Path) pathBinder.entryToObject(pathValue);
        }
        throw new DatabaseException("Path: " + ConceptBean.get(nativeId).toString() + " not found.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StorePaths#hasPath(int)
     */
    public boolean hasPath(int nativeId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting path : " + nativeId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry pathKey = new DatabaseEntry();
        DatabaseEntry pathValue = new DatabaseEntry();
        intBinder.objectToEntry(nativeId, pathKey);
        if (pathDb.get(BdbEnv.transaction, pathKey, pathValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Got path: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return true;
        }
        return false;
    }

    public void iteratePaths(I_ProcessPathEntries processor) throws Exception {
        Cursor pathCursor = pathDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (pathCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processPath(foundKey, foundData);
            } catch (Exception e) {
                pathCursor.close();
                throw e;
            }
        }
        pathCursor.close();
    }

    public I_Path pathEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return (I_Path) pathBinder.entryToObject(value);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        // Nothing to do

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
