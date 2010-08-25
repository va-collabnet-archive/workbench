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

import org.jmock.Expectations;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.jmock.lib.legacy.ClassImposteriser;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthew Edwards
 */
public class SnomedStatusCheckerTest {

    private Mockery context;
    /** The Terminology Factory for accessing Concept information.*/
    private I_TermFactory termFactory;
    /** The Active Concept for RF2.*/
    private I_GetConceptData rf2ActiveConcept;
    /** The active concept. */
    private I_GetConceptData activeConcept;
    /** The Current concept. */
    private I_GetConceptData currentConcept;
    /** The Pending Move Concept.*/
    private I_GetConceptData pendingMove;
    /** The Concept denoting a retired concept.*/
    private I_GetConceptData conceptRetired;
    /** Native ID For the Moved Elsewhere status in ace.*/
    private int aceMovedElsewhereStatusNId = Integer.MAX_VALUE;
    /**Status Concept Mock Object.*/
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
        rf2ActiveConcept = context.mock(I_GetConceptData.class, "rf2ActiveConcept");
        pendingMove = context.mock(I_GetConceptData.class, "pendingMove");
        conceptRetired = context.mock(I_GetConceptData.class, "conceptRetired");
        statusConcept = context.mock(I_GetConceptData.class, "statusConcept");
        termFactory = context.mock(I_TermFactory.class, "termFactory");
        LocalVersionedTerminology.setStealthfactory(termFactory);
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveChildOfRf2ActiveConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(rf2ActiveConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(expResult));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveRf2ActiveConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(rf2ActiveConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(rf2ActiveConcept).getNid();
                will(returnValue(statusNid));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveActiveConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(rf2ActiveConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(rf2ActiveConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
                oneOf(activeConcept).getNid();
                will(returnValue(statusNid));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveCurrentConcept() throws Exception {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(rf2ActiveConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(rf2ActiveConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
                oneOf(currentConcept).getNid();
                will(returnValue(statusNid));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isActive method, of class AmtStatusChecker.
     */
    @Test
    public void testIsActiveFalse() throws Exception {
        final int statusNid = 0;
        final boolean expResult = false;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(termFactory).getConcept(statusNid);
                will(returnValue(statusConcept));
                oneOf(rf2ActiveConcept).isParentOf(statusConcept, null, null, null, false);
                will(returnValue(false));
                oneOf(rf2ActiveConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(statusConcept).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWithActiveConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
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
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWithCurrentConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
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
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWithPendingMoveConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {
            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(pendingMove).getNid();
                will(returnValue(statusNid));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

    /**
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveWithRetiredConcept() {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(pendingMove).getNid();
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
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionActiveAceMovedElsewhere() {
        final int statusNid = 0;
        final boolean expResult = true;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, statusNid);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(pendingMove).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(conceptRetired).getNid();
                will(returnValue(Integer.MAX_VALUE));
            }
        });

        boolean result = instance.isDescriptionActive(statusNid);
        assertEquals(expResult, result);
        context.assertIsSatisfied();
    }

       /**
     * Test of isDescriptionActive method, of class SnomedStatusChecker.
     */
    @Test
    public void testIsDescriptionFalse() {
        final int statusNid = 0;
        final boolean expResult = false;
        StatusChecker instance =
                new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, Integer.MIN_VALUE);
        context.checking(new Expectations() {

            {
                oneOf(activeConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(currentConcept).getNid();
                will(returnValue(Integer.MAX_VALUE));
                oneOf(pendingMove).getNid();
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
