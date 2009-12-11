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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.util.id.Type5UuidFactory;

public class UuidSctidMapDbTest extends TestCase {
    private File testFixedMapRoot = new File("target", "test-fixed-map");
    private File testMapRoot = new File("target", "test-map");
    private File testMap = new File(testMapRoot, "sample.map");
    private File testFixedMap = new File(testFixedMapRoot, "sample.map");
    private File testDBMap = new File(new File("target", "test"), "sample.map.db");
    private UuidSctidMapDb mapDb;
    private int mapSize = 15001;
    private Random random = new Random(new Date().getTime());

    private void generateTestMap() throws IOException, NoSuchAlgorithmException {
        if(testFixedMap.getParentFile().mkdirs()){
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
        generateTestMap();
        mapDb = new UuidSctidMapDb(testDBMap, testFixedMap.getParentFile(), testMap.getParentFile(), true, false);

        assertTrue("Missing elements in the database.", mapSize < mapDb.size());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mapDb.close();
    }

    public void testGetSctId() throws Exception {
        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("second" + Integer.toString(10)));
        assertTrue(sctId.toString() + " must start with 10", sctId.toString().startsWith("10"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString() + " must start with 2", sctId.toString().startsWith("2"));

        assertTrue(mapDb.getSctId(Type5UuidFactory.get("blar" + Integer.toString(1))) == null);

        mapDb.close();
    }

    public void testGetUuidList() throws Exception {
        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertFalse(mapDb.getUuidList(sctId).isEmpty());
        assertTrue(mapDb.getUuidList(sctId).get(0).equals(Type5UuidFactory.get("first" + Integer.toString(1))));

        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(10)));
        assertFalse(mapDb.getUuidList(sctId).isEmpty());
        assertTrue(mapDb.getUuidList(sctId).size() == 2);
        assertTrue(mapDb.getUuidList(sctId).get(0).equals(Type5UuidFactory.get("first" + Integer.toString(10))));
        assertTrue(mapDb.getUuidList(sctId).get(1).equals(Type5UuidFactory.get("second" + Integer.toString(10))));
        assertTrue(mapDb.getUuidList(0L).isEmpty());

        mapDb.close();
    }

    public void testRemoveUuid() throws Exception {
        mapDb.removeUuid(Type5UuidFactory.get("first" + Integer.toString(1000)));
        assertTrue(mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1000))) == null);

        mapDb.close();
    }

    public void testAddUUIDSctIdEntry() throws Exception {
        mapDb.addUUIDSctIdEntry(Type5UuidFactory.get("new" + Integer.toString(1)),
                Long.valueOf(SctIdGenerator.generate(mapSize + 1, getRandomNamespace(), getRandomType())));
        assertTrue(mapDb.getSctId(Type5UuidFactory.get("new" + Integer.toString(1))) != null);

        mapDb.close();
    }

    public void testExistingDb() throws Exception {
//        mapDb = new UuidSctidMapDb(testDBMap);
//
//        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
//        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
//        sctId = mapDb.getSctId(Type5UuidFactory.get("second" + Integer.toString(10)));
//        assertTrue(sctId.toString() + " must start with 10", sctId.toString().startsWith("10"));
//        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(2)));
//        assertTrue(sctId.toString() + " must start with 2", sctId.toString().startsWith("2"));

        mapDb.close();
        mapDb = new UuidSctidMapDb(testDBMap, testFixedMap.getParentFile(), testMap.getParentFile(), false, false);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("second" + Integer.toString(10)));
        assertTrue(sctId.toString() + " must start with 10", sctId.toString().startsWith("10"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString() + " must start with 2", sctId.toString().startsWith("2"));

        mapDb.close();
        mapDb = new UuidSctidMapDb(testDBMap);

        mapDb.close();
    }

    public void testCreateDbWithValidationOn() throws Exception {
        testMap = new File(testMapRoot, "sample.ValidationOn.map");
        testFixedMap = new File(testFixedMapRoot, "sample.ValidationOn.map");
        testDBMap = new File(new File("target", "test"), "sample.ValidationOn.map.db");

        testDBMap = new File(new File("target", "test"), "sample2.ValidationOn.map.db");

        if(!testFixedMap.getParentFile().exists()){
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

        UuidSctidMapDb mapDb = new UuidSctidMapDb(testDBMap, testFixedMap.getParentFile(), testMap.getParentFile(), true, true);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first90000000000000" ));
        assertTrue(sctId.toString() + " must start with 900000000000000", sctId.toString().startsWith("900000000000000"));

        mapDb.close();
    }

    public void testUpdateDbWithValidationOn() throws Exception {
        testFixedMap = new File(testFixedMapRoot, "sample.ValidationOn.map");
        testDBMap = new File(new File("target", "test"), "sample.ValidationOn.map.db");

        testDBMap = new File(new File("target", "test"), "sample2.ValidationOn.map.db");

        if(!testFixedMap.getParentFile().exists()){
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

        UuidSctidMapDb mapDb = new UuidSctidMapDb(testDBMap, testFixedMap.getParentFile(), null, true, true);

        Long sctId = mapDb.getSctId(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString() + " must start with 1", sctId.toString().startsWith("1"));
        sctId = mapDb.getSctId(Type5UuidFactory.get("first90000000000000" ));
        assertTrue(sctId.toString() + " must start with 900000000000000", sctId.toString().startsWith("900000000000000"));

        mapDb.close();
    }
    public void testGetSequence() throws Exception {
        assertTrue("SNOMED_META_DATA sequence should start at 900000000000000 not " +
            mapDb.getSctSequenceId(NAMESPACE.SNOMED_META_DATA, TYPE.CONCEPT),
            mapDb.getSctSequenceId(NAMESPACE.SNOMED_META_DATA, TYPE.CONCEPT).equals(900000000000000l));
        mapDb.close();
    }
}
