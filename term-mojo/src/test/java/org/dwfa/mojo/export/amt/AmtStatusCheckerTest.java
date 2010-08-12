/*
 *  Copyright 2010 matt.
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
package org.dwfa.mojo.export.amt;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 *
 * @author matt
 */
public final class AmtStatusCheckerTest {

    private Mockery context;
    /** The Terminology Factory for accessing Concept information.*/
    private I_TermFactory termFactory;
    /** The active concept. */
    private I_GetConceptData activeConcept;
    /** The Current concept. */
    private I_GetConceptData currentConcept;
    /** The Erroneous concept. */
    private I_GetConceptData erroneous;
    /** The Concept Retired concept. */
    private I_GetConceptData conceptRetired;
    private I_GetConceptData statusConcept;

    @Before
    public void setUp() {
        context = new Mockery() {

            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        activeConcept = context.mock(I_GetConceptData.class, "activeConcept");
        currentConcept = context.mock(I_GetConceptData.class, "currentConcept");
        erroneous = context.mock(I_GetConceptData.class, "erroneous");
        conceptRetired = context.mock(I_GetConceptData.class, "conceptRetired");
        statusConcept = context.mock(I_GetConceptData.class, "statusConcept");
        termFactory = context.mock(I_TermFactory.class, "termFactory");
        LocalVersionedTerminology.setStealthfactory(termFactory);
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveChildOfActiveConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(activeConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(expResult));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsActiveErroneousConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(activeConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(erroneous).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    @Test
    public void testIsActiveNotChildOfActiveAndNotErroneous() throws Exception {
        final int statusNid = 0;
        final boolean expResult = false;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(activeConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(erroneous).getNid();
                will(returnValue(Integer.MAX_VALUE));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWtihActiveConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWtihCurrentConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWtihErroneousConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(erroneous).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWtihRetiredConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(erroneous).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(conceptRetired).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveFalse() {
        final int statusNid = 0;
        final boolean expResult = false;
        AmtStatusChecker instance =
                new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired, termFactory);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(erroneous).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(conceptRetired).getNid();
                will(returnValue(Integer.MAX_VALUE));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }
}
