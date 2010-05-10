package org.dwfa.mojo.export;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.I_ConceptEnumeration;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;
import org.dwfa.vodb.process.ProcessQueue;
import org.easymock.EasyMock;

/**
 * Mocking code for export concepts tests.
 */
public class ConceptMockery {

    List<Object> interfaceMocks = new ArrayList<Object>();
    List<Object> classMocks = new ArrayList<Object>();

    DatabaseExport databaseExport = new DatabaseExport();
    static File dbDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "test-id-db");
    I_TermFactory termFactory;
    int sctSequence = 1;

    int exportPathNid = Integer.MAX_VALUE;
    int activeStatusNid = Integer.MAX_VALUE - 1;
    int snomedIntNid = Integer.MAX_VALUE - 2;
    int snomedT3UuidNid = Integer.MAX_VALUE - 3;
    int fullySpecifiedNameTypeNid = Integer.MAX_VALUE - 4;
    int preferredDescriptionTypeNid = Integer.MAX_VALUE - 5;
    int definingCharacteristicNid = Integer.MAX_VALUE - 6;
    int optionalRefinabilityNid = Integer.MAX_VALUE - 7;
    int isANid = Integer.MAX_VALUE - 8;
    int ctv3Nid = Integer.MAX_VALUE - 9;
    int conceptExtensionNid = Integer.MAX_VALUE - 10;
    int conceptStringExtensionNid = Integer.MAX_VALUE - 11;
    int conceptIntExtensionNid = Integer.MAX_VALUE - 12;
    int conceptConceptExtensionNid = Integer.MAX_VALUE - 13;
    int conceptConceptStringExtensionNid = Integer.MAX_VALUE - 14;
    int conceptConceptConceptExtensionNid = Integer.MAX_VALUE - 15;
    int snomedCoreNid = Integer.MAX_VALUE - 16;
    int snomedRtNid = Integer.MAX_VALUE - 17;
    int currentStatusNid = Integer.MAX_VALUE - 18;
    int ctv3MapNid = Integer.MAX_VALUE - 19;
    int snomedIdMapNid = Integer.MAX_VALUE - 20;
    int inActiveStatusNid = Integer.MAX_VALUE - 21;
    int duplicateStatusNId = Integer.MAX_VALUE - 22;
    int ambiguousStatusNId = Integer.MAX_VALUE - 23;
    int erroneousStatusNId = Integer.MAX_VALUE - 24;
    int outdatedStatusNId = Integer.MAX_VALUE - 25;
    int aceDuplicateStatusNId = Integer.MAX_VALUE - 26;
    int aceAmbiguousStatusNId = Integer.MAX_VALUE - 27;
    int aceErroneousStatusNId = Integer.MAX_VALUE - 28;
    int aceOutdatedStatusNId = Integer.MAX_VALUE - 29;
    int inappropriateStatusNId = Integer.MAX_VALUE - 30;
    int movedElsewhereStatusNId = Integer.MAX_VALUE - 31;
    int aceInappropriateStatusNId = Integer.MAX_VALUE - 32;
    int aceMovedElsewhereStatusNId = Integer.MAX_VALUE - 33;
    int descriptionInactivationIndicatorNid = Integer.MAX_VALUE - 34;
    int relationshipInactivationIndicatorNid = Integer.MAX_VALUE - 35;
    int conceptInactivationIndicatorNid = Integer.MAX_VALUE - 36;
    int movedFromHistoryNid = Integer.MAX_VALUE - 37;
    int movedFromHistoryRefsetNid = Integer.MAX_VALUE - 38;
    int movedToHistoryNid = Integer.MAX_VALUE - 39;
    int movedToHistoryRefsetNid = Integer.MAX_VALUE - 4;
    int replacedByHistoryNid = Integer.MAX_VALUE - 41;
    int replacedByHistoryRefsetNid = Integer.MAX_VALUE - 42;
    int sameAsHistoryNid = Integer.MAX_VALUE - 43;
    int sameAsHistoryRefsetNid = Integer.MAX_VALUE - 44;
    int wasAHistoryNid = Integer.MAX_VALUE - 45;
    int wasAHistoryRefsetNid = Integer.MAX_VALUE - 46;
    int initialCharacterNotCaseSensitiveNid = Integer.MAX_VALUE - 47;
    int allCharactersCaseSensitiveNid = Integer.MAX_VALUE - 48;
    int unspecifiedDescriptionTypeNid = Integer.MAX_VALUE - 49;
    int synonymDescriptionTypeNid = Integer.MAX_VALUE - 50;
    int statedRelationshipNid = Integer.MAX_VALUE - 51;
    int qualifierCharacteristicNid = Integer.MAX_VALUE - 52;
    int historicalCharacteristicNid = Integer.MAX_VALUE - 53;
    int additionalCharacteristicNid = Integer.MAX_VALUE - 54;
    int notRefinableNid = Integer.MAX_VALUE - 55;
    int mandatoryRefinabilityNid = Integer.MAX_VALUE - 56;
    int enNid = Integer.MAX_VALUE - 57;
    int enUsNid = Integer.MAX_VALUE - 58;
    int sourceUuidNid = Integer.MAX_VALUE - 59;
    int relationshipRefinabilityExtensionNid = Integer.MAX_VALUE - 60;
    int stringExtensionNid = Integer.MAX_VALUE - 61;
    int promotesToNid = Integer.MAX_VALUE - 62;
    int activeValueNid = Integer.MAX_VALUE - 63;
    int limitedStatusNId = Integer.MAX_VALUE - 64;
    int aceLimitedStatusNId = Integer.MAX_VALUE - 65;
    int retiredStatusNid = Integer.MAX_VALUE - 66;
    int rf2AcceptableDescriptionTypeNid = Integer.MAX_VALUE - 67;
    int rf2PreferredDescriptionTypeNid = Integer.MAX_VALUE - 68;
    int snomedIsANId = Integer.MAX_VALUE - 69;
    int conceptRetiredStatusNid = Integer.MAX_VALUE - 70;
    int pendingMoveStatusNid = Integer.MAX_VALUE - 71;

    I_GetConceptData activeConceptData;
    I_GetConceptData snomedIntIdConceptData;
    I_GetConceptData snomedT3UuidConceptData;
    I_GetConceptData fullySpecifiedDescriptionConceptData;
    I_GetConceptData preferredDescriptionTypeConceptData;
    I_GetConceptData snomedCoreConcept;
    I_GetConceptData snomedRtIdConcept;
    I_GetConceptData currentConceptData;
    I_GetConceptData snomedIdMapExtensionConceptData;
    I_GetConceptData ctv3IdMapExtensionConceptData;
    I_GetConceptData incluesionRootConceptData;
    I_GetConceptData exclusionsRootConceptData;

    List<UUID> snomedIsAUuuidList = new ArrayList<UUID>();
    List<UUID> snomedIntIdUuidList;
    List<UUID> activeUuidList;
    List<UUID> fullySpecifiedDescriptionTypeUuidList;
    List<UUID> preferredDescriptionTypeUuidList;
    List<UUID> snomedCoreUuidList;
    List<UUID> snomedRtIdUuidList;

    // setup fsn int type used to get the concepts FSN
    I_IntSet fsnIIntSet = createMock(I_IntSet.class);
    private ArrayList<UUID> inActiveUuidList;
    private I_GetConceptData inActiveConceptData;
    private ArrayList<UUID> currentUuidList;
    private I_GetConceptData sourceUuidConceptData;

    /**
     * Sets up the meta data concepts and the term factory
     *
     * @throws Exception
     */
    public ConceptMockery() throws Exception {
        System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
        System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, "jdbc:derby:directory:" + dbDirectory.getCanonicalPath() + ";create=true;");
        UuidSctidMapDb.getInstance().openDb();

        snomedIsAUuuidList.add(ConceptConstants.SNOMED_IS_A.getUuids()[0]);

        // mock the LocalVersionedTerminology
        termFactory = createMock(I_TermFactory.class);
        Field factoryField = LocalVersionedTerminology.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, termFactory);

        databaseExport.setTermFactory(termFactory);

        expect(termFactory.newProcessQueue(1)).andReturn(new ProcessQueue(1));

        //mock in inclusion and exclusion roots
        incluesionRootConceptData = createMock(I_GetConceptData.class);
        exclusionsRootConceptData = createMock(I_GetConceptData.class);

        // mock export specification constants
        activeUuidList = new ArrayList<UUID>();
        activeUuidList.add(UUID.randomUUID());
        activeConceptData = mockConceptEnum(activeUuidList, ArchitectonicAuxiliary.Concept.ACTIVE, activeStatusNid);
        expect(activeConceptData.isParentOf(activeConceptData, null, null, null, false)).andReturn(false).anyTimes();
        expect(termFactory.getConcept(activeStatusNid)).andReturn(activeConceptData).anyTimes();

        inActiveUuidList = new ArrayList<UUID>();
        inActiveUuidList.add(UUID.randomUUID());
        inActiveConceptData = mockConceptEnum(inActiveUuidList, ArchitectonicAuxiliary.Concept.INACTIVE,
            inActiveStatusNid);
        replay(inActiveConceptData);

        currentUuidList = new ArrayList<UUID>();
        currentUuidList.add(UUID.randomUUID());
        currentConceptData = mockConceptEnum(currentUuidList, ArchitectonicAuxiliary.Concept.CURRENT, currentStatusNid);
        expect(activeConceptData.isParentOf(currentConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(termFactory.getConcept(currentStatusNid)).andReturn(currentConceptData).anyTimes();
        replay(currentConceptData);

        List<UUID> retiredUuidList = new ArrayList<UUID>();
        retiredUuidList.add(UUID.randomUUID());
        I_GetConceptData retiredConceptData = mockConceptEnum(retiredUuidList, ArchitectonicAuxiliary.Concept.RETIRED,
            retiredStatusNid);
        expect(retiredConceptData.getConceptId()).andReturn(retiredStatusNid).anyTimes();
        replay(retiredConceptData);

        List<UUID> conceptRetiredUuidList = new ArrayList<UUID>();
        conceptRetiredUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptRetiredConceptData = mockConceptEnum(retiredUuidList, ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED,
            conceptRetiredStatusNid);
        replay(conceptRetiredConceptData);

        List<UUID> pendingMoveUuidList = new ArrayList<UUID>();
        pendingMoveUuidList.add(UUID.randomUUID());
        I_GetConceptData pendingMoveConceptData = mockConceptEnum(retiredUuidList, ArchitectonicAuxiliary.Concept.PENDING_MOVE,
            pendingMoveStatusNid);
        replay(pendingMoveConceptData);

        snomedCoreUuidList = new ArrayList<UUID>();
        snomedCoreUuidList.add(UUID.randomUUID());
        snomedCoreConcept = mockConceptEnum(snomedCoreUuidList, ArchitectonicAuxiliary.Concept.SNOMED_CORE,
            snomedCoreNid);

        snomedRtIdUuidList = new ArrayList<UUID>();
        snomedRtIdUuidList.add(UUID.randomUUID());
        snomedRtIdConcept = mockConceptEnum(snomedRtIdUuidList, ArchitectonicAuxiliary.Concept.SNOMED_RT_ID,
            snomedRtNid);
        expect(snomedRtIdConcept.getConceptId()).andReturn(snomedRtNid).anyTimes();
        replay(snomedRtIdConcept);

        snomedIntIdUuidList = new ArrayList<UUID>();
        snomedIntIdUuidList.add(UUID.randomUUID());
        snomedIntIdConceptData = mockConceptEnum(snomedIntIdUuidList, ArchitectonicAuxiliary.Concept.SNOMED_INT_ID,
            snomedIntNid);
        expect(snomedIntIdConceptData.getConceptId()).andReturn(snomedIntNid).anyTimes();
        replay(snomedIntIdConceptData);

        List<UUID> snomedT3UuidUuidList = new ArrayList<UUID>();
        snomedT3UuidUuidList.add(UUID.randomUUID());
        snomedT3UuidConceptData = mockConceptEnum(snomedT3UuidUuidList, ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID,
            snomedT3UuidNid);
        expect(snomedT3UuidConceptData.getConceptId()).andReturn(snomedT3UuidNid).anyTimes();
        replay(snomedT3UuidConceptData);

        List<UUID> sourceUuidUuidList = new ArrayList<UUID>();
        sourceUuidUuidList.add(UUID.randomUUID());
        sourceUuidConceptData = mockConceptEnum(sourceUuidUuidList, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID,
            sourceUuidNid);
        expect(sourceUuidConceptData.getConceptId()).andReturn(sourceUuidNid).anyTimes();
        replay(sourceUuidConceptData);

        List<UUID> ctv3IdUuidList = new ArrayList<UUID>();
        ctv3IdUuidList.add(UUID.randomUUID());
        I_GetConceptData ctv3IdconceptData = mockConceptEnum(ctv3IdUuidList, ArchitectonicAuxiliary.Concept.CTV3_ID,
            ctv3Nid);
        expect(ctv3IdconceptData.getConceptId()).andReturn(ctv3Nid).anyTimes();
        replay(ctv3IdconceptData);

        List<UUID> isANidUuidList = new ArrayList<UUID>();
        isANidUuidList.add(UUID.randomUUID());
        I_GetConceptData isAConceptData = mockConceptEnum(isANidUuidList, ArchitectonicAuxiliary.Concept.IS_A_REL,
            isANid);
        expect(isAConceptData.getConceptId()).andReturn(isANid).anyTimes();
        replay(isAConceptData);

        List<UUID> activeValueUuidList = new ArrayList<UUID>();
        activeValueUuidList.add(UUID.randomUUID());
        I_GetConceptData activeValueConceptData = mockConceptSpec(ConceptConstants.ACTIVE_VALUE, activeValueNid);
        expect(termFactory.getConcept(activeValueNid)).andReturn(activeValueConceptData).anyTimes();
        replay(activeValueConceptData);

        List<UUID> ctv3IdMapExtensionUuidList = new ArrayList<UUID>();
        ctv3IdMapExtensionUuidList.add(UUID.randomUUID());
        ctv3IdMapExtensionConceptData = mockConceptSpec(ConceptConstants.CTV3_ID_MAP_EXTENSION, ctv3MapNid);
        expect(termFactory.getConcept(ctv3MapNid)).andReturn(ctv3IdMapExtensionConceptData).anyTimes();
        expect(ctv3IdMapExtensionConceptData.getConceptId()).andReturn(ctv3MapNid).anyTimes();
        expect(ctv3IdMapExtensionConceptData.getInitialText()).andReturn("CTV3Map").anyTimes();
        replay(ctv3IdMapExtensionConceptData);

        List<UUID> snomedRtIdExtensionUuidList = new ArrayList<UUID>();
        snomedRtIdExtensionUuidList.add(UUID.randomUUID());
        snomedIdMapExtensionConceptData = mockConceptSpec(ConceptConstants.SNOMED_ID_MAP_EXTENSION, snomedIdMapNid);
        expect(termFactory.getConcept(snomedIdMapNid)).andReturn(snomedIdMapExtensionConceptData).anyTimes();
        expect(snomedIdMapExtensionConceptData.getConceptId()).andReturn(snomedIdMapNid).anyTimes();
        expect(snomedIdMapExtensionConceptData.getInitialText()).andReturn("SNOMED_RT_IDMap").anyTimes();
        replay(snomedIdMapExtensionConceptData);

        I_GetConceptData duplicateStatusConceptData = mockConceptSpec(ConceptConstants.DUPLICATE_STATUS, duplicateStatusNId);
        expect(termFactory.getConcept(duplicateStatusNId)).andReturn(duplicateStatusConceptData);
        replay(duplicateStatusConceptData);

        I_GetConceptData ambiguousConceptData = mockConceptSpec(ConceptConstants.AMBIGUOUS_STATUS, ambiguousStatusNId);
        expect(termFactory.getConcept(ambiguousStatusNId)).andReturn(ambiguousConceptData);
        replay(ambiguousConceptData);

        I_GetConceptData erroneousStatusConceptData = mockConceptSpec(ConceptConstants.ERRONEOUS_STATUS, erroneousStatusNId);
        expect(termFactory.getConcept(erroneousStatusNId)).andReturn(erroneousStatusConceptData);
        replay(erroneousStatusConceptData);

        I_GetConceptData outdatedStatusConceptData = mockConceptSpec(ConceptConstants.OUTDATED_STATUS, outdatedStatusNId);
        expect(termFactory.getConcept(outdatedStatusNId)).andReturn(outdatedStatusConceptData);
        replay(outdatedStatusConceptData);

        I_GetConceptData limitedConceptData = mockConceptSpec(ConceptConstants.LIMITED, limitedStatusNId);
        expect(termFactory.getConcept(limitedStatusNId)).andReturn(limitedConceptData);
        replay(limitedConceptData);

        I_GetConceptData inappropriateStatusConceptData = mockConceptSpec(ConceptConstants.INAPPROPRIATE_STATUS, inappropriateStatusNId);
        expect(termFactory.getConcept(inappropriateStatusNId)).andReturn(inappropriateStatusConceptData);
        replay(inappropriateStatusConceptData);

        I_GetConceptData movedElsewhereStatusConceptData = mockConceptSpec(ConceptConstants.MOVED_ELSEWHERE_STATUS, movedElsewhereStatusNId);
        expect(termFactory.getConcept(movedElsewhereStatusNId)).andReturn(movedElsewhereStatusConceptData);
        replay(movedElsewhereStatusConceptData);

        I_GetConceptData descriptionInactivationIndicatorConceptData = mockConceptSpec(ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR, descriptionInactivationIndicatorNid);
        expect(termFactory.getConcept(descriptionInactivationIndicatorNid)).andReturn(descriptionInactivationIndicatorConceptData).anyTimes();
        expect(descriptionInactivationIndicatorConceptData.getInitialText()).andReturn("description_inactivation").anyTimes();
        expect(incluesionRootConceptData.isParentOf(descriptionInactivationIndicatorConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(descriptionInactivationIndicatorConceptData, null, null, null, false)).andReturn(false).anyTimes();
        replay(descriptionInactivationIndicatorConceptData);

        I_GetConceptData relationshipInactivationIndicatorConceptData = mockConceptSpec(ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR, relationshipInactivationIndicatorNid);
        expect(termFactory.getConcept(relationshipInactivationIndicatorNid)).andReturn(relationshipInactivationIndicatorConceptData).anyTimes();
        expect(relationshipInactivationIndicatorConceptData.getInitialText()).andReturn("relationship_inactivation").anyTimes();
        expect(incluesionRootConceptData.isParentOf(relationshipInactivationIndicatorConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(relationshipInactivationIndicatorConceptData, null, null, null, false)).andReturn(false).anyTimes();
        replay(relationshipInactivationIndicatorConceptData);

        I_GetConceptData conceptInactivationIndicatorConceptData = mockConceptSpec(ConceptConstants.CONCEPT_INACTIVATION_INDICATOR, conceptInactivationIndicatorNid);
        expect(termFactory.getConcept(conceptInactivationIndicatorNid)).andReturn(conceptInactivationIndicatorConceptData).anyTimes();
        expect(conceptInactivationIndicatorConceptData.getInitialText()).andReturn("concept_inactivation").anyTimes();
        expect(incluesionRootConceptData.isParentOf(conceptInactivationIndicatorConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(conceptInactivationIndicatorConceptData, null, null, null, false)).andReturn(false).anyTimes();
        replay(conceptInactivationIndicatorConceptData);

        I_GetConceptData movedFromHistoryConceptData = mockConceptSpec(ConceptConstants.MOVED_FROM_HISTORY, movedFromHistoryNid);
        expect(termFactory.getConcept(movedFromHistoryNid)).andReturn(movedFromHistoryConceptData);
        replay(movedFromHistoryConceptData);

        I_GetConceptData movedFromHistoryRefsetConceptData = mockConceptSpec(ConceptConstants.MOVED_FROM_HISTORY_REFSET, movedFromHistoryRefsetNid);
        expect(termFactory.getConcept(movedFromHistoryRefsetNid)).andReturn(movedFromHistoryRefsetConceptData);
        expect(movedFromHistoryRefsetConceptData.getInitialText()).andReturn("MOVED_FROM_HISTORY").anyTimes();
        replay(movedFromHistoryRefsetConceptData);

        I_GetConceptData movedToHistoryConceptData = mockConceptSpec(ConceptConstants.MOVED_TO_HISTORY, movedToHistoryNid);
        expect(termFactory.getConcept(movedToHistoryNid)).andReturn(movedToHistoryConceptData);
        replay(movedToHistoryConceptData);

        I_GetConceptData movedToHistoryRefsetConceptData = mockConceptSpec(ConceptConstants.MOVED_TO_HISTORY_REFSET, movedToHistoryRefsetNid);
        expect(termFactory.getConcept(movedToHistoryRefsetNid)).andReturn(movedToHistoryRefsetConceptData);
        expect(movedToHistoryRefsetConceptData.getInitialText()).andReturn("MOVED_TO_HISTORY").anyTimes();
        replay(movedToHistoryRefsetConceptData);

        I_GetConceptData replacedByHistoryConceptData = mockConceptSpec(ConceptConstants.REPLACED_BY_HISTORY, replacedByHistoryNid);
        expect(termFactory.getConcept(replacedByHistoryNid)).andReturn(replacedByHistoryConceptData);
        replay(replacedByHistoryConceptData);

        I_GetConceptData replacedByHistoryRefsetConceptData = mockConceptSpec(ConceptConstants.REPLACED_BY_HISTORY_REFSET, replacedByHistoryRefsetNid);
        expect(termFactory.getConcept(replacedByHistoryRefsetNid)).andReturn(replacedByHistoryRefsetConceptData);
        expect(replacedByHistoryRefsetConceptData.getInitialText()).andReturn("REPLACED_BY_HISTORY").anyTimes();
        replay(replacedByHistoryRefsetConceptData);

        I_GetConceptData sameAsHistoryConceptData = mockConceptSpec(ConceptConstants.SAME_AS_HISTORY, sameAsHistoryNid);
        expect(termFactory.getConcept(sameAsHistoryNid)).andReturn(sameAsHistoryConceptData);
        replay(sameAsHistoryConceptData);

        I_GetConceptData sameAsHistoryRefsetConceptData = mockConceptSpec(ConceptConstants.SAME_AS_HISTORY_REFSET, sameAsHistoryRefsetNid);
        expect(termFactory.getConcept(sameAsHistoryRefsetNid)).andReturn(sameAsHistoryRefsetConceptData);
        expect(sameAsHistoryRefsetConceptData.getInitialText()).andReturn("SAME_AS_HISTORY").anyTimes();
        replay(sameAsHistoryRefsetConceptData);

        I_GetConceptData wasAHistoryConceptData = mockConceptSpec(ConceptConstants.WAS_A_HISTORY, wasAHistoryNid);
        expect(termFactory.getConcept(wasAHistoryNid)).andReturn(wasAHistoryConceptData);
        replay(wasAHistoryConceptData);

        I_GetConceptData rf2AcceptableDescriptionTypeConceptData = mockConceptSpec(org.dwfa.ace.refset.ConceptConstants.ACCEPTABLE, rf2AcceptableDescriptionTypeNid);
        expect(termFactory.getConcept(rf2AcceptableDescriptionTypeNid)).andReturn(rf2AcceptableDescriptionTypeConceptData);
        replay(rf2AcceptableDescriptionTypeConceptData);

        I_GetConceptData rf2PreferredDescriptionTypeConceptData = mockConceptSpec(org.dwfa.ace.refset.ConceptConstants.PREFERRED, rf2PreferredDescriptionTypeNid);
        expect(termFactory.getConcept(rf2PreferredDescriptionTypeNid)).andReturn(rf2PreferredDescriptionTypeConceptData);
        replay(rf2PreferredDescriptionTypeConceptData);

        I_GetConceptData snomedIsAConceptData = mockConceptSpec(org.dwfa.ace.refset.ConceptConstants.SNOMED_IS_A, snomedIsANId);
        expect(termFactory.getConcept(snomedIsANId)).andReturn(snomedIsAConceptData);
        expect(snomedIsAConceptData.getConceptId()).andReturn(snomedIsANId);
        replay(snomedIsAConceptData);

        I_GetConceptData wasAHistoryRefsetConceptData = mockConceptSpec(ConceptConstants.WAS_A_HISTORY_REFSET, wasAHistoryRefsetNid);
        expect(termFactory.getConcept(wasAHistoryRefsetNid)).andReturn(wasAHistoryRefsetConceptData).anyTimes();
        expect(incluesionRootConceptData.isParentOf(wasAHistoryRefsetConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(wasAHistoryRefsetConceptData, null, null, null, false)).andReturn(false).anyTimes();
        expect(wasAHistoryRefsetConceptData.getInitialText()).andReturn("WAS_A_HISTORY").anyTimes();
        replay(wasAHistoryRefsetConceptData);

        I_GetConceptData relationshipRefinabilityExtensionConceptData = mockConceptSpec(ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION,
            relationshipRefinabilityExtensionNid);
        expect(termFactory.getConcept(relationshipRefinabilityExtensionNid)).andReturn(relationshipRefinabilityExtensionConceptData).anyTimes();
        expect(incluesionRootConceptData.isParentOf(relationshipRefinabilityExtensionConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(relationshipRefinabilityExtensionConceptData, null, null, null, false)).andReturn(false).anyTimes();
        expect(relationshipRefinabilityExtensionConceptData.getInitialText()).andReturn("RELATIONSHIP_REFINABILITY_EXTENSION").anyTimes();
        replay(relationshipRefinabilityExtensionConceptData);

        I_GetConceptData promotesToConceptData = mockConceptSpec(org.dwfa.ace.refset.ConceptConstants.PROMOTES_TO,
            promotesToNid);
        expect(termFactory.getConcept(promotesToNid)).andReturn(promotesToConceptData).anyTimes();
        expect(incluesionRootConceptData.isParentOf(promotesToConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(exclusionsRootConceptData.isParentOf(promotesToConceptData, null, null, null, false)).andReturn(false).anyTimes();
        expect(promotesToConceptData.getInitialText()).andReturn("PROMOTES_TO").anyTimes();
        replay(promotesToConceptData);


        List<UUID> duplicateUuidList = new ArrayList<UUID>();
        duplicateUuidList.add(UUID.randomUUID());
        I_GetConceptData duplicateConceptData = mockConceptEnum(duplicateUuidList, ArchitectonicAuxiliary.Concept.DUPLICATE,
            aceDuplicateStatusNId);
        replay(duplicateConceptData);

        List<UUID> ambiguousUuidList = new ArrayList<UUID>();
        ambiguousUuidList.add(UUID.randomUUID());
        I_GetConceptData aceAmbiguousConceptData = mockConceptEnum(ambiguousUuidList, ArchitectonicAuxiliary.Concept.AMBIGUOUS,
            aceAmbiguousStatusNId);
        expect(activeConceptData.isParentOf(aceAmbiguousConceptData, null, null, null, false)).andReturn(true).anyTimes();
        expect(termFactory.getConcept(aceAmbiguousStatusNId)).andReturn(aceAmbiguousConceptData).anyTimes();
        replay(aceAmbiguousConceptData);

        List<UUID> erroneousUuidList = new ArrayList<UUID>();
        erroneousUuidList.add(UUID.randomUUID());
        I_GetConceptData erroneousConceptData = mockConceptEnum(erroneousUuidList, ArchitectonicAuxiliary.Concept.ERRONEOUS,
            aceErroneousStatusNId);
        replay(erroneousConceptData);

        List<UUID> outdatedUuidList = new ArrayList<UUID>();
        outdatedUuidList.add(UUID.randomUUID());
        I_GetConceptData outdatedConceptData = mockConceptEnum(outdatedUuidList, ArchitectonicAuxiliary.Concept.OUTDATED,
            aceOutdatedStatusNId);
        replay(outdatedConceptData);

        List<UUID> inappropriateUuidList = new ArrayList<UUID>();
        inappropriateUuidList.add(UUID.randomUUID());
        I_GetConceptData inappropriateConceptData = mockConceptEnum(inappropriateUuidList, ArchitectonicAuxiliary.Concept.INAPPROPRIATE,
            aceInappropriateStatusNId);
        replay(inappropriateConceptData);

        List<UUID> movedElsewhereUuidList = new ArrayList<UUID>();
        movedElsewhereUuidList.add(UUID.randomUUID());
        I_GetConceptData movedElsewhereConceptData = mockConceptEnum(movedElsewhereUuidList, ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE,
            aceMovedElsewhereStatusNId);
        replay(movedElsewhereConceptData);

        List<UUID> aceLimitedUuidList = new ArrayList<UUID>();
        aceLimitedUuidList.add(UUID.randomUUID());
        I_GetConceptData aceLimitedConceptData = mockConceptEnum(movedElsewhereUuidList, ArchitectonicAuxiliary.Concept.LIMITED,
            aceLimitedStatusNId);
        replay(aceLimitedConceptData);

        List<UUID> initialCharacterNotCaseSensitiveUuidList = new ArrayList<UUID>();
        initialCharacterNotCaseSensitiveUuidList.add(UUID.randomUUID());
        I_GetConceptData initialCharacterNotCaseSensitiveConceptData = mockConceptEnum(
            initialCharacterNotCaseSensitiveUuidList,
            ArchitectonicAuxiliary.Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE, initialCharacterNotCaseSensitiveNid);
        replay(initialCharacterNotCaseSensitiveConceptData);

        List<UUID> allCharactersCaseSensitiveUuidList = new ArrayList<UUID>();
        allCharactersCaseSensitiveUuidList.add(UUID.randomUUID());
        I_GetConceptData allCharactersCaseSensitiveConceptData = mockConceptEnum(allCharactersCaseSensitiveUuidList,
            ArchitectonicAuxiliary.Concept.ALL_CHARACTERS_CASE_SENSITIVE, allCharactersCaseSensitiveNid);
        replay(allCharactersCaseSensitiveConceptData);

        fullySpecifiedDescriptionTypeUuidList = new ArrayList<UUID>();
        fullySpecifiedDescriptionTypeUuidList.add(UUID.randomUUID());
        fullySpecifiedDescriptionConceptData = mockConceptEnum(fullySpecifiedDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE, Integer.MAX_VALUE);
        expect(fullySpecifiedDescriptionConceptData.getConceptId()).andReturn(fullySpecifiedNameTypeNid).anyTimes();
        expect(termFactory.getConcept(fullySpecifiedNameTypeNid)).andReturn(fullySpecifiedDescriptionConceptData).anyTimes();;
        replay(fullySpecifiedDescriptionConceptData);

        List<UUID> unspecifiedDescriptionTypeUuidList = new ArrayList<UUID>();
        unspecifiedDescriptionTypeUuidList.add(UUID.randomUUID());
        I_GetConceptData unspecifiedDescriptionTypeConceptData = mockConceptEnum(unspecifiedDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE, unspecifiedDescriptionTypeNid);
        replay(unspecifiedDescriptionTypeConceptData);

        preferredDescriptionTypeUuidList = new ArrayList<UUID>();
        preferredDescriptionTypeUuidList.add(UUID.randomUUID());
        preferredDescriptionTypeConceptData = mockConceptEnum(preferredDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE, preferredDescriptionTypeNid);
        expect(termFactory.getConcept(preferredDescriptionTypeNid)).andReturn(preferredDescriptionTypeConceptData).anyTimes();
        replay(preferredDescriptionTypeConceptData);

        List<UUID> synonymDescriptionTypeUuidList = new ArrayList<UUID>();
        synonymDescriptionTypeUuidList.add(UUID.randomUUID());
        I_GetConceptData synonymDescriptionTypeConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE, synonymDescriptionTypeNid);
        replay(synonymDescriptionTypeConceptData);

        List<UUID>definingCharacteristicUuidList = new ArrayList<UUID>();
        definingCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData definingCharacteristicConceptData = mockConceptEnum(definingCharacteristicUuidList,
            ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC, definingCharacteristicNid);
        replay(definingCharacteristicConceptData);

        List<UUID> statedRelationshipUuidList = new ArrayList<UUID>();
        statedRelationshipUuidList.add(UUID.randomUUID());
        I_GetConceptData statedRelationshipConceptData = mockConceptEnum(statedRelationshipUuidList,
            ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP, statedRelationshipNid);
        replay(statedRelationshipConceptData);

        List<UUID> qualifierCharacteristicUuidList = new ArrayList<UUID>();
        qualifierCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData qualifierCharacteristicConceptData = mockConceptEnum(qualifierCharacteristicUuidList,
            ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC, qualifierCharacteristicNid);
        replay(qualifierCharacteristicConceptData);

        List<UUID> historicalCharacteristicUuidList = new ArrayList<UUID>();
        historicalCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData historicalCharacteristicConceptData = mockConceptEnum(historicalCharacteristicUuidList,
            ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC, historicalCharacteristicNid);
        replay(historicalCharacteristicConceptData);

        List<UUID> additionalCharacteristicUuidList = new ArrayList<UUID>();
        additionalCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData additionalCharacteristicConceptData = mockConceptEnum(additionalCharacteristicUuidList,
            ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC, additionalCharacteristicNid);
        replay(additionalCharacteristicConceptData);

        List<UUID> notRefinableUuidList = new ArrayList<UUID>();
        notRefinableUuidList.add(UUID.randomUUID());
        I_GetConceptData notRefinableConceptData = mockConceptEnum(notRefinableUuidList,
            ArchitectonicAuxiliary.Concept.NOT_REFINABLE, notRefinableNid);
        replay(notRefinableConceptData);

        List<UUID> optionalRefinabilityUuidList = new ArrayList<UUID>();
        optionalRefinabilityUuidList.add(UUID.randomUUID());
        I_GetConceptData optionalRefinabilityConceptData = mockConceptEnum(optionalRefinabilityUuidList,
            ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY, optionalRefinabilityNid);
        replay(optionalRefinabilityConceptData);

        List<UUID> mandatoryRefinabilityUuidList = new ArrayList<UUID>();
        mandatoryRefinabilityUuidList.add(UUID.randomUUID());
        I_GetConceptData mandatoryRefinabilityConceptData = mockConceptEnum(mandatoryRefinabilityUuidList,
            ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY, mandatoryRefinabilityNid);
        replay(mandatoryRefinabilityConceptData);

        List<UUID> enUuidList = new ArrayList<UUID>();
        enUuidList.add(UUID.randomUUID());
        I_GetConceptData enConceptData = mockConceptEnum(enUuidList,
            ArchitectonicAuxiliary.Concept.EN, enNid);
        replay(enConceptData);

        List<UUID> enUsUuidList = new ArrayList<UUID>();
        enUsUuidList.add(UUID.randomUUID());
        I_GetConceptData enUsConceptData = mockConceptEnum(enUsUuidList,
            ArchitectonicAuxiliary.Concept.EN_US, enUsNid);
        replay(enUsConceptData);

        List<UUID> stringExtensionUuidList = new ArrayList<UUID>();
        stringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData stringExtensionConceptData = mockConceptEnum(stringExtensionUuidList,
            RefsetAuxiliary.Concept.STRING_EXTENSION, stringExtensionNid);
        replay(stringExtensionConceptData);

        List<UUID> conceptExtensionUuidList = new ArrayList<UUID>();
        conceptExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptExtensionConceptData = mockConceptEnum(conceptExtensionUuidList,
            RefsetAuxiliary.Concept.CONCEPT_EXTENSION, conceptExtensionNid);
        replay(conceptExtensionConceptData);

        List<UUID> conceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptStringExtensionConceptData = mockConceptEnum(conceptStringExtensionUuidList,
            RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION, conceptStringExtensionNid);
        replay(conceptStringExtensionConceptData);

        List<UUID> conceptIntExtensionUuidList = new ArrayList<UUID>();
        conceptIntExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptIntExtensionConceptData = mockConceptEnum(conceptIntExtensionUuidList,
            RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION, conceptIntExtensionNid);
        replay(conceptIntExtensionConceptData);

        List<UUID> conceptConceptExtensionUuidList = new ArrayList<UUID>();
        conceptConceptExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptExtensionConceptData = mockConceptEnum(conceptConceptExtensionUuidList,
            RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION, conceptConceptExtensionNid);
        replay(conceptConceptExtensionConceptData);

        List<UUID> conceptConceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptConceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptStringExtensionConceptData = mockConceptEnum(conceptConceptStringExtensionUuidList,
            RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION, conceptConceptStringExtensionNid);
        replay(conceptConceptStringExtensionConceptData);

        List<UUID> conceptConceptConceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptConceptConceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptConceptStringExtensionConceptData = mockConceptEnum(
            conceptConceptConceptStringExtensionUuidList, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION,
            conceptConceptConceptExtensionNid);
        replay(conceptConceptConceptStringExtensionConceptData);

        replay(activeConceptData);
    }

    /**
     * Mocks the components id version and tuples for both sctid and uuid
     *
     * @param tuple
     * @param version
     * @param date
     * @param idParts
     * @param idVersioned
     * @param sctIdPart
     * @param uuidIdPart
     * @param componentUuidList
     * @param type
     * @throws TerminologyException
     * @throws IOException
     */
    public Long mockConceptId(I_AmPart tuple, int version, Date date, List<I_IdPart> idParts,
            I_IdVersioned idVersioned, I_IdPart sctIdPart, I_IdPart uuidIdPart, int sourceUuidNid, List<UUID> componentUuidList, TYPE type)
            throws TerminologyException, IOException {
        Long sctId = setNameSpaceId(idParts, sctIdPart, type, version);

        expect(idVersioned.getVersions()).andReturn(idParts).anyTimes();
        expect(tuple.getVersion()).andReturn(version).anyTimes();
        expect(tuple.getPathId()).andReturn(exportPathNid);

        expect(uuidIdPart.getSource()).andReturn(sourceUuidNid).anyTimes();
        expect(uuidIdPart.getVersion()).andReturn(version).anyTimes();
        expect(uuidIdPart.getSourceId()).andReturn(componentUuidList.get(0).toString()).anyTimes();
        expect(tuple.getTime()).andReturn(date.getTime());
        idParts.add(uuidIdPart);

        return sctId;
    }

    /**
     * Mock a SNOMED RT Id for the concept
     *
     * @param exportIdVersioned exportIdVersioned
     * @param conceptIdParts List<I_IdPart>
     * @param exportableConcept I_GetConceptData
     * @return I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    protected I_IdPart setSnomedRtId(I_IdVersioned exportIdVersioned, List<I_IdPart> conceptIdParts,
            I_GetConceptData exportableConcept, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, int exportVersion) throws IOException, TerminologyException {
        I_IdPart exportableConceptSnomedRtIdPart = createMock(I_IdPart.class);
        conceptIdParts.add(exportableConceptSnomedRtIdPart);

        expect(exportableConceptSnomedRtIdPart.getSource()).andReturn(snomedRtNid).anyTimes();
        expect(exportableConceptSnomedRtIdPart.getVersion()).andReturn(exportVersion).anyTimes();
        expect(exportableConceptSnomedRtIdPart.getSourceId()).andReturn("SRT1").times(3);
        expect(exportableConceptSnomedRtIdPart.getPathId()).andReturn(exportPathNid).times(2);
        expect(exportableConceptSnomedRtIdPart.getStatusId()).andReturn(activeStatusNid);
        expect(exportableConcept.getId()).andReturn(exportIdVersioned);
        expect(incluesionRootConceptData.isParentOf(snomedIdMapExtensionConceptData, null, null, null, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(snomedIdMapExtensionConceptData, null, null, null, false)).andReturn(false);

        return exportableConceptSnomedRtIdPart;
    }

    /**
     * Mock a SNOMED RT Id for the concept
     *
     * @param exportIdVersioned exportIdVersioned
     * @param conceptIdParts List<I_IdPart>
     * @param exportableConcept I_GetConceptData
     * @return I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    protected I_IdPart setCtv3Id(I_IdVersioned exportIdVersioned, List<I_IdPart> conceptIdParts,
            I_GetConceptData exportableConcept, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, int exportVersion) throws IOException, TerminologyException {
        I_IdPart exportableConceptSnomedRtIdPart = createMock(I_IdPart.class);
        conceptIdParts.add(exportableConceptSnomedRtIdPart);

        expect(exportableConceptSnomedRtIdPart.getSource()).andReturn(ctv3Nid).anyTimes();
        expect(exportableConceptSnomedRtIdPart.getVersion()).andReturn(exportVersion).anyTimes();
        expect(exportableConceptSnomedRtIdPart.getSourceId()).andReturn("CTV3Id").times(3);
        expect(exportableConceptSnomedRtIdPart.getPathId()).andReturn(exportPathNid).times(2);
        expect(exportableConceptSnomedRtIdPart.getStatusId()).andReturn(activeStatusNid);
        expect(exportableConcept.getId()).andReturn(exportIdVersioned);
        expect(incluesionRootConceptData.isParentOf(ctv3IdMapExtensionConceptData, null, null, null, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(ctv3IdMapExtensionConceptData, null, null, null, false)).andReturn(false);

        return exportableConceptSnomedRtIdPart;
    }

    /**
     * Basic component export details
     *
     * @param tuplePart
     * @param version
     * @param date
     * @param position
     * @param pathUuidList
     * @throws TerminologyException
     * @throws IOException
     */
    private void mockBaseDetails(I_AmPart tuplePart, int version, int statusNid, Date date, I_GetConceptData position,
            List<UUID> pathUuidList) throws TerminologyException, IOException {
        expect(tuplePart.getTime()).andReturn(date.getTime()).times(2);
        expect(tuplePart.getPathId()).andReturn(exportPathNid).times(2);
        expect(termFactory.getConcept(exportPathNid)).andReturn(position).times(5);
        expect(position.getUids()).andReturn(pathUuidList).times(4);
        expect(tuplePart.getStatusId()).andReturn(statusNid).anyTimes();
        //expect(tuplePart.getVersion()).andReturn(version);
    }

    /**
     * Mock a exportable full concept including base details and ids.
     *
     * @param exportConceptNid
     * @param version
     * @param date
     * @param exportConceptUuidList
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    public I_GetConceptData mockConcept(int exportConceptNid, int version, int statusNid, Date date, List<UUID> exportConceptUuidList,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList, int sourceUuidNid, I_IdVersioned exportIdVersioned, List<I_IdPart> conceptIdParts,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData) throws IOException,
            TerminologyException {
        I_GetConceptData exportableConcept = createMock(I_GetConceptData.class);
        I_IdPart exportableConceptIdPart = createMock(I_IdPart.class);
        I_IdPart exportableConceptUuidIdPart = createMock(I_IdPart.class);
        I_ConceptAttributeTuple exportableConceptTuple = createMock(I_ConceptAttributeTuple.class);
        List<I_ConceptAttributeTuple> conceptTupleList = new ArrayList<I_ConceptAttributeTuple>();

        // expect the concept to be exportable
        expect(incluesionRootConceptData.isParentOf(exportableConcept, null, null, null, false)).andReturn(true).times(2);
        expect(exclusionsRootConceptData.isParentOf(exportableConcept, null, null, null, false)).andReturn(false).times(2);

        // mock the exportable tuple
        conceptTupleList.add(exportableConceptTuple);
        expect(exportableConcept.getConceptAttributeTuples(null, null, false, false)).andReturn(conceptTupleList);

        // Id
        exportConceptUuidList.add(UUID.randomUUID());
        expect(exportableConcept.getUids()).andReturn(exportConceptUuidList).anyTimes();
        expect(termFactory.getConcept(exportConceptNid)).andReturn(exportableConcept).anyTimes();
        expect(exportableConceptIdPart.getSource()).andReturn(snomedIntNid);
        expect(exportableConceptUuidIdPart.getSource()).andReturn(sourceUuidNid);

        mockConceptId(exportableConceptTuple, version, date, conceptIdParts, exportIdVersioned,
            exportableConceptIdPart, exportableConceptUuidIdPart, sourceUuidNid, exportConceptUuidList, TYPE.CONCEPT);
        expect(exportableConcept.getId()).andReturn(exportIdVersioned).times(6);
        expect(exportableConcept.getNid()).andReturn(exportConceptNid).anyTimes();
        expect(termFactory.getId(exportConceptNid)).andReturn(exportIdVersioned).anyTimes();


        // Details.
        mockBaseDetails(exportableConceptTuple, version, statusNid, date, exportPositionConceptData, pathUuidList);

        // mock the concept details to add to the component DTO
        I_AmTermComponent exportableTermComponent = createMock(I_AmTermComponent.class);
        expect(exportableTermComponent.getNid()).andReturn(exportConceptNid).times(2);
        expect(exportableConceptTuple.getFixedPart()).andReturn(exportableTermComponent);
        expect(exportableConceptTuple.getConId()).andReturn(exportConceptNid).anyTimes();
        expect(exportPositionConceptData.getConceptId()).andReturn(exportPathNid);
        expect(exportableConceptTuple.isDefined()).andReturn(true).times(2);
        expect(exportableConceptTuple.getPathId()).andReturn(exportPathNid).times(2);

        replay(exportableConceptTuple, exportableConceptIdPart, exportableConceptUuidIdPart, exportableTermComponent);

        return exportableConcept;
    }

    /**
     * Mock the concept description.
     *
     * @param exportPosition
     * @param version
     * @param date
     * @param pathUuidList
     * @param conceptUuids
     * @param conceptNid
     * @param descriptionTypeUuidList
     * @param descriptionType
     * @param descriptionTypeNid
     * @param descriptionNid
     * @param text
     * @param exportDescriptionUuidList
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    public I_DescriptionTuple getMockDescription(I_GetConceptData exportPosition, int version, int statusNid, Date date,
            List<UUID> pathUuidList, List<UUID> conceptUuids, int conceptNid,
            I_GetConceptData descriptionType, int descriptionTypeNid, int descriptionNid, String text,
            List<UUID> exportDescriptionUuidList) throws TerminologyException, IOException {
        I_DescriptionTuple exportableFsnDescriptionTuple = createMock(I_DescriptionTuple.class);
        I_IdVersioned exportDescriptionIdVersioned = createMock(I_IdVersioned.class);
        I_IdPart exportableDescriptionFsnIdPart = createMock(I_IdPart.class);
        I_IdPart exportableDescriptionFsnUuuidPart = createMock(I_IdPart.class);
        List<I_IdPart> descriptionIdParts = new ArrayList<I_IdPart>();
        expect(termFactory.getId(descriptionNid)).andReturn(exportDescriptionIdVersioned).anyTimes();

        // matching export tuple position
        expect(exportableFsnDescriptionTuple.getPathId()).andReturn(exportPathNid).times(2);
        expect(exportableFsnDescriptionTuple.getTime()).andReturn(date.getTime());
        org.easymock.classextension.EasyMock.expect(exportPosition.getConceptId()).andReturn(exportPathNid);

        mockConceptId(exportableFsnDescriptionTuple, version, date, descriptionIdParts, exportDescriptionIdVersioned,
            exportableDescriptionFsnIdPart, exportableDescriptionFsnUuuidPart, sourceUuidNid, exportDescriptionUuidList,
            TYPE.DESCRIPTION);

        mockBaseDetails(exportableFsnDescriptionTuple, version, statusNid, date, exportPosition, pathUuidList);

        expect(exportableFsnDescriptionTuple.getText()).andReturn(text);
        expect(exportableFsnDescriptionTuple.getInitialCaseSignificant()).andReturn(true);
        expect(exportableFsnDescriptionTuple.getConceptId()).andReturn(conceptNid);
        expect(exportableFsnDescriptionTuple.getDescId()).andReturn(descriptionNid).times(4);
        expect(exportableFsnDescriptionTuple.getInitialCaseSignificant()).andReturn(false);
        expect(exportableFsnDescriptionTuple.getLang()).andReturn("en").times(2);
        expect(exportableFsnDescriptionTuple.getTypeId()).andReturn(descriptionTypeNid).times(2);
        expect(exportableFsnDescriptionTuple.getText()).andReturn("FSN" + conceptNid);

        expect(termFactory.getUids(conceptNid)).andReturn(conceptUuids).times(2);
        expect(termFactory.getUids(descriptionNid)).andReturn(exportDescriptionUuidList).times(4);
        List<UUID> descriptionFsnUuids = new ArrayList<UUID>();
        descriptionFsnUuids.add(UUID.randomUUID());

        replay(exportableFsnDescriptionTuple, exportDescriptionIdVersioned, exportableDescriptionFsnIdPart,
            exportableDescriptionFsnUuuidPart);

        return exportableFsnDescriptionTuple;
    }

    /**
     * Relationship mockery
     *
     * @param relNid
     * @param version
     * @param date
     * @param exportRelIdUuidList
     * @param concept
     * @param conceptUuidList
     * @param conceptNid
     * @param destinationNid
     * @param positionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param characteristicUuidList
     * @param characteristicNid
     * @param refinabilityUuidList
     * @throws IOException
     * @throws TerminologyException
     */
    public void mockRelationship(int relNid, List<I_RelTuple> relationshipTuples, int version, int statusNid, Date date, List<UUID> exportRelIdUuidList,
            I_GetConceptData concept, List<UUID> conceptUuidList, int conceptNid,
            List<UUID> exportRelationshipDestinationUuidList, int destinationNid, I_GetConceptData positionConceptData,
            List<UUID> pathUuidList, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, int characteristicNid, int typeNid) throws IOException, TerminologyException {
        I_RelTuple relationshipTuple = createMock(I_RelTuple.class);

        // matching export tuple position
        expect(relationshipTuple.getPathId()).andReturn(exportPathNid);
        expect(relationshipTuple.getTime()).andReturn(date.getTime());
        org.easymock.classextension.EasyMock.expect(positionConceptData.getConceptId()).andReturn(exportPathNid);

        // Matching relationship destination to be exportable
        I_GetConceptData destinationConceptData = createMock(I_GetConceptData.class);
        expect(relationshipTuple.getC2Id()).andReturn(destinationNid);
        expect(incluesionRootConceptData.isParentOf(destinationConceptData, null, null, null, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(destinationConceptData, null, null, null, false)).andReturn(false);

        // tuple ids
        I_IdPart exportableRelationshipIdPart = createMock(I_IdPart.class);
        I_IdPart exportableRelationshipUuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> relationshipIdParts = new ArrayList<I_IdPart>();
        I_IdVersioned exportRelationshipIdVersioned = createMock(I_IdVersioned.class);
        exportRelIdUuidList.add(UUID.randomUUID());
        expect(termFactory.getId(relNid)).andReturn(exportRelationshipIdVersioned).anyTimes();

        mockConceptId(relationshipTuple, version, date, relationshipIdParts, exportRelationshipIdVersioned,
            exportableRelationshipIdPart, exportableRelationshipUuidIdPart, sourceUuidNid, exportRelIdUuidList, TYPE.RELATIONSHIP);

        // base concept details.
        mockBaseDetails(relationshipTuple, version, statusNid, date, positionConceptData, pathUuidList);

        // relationship details
        expect(relationshipTuple.getCharacteristicId()).andReturn(characteristicNid).times(2);
        expect(relationshipTuple.getRelId()).andReturn(relNid).times(5);
        expect(termFactory.getUids(relNid)).andReturn(exportRelIdUuidList).times(7);
        expect(termFactory.getUids(conceptNid)).andReturn(conceptUuidList).times(2);
        expect(relationshipTuple.getC1Id()).andReturn(sourceUuidNid);
        expect(relationshipTuple.getC2Id()).andReturn(destinationNid);
        expect(termFactory.getUids(destinationNid)).andReturn(exportRelationshipDestinationUuidList);
        expect(relationshipTuple.getRefinabilityId()).andReturn(optionalRefinabilityNid).times(3);
        expect(relationshipTuple.getGroup()).andReturn(1);
        expect(relationshipTuple.getC1Id()).andReturn(conceptNid);
        expect(relationshipTuple.getTypeId()).andReturn(typeNid);
        expect(relationshipTuple.getPathId()).andReturn(exportPathNid).times(3);


        //relationship versions
        I_RelVersioned relVersioned = createMock(I_RelVersioned.class);
        expect(relationshipTuple.getRelVersioned()).andReturn(relVersioned);
        expect(relVersioned.getC1Id()).andReturn(sourceUuidNid);
        expect(relVersioned.getC2Id()).andReturn(destinationNid);
        I_RelPart relPart = createMock(I_RelPart.class);
        List<I_RelPart> relParts = new ArrayList<I_RelPart>();
        expect(relVersioned.getVersions()).andReturn(relParts);

        expect(relPart.getTypeId()).andReturn(typeNid).times(2);
        expect(relPart.getPathId()).andReturn(exportPathNid).times(2);
        expect(relPart.getStatusId()).andReturn(statusNid).times(1);
        expect(relPart.getVersion()).andReturn(version).times(1);
        relParts.add(relPart);

        // Exportable relationships
        relationshipTuples.add(relationshipTuple);

        replay(destinationConceptData, exportableRelationshipIdPart, relationshipTuple, exportRelationshipIdVersioned,
            exportableRelationshipUuidIdPart, relVersioned, relPart);
    }

    /**
     * Base component extension details for export.
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param part
     * @param extensionTypeNid
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    private void mockBaseExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid,
            List<UUID> refsetIdUuidList, int memberNid, int componentNid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            I_ThinExtByRefPart part, int extensionTypeNid, List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        List<I_ThinExtByRefTuple> extensionConceptTuples = new ArrayList<I_ThinExtByRefTuple>();
        I_ThinExtByRefVersioned conceptExtension = createMock(I_ThinExtByRefVersioned.class);
        expect(conceptExtension.getRefsetId()).andReturn(refsetNid).times(2);
        expect(conceptExtension.getMemberId()).andReturn(memberNid).times(4);
        expect(conceptExtension.getComponentId()).andReturn(componentNid);
        expect(conceptExtension.getTuples(null, null, false, false)).andReturn(extensionConceptTuples);

        I_ThinExtByRefTuple conceptExtensionTuple = createMock(I_ThinExtByRefTuple.class);
        expect(conceptExtensionTuple.getRefsetId()).andReturn(refsetNid).times(2);
        expect(conceptExtensionTuple.getPart()).andReturn(part);
        expect(conceptExtensionTuple.getPathId()).andReturn(pathNid);
        expect(conceptExtensionTuple.getTime()).andReturn(date.getTime());
        expect(conceptExtensionTuple.getTypeId()).andReturn(extensionTypeNid);

        extensionConceptTuples.add(conceptExtensionTuple);

        I_IdPart memberIdPart = createMock(I_IdPart.class);
        I_IdPart memberUuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> memberIdParts = new ArrayList<I_IdPart>();
        I_IdVersioned memberIdVersioned = createMock(I_IdVersioned.class);
        List<UUID> memberIdUuidList = new ArrayList<UUID>();
        memberIdUuidList.add(UUID.randomUUID());

        expect(incluesionRootConceptData.isParentOf(refsetConcept, null, null, null, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(refsetConcept, null, null, null, false)).andReturn(false);

        expect(termFactory.getId(memberNid)).andReturn(memberIdVersioned);
        org.easymock.classextension.EasyMock.expect(exportPositionConceptData.getConceptId()).andReturn(exportPathNid);

        mockBaseDetails(part, version, statusNid,date, exportPositionConceptData, pathUuidList);
        mockConceptId(part, version, date, memberIdParts, memberIdVersioned, memberIdPart, memberUuidIdPart,
            sourceUuidNid, memberIdUuidList, TYPE.REFSET);

        expect(termFactory.getUids(refsetNid)).andReturn(refsetIdUuidList);
        expect(termFactory.getUids(memberNid)).andReturn(memberIdUuidList);
        expect(termFactory.getUids(componentNid)).andReturn(conceptUuidList).times(3);

        conceptExtensions.add(conceptExtension);
        expect(termFactory.getAllExtensionsForComponent(componentNid)).andReturn(conceptExtensions);
        expect(conceptExtension.getTuples(null, null, false, false)).andReturn(extensionConceptTuples);

        replay(memberIdPart, memberUuidIdPart, memberIdVersioned, conceptExtension, conceptExtensionTuple);
    }

    /**
     * Creates a mock string extension
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param String
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, String str, List<UUID> conceptUuidList, int pathNid, I_GetConceptData exportPositionConceptData,
            List<UUID> pathUuidList, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartString extensionStringPart = createMock(I_ThinExtByRefPartString.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, extensionStringPart, stringExtensionNid, conceptExtensions);

        expect(extensionStringPart.getStringValue()).andReturn(str).times(2);

        replay(extensionStringPart);
    }

    /**
     * Creates a mock concept extension
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, List<UUID> conceptUuidList, int pathNid, I_GetConceptData exportPositionConceptData,
            List<UUID> pathUuidList, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConcept.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensionPart, conceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);

        replay(conceptExtensionPart);
    }

    /**
     * Concept string mocking
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param stringValue
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, String stringValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions) throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptString conceptExtensionPart = createMock(I_ThinExtByRefPartConceptString.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptStringExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);
        expect(conceptExtensionPart.getStr()).andReturn(stringValue);

        replay(conceptExtensionPart);
    }

    /**
     * Concept int mocking
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param intValue Integer NB make sure you use Integer and not int or
     *            you'll get a concept concept extension and you don't want
     *            that.
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, Integer intValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions) throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptInt conceptExtensionPart = createMock(I_ThinExtByRefPartConceptInt.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensionPart, conceptIntExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);
        expect(conceptExtensionPart.getIntValue()).andReturn(intValue);

        replay(conceptExtensionPart);
    }

    /**
     * Concept concept extension
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param component2Nid
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, int component2Nid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions) throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConcept.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensionPart, conceptConceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);
        expect(conceptExtensionPart.getC2id()).andReturn(component2Nid);

        replay(conceptExtensionPart);
    }

    /**
     * Two concepts one string...
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param component2Nid
     * @param stringValue
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, int component2Nid, String stringValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions) throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConceptString conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConceptString.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensionPart, conceptConceptStringExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);
        expect(conceptExtensionPart.getC2id()).andReturn(component2Nid);
        expect(conceptExtensionPart.getStringValue()).andReturn(stringValue);

        replay(conceptExtensionPart);
    }

    /**
     * Concepts cubed extension
     *
     * @param refsetConcept
     * @param version
     * @param date
     * @param refsetNid
     * @param memberNid
     * @param componentNid
     * @param component1Nid
     * @param component2Nid
     * @param component3Nid
     * @param conceptUuidList
     * @param pathNid
     * @param exportPositionConceptData
     * @param pathUuidList
     * @param incluesionRootConceptData
     * @param exclusionsRootConceptData
     * @param conceptExtensions
     * @throws TerminologyException
     * @throws IOException
     */
    public void mockExtension(I_GetConceptData refsetConcept, int version, int statusNid, Date date, int refsetNid, List<UUID> refsetIdUuidList, int memberNid,
            int componentNid, int component1Nid, int component2Nid, int component3Nid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions) throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConceptConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConceptConcept.class);

        mockBaseExtension(refsetConcept, version, statusNid, date, refsetNid, refsetIdUuidList, memberNid, componentNid,
            conceptUuidList, pathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensionPart, conceptConceptConceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(component1Nid).times(2);
        expect(conceptExtensionPart.getC2id()).andReturn(component2Nid);
        expect(conceptExtensionPart.getC3id()).andReturn(component3Nid);

        replay(conceptExtensionPart);
    }

    /**
     * Sets Mock responses for a sctid.
     *
     * @param idParts
     * @param exportableConceptIdPart
     */
    public Long setNameSpaceId(List<I_IdPart> idParts, I_IdPart exportableConceptIdPart, TYPE type, int exportVersion) {
        expect(exportableConceptIdPart.getSource()).andReturn(snomedIntNid).anyTimes();
        expect(exportableConceptIdPart.getStatusId()).andReturn(activeStatusNid);
        expect(exportableConceptIdPart.getVersion()).andReturn(exportVersion).anyTimes();
        String sctid = SctIdGenerator.generate(sctSequence++, PROJECT.AU, NAMESPACE.NEHTA, type);

        expect(exportableConceptIdPart.getSourceId()).andReturn(sctid).anyTimes();
        idParts.add(exportableConceptIdPart);

        return Long.valueOf(sctid);
    }

    /**
     * Mocks a CONCEPT enum in ArchitectAuxilery RefsetAuxilery or
     * LadiesAuxilery...
     *
     * @param uuidList
     * @param enumConcept
     * @param nid
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IOException
     * @throws TerminologyException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public I_GetConceptData mockConceptEnum(List<UUID> uuidList, I_ConceptEnumeration enumConcept, Integer nid)
            throws SecurityException, NoSuchFieldException, IOException, TerminologyException,
            IllegalArgumentException, IllegalAccessException, SQLException {
        Field conceptLocalField = enumConcept.getClass().getDeclaredField("local");
        conceptLocalField.setAccessible(true);
        Field conceptUidsField = enumConcept.getClass().getDeclaredField("conceptUids");
        conceptUidsField.setAccessible(true);

        I_ConceptualizeLocally conceptualizeLocally = createMock(I_ConceptualizeLocally.class);
        I_GetConceptData conceptData = createMock(I_GetConceptData.class);

        expect(conceptualizeLocally.getUids()).andReturn(uuidList);
        expect(conceptualizeLocally.getNid()).andReturn(nid).anyTimes();
        conceptLocalField.set(enumConcept, conceptualizeLocally);
        conceptUidsField.set(enumConcept, uuidList);

        expect(termFactory.getConcept(uuidList.iterator().next())).andReturn(conceptData);
        expect(termFactory.getUids(nid)).andReturn(uuidList).anyTimes();
        expect(conceptData.getUids()).andReturn(uuidList).anyTimes();
        expect(conceptData.getNid()).andReturn(nid).anyTimes();

        I_DescriptionTuple conceptTuple = createMock(I_DescriptionTuple.class);
        I_IdPart idPart = createMock(I_IdPart.class);
        I_IdPart uuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> idParts = new ArrayList<I_IdPart>();
        I_IdVersioned idVersioned = createMock(I_IdVersioned.class);
        Long sctId = mockConceptId(conceptTuple, 0, new Date(), idParts, idVersioned, idPart, uuidIdPart, sourceUuidNid, uuidList,
            TYPE.CONCEPT);

        if ( ! UuidSctidMapDb.getInstance().containsUuid(uuidList.get(0))) {
            UuidSctidMapDb.getInstance().addUUIDSctIdEntry(uuidList.get(0), sctId);
        }
        expect(termFactory.getId(nid)).andReturn(idVersioned).anyTimes();

        replay(conceptualizeLocally, conceptTuple, idPart, uuidIdPart, idVersioned);

        return conceptData;
    }

    public I_GetConceptData mockConceptSpec(ConceptSpec conceptSpec, Integer nid)
            throws SecurityException, NoSuchFieldException, IOException, TerminologyException,
            IllegalArgumentException, IllegalAccessException, SQLException {
        Field conceptLocalField = conceptSpec.getClass().getDeclaredField("local");
        conceptLocalField.setAccessible(true);

        I_ConceptualizeLocally conceptualizeLocally = createMock(I_ConceptualizeLocally.class);
        I_GetConceptData conceptData = createMock(I_GetConceptData.class);
        I_DescribeConceptLocally describeConceptLocally = createMock(I_DescribeConceptLocally.class);

        expect(conceptualizeLocally.getNid()).andReturn(nid);
        expect(conceptualizeLocally.getDescriptions()).andReturn(Arrays.asList(describeConceptLocally));
        expect(describeConceptLocally.getText()).andReturn(conceptSpec.getDescription());
        conceptLocalField.set(conceptSpec, conceptualizeLocally);

        expect(termFactory.getConcept(conceptSpec.getUuids()[0])).andReturn(conceptData);
        expect(termFactory.getUids(nid)).andReturn(Arrays.asList(conceptSpec.getUuids())).anyTimes();
        expect(conceptData.getUids()).andReturn(Arrays.asList(conceptSpec.getUuids())).anyTimes();
        expect(conceptData.getNid()).andReturn(nid).anyTimes();

        I_DescriptionTuple conceptTuple = createMock(I_DescriptionTuple.class);
        I_IdPart idPart = createMock(I_IdPart.class);
        I_IdPart uuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> idParts = new ArrayList<I_IdPart>();
        I_IdVersioned idVersioned = createMock(I_IdVersioned.class);
        Long sctId = mockConceptId(conceptTuple, 0, new Date(), idParts, idVersioned, idPart, uuidIdPart, sourceUuidNid,
            Arrays.asList(conceptSpec.getUuids()), TYPE.CONCEPT);
        expect(termFactory.getId(nid)).andReturn(idVersioned).anyTimes();

        if ( ! UuidSctidMapDb.getInstance().containsUuid(conceptSpec.getUuids()[0])) {
            UuidSctidMapDb.getInstance().addUUIDSctIdEntry(conceptSpec.getUuids()[0], sctId);
        }

        replay(conceptualizeLocally, describeConceptLocally, conceptTuple, idPart, uuidIdPart, idVersioned);

        return conceptData;
    }

    /**
     * MAock master to hold all mocked object for reseting between tests.
     *
     * @param toMock Class or interface to mock
     *
     * @return a Mocked object.
     */
    public <T> T createMock(Class<T> toMock) {
        T mock;
        if (toMock.isInterface()) {
            mock = EasyMock.createMock(toMock);
            interfaceMocks.add(mock);
        } else {
            mock = org.easymock.classextension.EasyMock.createMock(toMock);
            classMocks.add(mock);
        }

        return mock;
    }

    /**
     * Reset the mock bucket
     */
    public void resetAll() {
        for (Object object : interfaceMocks) {
            reset(object);
        }
        for (Object object : classMocks) {
            org.easymock.classextension.EasyMock.reset(object);
        }
    }
}
