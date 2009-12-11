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
package org.dwfa.maven.sctid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Derby DB containing the Uuid-SctId mappings for an id mapping file.
 *
 * @author Ean Dungey
 */
public class UuidSctidMapDb {
    /** Class logger */
    private Logger logger = Logger.getLogger(UuidSctidMapDb.class.getName());
    /** DB connection */
    private Connection conn;
    /** Prepared Statement to check if key is in DB. */
    private PreparedStatement containsUuid;
    /** Prepared Statement to get a value from the DB. */
    private PreparedStatement getValue;
    /** Prepared Statement to remove a value from the DB. */
    private PreparedStatement deleteValue;
    /** Prepared Statement to list all the keys in the DB. */
    private PreparedStatement getUuidListForSctid;
    /** Prepared Statement to get the DB size. */
    private PreparedStatement count;
    /** Prepared Statement to insert a new row in the DB. */
    private PreparedStatement insertIdMapRow;
    /** Prepared Statement to insert a new TYPE row the DB. */
    private PreparedStatement insertTypeRow;
    /** Prepared Statement to insert a new NAMESPACE row in the DB. */
    private PreparedStatement insertNamespaceRow;
    /** Prepared Statement to count the number of TYPEs in the DB. */
    private PreparedStatement countTypeRows;
    /** Prepared Statement to count the number of NAMESPACEs in the DB. */
    private PreparedStatement countNamespaceRows;
    /** Prepared Statement to get a TYPE row. */
    private PreparedStatement getTypeRow;
    /** Prepared Statement to get a NAMESPACE row. */
    private PreparedStatement getNamespaceRow;
    /** Prepared Statement to get the current max sequence number of a NAMESPACE and TYPE. */
    private PreparedStatement getMaxSctIdForNamespaceAndType;
    /** Validate UUIDs when adding to the DB */
    private boolean validate = false;
    /** DB directory used to remove lock files. */
    private File databaseDirectory;

    /**
     * Creates or Opens a Derby DB.
     *
     * If the DB files exits it is simply opened other wise a new database is
     * created.
     *
     * @param dbFolder File
     * @throws IOException error opening or creating the DB.
     * @throws ClassNotFoundException
     * @throws SQLException creating the DB
     */
    public UuidSctidMapDb(File databaseDirectory) throws IOException, SQLException, ClassNotFoundException {
        if (databaseDirectory.exists() && databaseDirectory.canRead()) {
            openExistingDb(databaseDirectory);
        } else {
            createDb(databaseDirectory);
        }
    }

