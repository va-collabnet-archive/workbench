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
package org.dwfa.derby.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Derby DB containing the Uuid-SctId mappings for an id mapping file.
 * 
 * @author Keith Camble, Ean Dungey
 */
public class DerbyBackedUuidSctidFixedMap implements Map<UUID, Long> {
    /** Class logger */
    private Logger logger = Logger.getLogger(DerbyBackedUuidSctidFixedMap.class.getName());
    /** DB connection */
    private Connection conn;
    /** Prepared Statement to check if value is in DB. */
    private PreparedStatement containsValue;
    /** Prepared Statement to check if key is in DB. */
    private PreparedStatement containsKey;
    /** Prepared Statement to get a value from the DB. */
    private PreparedStatement getValue;
    /** Prepared Statement to remove a value from the DB. */
    private PreparedStatement deleteValue;
    /** Prepared Statement to list all the keys in the DB. */
    private PreparedStatement listKeys;
    /** Prepared Statement to list all the values in the DB. */
    private PreparedStatement listValues;
    /** Prepared Statement to get the DB size. */
    private PreparedStatement count;
    /** Prepared Statement to insert a new row in the DB. */
    private PreparedStatement insert;
    /** Prepared Statement to delete all rows in the DB. */
    private PreparedStatement deleteAll;
    /** Used to remove lck files */
    private File dbFolder;

    /**
     * Reads the contents of a mapping file. IF no database exists then a new
     * database is created from the contents of the mapping file. If the
     * database does exist them the database in opened.
     * 
     * @param fixedMapFile id map file to create a DB from if no DB exits.
     * 
     * @return DerbyBackedUuidSctidFixedMap
     * 
     * @throws IOException if cannot read/create/open the mapping file/database.
     */
    public static DerbyBackedUuidSctidFixedMap read(File fixedMapFile) throws IOException {
        return new DerbyBackedUuidSctidFixedMap(new File(fixedMapFile.getParent(), fixedMapFile.getName() + ".bdb"),
            fixedMapFile);
    }

    /**
     * Creates or Opens a Derby DB.
     * 
     * If the DB files exits it is simply opened other wise a new database is
     * created.
     * 
     * @param dbFolder File
     * @param fixedMapFile File
     * @throws IOException error opening or creating the DB.
     */
    private DerbyBackedUuidSctidFixedMap(File dbFolder, File fixedMapFile) throws IOException {
        super();

        File databaseFile = new File(fixedMapFile.getParent(), fixedMapFile.getName() + ".bdb");

        if (databaseFile.exists() && databaseFile.canRead()) {
            openExistingDb(databaseFile);
        } else {
            createDb(dbFolder, fixedMapFile);
        }
    }

    /**
     * Open the exiting DB.
     * 
     * Setup the PreparedStatement of the DB.
     * 
     * @param dbFolder File
     * @throws IOException error opening the DB.
     */
    private void openExistingDb(File dbFolder) throws IOException {
        logger.info("Opening existing DB " + dbFolder.getName());
        try {
            connectToDataBase(dbFolder, false);
            createStatements();
        } catch (SQLException e) {
            toRuntimeException(e);
        }
    }

