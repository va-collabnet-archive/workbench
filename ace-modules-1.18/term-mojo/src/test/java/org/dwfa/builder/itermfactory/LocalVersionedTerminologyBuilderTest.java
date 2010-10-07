/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.builder.itermfactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.builder.BuilderException;
import org.dwfa.vodb.VodbEnv;
import org.jmock.Mockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for LocalVersionedTerminologyBuilder
 * @author Matthew Edwards
 */
public class LocalVersionedTerminologyBuilderTest {

    private final Logger logger;
    private static final Long TEST_DEFAULT_CACHE_VALUE = 80L;
    private static final File VALID_VODB_DIR = new File("target", "vodbDirectory");
    private LocalVersionedTerminologyBuilder instance;
    private Mockery context;
    private I_ImplementTermFactory iImplementTermFactoryMock;

    public LocalVersionedTerminologyBuilderTest() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Before
    public void setUp() {
        context = new Mockery();
        iImplementTermFactoryMock = context.mock(I_ImplementTermFactory.class);
        LocalVersionedTerminology.setStealthfactory(null);
    }

    @AfterClass
    public static void tearDownTest() {
        VALID_VODB_DIR.delete();
    }

    /**
     * Test of build method, of class LocalVersionedTerminologyBuilder.
     */
    @Test
    public void testUseDefaultCacheValue() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testUseDefaultCacheValue()");
        instance = new LocalVersionedTerminologyBuilder(null, false, null, false, null);
        Long expResult = LocalVersionedTerminologyBuilder.DEFAULT_CACHE_SIZE;
        Long result = (Long) getField(instance.getClass(), "cacheSize").get(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class LocalVersionedTerminologyBuilder.
     */
    @Test
    public void testSetCacheValue() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testSetCacheValue()");
        Long expResult = TEST_DEFAULT_CACHE_VALUE;
        instance = new LocalVersionedTerminologyBuilder(null, false, expResult, false, null);
        Long result = (Long) getField(instance.getClass(), "cacheSize").get(instance);
        assertEquals(expResult, result);
    }

    @Test
    public void testUseDefaultDBSetupConfig() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testUseDefaultDBSetupConfig()");
        instance = new LocalVersionedTerminologyBuilder(null, false, null, false, null);
        DatabaseSetupConfig result = (DatabaseSetupConfig) getField(instance.getClass(), "dbSetupConfig").get(instance);
        assertNotNull(result);
    }

    @Test
    public void testSetDBSetupConfig() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testSetDBSetupConfig()");
        DatabaseSetupConfig expResult = new DatabaseSetupConfig();
        instance = new LocalVersionedTerminologyBuilder(null, false, null, false, expResult);
        DatabaseSetupConfig result = (DatabaseSetupConfig) getField(instance.getClass(), "dbSetupConfig").get(instance);
        assertEquals(expResult, result);
    }

    @Test
    public void testNewConnectionWithValidVodbDirectory() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testNewConnectionWithValidVodbDirectory()");

        instance = new LocalVersionedTerminologyBuilder(VALID_VODB_DIR, false, null, false, null);

        try {
            I_TermFactory result = instance.build();
            System.out.println(result);
            assertTrue(result instanceof VodbEnv);
            File envHome = (File) getField(VodbEnv.class, "envHome").get(result);
            assertEquals(VALID_VODB_DIR, envHome);
        } finally {
            LocalVersionedTerminology.close(LocalVersionedTerminology.get());
        }
    }

    @Test
    public void testReconnectWithValidVodbDirectory() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testReconnectWithValidVodbDirectory()");
        instance = new LocalVersionedTerminologyBuilder(VALID_VODB_DIR, false, null, true, null);
        LocalVersionedTerminology.setStealthfactory(iImplementTermFactoryMock);
        try {
            I_TermFactory result = instance.build();
            assertTrue(result instanceof VodbEnv);
            File envHome = (File) getField(VodbEnv.class, "envHome").get(result);
            assertEquals(VALID_VODB_DIR, envHome);
        } finally {
            LocalVersionedTerminology.close(LocalVersionedTerminology.get());
        }
    }

    @Test
    public void testNewConnectionWithNullVodbDirectory() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testNewConnectionWithNullVodbDirectory()");
        instance = new LocalVersionedTerminologyBuilder(null, false, null, false, null);
        try {
            instance.build();
            fail("Should have thrown BuilderException");
        } catch (Exception ex) {
            assertTrue(ex instanceof BuilderException);
        } finally {
            LocalVersionedTerminology.close(LocalVersionedTerminology.get());
        }
    }

    @Test
    public void testReconnectWithNullVodbDirectory() throws Exception {
        logger.info("LocalVersionedTerminologyBuilderTest.testReconnectWithNullVodbDirectory()");
        instance = new LocalVersionedTerminologyBuilder(null, false, null, true, null);
        LocalVersionedTerminology.setStealthfactory(iImplementTermFactoryMock);
        try {
            instance.build();
            fail("Should have thrown BuilderException");
        } catch (Exception ex) {
            assertTrue(ex instanceof BuilderException);
        } finally {
            LocalVersionedTerminology.close(LocalVersionedTerminology.get());
        }
    }

    private static Field getField(final Class<?> clazz, final String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