    /**
     * Creates or Opens a Derby DB.
     *
     * If the DB files exits it is simply opened other wise a new database is
     * created.
     *
     * If <code>validate</code> is true the uniqueness of the UUIDs is checked
     * before adding to the DB. If there are duplicate UUIDs a error is logged
     * and the UUID is not added to the DB.
     *
     * Performance is degraded if <code>validate</code> is true but all the map
     * files will be validated.
     *
     * @param databaseFile File
     * @param fixedMapDirectory File
     * @param idMapDirectory File
     * @param validate If true check the uniqueness of the UUIDs.
     * @throws IOException error opening or creating the DB.
     * @throws SQLException creating the DB
     * @throws ClassNotFoundException
     */
    public UuidSctidMapDb(File databaseFile, File fixedMapDirectory, File idMapDirectory, boolean validate, boolean appendToDb)
            throws IOException, SQLException, ClassNotFoundException {
        this.validate = validate;
        if (databaseFile.exists() && databaseFile.canRead()) {
            logger.warning("Opening exiting DB, ignoring the Map files.");
            openExistingDb(databaseFile);
            if (appendToDb) {
                updateDbFromMapDirectories(fixedMapDirectory, idMapDirectory);
            }
        } else {
            createDb(databaseFile, fixedMapDirectory, idMapDirectory);
        }
    }
    /**
     * Open the exiting DB.
     *
     * Setup the PreparedStatement of the DB.
     *
     * @param databaseDirectory File
     * @throws IOException error opening the DB.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void openExistingDb(File databaseDirectory) throws IOException, SQLException, ClassNotFoundException {
        logger.info("Opening existing DB " + databaseDirectory.getName());
        this.databaseDirectory = new File(databaseDirectory.getCanonicalFile().toString());
        connectToDataBase(databaseDirectory, false);
        createStatements();
        updateTypeTable();
        updateNamespaceTable();
        conn.commit();
    }

    /**
     * Create a new DB and load with the contents of the UUIDSctIdMapFile
     *
     * @param dbFolder File
     * @param fixedMapFile File
     * @throws IOException reading the map file or creating the DB.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void createDb(File databaseFile) throws IOException, SQLException, ClassNotFoundException {
        logger.info("Creating new DB in " + databaseFile.getName());

        File dbErrLog = new File(databaseFile.getParentFile(), "derbyErr.log");
        dbErrLog.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(dbErrLog);
        fw.append("Created by DerbyBackedUuidSctidMap.\n");
        fw.close();

        connectToDataBase(databaseFile, false);
        createTables();
        createStatements();
        createConstraints();
        updateTypeTable();
        updateNamespaceTable();
        createIndexes();

        logger.info("Created new DB " + databaseFile.getName());
    }

    /**
     * Creates a new database using the files in the fixed map directory and read write map directory.
     *
     * @param databaseFile File
     * @param fixedMapDirectory File
     * @param idMapDirectory File
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void createDb(File databaseFile, File fixedMapDirectory, File idMapDirectory) throws IOException, SQLException, ClassNotFoundException {
        connectToDataBase(databaseFile, false);
        createTables();
        createStatements();
        createConstraints();
        updateTypeTable();
        updateNamespaceTable();
        conn.commit();

        updateDbFromMapDirectories(fixedMapDirectory, idMapDirectory);

        createIndexes();
    }

    /**
     * Updates DB from the if map directories.
     *
     * @param fixedMapDirectory File
     * @param idMapDirectory File can be null if no generated ids to load.
     * @throws SQLException
     * @throws IOException
     */
    private void updateDbFromMapDirectories(File fixedMapDirectory, File idMapDirectory) throws SQLException,
            IOException {
        for (File fixedMapFile : fixedMapDirectory.listFiles()) {
            if (fixedMapFile.isFile()) {
                updateDbFromFixedSctMapFile(fixedMapFile);
            }
        }

        if (idMapDirectory != null) {
            for (File idMapFile : idMapDirectory.listFiles()) {
                if (idMapFile.isFile()) {
                    updateDbFromSctMapFile(idMapFile);
                }
            }
        }
    }

    /**
     * Creates a new DB from a UUID sctId map file.
     *
     * @param mapFile mapping flat file.
     * @throws SQLException creating the DB
     * @throws IOException reading the map file.
     */
    private void updateDbFromFixedSctMapFile(File mapFile) throws SQLException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        int insertCount = 0;

