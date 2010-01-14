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
package org.dwfa.maven.sctid.transform;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;

public class UuidToSctRelIdWithGenerationTest extends TestCase {
    Random random = new Random(new Date().getTime());
    UuidToSctRelIdWithGeneration uuidToSctRelIdWithGeneration;
    Long loadTestSize = 10001l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        uuidToSctRelIdWithGeneration = new UuidToSctRelIdWithGeneration();
        uuidToSctRelIdWithGeneration.setupImpl(null, new File("target", "TestDb"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        uuidToSctRelIdWithGeneration.cleanup(null);
    }

    private NAMESPACE getRandomNamespace() {
        return NAMESPACE.values()[random.nextInt(NAMESPACE.values().length)];
    }

    public void testConceptIdGeneration() throws Exception {
        UUID uuid = UUID.randomUUID();
        NAMESPACE namespace = getRandomNamespace();
        String sctId = uuidToSctRelIdWithGeneration.transform(uuid.toString(), namespace);

        assertTrue("Must return the same sctId for the same UUID",
                sctId.equals(uuidToSctRelIdWithGeneration.transform(uuid.toString(), namespace)));
    }

    public void testLoad() throws Exception {
        for(int i = 0; i < loadTestSize; i++){
            String sctId = uuidToSctRelIdWithGeneration.transform(UUID.randomUUID().toString(), getRandomNamespace());
            assertNotNull(sctId);
        }
    }
}