    /**
     * Create a new DB and load with the contents of the fixedMapFile
     * 
     * @param dbFolder File
     * @param fixedMapFile File
     * @throws IOException reading the map file or creating the DB.
     */
    private void createDb(File dbFolder, File fixedMapFile) throws IOException {
        logger.info("Creating new DB " + dbFolder.getName());

        int insertCount = 0;
        try {
            connectToDataBase(dbFolder, false);
            createTables();
            createStatements();

            BufferedReader br = new BufferedReader(new FileReader(fixedMapFile));

            String uuidLineStr;
            String sctIdLineStr;
            while ((uuidLineStr = br.readLine()) != null) {
                sctIdLineStr = br.readLine();
                Long sctId = Long.parseLong(sctIdLineStr);
                for (String uuidStr : uuidLineStr.split("\t")) {
                    UUID uuid = UUID.fromString(uuidStr);
                    insert.setLong(1, uuid.getMostSignificantBits());
                    insert.setLong(2, uuid.getLeastSignificantBits());
                    insert.setLong(3, sctId);
                    insert.execute();
                    insertCount++;

                    if (insertCount % 10000 == 0) {
                        logger.info("Created " + insertCount + " rows");
                    }
                }
            }
            br.close();
            conn.prepareStatement("CREATE INDEX UUID_IDX ON UUID_SCT_MAP (MSB, LSB)").execute();
            conn.prepareStatement("CREATE INDEX SCTID_IDX ON UUID_SCT_MAP (SCTID)").execute();

            conn.commit();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        logger.info("Created new DB " + dbFolder.getName());
    }

    /**
     * Create the DB tables and indexes.
     * 
     * @throws SQLException creating the tables/indexes.
     */
    private void createTables() throws SQLException {
        conn.prepareStatement("CREATE TABLE UUID_SCT_MAP (MSB BIGINT, LSB BIGINT, SCTID BIGINT)").execute();
    }

    /**
     * Creates the prepare statements for this DB.
     * 
     * @throws SQLException creating the prepare statements
     */
    private void createStatements() throws SQLException {
        containsValue = conn.prepareStatement("SELECT COUNT(SCTID) FROM UUID_SCT_MAP WHERE SCTID = ?");
        getValue = conn.prepareStatement("SELECT SCTID FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
        deleteValue = conn.prepareStatement("DELETE FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
        count = conn.prepareStatement("SELECT COUNT(*) FROM UUID_SCT_MAP");
        insert = conn.prepareStatement("INSERT INTO UUID_SCT_MAP(MSB, LSB, SCTID) VALUES(?, ?, ?)");
        listKeys = conn.prepareStatement("SELECT MSB, LSB FROM UUID_SCT_MAP");
        listValues = conn.prepareStatement("SELECT SCTID FROM UUID_SCT_MAP");
        deleteAll = conn.prepareStatement("DELETE FROM UUID_SCT_MAP");
        containsKey = conn.prepareStatement("SELECT COUNT(SCTID) FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
    }

    /**
     * Connects to the database, if no database exists then a new one is
     * created.
     * 
     * @param dbFolder File
     * @param autoCommit boolean
     * @throws SQLException connecting to the DB.
     * @throws IOException reading the DB.
     */
    private void connectToDataBase(File dbFolder, boolean autoCommit) throws SQLException, IOException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            toRuntimeException(e);
        }

        conn = DriverManager.getConnection("jdbc:derby:directory:" + dbFolder.getCanonicalPath() + ";create=true;");

        conn.setAutoCommit(autoCommit);

        this.dbFolder = dbFolder;
    }

    /**
     * Exception wrapper.
     * 
     * @param ex Exception
     * @throws RuntimeException always thrown
     */
    private void toRuntimeException(Exception ex) throws RuntimeException {
        throw new RuntimeException(ex);

    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        try {
            deleteAll.execute();
            conn.commit();
        } catch (SQLException e) {
            toRuntimeException(e);
        }
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        boolean hasKey = false;

        try {
            UUID uuid = (UUID) key;
            containsKey.setLong(1, uuid.getMostSignificantBits());
            containsKey.setLong(2, uuid.getLeastSignificantBits());
            ResultSet results = containsKey.executeQuery();
            if (results.next()) {
                hasKey = results.getInt(1) > 0;
            }
            results.close();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return hasKey;
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        boolean hasValue = false;

        try {
            Long sctid = (Long) value;
            containsValue.setLong(1, sctid);
            ResultSet results = containsValue.executeQuery();
            if (results.next()) {
                hasValue = results.getInt(1) > 0;
            }
            results.close();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return hasValue;
    }

    /**
     * @see java.util.Map#entrySet()
     * 
     * @throws UnsupportedOperationException
     */
    public Set<java.util.Map.Entry<UUID, Long>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Long get(Object key) {
        Long value = null;
        try {
            if (containsKey(key)) {
                UUID uuid = (UUID) key;
                getValue.setLong(1, uuid.getMostSignificantBits());
                getValue.setLong(2, uuid.getLeastSignificantBits());
                ResultSet results = getValue.executeQuery();
                if (results.next()) {
                    value = results.getLong(1);
                }
                results.close();
            }
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return value;
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        boolean isEmpty = false;
        try {
            ResultSet results = count.executeQuery();
            if (results.next()) {
                isEmpty = results.getInt(1) == 0;
            }
            results.close();
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return isEmpty;
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<UUID> keySet() {
        Set<UUID> keySet = new HashSet<UUID>();

        ResultSet results;
        try {
            results = listKeys.executeQuery();
            for (; results.next();) {
                UUID uuid = new UUID(results.getLong(1), results.getLong(2));
                keySet.add(uuid);
            }
            results.close();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return keySet;

    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Long put(UUID key, Long value) {
        return put(key, value, true);
    }

    /**
     * Execute the insert/update SQL and optionally commit the changes.
     * 
     * @param key UUID
     * @param value Long
     * @param commit commit after putting
     * @return previous value.
     */
    private Long put(UUID key, Long value, boolean commit) {
        Long currentValue = get(key);

        try {
            if (currentValue == null || !currentValue.equals(value)) {
                insert.setLong(1, key.getMostSignificantBits());
                insert.setLong(2, key.getLeastSignificantBits());
                insert.setLong(3, value);
                insert.execute();
                if (commit) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return currentValue;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends UUID, ? extends Long> t) {
        for (UUID uuidKey : t.keySet()) {
            put(uuidKey, t.get(uuidKey), false);
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            toRuntimeException(e);
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Long remove(Object key) {
        Long currentValue = get(key);

        try {
            deleteValue.setLong(1, ((UUID) key).getMostSignificantBits());
            deleteValue.setLong(2, ((UUID) key).getLeastSignificantBits());
            deleteValue.execute();

            conn.commit();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return currentValue;
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        try {
            ResultSet results = count.executeQuery();
            results.next();
            int count = results.getInt(1);
            results.close();
            return count;
        } catch (SQLException e) {
            toRuntimeException(e);
        }
        return -1;
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<Long> values() {
        List<Long> values = new ArrayList<Long>();

        ResultSet results;
        try {
            results = listValues.executeQuery();
            for (; results.next();) {
                values.add(results.getLong(1));
            }
            results.close();
        } catch (SQLException e) {
            toRuntimeException(e);
        }

        return values;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (conn != null && conn.isClosed() == false) {
            conn.commit();
            conn.close();

            deleteLockFiles();
        }
    }

    /**
     * This is a workaround for derby not removing lock files at runtime.
     * 
     * deletes the lock files.
     */
    private void deleteLockFiles() {
        File lockFile = new File(dbFolder, "db.lck");
        if (lockFile.exists()) {
            lockFile.delete();
        }

        lockFile = new File(dbFolder, "dbex.lck");
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

}