        try {
            String uuidLineStr;
            String sctIdLineStr;
            while ((uuidLineStr = br.readLine()) != null) {
                sctIdLineStr = br.readLine();
                Long sctId = Long.parseLong(sctIdLineStr);

                for (String uuidStr : uuidLineStr.split("\t")) {
                    UUID uuid = UUID.fromString(uuidStr);
                    if (!validate || validateFileUuid(uuid, sctId, mapFile)) {
                        addUUIDSctIdEntry(uuid, sctId, false);
                        insertCount++;
                        commitBatch(insertCount);
                    }
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * Creates a new DB from a UUID sctId map file.
     *
     * @param mapFile mapping flat file.
     * @throws SQLException creating the DB
     * @throws IOException reading the map file.
     */
    private void updateDbFromSctMapFile(File mapFile) throws SQLException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        int insertCount = 0;

        try {

            String uuidLineStr;
            String sctIdLineStr;
            while ((uuidLineStr = br.readLine()) != null) {
                sctIdLineStr = br.readLine();
                String[] parts = sctIdLineStr.split("\t");
                Long sctId = Long.parseLong(parts[0]);

                for (String uuidStr : uuidLineStr.split("\t")) {
                    UUID uuid = UUID.fromString(uuidStr);
                    if (!validate || validateFileUuid(uuid, sctId, mapFile)) {
                        addUUIDSctIdEntry(uuid, sctId, false);
                        insertCount++;
                        commitBatch(insertCount);
                    }
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * Commit on the 10 000th insert.
     *
     * @param insertCount int
     * @throws SQLException if commit fails.
     */
    private void commitBatch(int insertCount) throws SQLException {
        if (insertCount % 10000 == 0) {
            conn.commit();
            logger.info("Created " + insertCount + " rows");
        }
    }

    /**
     * Validate the UUID is not currently in the DB
     *
     * @param uuid UUID to check
     * @param sctId Long sctId mapped in the files.
     * @param mapFile file that contains the UUID, for reporting purposes
     * @return false is UUID exists in the DB
     * @throws SQLException
     */
    private boolean validateFileUuid(UUID uuid, Long sctId, File mapFile) throws SQLException {
        Long sctIdCurrent = getSctId(uuid);

        if (sctIdCurrent != null && !sctIdCurrent.equals(sctId)) {
            logger.severe("Duplicate UUID: " + uuid + " in file " + mapFile.getAbsoluteFile()
                + uuid + " mapped to " + sctIdCurrent + " not " + sctId);
        }

        return sctIdCurrent == null;
    }

    /**
     * Get the TYPE from the sctid string
     *
     * Type for our purposes is the second last number ie 0=concept 1=description etc.
     *
     * @param sctId String
     * @return TYPE
     * @throws SQLException
     * @throws NoSuchElementException
     */
    private int getSctIdType(String sctId) throws NoSuchElementException, SQLException {
        int type = Integer.parseInt(sctId.substring(sctId.length() - 2, sctId.length() - 1));

        return getTypeId(TYPE.fromString("" + type));
    }

    /**
     * Gets the primary key for a TYPE.
     *
     * @param type TYPE
     * @return int primary key
     * @throws SQLException cannot find the TYPE in the DB
     */
    private int getTypeId(TYPE type) throws SQLException{
        int typeId;
        ResultSet results = null;
        try {
            getTypeRow.setString(1, type.toString());
            results = getTypeRow.executeQuery();
            results.next();
            typeId = results.getInt(1);
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return typeId;
    }

    /**
     * Gets the namespace key from the sctid.
     *
     * @param sctId String SctId
     * @return int primary key
     * @throws NoSuchElementException cannot find the NAMESPACE
     * @throws SQLException
     */
    private int getSctIdNamespace(String sctId) throws NoSuchElementException, SQLException{
        int namespace = 0;

        if (!sctId.substring(sctId.length() - 3, sctId.length() - 2).equals(NAMESPACE.SNOMED_META_DATA.getDigits())) {
            namespace = Integer.parseInt(sctId.substring(sctId.length() - 10, sctId.length() - 2));
        }

        return getNamespaceId(NAMESPACE.fromString("" + namespace));
    }

    /**
     * Gets the primary key for a NAMESPACE.
     *
     * @param type NAMESPACE
     * @return int primary key
     * @throws SQLException cannot find the NAMESPACE in the DB
     */
    private int getNamespaceId(NAMESPACE namespace) throws SQLException {
        int namespaceId;
        ResultSet results = null;

        try {
            getNamespaceRow.setString(1, namespace.toString());
            results = getNamespaceRow.executeQuery();
            results.next();
            namespaceId = results.getInt(1);
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return namespaceId;
    }

    /**
     * Adds SCTID types to the type table
     *
     * @throws SQLException running SQL statement
     */
    private void updateTypeTable() throws SQLException {
        ResultSet results = countTypeRows.executeQuery();
        try {
            results.next();
            int currentCount = results.getInt(1);
            for (TYPE type : TYPE.values()) {
                if (getType(type.toString()) == null) {
                    insertTypeRow.setInt(1, ++currentCount);
                    insertTypeRow.setString(2, type.getDigits());
                    insertTypeRow.setString(3, type.toString());

                    insertTypeRow.executeUpdate();
                }
            }
        } finally {
            results.close();
        }
    }

    /**
     * Gets the <code>TYPE</code> for the string
     *
     * @param typeStr String code eg 0, 1, 6.
     * @return TYPE
     * @throws SQLException running SQL statement
     */
    private TYPE getType(String typeStr) throws SQLException {
        TYPE type = null;
        ResultSet results = null;

        try {
            getTypeRow.setString(1, typeStr);
            results = getTypeRow.executeQuery();

            if (results.next()) {
                type = TYPE.valueOf(results.getString(3).trim());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return type;
    }

    /**
     * Adds SCTID namespaces to the namespace table
     *
     * @throws SQLException running SQL statement
     */
    private void updateNamespaceTable() throws SQLException {
        ResultSet results = countNamespaceRows.executeQuery();

        try {
            results.next();
            int currentCount = results.getInt(1);
            for (NAMESPACE namespace : NAMESPACE.values()) {
                if (getNamespace(namespace.toString()) == null) {
                    insertNamespaceRow.setInt(1, ++currentCount);
                    insertNamespaceRow.setInt(2, Integer.parseInt(namespace.getDigits()));
                    insertNamespaceRow.setString(3, namespace.toString());

                    insertNamespaceRow.executeUpdate();
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    /**
     * Gets the <code>NAMESPACE</code> for the string
     *
     * @param namespaceStr String code eg 19999991.
     * @return NAMESPACE
     * @throws SQLException running SQL statement
     */
    private NAMESPACE getNamespace(String namespaceStr) throws SQLException {
        NAMESPACE namespace = null;
        ResultSet results = null;

        try {
            getNamespaceRow.setString(1, namespaceStr);
            results = getNamespaceRow.executeQuery();
            if (results.next()) {
                namespace = NAMESPACE.valueOf(results.getString(3).trim());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return namespace;
    }

    /**
     * Create the DB tables and indexes.
     *
     * @throws SQLException creating the tables/indexes.
     */
    private void createTables() throws SQLException {
        runSql("CREATE TABLE SCT_TYPE (SCT_TYPE_ID INTEGER PRIMARY KEY, SCT_TYPE_CODE CHAR(2) NOT NULL, SCT_TYPE_NAME CHAR(128) NOT NULL)");
        runSql("CREATE TABLE SCT_NAMESPACE (SCT_NAMESPACE_ID INTEGER PRIMARY KEY, SCT_NAMESPACE INTEGER NOT NULL, SCT_NAMESPACE_NAME CHAR(128) NOT NULL)");
        runSql("CREATE TABLE UUID_SCT_MAP (MSB BIGINT NOT NULL, LSB BIGINT NOT NULL, SCTID BIGINT NOT NULL, TYPE_ID INTEGER NOT NULL, NAMESPACE_ID INTEGER NOT NULL)");
    }

    /**
     * Add referential integrity and unique constraints.
     *
     * @throws SQLException create constraints.
     */
    private void createConstraints() throws SQLException {
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT SCT_TYPE_FK FOREIGN KEY (\"TYPE_ID\") REFERENCES SCT_TYPE (\"SCT_TYPE_ID\")");
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT SCT_NAMESPACE_FK FOREIGN KEY (\"NAMESPACE_ID\") REFERENCES SCT_NAMESPACE (\"SCT_NAMESPACE_ID\")");
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT UNIQUE_UUID UNIQUE (MSB, LSB)");
        runSql("CREATE INDEX UUID_IDX ON UUID_SCT_MAP (MSB, LSB)");
        runSql("CREATE INDEX SCTID_IDX ON UUID_SCT_MAP (SCTID)");
        conn.commit();
    }

    private void runSql(String sql) throws SQLException {
        PreparedStatement prepareStatement = null;

        try {
            prepareStatement = conn.prepareStatement(sql);
            prepareStatement.execute();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
    }

    /**
     * Creates the prepare statements for this DB.
     *
     * @throws SQLException creating the prepare statements
     */
    private void createStatements() throws SQLException {

        count = conn.prepareStatement("SELECT COUNT(*) FROM UUID_SCT_MAP");
        countTypeRows = conn.prepareStatement("SELECT COUNT(*) FROM SCT_TYPE");
        countNamespaceRows = conn.prepareStatement("SELECT COUNT(*) FROM SCT_NAMESPACE");

        insertIdMapRow = conn.prepareStatement("INSERT INTO UUID_SCT_MAP(MSB, LSB, SCTID, TYPE_ID, NAMESPACE_ID) VALUES(?, ?, ?, ?, ?)");
        insertTypeRow = conn.prepareStatement("INSERT INTO SCT_TYPE(SCT_TYPE_ID, SCT_TYPE_CODE, SCT_TYPE_NAME) VALUES(?, ?, ?)");
        insertNamespaceRow = conn.prepareStatement("INSERT INTO SCT_NAMESPACE(SCT_NAMESPACE_ID, SCT_NAMESPACE, SCT_NAMESPACE_NAME) VALUES(?, ?, ?)");

        getTypeRow = conn.prepareStatement("SELECT SCT_TYPE_ID, SCT_TYPE_CODE, SCT_TYPE_NAME FROM SCT_TYPE WHERE SCT_TYPE_NAME = ?");
        getNamespaceRow = conn.prepareStatement("SELECT SCT_NAMESPACE_ID, SCT_NAMESPACE, SCT_NAMESPACE_NAME FROM SCT_NAMESPACE WHERE SCT_NAMESPACE_NAME = ?");
        getValue = conn.prepareStatement("SELECT SCTID FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
        deleteValue = conn.prepareStatement("DELETE FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
        getUuidListForSctid = conn.prepareStatement("SELECT MSB, LSB FROM UUID_SCT_MAP WHERE SCTID = ?");
        getMaxSctIdForNamespaceAndType = conn.prepareStatement("SELECT MAX(SCTID) FROM UUID_SCT_MAP WHERE TYPE_ID = ? AND NAMESPACE_ID = ?");
        containsUuid = conn.prepareStatement("SELECT COUNT(SCTID) FROM UUID_SCT_MAP WHERE MSB = ? and LSB = ?");
    }

    /**
     * Creates the DB indexes.
     *
     * @throws SQLException if index cannot be created
     */
    private void createIndexes() throws SQLException {
        runSql("CREATE INDEX UUID_IDX ON UUID_SCT_MAP (MSB, LSB)");
        runSql("CREATE INDEX SCTID_IDX ON UUID_SCT_MAP (SCTID)");
        conn.commit();
    }

    /**
     * Connects to the database, if no database exists then a new one is
     * created.
     *
     * @param databaseDirectory File
     * @param autoCommit boolean
     * @throws SQLException connecting to the DB.
     * @throws IOException reading the DB.
     * @throws ClassNotFoundException
     */
    private void connectToDataBase(File databaseDirectory, boolean autoCommit) throws SQLException, IOException, ClassNotFoundException {
        this.databaseDirectory = new File(databaseDirectory.getCanonicalFile().toString());

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        conn = DriverManager.getConnection("jdbc:derby:directory:" + databaseDirectory.getCanonicalPath() + ";create=true;");

        conn.setAutoCommit(autoCommit);
    }

    /**
     * Is the UUID in the DB.
     *
     * @param uuid UUID
     * @return boolean true if UUID in DB else false
     * @throws SQLException finding uuid
     */
    public boolean containsUuid(UUID uuid) throws SQLException {
        boolean hasUuid = false;

        containsUuid.setLong(1, uuid.getMostSignificantBits());
        containsUuid.setLong(2, uuid.getLeastSignificantBits());
        ResultSet results = containsUuid.executeQuery();

        try {
            if (results.next()) {
                hasUuid = results.getInt(1) > 0;
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return hasUuid;
    }

    /**
     * Gets the SCTID for the UUID
     *
     * @param uuid UUID
     * @return SCTID as a Long
     * @throws SQLException finding the SCTID
     */
    public Long getSctId(UUID uuid) throws SQLException {
        Long value = null;
        ResultSet results = null;

        try {
            if (containsUuid(uuid)) {
                getValue.setLong(1, uuid.getMostSignificantBits());
                getValue.setLong(2, uuid.getLeastSignificantBits());
                results = getValue.executeQuery();
                if (results.next()) {
                    value = results.getLong(1);
                }
                results.close();
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return value;
    }

    /**
     * Gets the list of SCTID for a UUID
     *
     * @param sctId Long
     * @return List of UUIDs
     * @throws SQLException finding UUIDs
     */
    public List<UUID> getUuidList(Long sctId) throws SQLException {
        List<UUID> uuidList = new ArrayList<UUID>();
        ResultSet results = null;

        try {
            getUuidListForSctid.setLong(1, sctId);
            results = getUuidListForSctid.executeQuery();
            for (; results.next();) {
                uuidList.add(new UUID(results.getLong(1), results.getLong(2)));
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return uuidList;
    }

    /**
     * Removes a UUID from the DB
     * @param uuid UUID
     * @throws SQLException deleting the UUID
     */
    public void removeUuid(UUID uuid) throws SQLException {
        deleteValue.setLong(1, uuid.getMostSignificantBits());
        deleteValue.setLong(2, uuid.getLeastSignificantBits());
        deleteValue.execute();

        conn.commit();
    }

    /**
     * Adds an entry to the UUID SctId map table.
     *
     * This will check the the UUID is unique
     * The SCTID is valid (TYPE and NAMESPACE exist)
     *
     * @param uuid UUID
     * @param sctId LONG
     * @param commit boolean true to changes.
     * @throws SQLException adding new mapping row.
     */
    private void addUUIDSctIdEntry(UUID uuid, Long sctId, boolean commit) throws SQLException {
        insertIdMapRow.setLong(1, uuid.getMostSignificantBits());
        insertIdMapRow.setLong(2, uuid.getLeastSignificantBits());
        insertIdMapRow.setLong(3, sctId);
        insertIdMapRow.setInt(4, getSctIdType(sctId.toString()));
        insertIdMapRow.setInt(5, getSctIdNamespace(sctId.toString()));
        insertIdMapRow.execute();

        if (commit) {
            conn.commit();
        }
    }

    /**
     * Adds an entry to the UUID SctId map table.
     *
     * This will check the the UUID is unique
     * The SCTID is valid (TYPE and NAMESPACE exist)
     *
     * @param uuid UUID
     * @param sctId LONG
     * @throws SQLException adding new mapping row.
     */
    public void addUUIDSctIdEntry(UUID uuid, Long sctId) throws SQLException {
        addUUIDSctIdEntry(uuid, sctId, true);
    }

    /**
     * Adds all the elements in the Map to the DB.
     *
     * @param entryList Map of UUID,Long
     * @throws SQLException adding new mapping row.
     */
    public void addUUIDSctIdEntryList(Map<UUID,Long> entryList) throws SQLException {
        for (UUID uuid : entryList.keySet()) {
            addUUIDSctIdEntry(uuid, entryList.get(uuid), false);
        }
        conn.commit();
    }

    /**
     * Size of the DB
     *
     * @return int
     * @throws SQLException
     */
    public int size() throws SQLException {
        int size;
        ResultSet results = null;

        try {
            results = count.executeQuery();
            results.next();
            size = results.getInt(1);
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return size;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    /**
     * Commits and closes the connection.
     *
     * Closes all PreparedStatement objects
     *
     * @throws SQLException error closing connection
     */
    public void close() throws SQLException {
        logger.info("Closing DB");
        if (conn != null && !conn.isClosed()) {
            containsUuid.close();
            getValue.close();
            deleteValue.close();
            getUuidListForSctid.close();
            count.close();
            insertIdMapRow.close();
            insertTypeRow.close();
            insertNamespaceRow.close();
            countTypeRows.close();
            countNamespaceRows.close();
            getTypeRow.close();
            getNamespaceRow.close();
            getMaxSctIdForNamespaceAndType.close();

            conn.commit();
            conn.close();

            deleteLockFiles();
        }
        logger.info("Closed DB");
    }

    /**
     * This is a workaround for derby not removing lock files at runtime.
     *
     * deletes the lock files.
     */
    private void deleteLockFiles() {
        File lockFile = new File(databaseDirectory, "db.lck");
        if(lockFile.exists()){
            lockFile.delete();
        }

        lockFile = new File(databaseDirectory, "dbex.lck");
        if(lockFile.exists()){
            lockFile.delete();
        }
    }

    /**
     * Gets the sequence number from the DB based on the TYPE and NAMESPACE.
     *
     * @param namespace NAMESPACE
     * @param type TYPE
     * @return Long 1 is returned for the first sequence number.
     * @throws NoSuchElementException no data found.
     * @throws SQLException SQL error
     */
    public Long getSctSequenceId(NAMESPACE namespace, TYPE type) throws NoSuchElementException, SQLException {
        Long sequenceId = 0l;
        ResultSet results = null;

        try {
            getMaxSctIdForNamespaceAndType.setInt(1, getTypeId(type));
            getMaxSctIdForNamespaceAndType.setInt(2, getNamespaceId(namespace));
            results = getMaxSctIdForNamespaceAndType.executeQuery();

            if (results.next()) {
                String maxSctId = results.getLong(1) + "";
                if (maxSctId.length() > (type.getDigits().length() + namespace.getDigits().length())) {
                    sequenceId =
                            Long.valueOf(maxSctId.substring(0, maxSctId.length()
                                - (type.getDigits().length() + namespace.getDigits().length() + 1)));
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return sequenceId;
    }
}
