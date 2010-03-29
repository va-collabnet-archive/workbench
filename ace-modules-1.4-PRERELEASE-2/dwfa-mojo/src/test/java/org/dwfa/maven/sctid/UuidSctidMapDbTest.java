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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Test the UuidSctidMapDb - tests will be executed against a Derby embedded database unless the following system
 * properties are set
 * <ul>
 * <li>uuid.map.test.database.password</li>
 * <li>uuid.map.test.database.user</li>
 * <li>uuid.map.test.database.url</li>
 * <li>uuid.map.test.database.driver</li>
 * </ul>
 */
public class UuidSctidMapDbTest extends TestCase {
    public static final String UUID_MAP_TEST_DATABASE_PASSWORD = "uuid.map.test.database.password";
    public static final String UUID_MAP_TEST_DATABASE_USER = "uuid.map.test.database.user";
    public static final String UUID_MAP_TEST_DATABASE_URL = "uuid.map.test.database.url";
    public static final String UUID_MAP_TEST_DATABASE_DRIVER = "uuid.map.test.database.driver";
    private File testFixedMapRoot = new File("target", "test-fixed-map");
    private File testMapRoot = new File("target", "test-map");
    private File testMap = new File(testMapRoot, "sample.map");
    private File testFixedMap = new File(testFixedMapRoot, "sample.map");
    private File testDBMap = new File(new File("target", "test"), "sample.map.db");
    private UuidSctidMapDb mapDb;
    private int mapSize = 15001;
    private Random random = new Random(new Date().getTime());

