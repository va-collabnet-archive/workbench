/*
 *  Copyright 2010 International Health Terminology Standards Development  *  Organisation..
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.mojo.export;

import org.dwfa.builder.BuilderException;
import org.junit.BeforeClass;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.export.amt.AmtOriginProcessor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 *
 * @author Matthew Edwards
 */
public final class OriginProcessorFactoryTest {

    private Mockery context;
    private I_GetConceptData conceptDataMock;
    private PositionDescriptor positionDescriptor;
    private PositionDescriptor[] posDescriptorArray;
    private ConceptDescriptor conceptDescriptor;
    private I_TermFactory termFactoryMock;

    @BeforeClass
    public static void setupLocalVersionedTerminology() throws BuilderException {
    }

    @Before
    public void setup() {
        positionDescriptor = new PositionDescriptor();
        posDescriptorArray = new PositionDescriptor[0];
        conceptDescriptor = new ConceptDescriptor();
        context = new Mockery() {

            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        termFactoryMock = context.mock(I_TermFactory.class);
        LocalVersionedTerminology.setStealthfactory(termFactoryMock);
        assertEquals(termFactoryMock, LocalVersionedTerminology.get());
        conceptDataMock = context.mock(I_GetConceptData.class, "conceptDataConcept");
    }

    /**
     * Test of getInstance method, of class OriginProcessorFactory.
     */
    @Test
    public void testGetAmtOriginProcessor() throws Exception {

        String originProcessorType = "AMT";
        OriginProcessorFactory instance = new OriginProcessorFactory(conceptDataMock, positionDescriptor,
                posDescriptorArray, conceptDescriptor);
        OriginProcessor result = instance.getInstance(originProcessorType);
        assertTrue(result instanceof AmtOriginProcessor);
    }
    /**
     * Test of getInstance method, of class OriginProcessorFactory.
     */
//    @Test
    public void testGetSnomedOriginProcessor() throws Exception {

        context.checking(new Expectations(){{
            oneOf(termFactoryMock).getConcept(1);
            will(returnValue(conceptDataMock));
        }});

        String originProcessorType = "SNOMED";

        OriginProcessorFactory instance = new OriginProcessorFactory(conceptDataMock, positionDescriptor,
                posDescriptorArray, conceptDescriptor);

        OriginProcessor result = instance.getInstance(originProcessorType);

        assertTrue(result instanceof SnomedOriginProcessor);
    }

    /**
     * Test of getInstance method, of class OriginProcessorFactory.
     */
    @Test
    public void testNullThrowsIllegalArgumentException() throws Exception {
        String originProcessorType = null;
        OriginProcessorFactory instance = new OriginProcessorFactory(conceptDataMock, positionDescriptor,
                posDescriptorArray, conceptDescriptor);
        OriginProcessor result = null;
        try {
            result = instance.getInstance(originProcessorType);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertNull(result);
        }
    }

    /**
     * Test of getInstance method, of class OriginProcessorFactory.
     */
    @Test
    public void testInvalidFlagThrowsIllegalArgumentException() throws Exception {
        String originProcessorType = "invalid";
        OriginProcessorFactory instance = new OriginProcessorFactory(conceptDataMock, positionDescriptor,
                posDescriptorArray, conceptDescriptor);
        OriginProcessor result = null;
        try {
            result = instance.getInstance(originProcessorType);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertNull(result);
        }
    }
}
