package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessExtByRefEntries extends I_ProcessEntries {
    public void processEbr(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
