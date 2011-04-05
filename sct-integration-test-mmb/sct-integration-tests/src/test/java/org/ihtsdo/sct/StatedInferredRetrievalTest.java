/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.sct;

import java.io.IOException;
import java.util.UUID;
import junit.framework.Assert;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for tracker id: artf221341
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()
public class StatedInferredRetrievalTest {

    public StatedInferredRetrievalTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsaOccupationalTherapy() throws IOException, ContraditionException {
        RelationshipChronicleBI rel =
                (RelationshipChronicleBI) Ts.get().getComponent(UUID.fromString("991c48bb-147c-55c9-86a5-97bbd8260a69"));

        ViewCoordinate primordialVc = Ts.get().getMetadataVC();
        System.out.println("found rel: " + rel);

        RelationshipVersionBI v = rel.getVersion(primordialVc);
        System.out.println("primordial version: " + v);
        Assert.assertNull(v);

        PathBI snomedPath = Ts.get().getPath(rel.getPrimordialVersion().getPathNid());
        PositionBI latestOnSnomedPath = new Position(Long.MAX_VALUE, snomedPath);
        ViewCoordinate statedVc = new ViewCoordinate(primordialVc);
        statedVc.setRelAssertionType(RelAssertionType.STATED);

        statedVc.setPositionSet(new PositionSet(latestOnSnomedPath));

        v = rel.getVersion(statedVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() != statedVc.getClassifierNid());
        System.out.println("stated version: " + v);

        ViewCoordinate inferredVc = new ViewCoordinate(statedVc);
        inferredVc.setRelAssertionType(RelAssertionType.INFERRED);
        
        v = rel.getVersion(inferredVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() == statedVc.getClassifierNid());
        System.out.println("inferred version: " + v);


        ViewCoordinate inferredThenStatedVc = new ViewCoordinate(statedVc);
        inferredThenStatedVc.setRelAssertionType(RelAssertionType.INFERRED_THEN_STATED);
        
        v = rel.getVersion(inferredThenStatedVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() == statedVc.getClassifierNid());
        System.out.println("inferred then stated version: " + v);
    }

    @Test
    public void testCornealGluingIsaOperativeProcedure() throws IOException, ContraditionException {
        RelationshipChronicleBI rel =
                (RelationshipChronicleBI) Ts.get().getComponent(UUID.fromString("12d3d337-f810-5dc5-bc63-ac54459d9c18"));

        ViewCoordinate primordialVc = Ts.get().getMetadataVC();
        System.out.println("found rel: " + rel);

        RelationshipVersionBI v = rel.getVersion(primordialVc);
        System.out.println("primordial version: " + v);
        Assert.assertNull(v);

        PathBI snomedPath = Ts.get().getPath(rel.getPrimordialVersion().getPathNid());
        PositionBI latestOnSnomedPath = new Position(Long.MAX_VALUE, snomedPath);
        ViewCoordinate statedVc = new ViewCoordinate(primordialVc);
        statedVc.setRelAssertionType(RelAssertionType.STATED);

        statedVc.setPositionSet(new PositionSet(latestOnSnomedPath));

        v = rel.getVersion(statedVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() != statedVc.getClassifierNid());
        System.out.println("stated version: " + v);

        ViewCoordinate inferredVc = new ViewCoordinate(statedVc);
        inferredVc.setRelAssertionType(RelAssertionType.INFERRED);

        v = rel.getVersion(inferredVc);
        Assert.assertNull(v);
        // Assert.assertTrue(v.getAuthorNid() == statedVc.getClassifierNid());
        // System.out.println("inferred version: " + v);
        System.out.println("inferred version: null");


        ViewCoordinate inferredThenStatedVc = new ViewCoordinate(statedVc);
        inferredThenStatedVc.setRelAssertionType(RelAssertionType.INFERRED_THEN_STATED);

        v = rel.getVersion(inferredThenStatedVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() != statedVc.getClassifierNid());
        System.out.println("inferred then stated version: " + v);
    }

        @Test
    public void testCornealGluingIsaGluingOfCornea() throws IOException, ContraditionException {
        RelationshipChronicleBI rel =
                (RelationshipChronicleBI) Ts.get().getComponent(UUID.fromString("64469c96-871c-56f9-90d9-6207185715cb"));

        ViewCoordinate primordialVc = Ts.get().getMetadataVC();
        System.out.println("found rel: " + rel);

        RelationshipVersionBI v = rel.getVersion(primordialVc);
        System.out.println("primordial version: " + v);
        Assert.assertNull(v);

        PathBI snomedPath = Ts.get().getPath(rel.getPrimordialVersion().getPathNid());
        PositionBI latestOnSnomedPath = new Position(Long.MAX_VALUE, snomedPath);
        ViewCoordinate statedVc = new ViewCoordinate(primordialVc);
        statedVc.setRelAssertionType(RelAssertionType.STATED);

        statedVc.setPositionSet(new PositionSet(latestOnSnomedPath));

        v = rel.getVersion(statedVc);
        Assert.assertNull(v);
        // Assert.assertTrue(v.getAuthorNid() != statedVc.getClassifierNid());
        // System.out.println("stated version: " + v);
        System.out.println("stated version: null");

        ViewCoordinate inferredVc = new ViewCoordinate(statedVc);
        inferredVc.setRelAssertionType(RelAssertionType.INFERRED);

        v = rel.getVersion(inferredVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() == statedVc.getClassifierNid());
        System.out.println("inferred version: " + v);


        ViewCoordinate inferredThenStatedVc = new ViewCoordinate(statedVc);
        inferredThenStatedVc.setRelAssertionType(RelAssertionType.INFERRED_THEN_STATED);

        v = rel.getVersion(inferredThenStatedVc);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.getAuthorNid() == statedVc.getClassifierNid());
        System.out.println("inferred then stated version: " + v);
    }
}
