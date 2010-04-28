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

import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.sctid.UuidSctidMapDbTest;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;

/**
 * Test the UuidToSctDescIdWithGeneration - tests will be executed against a Derby embedded database unless the following system
 * properties are set
 * <ul>
 * <li>uuid.map.test.database.password</li>
 * <li>uuid.map.test.database.user</li>
 * <li>uuid.map.test.database.url</li>
 * <li>uuid.map.test.database.driver</li>
 * </ul>
 */
public class UuidToSctDescIdWithGenerationTest extends TestCase {
    private File testDatabase = new File("target", "TestDb");
    Random random = new Random(new Date().getTime());
    UuidToSctDescIdWithGeneration uuidToSctDescIdWithGeneration;
    Long loadTestSize = 10001l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        uuidToSctDescIdWithGeneration = new UuidToSctDescIdWithGeneration();

        if (System.getProperty(UuidSctidMapDbTest.UUID_MAP_TEST_DATABASE_DRIVER) == null) {
            UuidSctidMapDb.setDatabaseProperties("org.apache.derby.jdbc.EmbeddedDriver",
                "jdbc:derby:directory:" + testDatabase.getCanonicalPath() + ";create=true;");

        } else {
            UuidSctidMapDb.setDatabaseProperties(System.getProperty(UuidSctidMapDbTest.UUID_MAP_TEST_DATABASE_DRIVER),
                System.getProperty(UuidSctidMapDbTest.UUID_MAP_TEST_DATABASE_URL),
                System.getProperty(UuidSctidMapDbTest.UUID_MAP_TEST_DATABASE_USER),
                System.getProperty(UuidSctidMapDbTest.UUID_MAP_TEST_DATABASE_PASSWORD));
        }

        uuidToSctDescIdWithGeneration.setupImpl(null);

        deleteTestDatabase();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        uuidToSctDescIdWithGeneration.cleanup(null);
        deleteTestDatabase();
    }

    private void deleteTestDatabase() {
        if (testDatabase.exists()) {
            testDatabase.delete();
        }
    }

    private NAMESPACE getRandomNamespace() {
        return NAMESPACE.values()[random.nextInt(NAMESPACE.values().length)];
    }

    private PROJECT getRandomProject() {
        return PROJECT.values()[random.nextInt(PROJECT.values().length)];
    }

    public void testConceptIdGeneration() throws Exception {
        UUID uuid = UUID.randomUUID();
        NAMESPACE namespace = getRandomNamespace();
        PROJECT project = getRandomProject();
        String sctId = uuidToSctDescIdWithGeneration.transform(uuid.toString(), namespace, project);

        assertTrue("Must return the same sctId for the same UUID",
            sctId.equals(uuidToSctDescIdWithGeneration.transform(uuid.toString(), namespace, project)));
    }

    public void testLoad() throws Exception {
        for (int i = 0; i < loadTestSize; i++) {
            String sctId = uuidToSctDescIdWithGeneration.transform(UUID.randomUUID().toString(), getRandomNamespace(), getRandomProject());
            assertNotNull(sctId);
        }
    }
}
