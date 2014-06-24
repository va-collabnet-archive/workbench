package org.ihtsdo.tk.api.test.refset;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.test.ConceptHelper;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.api.test.NewConceptBuilder;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = { DefaultProfileBuilder.class, NewConceptBuilder.class })
public class NewRefsetTest {

    private static final String UUID_REFERENCE_SET = "7e38cd2d-6f1a-3a81-be0b-21e6090573c2";
    private static final String UUID_ACTIVE_VALUE = "d12702ee-c37f-385f-a070-61d56d4d0f1f";
    private static final String UUID_USER = "f7495b58-6630-3499-a44e-2052b5fcf06c";

    private static boolean propertyChangeListenerFired = false;

    @Test
    public void createMemberRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.MEMBER);
    }

    @Test
    public void createCidRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID);
    }

    @Test
    public void createCidCidRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_CID);
    }

    @Test
    public void createCidCidCidRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_CID_CID);
    }

    @Test
    public void createCidCidStringRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_CID_STR);
    }

    @Test
    public void createStringRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.STR);
    }

    @Test
    public void createIntRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.INT);
    }

    @Test
    public void createCidIntRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_INT);
    }

    @Test
    public void createBooleanRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.BOOLEAN);
    }

    @Test
    public void createCidStrRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_STR);
    }

    @Test
    public void createCidFloatRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_FLOAT);
    }

    @Test
    public void createCidLongRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.CID_LONG);
    }

    @Test
    public void createLongRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.LONG);
    }

    @Test
    public void createArrayByteArrayRefset() throws Exception {
        standardTest(TK_REFEX_TYPE.ARRAY_BYTEARRAY);
    }

    private void standardTest(TK_REFEX_TYPE refexType) throws Exception {
        ConceptChronicleBI referenceSet = Ts.get().getConcept(UUID.fromString(UUID_REFERENCE_SET));

        ConceptChronicleBI refsetConcept = ConceptHelper.createNewConcept("refset (test concept)", "refset",
            referenceSet.getNid());

        ConceptChronicleBI memberConcept = ConceptHelper.createNewConcept("member (test concept)", "member",
            referenceSet.getNid());

        ConceptChronicleBI refConcept1 = ConceptHelper.createNewConcept("ref concept one (test concept)",
            "ref concept one", referenceSet.getNid());

        ConceptChronicleBI refConcept2 = ConceptHelper.createNewConcept("ref concept two (test concept)",
            "ref concept two", referenceSet.getNid());

        ConceptChronicleBI refConcept3 = ConceptHelper.createNewConcept("ref concept three (test concept)",
            "ref concept three", referenceSet.getNid());

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        TerminologyBuilderBI ammender = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
            config.getViewCoordinate());

        RefexCAB refexSpec = new RefexCAB(refexType, memberConcept.getNid(), refsetConcept.getNid());
        switch (refexType) {
        case CID:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            break;

        case CID_CID:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.CNID2, refConcept2.getConceptNid());
            break;

        case CID_CID_CID:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.CNID2, refConcept2.getConceptNid());
            refexSpec.put(RefexProperty.CNID3, refConcept3.getConceptNid());
            break;

        case CID_CID_STR:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.CNID2, refConcept2.getConceptNid());
            refexSpec.put(RefexProperty.STRING1, "string value");
            break;

        case STR:
            refexSpec.put(RefexProperty.STRING1, "string value");
            break;

        case INT:
            refexSpec.put(RefexProperty.INTEGER1, 1);
            break;

        case CID_INT:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.INTEGER1, 1);
            break;

        case CID_STR:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.STRING1, "string value");
            break;

        case CID_FLOAT:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.FLOAT1, 123.456f);
            break;

        case CID_LONG:
            refexSpec.put(RefexProperty.CNID1, refConcept1.getConceptNid());
            refexSpec.put(RefexProperty.LONG1, 123456L);
            break;

        case LONG:
            refexSpec.put(RefexProperty.LONG1, 123456L);
            break;

        case ARRAY_BYTEARRAY:
            refexSpec.put(RefexProperty.ARRAY_BYTEARRAY, new byte[][] { "this is a byte array".getBytes() });
            break;

        case BOOLEAN:
            refexSpec.put(RefexProperty.BOOLEAN1, true);
            break;

        default:
        }
        RefexChronicleBI<?> refsetMember = ammender.constructIfNotCurrent(refexSpec);

        int expectedAnnotationsCount = 0;
        if (refsetConcept.isAnnotationStyleRefex()) {
            memberConcept.addAnnotation(refsetMember);
            Ts.get().addUncommitted(memberConcept);
            expectedAnnotationsCount = 1;
        } else {
            Ts.get().addUncommitted(refsetConcept);
            Ts.get().addUncommitted(memberConcept);
        }

        Collection<? extends RefexChronicleBI<?>> members = memberConcept.getRefexMembers(refsetConcept.getNid());
        assertEquals(1, members.size());
        RefexAnalogBI<?> refexAnalog = RefexAnalogBI.class.cast(members.iterator().next());

        performStandardChecks(refexAnalog, refsetConcept, memberConcept, refsetMember, expectedAnnotationsCount);
    }

    private static void performStandardChecks(RefexAnalogBI<?> refexAnalog, ConceptChronicleBI refsetConcept,
            ConceptChronicleBI memberConcept, RefexChronicleBI<?> refsetMember, int expectedAnnotationsCount)
            throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        exerciseRefexAnalogInterface(refexAnalog);
        exerciseRefsetConceptInterface(refsetConcept);
        exerciseMemberConceptInterface(memberConcept);

        assertTrue(refsetMember.getUUIDs().get(0).equals(refexAnalog.getUUIDs().get(0)));

        Set<Integer> nids = refexAnalog.getAllNidsForVersion();
        assertTrue(nids.contains(memberConcept.getNid()));
        assertTrue(nids.contains(refsetConcept.getNid()));
        assertTrue(nids.contains(refexAnalog.getNid()));
        assertTrue(nids.contains(Ts.get().getConcept(UUID.fromString(UUID_ACTIVE_VALUE)).getNid()));
        assertTrue(nids.contains(Ts.get().getConcept(UUID.fromString(UUID_USER)).getNid()));
        for (PathBI path : config.getEditingPathSet()) {
            assertTrue(nids.contains(path.getConceptNid()));
        }
        assertTrue(nids.contains(refexAnalog.getAuthorNid()));
        assertTrue(nids.contains(refexAnalog.getPathNid()));

        Collection<? extends RefexVersionBI> annotations = refexAnalog.getAnnotationsActive(config.getViewCoordinate());
        assertEquals(expectedAnnotationsCount, annotations.size());
        assertEquals(expectedAnnotationsCount, refexAnalog.getAnnotations().size());
        assertEquals(expectedAnnotationsCount,
            refexAnalog.getAnnotationMembersActive(config.getViewCoordinate(), refexAnalog.getNid()).size());

        ComponentChronicleBI<?> refexChronicle = refexAnalog.getChronicle();
        assertEquals(refexChronicle.getNid(), refexAnalog.getNid());
        assertEquals(refexChronicle.getPrimUuid(), refexAnalog.getUUIDs().get(0));
        assertEquals(1, refexChronicle.getVersions(config.getViewCoordinate()).size());

        assertTrue(refexAnalog.getEnclosingConcept().equals(refsetConcept));

        Collection<? extends RefexChronicleBI> members = refsetConcept.getRefsetMembers();
        assertEquals(1, members.size());
        assertTrue(members.iterator().next().equals(refsetMember));

        modifyRefsetMember(refsetMember);
    }

    private static void exerciseRefexAnalogInterface(RefexAnalogBI<?> refexAnalog) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        PositionBI pos = refexAnalog.getPosition();
        assertEquals(refexAnalog.getPathNid(), pos.getPath().getConceptNid());
        assertEquals(1, refexAnalog.getPositions().size());
        assertTrue(refexAnalog.getPositions().contains(pos));

        Collection<? extends IdBI> additionalIds = refexAnalog.getAdditionalIds();
        // TODO FIX assertTrue(additionalIds != null);

        Collection<? extends IdBI> allIds = refexAnalog.getAllIds();
        assertEquals(1, allIds.size());
        assertEquals(allIds.iterator().next().getDenotation(), refexAnalog.getUUIDs().get(0));

        Set<Integer> stampNids = refexAnalog.getAllStampNids();
        assertEquals(1, stampNids.size());
        assertTrue(stampNids.contains(refexAnalog.getStampNid()));

        assertTrue(refexAnalog.compareTo(RefexVersionBI.class.cast(refexAnalog.getVersion(config.getViewCoordinate()))) == 0);

        assertTrue(refexAnalog.getPrimordialVersion().compareTo(
            RefexVersionBI.class.cast(refexAnalog.getVersion(config.getViewCoordinate()))) == 0);

        assertEquals(0, refexAnalog.getRefexes().size());
        assertEquals(0, refexAnalog.getRefexesActive(config.getViewCoordinate()).size());
        assertEquals(0, refexAnalog.getRefexesInactive(config.getViewCoordinate()).size());
        assertEquals(0, refexAnalog.getRefexMembers(refexAnalog.getRefexNid()).size());
        assertEquals(0, refexAnalog.getRefexMembersActive(config.getViewCoordinate(), refexAnalog.getRefexNid()).size());
    }

    private static void exerciseRefsetConceptInterface(ConceptChronicleBI refsetConcept) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        Collection<? extends IdBI> additionalIds = refsetConcept.getAdditionalIds();
        // TODO FIX assertTrue(additionalIds != null);

        Collection<? extends IdBI> allIds = refsetConcept.getAllIds();
        assertEquals(1, allIds.size());
        assertEquals(allIds.iterator().next().getDenotation(), refsetConcept.getUUIDs().get(0));

        Set<Integer> stampNids = refsetConcept.getAllStampNids();
        assertEquals(1, stampNids.size());

        assertEquals(0, refsetConcept.getRefexes().size());
        assertEquals(0, refsetConcept.getRefexesActive(config.getViewCoordinate()).size());
        assertEquals(0, refsetConcept.getRefexesInactive(config.getViewCoordinate()).size());
        assertEquals(0, refsetConcept.getRefexMembers(refsetConcept.getNid()).size());
        assertEquals(0, refsetConcept.getRefexMembersActive(config.getViewCoordinate(), refsetConcept.getNid()).size());
    }

    private static void exerciseMemberConceptInterface(ConceptChronicleBI memberConcept) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        Collection<? extends IdBI> additionalIds = memberConcept.getAdditionalIds();
        // TODO FIX assertTrue(additionalIds != null);

        Collection<? extends IdBI> allIds = memberConcept.getAllIds();
        assertEquals(1, allIds.size());
        assertEquals(allIds.iterator().next().getDenotation(), memberConcept.getUUIDs().get(0));

        Set<Integer> stampNids = memberConcept.getAllStampNids();
        assertEquals(1, stampNids.size());

        assertEquals(1, memberConcept.getRefexes().size());
        assertEquals(1, memberConcept.getRefexesActive(config.getViewCoordinate()).size());
        assertEquals(0, memberConcept.getRefexesInactive(config.getViewCoordinate()).size());
    }

    private static void modifyRefsetMember(RefexChronicleBI refsetMember) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        CreateOrAmendBlueprint cab = refsetMember.getVersion(config.getViewCoordinate()).makeBlueprint(
            config.getViewCoordinate());
        if (!RefexCAB.class.isAssignableFrom(cab.getClass())) {
            assertTrue(false);
        }

        RefexCAB bp = RefexCAB.class.cast(cab);
        int authorityNid = refsetMember.getAllIds().iterator().next().getAuthorityNid();
        bp.addExtraUuid(UUID.randomUUID(), authorityNid);
        bp.addLongId(new Random().nextLong(), authorityNid);

        bp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                propertyChangeListenerFired = true;
            }
        });
        propertyChangeListenerFired = false;
        bp.setComponentUuid(UUID.randomUUID());
        assertTrue(propertyChangeListenerFired);

        bp.addStringId("string-id", authorityNid);

        TerminologyBuilderBI ammender = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
            config.getViewCoordinate());
        RefexChronicleBI modifiedRefex = ammender.construct(bp);

    }
}