    private void generateTestMap() throws IOException, NoSuchAlgorithmException {
        if (testFixedMap.getParentFile().exists() || testFixedMap.getParentFile().mkdirs()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(testFixedMap));
            for (int i = 1; i < mapSize; i++) {
                bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
                if (i % 10 == 0) {
                    bw.append("\t");
                    bw.append(Type5UuidFactory.get("second" + Integer.toString(i)).toString());
                }
                bw.append("\n");
                bw.append(SctIdGenerator.generate(i, getRandomNamespace(), getRandomType()));
                bw.append("\n");
            }
            bw.close();

            testMap.getParentFile().mkdirs();
            bw = new BufferedWriter(new FileWriter(testMap));
            for (int i = 1; i < mapSize + 1; i++) {
                bw.append(Type5UuidFactory.get("MAP" + Integer.toString(i)).toString());
                bw.append("\n");
                bw.append(SctIdGenerator.generate(i, NAMESPACE.NEHTA, getRandomType()));
                bw.append("\t");
                bw.append("2008-07-01 00:00:00");
                bw.append("\n");
            }

            bw.append(Type5UuidFactory.get("first90000000000000").toString());
            bw.append("\n");
            bw.append(SctIdGenerator.generate(900000000000000l, NAMESPACE.SNOMED_META_DATA, TYPE.CONCEPT));
            bw.append("\t");
            bw.append("2008-07-01 00:00:00");
            bw.append("\n");
            bw.close();
        }
    }

    private NAMESPACE getRandomNamespace() {
        return NAMESPACE.values()[random.nextInt(NAMESPACE.values().length)];
    }

    private TYPE getRandomType() {
        return TYPE.values()[random.nextInt(TYPE.values().length)];
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        if (System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER) == null) {
            UuidSctidMapDb.setDatabaseProperties("org.apache.derby.jdbc.EmbeddedDriver", 
                "jdbc:derby:directory:" + testDBMap.getCanonicalPath() + ";create=true;");

        } else {
            UuidSctidMapDb.setDatabaseProperties(System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER), 
                System.getProperty(UUID_MAP_TEST_DATABASE_URL), 
                System.getProperty(UUID_MAP_TEST_DATABASE_USER), 
                System.getProperty(UUID_MAP_TEST_DATABASE_PASSWORD));
        }
        
        mapDb = UuidSctidMapDb.getInstance(true);

        if (mapDb.isDatabaseInitialised()) {
            mapDb.openDb();
            mapDb.dropDb();
            mapDb.close();
        }
        removeTestFiles();
        
        generateTestMap();
        mapDb.createDb(testFixedMap.getParentFile(), testMap.getParentFile(), true);

        assertTrue("Missing elements in the database.", mapSize < mapDb.size());
    }

    private void removeTestFiles() {
        FilenameFilter mapFilter = new FilenameFilter() {  
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".map");
            }
        };
        if (testMapRoot.exists()) {
            File[] files = testMapRoot.listFiles(mapFilter);
            for (File file : files) {
                file.delete();
            }
        }
        if (testFixedMapRoot.exists()) {
            File[] files = testFixedMapRoot.listFiles(mapFilter);
            for (File file : files) {
                file.delete();
            }
        }
        if (testDBMap.exists()) {
            testDBMap.delete();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mapDb.openDb();
        mapDb.dropDb();
        mapDb.close();
        removeTestFiles();
    }

    public void testNoDatabaseDriver() throws Exception {
        System.clearProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER);
        try {
            UuidSctidMapDb.getInstance(true);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Database driver was not specified as required by the system property \"SctIdMap.driver\"", e.getMessage());
        }
    }
    
    public void testNoDatabaseUrl() throws Exception {
        System.clearProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL);
        try {
            UuidSctidMapDb.getInstance(true);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Database connection url was not specified as required by the system property \"SctIdMap.databaseConnectionUrl\"", e.getMessage());
        }
    }
    
    public void testRf2DataLoad() throws Exception {
        mapDb.clearDb();
        assertEquals(null, mapDb.getSctId(UUID.fromString("958096af-704d-4a22-a77e-9b9790c67471")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("e7b0a7ec-4d04-3674-9d2f-3a5f8bf18232")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("1d3977fb-3fc8-4e51-b486-b9f9de04efcf")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("7d115fb5-b468-3b3e-8e9d-07f6bd091dcc")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("d185189f-e9eb-4c7d-8cca-e470eaecef41")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("8b836a6a-4d13-477c-a22e-5e8360d6e62c")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("04aaf2d4-8d79-4da8-9c09-2b918bbf5885")));
        assertEquals(null, mapDb.getSctId(UUID.fromString("f30d99a4-c3f1-4821-a25a-9251e21d8a30")));
        
        mapDb.updateDbFromRf2IdFile(new File("src/test/resources/org/dwfa/maven/sctid/test.rf2.ids.txt"));
        assertEquals(new Long(900000000000003029L), mapDb.getSctId(UUID.fromString("958096af-704d-4a22-a77e-9b9790c67471")));
        assertEquals(new Long(900000000000004011L), mapDb.getSctId(UUID.fromString("e7b0a7ec-4d04-3674-9d2f-3a5f8bf18232")));
        assertEquals(new Long(900000000000005012L), mapDb.getSctId(UUID.fromString("1d3977fb-3fc8-4e51-b486-b9f9de04efcf")));
        assertEquals(new Long(900000000000006013L), mapDb.getSctId(UUID.fromString("7d115fb5-b468-3b3e-8e9d-07f6bd091dcc")));
        assertEquals(new Long(15984971000036168L), mapDb.getSctId(UUID.fromString("d185189f-e9eb-4c7d-8cca-e470eaecef41")));
        assertEquals(new Long(15984981000036165L), mapDb.getSctId(UUID.fromString("8b836a6a-4d13-477c-a22e-5e8360d6e62c")));
        assertEquals(new Long(17097511000036165L), mapDb.getSctId(UUID.fromString("04aaf2d4-8d79-4da8-9c09-2b918bbf5885")));
        assertEquals(new Long(17128811000036169L), mapDb.getSctId(UUID.fromString("f30d99a4-c3f1-4821-a25a-9251e21d8a30")));
    }
    
    public void testGetSctId() throws Exception {
        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("second" + Integer.toString(10)));
        assertTrue(sctId.toString() + " must start with 10", sctId.toString().startsWith("10"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString() + " must start with 2", sctId.toString().startsWith("2"));

        assertTrue(mapDb.getSctId(Type5UuidFactory.get("blar" + Integer.toString(1))) == null);
    }

    public void testGetUuidList() throws Exception {
        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertFalse(mapDb.getUuidList(sctId).isEmpty());
        assertTrue(mapDb.getUuidList(sctId).get(0).equals(Type5UuidFactory.get("first" + Integer.toString(1))));

        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(10)));
        assertFalse(mapDb.getUuidList(sctId).isEmpty());

        assertTrue(mapDb.getUuidList(sctId).get(0).equals(Type5UuidFactory.get("first" + Integer.toString(10))));
        assertTrue(mapDb.getUuidList(sctId).get(1).equals(Type5UuidFactory.get("second" + Integer.toString(10))));
        assertTrue(mapDb.getUuidList(0L).isEmpty());
    }

    public void testRemoveUuid() throws Exception {
        mapDb.removeUuid(Type5UuidFactory.get("first" + Integer.toString(1000)));
        assertTrue(mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1000))) == null);
    }

    public void testAddUUIDSctIdEntryList() throws Exception {
        Map<UUID, Long> map = new HashMap<UUID, Long>();
        for (int i = 0; i < 1000; i++) {
            map.put(UUID.randomUUID(), Long.parseLong(SctIdGenerator.generate(i+1, getRandomNamespace(), getRandomType())));
        }
        
        mapDb.addUUIDSctIdEntryList(map);
        
        for (UUID uuid : map.keySet()) {
            assertEquals(map.get(uuid), mapDb.getSctId(uuid));
        }
    }
    
    public void testAddUUIDSctIdEntry() throws Exception {
        mapDb.addUUIDSctIdEntry(Type5UuidFactory.get("new" + Integer.toString(1)),
            Long.valueOf(SctIdGenerator.generate(mapSize + 1, getRandomNamespace(), getRandomType())));
        assertTrue(mapDb.getSctId(Type5UuidFactory.get("new" + Integer.toString(1))) != null);
    }

    public void testExistingDb() throws Exception {
        mapDb.openDb(testFixedMap.getParentFile(), testMap.getParentFile(), true);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("second" + Integer.toString(10)));
        assertTrue(sctId.toString() + " must start with 10", sctId.toString().startsWith("10"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString() + " must start with 2", sctId.toString().startsWith("2"));

        mapDb.openDb();
    }

    public void testCreateDbWithValidationOn() throws Exception {
        testMap = new File(testMapRoot, "sample.ValidationOn.map");
        testFixedMap = new File(testFixedMapRoot, "sample.ValidationOn.map");
        testDBMap = new File(new File("target", "test"), "sample.ValidationOn.map.db");

        testDBMap = new File(new File("target", "test"), "sample2.ValidationOn.map.db");

        if (!testFixedMap.getParentFile().exists()) {
            testFixedMap.getParentFile().mkdirs();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(testFixedMap));
        for (int i = 1; i < 10; i++) {
            bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
            if (i % 10 == 0) {
                bw.append("\t");
                bw.append(Type5UuidFactory.get("second" + Integer.toString(i)).toString());
            }
            bw.append("\n");
            bw.append(SctIdGenerator.generate(i, getRandomNamespace(), getRandomType()));
            bw.append("\n");
        }
        bw.close();

        testMap.getParentFile().mkdirs();
        bw = new BufferedWriter(new FileWriter(testMap));
        for (int i = 1; i < 10 + 1; i++) {
            bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
            bw.append("\n");
            bw.append(SctIdGenerator.generate(i, NAMESPACE.NEHTA, getRandomType()));
            bw.append("\t");
            bw.append("2008-07-01 00:00:00");
            bw.append("\n");
        }

        bw.append(Type5UuidFactory.get("first90000000000000").toString());
        bw.append("\n");
        bw.append(SctIdGenerator.generate(900000000000000l, NAMESPACE.SNOMED_META_DATA, getRandomType()));
        bw.append("\t");
        bw.append("2008-07-01 00:00:00");
        bw.append("\n");
        bw.close();

        UuidSctidMapDb mapDb = UuidSctidMapDb.getInstance();
        mapDb.openDb(testFixedMap.getParentFile(), testMap.getParentFile(), true);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first90000000000000"));
        assertTrue(sctId.toString() + " must start with 900000000000000", sctId.toString()
            .startsWith("900000000000000"));
    }

    public void testUpdateDbWithValidationOn() throws Exception {
        testFixedMap = new File(testFixedMapRoot, "sample.ValidationOn.map");
        testDBMap = new File(new File("target", "test"), "sample.ValidationOn.map.db");

        testDBMap = new File(new File("target", "test"), "sample2.ValidationOn.map.db");

        if (!testFixedMap.getParentFile().exists()) {
            testFixedMap.getParentFile().mkdirs();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(testFixedMap));
        for (int i = 1; i < 10; i++) {
            bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
            if (i % 10 == 0) {
                bw.append("\t");
                bw.append(Type5UuidFactory.get("second" + Integer.toString(i)).toString());
            }
            bw.append("\n");
            bw.append(SctIdGenerator.generate(i, getRandomNamespace(), getRandomType()));
            bw.append("\n");
        }
        bw.close();

        UuidSctidMapDb mapDb = UuidSctidMapDb.getInstance();
        mapDb.openDb(testFixedMap.getParentFile(), null, true);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first90000000000000"));
        assertTrue(sctId.toString() + " must start with 900000000000000", sctId.toString()
            .startsWith("900000000000000"));
    }

    public void testGetSequence() throws Exception {
        assertTrue("SNOMED_META_DATA sequence should start at 900000000000000 not "
            + mapDb.getSctSequenceId(NAMESPACE.SNOMED_META_DATA, TYPE.CONCEPT), mapDb.getSctSequenceId(
            NAMESPACE.SNOMED_META_DATA, TYPE.CONCEPT).equals(900000000000000l));
    }
}
