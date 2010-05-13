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
import java.io.BufferedWriter;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Database containing the Uuid-SctId mappings for an id mapping file.
 * <p>
 * Currently the class supports an arbitrary ANSI SQL relational database, and has some special handling for
 * Derby Embedded databases. It has also been tested with MySQL.
 * <p>
 * In order to use this class you must first ensure that the 4 system properties listed below are set appropriately
 * to enable the class to connect to a database.
 * <ul>
 *  <li>SCT_ID_MAP_DATABASE_CONNECTION_URL - the JDBC connection URL for the database</li>
 *  <li>SCT_ID_MAP_DRIVER - JDBC driver fully qualified class name</li>
 *  <li>SCT_ID_MAP_USER - optionally a username to authenticate to the database with</li>
 *  <li>SCT_ID_MAP_PASSWORD - optionally a password used to authenticate with the given username</li>
 * </ul>
 * These properties are passed via system properties to allow these options to be set at a high level and not
 * passed throughout the entire call stack.
 * <p>
 * The example code below shows these properties being set for a MySQL database
 * <p><pre>
 *      System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, "com.mysql.jdbc.Driver");
 *      System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, "jdbc:mysql://localhost:3306/mysql");
 *      System.setProperty(UuidSctidMapDb.SCT_ID_MAP_USER, "username");
 *      System.setProperty(UuidSctidMapDb.SCT_ID_MAP_PASSWORD, "password");
 * </pre>
 * Alternatively there is a method provided to set these properties
 * <code>setDatabaseProperties(String dbDriver, String dbConnectionUrl, String dbUsername, String dbPassword)</code>
 * OR <code>setDatabaseProperties(String dbDriver, String dbConnectionUrl)</code> which does not supply username and
 * password.
 * <p>
 * Then <code>UuidSctidMapDb.getInstance()</code> can be used to aquire an instance of this class, and the open and
 * create methods can be used to open and initialise the database for use.
 *
 * @author Ean Dungey
 */
public class UuidSctidMapDb {
    /** System property key for the database connection URL property */
    public static final String SCT_ID_MAP_DATABASE_CONNECTION_URL = "SctIdMap.databaseConnectionUrl";
    /** System property key for the database driver property */
    public static final String SCT_ID_MAP_DRIVER = "SctIdMap.driver";
    /** System property key for the database user name */
    public static final String SCT_ID_MAP_USER = "SctIdMap.user";
    /** System property key for the database password */
    public static final String SCT_ID_MAP_PASSWORD = "SctIdMap.password";
    /** Driver name of the Derby Embedded driver */
    public static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    /** Class logger */
    private static Logger logger = Logger.getLogger(UuidSctidMapDb.class.getName());
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
    /**
     * Prepared Statement to get the current max sequence number of a NAMESPACE
     * and TYPE.
     */
    private PreparedStatement getMaxSctIdForNamespaceAndType;
    /** Validate UUIDs when adding to the DB */
    private boolean validate = false;
    /** DB directory used to remove lock files. */
    private File derbyDatabaseDirectory;
    /** DB connection driver */
    private String databaseDriver;
    /** DB connection url */
    private String databaseConnectionUrl;
    /** DB user */
    private String databaseUser;
    /** DB password */
    private String databasePassword;
    private Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> namespaceMap = new HashMap<Integer, Integer>();
    /** Instance of the DB. */
    private static UuidSctidMapDb instance;

