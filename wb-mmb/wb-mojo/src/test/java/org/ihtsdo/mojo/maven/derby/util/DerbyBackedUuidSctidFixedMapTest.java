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
package org.ihtsdo.mojo.maven.derby.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.util.id.Type5UuidFactory;

public class DerbyBackedUuidSctidFixedMapTest extends TestCase {

    private File testRoot = new File("target", "test");
    private File testMap = new File(testRoot, "sample." + UUID.randomUUID() + ".map");
    private DerbyBackedUuidSctidFixedMap mapDb;
    private int mapSize = 50000;

    private void generateTestMap() throws IOException, NoSuchAlgorithmException {
        testMap.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(testMap));
        for (int i = 0; i < mapSize; i++) {
            bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
            if (i % 10 == 0) {
                bw.append("\t");
                bw.append(Type5UuidFactory.get("second" + Integer.toString(i)).toString());
            }
            bw.append("\n");
            bw.append(Long.toString(i));
            bw.append("\n");
        }
        bw.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateTestMap();
        mapDb = DerbyBackedUuidSctidFixedMap.read(testMap);
        assertTrue(mapSize < mapDb.size());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        testMap.delete();
    }

    public void test() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Long sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(0)));
        assertEquals(sctId.longValue(), 0L);
        sctId = mapDb.get(Type5UuidFactory.get("second" + Integer.toString(0)));
        assertEquals(sctId.longValue(), 0L);
        sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(1)));
        assertEquals(sctId.longValue(), 1L);
    }

}
