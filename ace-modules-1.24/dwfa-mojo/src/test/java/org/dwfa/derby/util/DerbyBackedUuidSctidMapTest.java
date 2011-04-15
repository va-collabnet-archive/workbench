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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.util.id.Type5UuidFactory;

public class DerbyBackedUuidSctidMapTest extends TestCase {

    private File testRoot = new File("target", "test");
    private File testMap = new File(testRoot, "sample." + UUID.randomUUID() + ".map");
    private DerbyBackedUuidSctidMap mapDb;
    private int mapSize = 1000;
    private int dbLoadSize = 100000;
    private Random random = new Random(new Date().getTime());

    private TYPE getRandomType() {
        return TYPE.values()[random.nextInt(TYPE.values().length)];
    }

    private void generateTestMap() throws IOException, NoSuchAlgorithmException {
        testMap.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(testMap));
        for (int i = 1; i < mapSize + 1; i++) {
            bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
            bw.append("\n");
            bw.append(SctIdGenerator.generate(i, PROJECT.SNOMED_CT, NAMESPACE.NEHTA, getRandomType()));
            bw.append("\t");
            bw.append("2008-07-01 00:00:00");
            bw.append("\n");
        }
        bw.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateTestMap();
        mapDb = DerbyBackedUuidSctidMap.read(testMap);
        assertTrue(mapSize == mapDb.size());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        testMap.delete();
    }

    public void test() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
        Long sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString().startsWith("1" + NAMESPACE.NEHTA.getDigits()));
        sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString().startsWith("2" + NAMESPACE.NEHTA.getDigits()));
    }

    public void testUseExistingDb() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
        mapDb = DerbyBackedUuidSctidMap.read(testMap);

        Long sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertTrue(sctId.toString().startsWith("1" + NAMESPACE.NEHTA.getDigits()));
        sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(2)));
        assertTrue(sctId.toString().startsWith("2" + NAMESPACE.NEHTA.getDigits()));
    }

    public void testDbLoad() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
        for (long i = 1; i < dbLoadSize + 1; i++) {
            mapDb.put(Type5UuidFactory.get("load" + i), Long.valueOf(SctIdGenerator.generate(i, PROJECT.SNOMED_CT, NAMESPACE.NEHTA,
                getRandomType())));
        }
    }
}