    private UuidSctidMapDb() {
        this.databaseDriver = System.getProperty(SCT_ID_MAP_DRIVER);
        this.databaseConnectionUrl = System.getProperty(SCT_ID_MAP_DATABASE_CONNECTION_URL);

        this.databaseUser = System.getProperty(SCT_ID_MAP_USER);
        this.databasePassword = System.getProperty(SCT_ID_MAP_PASSWORD);

        if (this.databaseDriver == null) {
            throw new IllegalArgumentException("Database driver was not specified as required by the system property \"SctIdMap.driver\"");
        }

        if (this.databaseConnectionUrl == null) {
            throw new IllegalArgumentException("Database connection url was not specified as required by the system property \"SctIdMap.databaseConnectionUrl\"");
        }

        //this section is purely for Derby Embedded support
        if (isDerbyEmbeddedDatabase()) {
            Pattern pattern = Pattern.compile("jdbc:derby:directory:([^;]*).*");
            Matcher matcher = pattern.matcher(this.databaseConnectionUrl);
            if (!matcher.find() || matcher.groupCount() != 1) {
                throw new IllegalArgumentException(
                    "Derby Embedded driver specified however the directory cannot be parsed from the connection URL. Driver was '"
                    + instance.databaseDriver + "' URL was '" + instance.databaseConnectionUrl + "'");
            }
            this.derbyDatabaseDirectory = new File(matcher.group(1));
        }
    }

    /**
     * Returns the singleton instance and initialises one if one does not exist. If one does exist and reinitialise is
     * true, then the singleton instance will be reconstructed.
     *
     * @param reinitialise if true the method will recreate a new UuidSctidMapDb instance regardless of whether one already exists
     * @return UuidSctidMapDb
     * @throws IllegalArgumentException
     */
    public static synchronized UuidSctidMapDb getInstance(boolean reinitialise) {
        if (instance == null || reinitialise) {
            if (instance != null) {
                try {
                    instance.close();
                } catch (SQLException e) {
                    logger.severe("failed to close UuidSctidMapDb when requested to reinitialise - continuing anyway");
                }
            }
            instance = new UuidSctidMapDb();
        }
        return instance;
    }

    /**
     * Returns the singleton instance and initialises one if one does not exist.
     *
     * @return UuidSctidMapDb
     */
    public static UuidSctidMapDb getInstance() {
        return getInstance(false);
    }

    /**
     * Convenience method to set database properties used to connect to the underlying database
     *
     * @param dbDriver
     * @param dbConnectionUrl
     * @param dbUsername
     * @param dbPassword
     */
    public static synchronized void setDatabaseProperties(String dbDriver, String dbConnectionUrl, String dbUsername, String dbPassword) {
        System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, dbDriver);
        System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL,
             dbConnectionUrl);

        if (dbUsername != null) {
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_USER, dbUsername);
        }
        if (dbPassword != null) {
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_PASSWORD, dbPassword);
        }
    }

    /**
     * Convenience method to set database properties used to connect to the underlying database
     *
     * @param dbDriver
     * @param dbConnectionUrl
     * @param dbUsername
     * @param dbPassword
     */
    public static synchronized void setDatabaseProperties(String dbDriver, String dbConnectionUrl) {
        setDatabaseProperties(dbDriver, dbConnectionUrl, null, null);
    }

    /**
     * Opens the map database and validates its presence. If the schema does not exist an IOException will be thrown.
     *
     * @param dbFolder File
     * @throws IOException error opening or creating the DB.
     * @throws ClassNotFoundException
     * @throws SQLException creating the DB
     */
    public void openDb() throws IOException, SQLException, ClassNotFoundException {
        openExistingDb();
    }


    /**
     * Opens the map database, validates its presence and appends the content of the supplied map files to the existing
     * map data.
     *
     * If <code>validate</code> is true the uniqueness of the UUIDs is checked
     * before adding to the DB. If there are duplicate UUIDs a error is logged
     * and the UUID is not added to the DB.
     *
     * Performance is degraded if <code>validate</code> is true but all the map
     * files will be validated.
     *
     * @param fixedMapDirectory File
     * @param idMapDirectory File
     * @param validate If true check the uniqueness of the UUIDs.
     * @throws IOException error opening or creating the DB.
     * @throws SQLException creating the DB
     * @throws ClassNotFoundException
     */
    public void openDb(File fixedMapDirectory, File idMapDirectory, boolean validate) throws IOException, SQLException, ClassNotFoundException {
        this.validate = validate;
        openDb();
        updateDbFromMapDirectories(fixedMapDirectory, idMapDirectory);
    }

    /**
     * Checks to see if the database specified by databaseDriver and databaseConnectionUrl exist.
     * <p>
     * This means
     * <ol>
     *  <li>the connection is established</li>
     *  <li>the schema exists</li>
     * </ol>
     *
     * @return boolean true if the database schema exists
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws SQLException
     */
    public boolean isDatabaseInitialised() throws IOException, ClassNotFoundException, SQLException {
        boolean disconnectAfterExecution = false;
        boolean tablesExist = false;

        if (!isDbConnectted()) {
            connectToDataBase(false);
            disconnectAfterExecution = true;
        }

        try {
            if (tableExists("SCT_TYPE") && tableExists("SCT_NAMESPACE") && tableExists("UUID_SCT_MAP")) {
                conn.rollback();
                tablesExist = true;
            }
            conn.rollback();
        } catch (SQLException e) {
            if (e.getMessage().contains("does not exist") || e.getMessage().contains("doesn't exist")) {
                tablesExist = false;
            } else {
                throw new SQLException("Unknown SQLException determining if the schema exists");
            }
        } finally {
            if (disconnectAfterExecution) {
                conn.close();
                conn = null;
            }
        }
        return tablesExist;
    }

    /**
     * Checks to see if the specified table name exists. Because the database implementation is not known a simple
     * test is done that runs a query that will fail if the table does not exists - not pretty but the best universal
     * option.
     *
     * @param table
     * @return true if the table exists false otherwise
     * @throws SQLException
     */
    private boolean tableExists(String table) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
        if (!rs.next()) {
            return false;
        }
        rs.close();

        return true;
    }


    /**
     * Open the exiting DB.
     *
     * Setup the PreparedStatement of the DB.
     *
     * @throws IOException error opening the DB.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void openExistingDb() throws IOException, SQLException, ClassNotFoundException {
        logger.info("Opening existing DB " + databaseConnectionUrl);

        if (!isDatabaseInitialised()) {
            throw new SQLException("Expecting to open an existing database but none is present");
        }

        if (conn == null) {
            connectToDataBase(false);

            prepareStatements();
            updateTypeTable();
            updateNamespaceTable();
            conn.commit();
        }
    }

    /**
     * Create a new blank database
     *
     * @param fixedMapFile File
     * @throws IOException reading the map file or creating the DB.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void createDb() throws IOException, SQLException, ClassNotFoundException {
        this.createDb(null, null, false);
    }

    /**
     * Creates a new database using the files in the fixed map directory and
     * read write map directory - this method loads the data before indexes are created
     *
     * @param fixedMapDirectory File
     * @param idMapDirectory File
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void createDb(File fixedMapDirectory, File idMapDirectory, boolean validate) throws IOException,
            SQLException, ClassNotFoundException {
        logger.info("Creating new DB in " + databaseConnectionUrl);

        if (isDatabaseInitialised()) {
            throw new IOException("Database is already initialised - cannot create a new database");
        }

        this.validate = validate;

        if (isDerbyEmbeddedDatabase()) {
            File dbErrLog = new File(this.derbyDatabaseDirectory.getParentFile(), "derbyErr.log");
            dbErrLog.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(dbErrLog);
            fw.append("Created by DerbyBackedUuidSctidMap.\n");
            fw.close();
        }

        connectToDataBase(false);
        createTables();
        prepareStatements();
        createConstraints();
        updateTypeTable();
        updateNamespaceTable();
        conn.commit();

        if (fixedMapDirectory != null) {
            updateDbFromMapDirectories(fixedMapDirectory, idMapDirectory);
            conn.commit();
        }

        createIndexes();

        logger.info("Created new DB " + databaseConnectionUrl);
    }

    /**
     * Clears out the map and reconstructs the namespace and type information.
     * @throws SQLException
     */
    public void clearDb() throws SQLException {
        logger.info("Truncating ID database schema");
        if (isDerbyEmbeddedDatabase()) {
            //Derby does not support truncate
            runSql("DELETE FROM UUID_SCT_MAP");
            runSql("DELETE FROM SCT_NAMESPACE");
            runSql("DELETE FROM SCT_TYPE");
        } else {
            runSql("TRUNCATE TABLE UUID_SCT_MAP");
            runSql("TRUNCATE TABLE SCT_NAMESPACE");
            runSql("TRUNCATE TABLE SCT_TYPE");
        }
        typeMap.clear();
        namespaceMap.clear();
        updateNamespaceTable();
        updateTypeTable();
    }

    /**
     * Drops the tables in the ID map schema
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void dropDb() throws SQLException, IOException, ClassNotFoundException {
        logger.info("Dropping ID database schema");
        runSql("DROP TABLE UUID_SCT_MAP");
        runSql("DROP TABLE SCT_NAMESPACE");
        runSql("DROP TABLE SCT_TYPE");
    }

    /**
     * Runs the sql against the DB.
     *
     * Commits the current connection.
     *
     * @param sql String SQL
     * @throws SQLException
     */
    public void runAndCommitSql(String sql) throws SQLException {
        runSql(sql);
        conn.commit();
    }

    private boolean isDerbyEmbeddedDatabase() {
        return this.databaseDriver.equals(DERBY_EMBEDDED_DRIVER);
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
     * Updated the DB using a rf2 id file - the file is expected to have a header row as per the RF2 spec.
     *
     * @param rf2IdFile File rf2 ids
     * @throws SQLException DB error
     * @throws IOException File error
     */
    public void updateDbFromRf2IdFile(File... rf2IdFiles) throws SQLException, IOException {
        logger.info("Importing RF2 data from file " + rf2IdFiles + " into UuidSctidMapDb");
        if (isDatabaseMySQL()) {
            for (File file : rf2IdFiles) {
                runSql("LOAD DATA LOCAL INFILE '" + file.getAbsolutePath() + "' INTO TABLE UUID_SCT_MAP"
                    + " FIELDS TERMINATED BY '\t' IGNORE 1 LINES"
                    + " (@identifierSchemeId, @uuid, @effectiveTime, @active, @moduleId, @sctId)"
                    + " SET MSB = (conv( substr( replace( @uuid, \"-\", \"\" ) ,1,16 ), 16, -10)),"
                    + " LSB = (conv(substr(replace(@uuid, \"-\", \"\"),17,32), 16, -10)),"
                    + " SCTID = @sctId,"
                    + " TYPE_ID = (SELECT SCT_TYPE_ID FROM SCT_TYPE WHERE SCT_TYPE_CODE = LEFT(RIGHT(@sctId, 2), 1)),"
                    + " NAMESPACE_ID = (IFNULL((SELECT SCT_NAMESPACE_ID FROM SCT_NAMESPACE WHERE SCT_NAMESPACE = LEFT( RIGHT(@sctId, 10), 8)), 0))");
            }
        } else if (isDerbyEmbeddedDatabase()) {
            File data = File.createTempFile("UuidSctid", "rf2");
            BufferedWriter writer = new BufferedWriter(new FileWriter(data, false));

            for (File file : rf2IdFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                //throw away header
                String line = reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split("\t");

                    UUID uuid = UUID.fromString(columns[1]);
                    String sctid = columns[5];

                    writer.append(uuid.getMostSignificantBits() + "");
                    writer.append("\t");
                    writer.append(uuid.getLeastSignificantBits() + "");
                    writer.append("\t");
                    writer.append(sctid);
                    writer.append("\t");
                    writer.append(getSctIdType(sctid) + "");
                    writer.append("\t");
                    writer.append(getSctIdNamespace(sctid) + "");
                    writer.append(System.getProperty("line.separator"));
                }
                reader.close();
            }

            writer.close();

            runSql("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (null,'UUID_SCT_MAP','" + data.getAbsolutePath() + "','\t','\"',null,0)");
            data.delete();
        } else {
            int insertCount = 0;
            BufferedReader br = null;
            try {
                String lineStr;
                for (File file : rf2IdFiles) {
                    br = new BufferedReader(new FileReader(file));
                    br.readLine();
                    while ((lineStr = br.readLine()) != null) {
                        String[] columns = lineStr.split("\t");
                        UUID uuid = UUID.fromString(columns[1]);
                        Long sctId = Long.parseLong(columns[5]);

                        if (!validate || validateFileUuid(uuid, sctId, file)) {
                            addUUIDSctIdEntry(uuid, sctId, false);
                            insertCount++;
                            commitBatch(insertCount);
                        }
                    }
                }
            } finally {
                if (br != null) {
                    br.close();
                }
                conn.commit();
            }
        }
    }

    private boolean isDatabaseMySQL() {
        return databaseDriver.equals("com.mysql.jdbc.Driver");
    }

    /**
     * Updated the DB using a rf2 id file
     *
     * @param rf2IdFile File rf2 ids
     * @throws SQLException DB error
     * @throws IOException File error
     */
    public void updateDbFromAceIdFile(File rf2IdFile) throws SQLException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(rf2IdFile));
        int insertCount = 0;

        try {
            br.readLine();
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                String[] columns = lineStr.split("\t");
                UUID uuid = UUID.fromString(columns[0]);

                Long sctId;
                try{
                    sctId = Long.parseLong(columns[2]);
                } catch (NumberFormatException nfe) {
                    continue;
                }

                if (!validate || validateFileUuid(uuid, sctId, rf2IdFile)) {
                    addUUIDSctIdEntry(uuid, sctId, false);
                    insertCount++;
                    commitBatch(insertCount);
                }
            }
        } finally {
            br.close();
            conn.commit();
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
            conn.commit();
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
            conn.commit();
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
            logger.severe("Duplicate UUID: " + uuid + " in file " + mapFile.getAbsoluteFile() + uuid + " mapped to "
                + sctIdCurrent + " not " + sctId);
        }

        return sctIdCurrent == null;
    }

    /**
     * Get the TYPE from the sctid string
     *
     * Type for our purposes is the second last number ie 0=concept
     * 1=description etc.
     *
     * @param sctId String
     * @return TYPE
     * @throws SQLException
     * @throws NoSuchElementException
     */
    private int getSctIdType(String sctId) throws NoSuchElementException, SQLException {
        int type = Integer.parseInt(sctId.substring(sctId.length() - 2, sctId.length() - 1));

        Integer result = typeMap .get(type);
        if (result == null) {
            result = getTypeId(TYPE.fromString("" + type));
        }
        typeMap.put(type, result);
        return result;
    }

    /**
     * Gets the primary key for a TYPE.
     *
     * @param type TYPE
     * @return int primary key
     * @throws SQLException cannot find the TYPE in the DB
     */
    private int getTypeId(TYPE type) throws SQLException {
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
    private int getSctIdNamespace(String sctId) throws NoSuchElementException, SQLException {
        int namespace = 0;

        if (!sctId.substring(sctId.length() - 3, sctId.length() - 2).equals(NAMESPACE.SNOMED_META_DATA.getDigits())) {
            namespace = Integer.parseInt(sctId.substring(sctId.length() - 10, sctId.length() - 2));
        }

        Integer result = namespaceMap.get(namespace);
        if (result == null) {
            result = getNamespaceId(NAMESPACE.fromString("" + namespace));
        }
        namespaceMap.put(namespace, result);
        return result;
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
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT SCT_TYPE_FK FOREIGN KEY (TYPE_ID) REFERENCES SCT_TYPE (SCT_TYPE_ID)");
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT SCT_NAMESPACE_FK FOREIGN KEY (NAMESPACE_ID) REFERENCES SCT_NAMESPACE (SCT_NAMESPACE_ID)");
        runSql("ALTER TABLE UUID_SCT_MAP ADD CONSTRAINT UNIQUE_UUID UNIQUE (MSB, LSB)");
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
    private void prepareStatements() throws SQLException {

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
     * Add referential integrity and unique constraints.
     *
     * @throws SQLException create constraints.
     * @return boolean false if the indexes did not exist, true if they existed and were dropped
     */
    private boolean dropIndexes() throws SQLException {
        try {
            if (isDerbyEmbeddedDatabase()) {
                runSql("DROP INDEX UUID_IDX");
                runSql("DROP INDEX SCTID_IDX");
            } else {
                runSql("DROP INDEX UUID_IDX ON UUID_SCT_MAP");
                runSql("DROP INDEX SCTID_IDX ON UUID_SCT_MAP");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("does not exist") || e.getMessage().contains("doesn't exist")) {
                return false;
            } else {
                throw new SQLException("Unknown SQLException determining if the schema exists");
            }
        } finally {
            conn.commit();
        }
        return true;
    }

    /**
     * Connects to the database, if no database exists then a new one is
     * created.
     * @param driver TODO
     * @param connectionUrl TODO
     * @param derbyDatabaseDirectory File
     * @param autoCommit boolean
     * @throws SQLException connecting to the DB.
     * @throws IOException reading the DB.
     * @throws ClassNotFoundException
     */
    private void connectToDataBase(boolean autoCommit) throws SQLException, IOException,
            ClassNotFoundException {

        Class.forName(databaseDriver);

        conn = DriverManager.getConnection(databaseConnectionUrl, databaseUser, databasePassword);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        conn.setAutoCommit(autoCommit);
        typeMap.clear();
        namespaceMap.clear();
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
     *
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
        if (!validate || validateFileUuid(uuid, sctId, derbyDatabaseDirectory)) {
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
    public void addUUIDSctIdEntryList(Map<UUID, Long> entryList) throws SQLException {
        logger.info("Adding batch of " + entryList.size());

        if ((isDatabaseMySQL() || isDerbyEmbeddedDatabase()) && entryList.size() > 100) {
            dataPumpBatch(entryList);
        } else {
            bulkInsertStatementBatch(entryList);
        }
        conn.commit();
        logger.info("Committed batch of " + entryList.size());
    }

    private void bulkInsertStatementBatch(Map<UUID, Long> entryList) throws SQLException {
        logger.info("initiating batch insert " + entryList.size());

        int count = 0;

        StringBuffer statement = new StringBuffer("INSERT INTO UUID_SCT_MAP VALUES ");

        Iterator<UUID> keySet = entryList.keySet().iterator();
        while (keySet.hasNext()) {
            UUID uuid = keySet.next();
            statement.append("(");
            statement.append(uuid.getMostSignificantBits());
            statement.append(",");
            statement.append(uuid.getLeastSignificantBits());
            statement.append(",");
            statement.append(entryList.get(uuid));
            statement.append(",");
            statement.append(getSctIdType(entryList.get(uuid).toString()));
            statement.append(",");
            statement.append(getSctIdNamespace(entryList.get(uuid).toString()));
            statement.append(")");
            if (count++ > 1000) {
                count = 0;
                conn.createStatement().executeUpdate(statement.toString());
                conn.commit();
                statement = new StringBuffer("INSERT INTO UUID_SCT_MAP VALUES ");
            } else if (keySet.hasNext()) {
                statement.append(",");
            }
        }

        if (!statement.toString().equals("INSERT INTO UUID_SCT_MAP VALUES ")) {
            conn.createStatement().executeUpdate(statement.toString());
        }
    }

    private void dataPumpBatch(Map<UUID, Long> entryList) throws SQLException {
        logger.info("initiating data pump " + entryList.size());

        File data;
        BufferedWriter writer;
        try {
            data = File.createTempFile("UuidSctid", "batch");

            writer = new BufferedWriter(new FileWriter(data, false));

            Iterator<UUID> keySet = entryList.keySet().iterator();
            while (keySet.hasNext()) {
                UUID uuid = keySet.next();
                writer.append(uuid.getMostSignificantBits() + "");
                writer.append("\t");
                writer.append(uuid.getLeastSignificantBits() + "");
                writer.append("\t");
                writer.append(entryList.get(uuid).toString());
                writer.append("\t");
                writer.append(getSctIdType(entryList.get(uuid).toString()) + "");
                writer.append("\t");
                writer.append(getSctIdNamespace(entryList.get(uuid).toString()) + "");
                if (keySet.hasNext()) {
                    writer.append(System.getProperty("line.separator"));
                }
            }

            if (isDatabaseMySQL()) {
                writer.close();
                runSql("LOAD DATA LOCAL INFILE '" + data.getAbsolutePath() + "' INTO TABLE UUID_SCT_MAP"
                    + " FIELDS TERMINATED BY '\t'"
                    + " (MSB, LSB, SCTID, TYPE_ID, NAMESPACE_ID)");
            } else if (isDerbyEmbeddedDatabase()){
                writer.append(System.getProperty("line.separator"));
                writer.close();
                runSql("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (null,'UUID_SCT_MAP','" + data.getAbsolutePath() + "','\t','\"',null,0)");
            } else {
                throw new SQLException("Database is of unknown type - pump not supported");
            }
            data.delete();

        } catch (IOException e) {
            throw new SQLException("Can't create local temporary file and write data into it for import!", e);
        }
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
    protected void finalize() throws Throwable {
        if (isDbConnectted()) {
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

            typeMap.clear();
            namespaceMap.clear();
            conn.commit();
            conn.close();
            conn = null;
        }

        if (isDerbyEmbeddedDatabase()) {
            deleteLockFiles();
        }
        logger.info("Closed DB");
    }

    /**
     * Commits and closes the connection.
     *
     * Closes all PreparedStatement objects
     *
     * @throws SQLException error closing connection
     */
    public void close() throws SQLException {
        if (isDbConnectted()) {
            conn.commit();
        }

        if (isDerbyEmbeddedDatabase()) {
            deleteLockFiles();
        }
        logger.info("Closed DB");
    }

    /**
     * Is the connected not null and connected.
     *
     * @return true if connection is open.
     */
    public boolean isDbConnectted() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * This is a workaround for derby not removing lock files at runtime.
     *
     * deletes the lock files.
     */
    private void deleteLockFiles() {
        File lockFile = new File(derbyDatabaseDirectory, "db.lck");
        if (lockFile.exists()) {
            lockFile.delete();
        }

        lockFile = new File(derbyDatabaseDirectory, "dbex.lck");
        if (lockFile.exists()) {
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
        Long sequenceId = 100l;
        ResultSet results = null;

        try {
            getMaxSctIdForNamespaceAndType.setInt(1, getTypeId(type));
            getMaxSctIdForNamespaceAndType.setInt(2, getNamespaceId(namespace));
            results = getMaxSctIdForNamespaceAndType.executeQuery();

            if (results.next()) {
                String maxSctId = results.getLong(1) + "";
                if (maxSctId.length() > (type.getDigits().length() + namespace.getDigits().length())) {
                    sequenceId = Long.valueOf(maxSctId.substring(0, maxSctId.length()
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

    /**
     * @param validate the validate to set
     */
    public final void setValidate(boolean validate) {
        this.validate = validate;
    }

}
