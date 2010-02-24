package org.dwfa.mojo.export;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.I_ConceptEnumeration;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.easymock.EasyMock;

/**
 * Mocking code for export concepts tests.
 */
public class ConceptMockery {

    List<Object> interfaceMocks = new ArrayList<Object>();
    List<Object> classMocks = new ArrayList<Object>();

    DatabaseExport databaseExport = new DatabaseExport();
    I_TermFactory termFactory;
    int sctSequence = 1;

    int exportPathNid = Integer.MAX_VALUE;
    int activeStatusNid = Integer.MAX_VALUE - 1;
    int snomedIntNid = Integer.MAX_VALUE - 2;
    int snomedT3UuidNid = Integer.MAX_VALUE - 3;
    int fullySpecifiedNameTypeNid = Integer.MAX_VALUE - 4;
    int preferredTypeNid = Integer.MAX_VALUE - 5;
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

    I_GetConceptData activeConceptData;
    I_GetConceptData snomedIntIdConceptData;
    I_GetConceptData snomedT3UuidConceptData;
    I_GetConceptData fullySpecifiedDescriptionConceptData;
    I_GetConceptData preferredDescriptionTypeConceptData;
    I_GetConceptData snomedCoreConcept;
    I_GetConceptData snomedRtIdConcept;

    List<UUID> snomedIsAUuuidList = new ArrayList<UUID>();
    List<UUID> snomedIntIdUuidList;
    List<UUID> activeUuidList;
    List<UUID> fullySpecifiedDescriptionTypeUuidList;
    List<UUID> preferredDescriptionTypeUuidList;
    List<UUID> definingCharacteristicUuidList;
    List<UUID> optionalRefinabilityUuidList;
    List<UUID> snomedCoreUuidList;
    List<UUID> snomedRtIdUuidList;

    // setup fsn int type used to get the concepts FSN
    I_IntSet fsnIIntSet = createMock(I_IntSet.class);

    /**
     * Sets up the meta data concepts and the term factory
     *
     * @throws Exception
     */
    public ConceptMockery() throws Exception {
        snomedIsAUuuidList.add(ConceptConstants.SNOMED_IS_A.getUuids()[0]);

        // mock the LocalVersionedTerminology
        termFactory = createMock(I_TermFactory.class);
        Field factoryField = LocalVersionedTerminology.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, termFactory);

        databaseExport.setTermFactory(termFactory);

        // mock export specification constants
        activeUuidList = new ArrayList<UUID>();
        activeUuidList.add(UUID.randomUUID());
        activeConceptData = mockConceptEnum(activeUuidList, ArchitectonicAuxiliary.Concept.ACTIVE, activeStatusNid);
        expect(activeConceptData.isParentOf(activeConceptData, false)).andReturn(true).anyTimes();
        replay(activeConceptData);

        snomedCoreUuidList = new ArrayList<UUID>();
        snomedCoreUuidList.add(UUID.randomUUID());
        snomedCoreConcept = mockConceptEnum(snomedCoreUuidList, ArchitectonicAuxiliary.Concept.SNOMED_CORE,
            snomedCoreNid);
        replay(snomedCoreConcept);

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

        List<UUID> ctv3IdUuidList = new ArrayList<UUID>();
        ctv3IdUuidList.add(UUID.randomUUID());
        I_GetConceptData ctv3IdconceptData = mockConceptEnum(ctv3IdUuidList, ArchitectonicAuxiliary.Concept.CTV3_ID,
            ctv3Nid);
        expect(ctv3IdconceptData.getConceptId()).andReturn(ctv3Nid).anyTimes();
        replay(ctv3IdconceptData);

        List<UUID> initialCharacterNotCaseSensitiveUuidList = new ArrayList<UUID>();
        initialCharacterNotCaseSensitiveUuidList.add(UUID.randomUUID());
        I_GetConceptData initialCharacterNotCaseSensitiveConceptData = mockConceptEnum(
            initialCharacterNotCaseSensitiveUuidList,
            ArchitectonicAuxiliary.Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE, Integer.MAX_VALUE);
        replay(initialCharacterNotCaseSensitiveConceptData);

        List<UUID> allCharactersCaseSensitiveUuidList = new ArrayList<UUID>();
        allCharactersCaseSensitiveUuidList.add(UUID.randomUUID());
        I_GetConceptData allCharactersCaseSensitiveConceptData = mockConceptEnum(allCharactersCaseSensitiveUuidList,
            ArchitectonicAuxiliary.Concept.ALL_CHARACTERS_CASE_SENSITIVE, Integer.MAX_VALUE);
        replay(allCharactersCaseSensitiveConceptData);

        fullySpecifiedDescriptionTypeUuidList = new ArrayList<UUID>();
        fullySpecifiedDescriptionTypeUuidList.add(UUID.randomUUID());
        fullySpecifiedDescriptionConceptData = mockConceptEnum(fullySpecifiedDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE, Integer.MAX_VALUE);
        expect(fullySpecifiedDescriptionConceptData.getConceptId()).andReturn(fullySpecifiedNameTypeNid).anyTimes();
        replay(fullySpecifiedDescriptionConceptData);

        List<UUID> unspecifiedDescriptionTypeUuidList = new ArrayList<UUID>();
        unspecifiedDescriptionTypeUuidList.add(UUID.randomUUID());
        I_GetConceptData unspecifiedDescriptionTypeConceptData = mockConceptEnum(unspecifiedDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE, Integer.MAX_VALUE);
        replay(unspecifiedDescriptionTypeConceptData);

        preferredDescriptionTypeUuidList = new ArrayList<UUID>();
        preferredDescriptionTypeUuidList.add(UUID.randomUUID());
        preferredDescriptionTypeConceptData = mockConceptEnum(preferredDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE, Integer.MAX_VALUE);
        replay(preferredDescriptionTypeConceptData);

        List<UUID> synonymDescriptionTypeUuidList = new ArrayList<UUID>();
        synonymDescriptionTypeUuidList.add(UUID.randomUUID());
        I_GetConceptData synonymDescriptionTypeConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE, Integer.MAX_VALUE);
        replay(synonymDescriptionTypeConceptData);

        definingCharacteristicUuidList = new ArrayList<UUID>();
        definingCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData definingCharacteristicConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC, Integer.MAX_VALUE);
        replay(definingCharacteristicConceptData);

        List<UUID> statedRelationshipUuidList = new ArrayList<UUID>();
        statedRelationshipUuidList.add(UUID.randomUUID());
        I_GetConceptData statedRelationshipConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP, Integer.MAX_VALUE);
        replay(statedRelationshipConceptData);

        List<UUID> qualifierCharacteristicUuidList = new ArrayList<UUID>();
        qualifierCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData qualifierCharacteristicConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC, Integer.MAX_VALUE);
        replay(qualifierCharacteristicConceptData);

        List<UUID> historicalCharacteristicUuidList = new ArrayList<UUID>();
        historicalCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData historicalCharacteristicConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC, Integer.MAX_VALUE);
        replay(historicalCharacteristicConceptData);

        List<UUID> additionalCharacteristicUuidList = new ArrayList<UUID>();
        additionalCharacteristicUuidList.add(UUID.randomUUID());
        I_GetConceptData additionalCharacteristicConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC, Integer.MAX_VALUE);
        replay(additionalCharacteristicConceptData);

        List<UUID> notRefinableUuidList = new ArrayList<UUID>();
        notRefinableUuidList.add(UUID.randomUUID());
        I_GetConceptData notRefinableConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.NOT_REFINABLE, Integer.MAX_VALUE);
        replay(notRefinableConceptData);

        optionalRefinabilityUuidList = new ArrayList<UUID>();
        optionalRefinabilityUuidList.add(UUID.randomUUID());
        I_GetConceptData optionalRefinabilityConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY, Integer.MAX_VALUE);
        replay(optionalRefinabilityConceptData);

        List<UUID> mandatoryRefinabilityUuidList = new ArrayList<UUID>();
        mandatoryRefinabilityUuidList.add(UUID.randomUUID());
        I_GetConceptData mandatoryRefinabilityConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY, Integer.MAX_VALUE);
        replay(mandatoryRefinabilityConceptData);

        List<UUID> enUuidList = new ArrayList<UUID>();
        enUuidList.add(UUID.randomUUID());
        I_GetConceptData enConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.EN, Integer.MAX_VALUE);
        replay(enConceptData);

        List<UUID> enUsUuidList = new ArrayList<UUID>();
        enUsUuidList.add(UUID.randomUUID());
        I_GetConceptData enUsConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            ArchitectonicAuxiliary.Concept.EN_US, Integer.MAX_VALUE);
        replay(enUsConceptData);

        List<UUID> conceptExtensionUuidList = new ArrayList<UUID>();
        conceptExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptExtensionConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            RefsetAuxiliary.Concept.CONCEPT_EXTENSION, conceptExtensionNid);
        expect(conceptExtensionConceptData.getNid()).andReturn(conceptExtensionNid).anyTimes();
        replay(conceptExtensionConceptData);

        List<UUID> conceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptStringExtensionConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION, conceptStringExtensionNid);
        expect(conceptStringExtensionConceptData.getNid()).andReturn(conceptStringExtensionNid).anyTimes();
        replay(conceptStringExtensionConceptData);

        List<UUID> conceptIntExtensionUuidList = new ArrayList<UUID>();
        conceptIntExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptIntExtensionConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION, conceptIntExtensionNid);
        expect(conceptIntExtensionConceptData.getNid()).andReturn(conceptIntExtensionNid).anyTimes();
        replay(conceptIntExtensionConceptData);

        List<UUID> conceptConceptExtensionUuidList = new ArrayList<UUID>();
        conceptConceptExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptExtensionConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION, conceptConceptExtensionNid);
        expect(conceptConceptExtensionConceptData.getNid()).andReturn(conceptConceptExtensionNid).anyTimes();
        replay(conceptConceptExtensionConceptData);

        List<UUID> conceptConceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptConceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptStringExtensionConceptData = mockConceptEnum(synonymDescriptionTypeUuidList,
            RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION, conceptConceptStringExtensionNid);
        expect(conceptConceptStringExtensionConceptData.getNid()).andReturn(conceptConceptStringExtensionNid)
            .anyTimes();
        replay(conceptConceptStringExtensionConceptData);

        List<UUID> conceptConceptConceptStringExtensionUuidList = new ArrayList<UUID>();
        conceptConceptConceptStringExtensionUuidList.add(UUID.randomUUID());
        I_GetConceptData conceptConceptConceptStringExtensionConceptData = mockConceptEnum(
            synonymDescriptionTypeUuidList, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION,
            conceptConceptConceptExtensionNid);
        expect(conceptConceptConceptStringExtensionConceptData.getNid()).andReturn(conceptConceptConceptExtensionNid)
            .anyTimes();
        replay(conceptConceptConceptStringExtensionConceptData);
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
    public void mockConceptId(I_AmPart tuple, int version, Date date, List<I_IdPart> idParts,
            I_IdVersioned idVersioned, I_IdPart sctIdPart, I_IdPart uuidIdPart, List<UUID> componentUuidList, TYPE type)
            throws TerminologyException, IOException {
        setNameSpaceId(idParts, sctIdPart, type, version);

        expect(idVersioned.getVersions()).andReturn(idParts).times(3);
        expect(tuple.getVersion()).andReturn(version).times(3);
        expect(tuple.getStatusId()).andReturn(activeStatusNid).times(2);
        expect(tuple.getPathId()).andReturn(exportPathNid);

        expect(uuidIdPart.getSource()).andReturn(snomedT3UuidNid).times(6);
        expect(uuidIdPart.getVersion()).andReturn(version).times(2);
        expect(uuidIdPart.getSourceId()).andReturn(componentUuidList.get(0).toString()).times(3);
        expect(tuple.getTime()).andReturn(date.getTime());
        idParts.add(uuidIdPart);
        expect(termFactory.getConcept(activeStatusNid)).andReturn(activeConceptData);
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
    private void mockBaseDetails(I_AmPart tuplePart, int version, Date date, I_GetConceptData position,
            List<UUID> pathUuidList) throws TerminologyException, IOException {
        expect(tuplePart.getStatusId()).andReturn(activeStatusNid);
        expect(termFactory.getConcept(activeStatusNid)).andReturn(activeConceptData).times(2);
        expect(tuplePart.getTime()).andReturn(date.getTime()).times(2);
        expect(tuplePart.getPathId()).andReturn(exportPathNid).times(2);
        expect(termFactory.getConcept(exportPathNid)).andReturn(position).times(5);
        expect(position.getUids()).andReturn(pathUuidList).times(4);
        expect(tuplePart.getStatusId()).andReturn(activeStatusNid);
        expect(termFactory.getConcept(activeStatusNid)).andReturn(activeConceptData).times(6);
        expect(tuplePart.getVersion()).andReturn(version);
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
    public I_GetConceptData mockConcept(int exportConceptNid, int version, Date date, List<UUID> exportConceptUuidList,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData) throws IOException,
            TerminologyException {
        I_GetConceptData exportableConcept = createMock(I_GetConceptData.class);
        I_IdPart exportableConceptIdPart = createMock(I_IdPart.class);
        I_IdPart exportableConceptUuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> conceptIdParts = new ArrayList<I_IdPart>();
        I_IdVersioned exportIdVersioned = createMock(I_IdVersioned.class);
        I_ConceptAttributeTuple exportableConceptTuple = createMock(I_ConceptAttributeTuple.class);
        List<I_ConceptAttributeTuple> conceptTupleList = new ArrayList<I_ConceptAttributeTuple>();

        // expect the concept to be exportable
        expect(incluesionRootConceptData.isParentOf(exportableConcept, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(exportableConcept, false)).andReturn(false);

        // mock the exportable tuple
        conceptTupleList.add(exportableConceptTuple);
        expect(exportableConcept.getConceptAttributeTuples(true)).andReturn(conceptTupleList);

        // Id
        exportConceptUuidList.add(UUID.randomUUID());
        expect(exportableConcept.getUids()).andReturn(exportConceptUuidList).anyTimes();
        expect(termFactory.getConcept(exportConceptNid)).andReturn(exportableConcept).anyTimes();
        expect(exportableConceptIdPart.getSource()).andReturn(snomedIntNid);
        expect(exportableConceptUuidIdPart.getSource()).andReturn(snomedT3UuidNid);

        mockConceptId(exportableConceptTuple, version, date, conceptIdParts, exportIdVersioned,
            exportableConceptIdPart, exportableConceptUuidIdPart, exportConceptUuidList, TYPE.CONCEPT);
        expect(exportableConcept.getId()).andReturn(exportIdVersioned).times(6);
        expect(exportIdVersioned.getVersions()).andReturn(conceptIdParts).times(2);

        // Details.
        mockBaseDetails(exportableConceptTuple, version, date, exportPositionConceptData, pathUuidList);

        // mock the concept details to add to the component DTO
        I_AmTermComponent exportableTermComponent = createMock(I_AmTermComponent.class);
        expect(exportableTermComponent.getNid()).andReturn(exportConceptNid).times(2);
        expect(exportableConceptTuple.getFixedPart()).andReturn(exportableTermComponent);
        expect(exportableConceptTuple.getConId()).andReturn(exportConceptNid);
        expect(exportPositionConceptData.getConceptId()).andReturn(exportPathNid);
        expect(exportableConceptTuple.getVersion()).andReturn(version);
        expect(exportableConceptTuple.isDefined()).andReturn(true);
        expect(exportableConceptTuple.isDefined()).andReturn(true);

        replay(exportableConceptTuple, exportableConceptIdPart, exportableConceptUuidIdPart, exportIdVersioned,
            exportableTermComponent);

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
    public I_DescriptionTuple getMockDescription(I_GetConceptData exportPosition, int version, Date date,
            List<UUID> pathUuidList, List<UUID> conceptUuids, int conceptNid, List<UUID> descriptionTypeUuidList,
            I_GetConceptData descriptionType, int descriptionTypeNid, int descriptionNid, String text,
            List<UUID> exportDescriptionUuidList) throws TerminologyException, IOException {
        I_DescriptionTuple exportableFsnDescriptionTuple = createMock(I_DescriptionTuple.class);
        I_IdVersioned exportDescriptionIdVersioned = createMock(I_IdVersioned.class);
        I_IdPart exportableDescriptionFsnIdPart = createMock(I_IdPart.class);
        I_IdPart exportableDescriptionFsnUuuidPart = createMock(I_IdPart.class);
        List<I_IdPart> descriptionIdParts = new ArrayList<I_IdPart>();
        exportDescriptionUuidList.add(UUID.randomUUID());
        expect(termFactory.getId(descriptionNid)).andReturn(exportDescriptionIdVersioned);

        // matching export tuple position
        expect(exportableFsnDescriptionTuple.getPathId()).andReturn(exportPathNid);
        expect(exportableFsnDescriptionTuple.getTime()).andReturn(date.getTime());
        org.easymock.classextension.EasyMock.expect(exportPosition.getConceptId()).andReturn(exportPathNid);

        mockConceptId(exportableFsnDescriptionTuple, version, date, descriptionIdParts, exportDescriptionIdVersioned,
            exportableDescriptionFsnIdPart, exportableDescriptionFsnUuuidPart, exportDescriptionUuidList,
            TYPE.DESCRIPTION);

        mockBaseDetails(exportableFsnDescriptionTuple, version, date, exportPosition, pathUuidList);

        expect(exportableFsnDescriptionTuple.getText()).andReturn(text);
        expect(exportableFsnDescriptionTuple.getInitialCaseSignificant()).andReturn(true);
        expect(exportableFsnDescriptionTuple.getConceptId()).andReturn(conceptNid);
        expect(exportableFsnDescriptionTuple.getDescId()).andReturn(descriptionNid).times(3);
        expect(exportableFsnDescriptionTuple.getInitialCaseSignificant()).andReturn(false);
        expect(exportableFsnDescriptionTuple.getLang()).andReturn("en").times(2);
        expect(exportableFsnDescriptionTuple.getTypeId()).andReturn(descriptionTypeNid).times(2);
        expect(exportableFsnDescriptionTuple.getText()).andReturn("FSN");
        expect(exportableFsnDescriptionTuple.getVersion()).andReturn(version).times(2);
        expect(exportableDescriptionFsnIdPart.getVersion()).andReturn(version);
        expect(exportableDescriptionFsnIdPart.getSource()).andReturn(snomedIntNid);
        expect(exportableDescriptionFsnUuuidPart.getSource()).andReturn(snomedT3UuidNid);

        List<UUID> descriptionUuids = new ArrayList<UUID>();
        descriptionUuids.add(UUID.randomUUID());
        expect(termFactory.getUids(conceptNid)).andReturn(conceptUuids);
        expect(termFactory.getUids(descriptionNid)).andReturn(descriptionUuids);
        expect(termFactory.getUids(descriptionTypeNid)).andReturn(descriptionTypeUuidList);
        List<UUID> descriptionFsnUuids = new ArrayList<UUID>();
        descriptionFsnUuids.add(UUID.randomUUID());
        expect(termFactory.getConcept(descriptionTypeNid)).andReturn(descriptionType);

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
    public void mockRelationship(int relNid, int version, Date date, List<UUID> exportRelIdUuidList,
            I_GetConceptData concept, List<UUID> conceptUuidList, int conceptNid, int destinationNid,
            I_GetConceptData positionConceptData, List<UUID> pathUuidList, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, List<UUID> characteristicUuidList, int characteristicNid,
            List<UUID> refinabilityUuidList) throws IOException, TerminologyException {
        I_RelTuple relationshipTuple = createMock(I_RelTuple.class);

        // matching export tuple position
        expect(relationshipTuple.getPathId()).andReturn(exportPathNid);
        expect(relationshipTuple.getTime()).andReturn(date.getTime());
        org.easymock.classextension.EasyMock.expect(positionConceptData.getConceptId()).andReturn(exportPathNid);

        // Matching relationship destination to be exportable
        List<UUID> exportRelationshipDestinationUuidList = new ArrayList<UUID>();
        exportRelationshipDestinationUuidList.add(UUID.randomUUID());
        I_GetConceptData destinationConceptData = createMock(I_GetConceptData.class);
        expect(relationshipTuple.getC2Id()).andReturn(destinationNid);
        expect(incluesionRootConceptData.isParentOf(destinationConceptData, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(destinationConceptData, false)).andReturn(false);
        expect(termFactory.getConcept(destinationNid)).andReturn(destinationConceptData);

        // tuple ids
        I_IdPart exportableRelationshipIdPart = createMock(I_IdPart.class);
        I_IdPart exportableRelationshipUuidIdPart = createMock(I_IdPart.class);
        List<I_IdPart> relationshipIdParts = new ArrayList<I_IdPart>();
        I_IdVersioned exportRelationshipIdVersioned = createMock(I_IdVersioned.class);
        exportRelIdUuidList.add(UUID.randomUUID());
        expect(relationshipTuple.getRelId()).andReturn(relNid);
        expect(termFactory.getId(relNid)).andReturn(exportRelationshipIdVersioned);

        mockConceptId(relationshipTuple, version, date, relationshipIdParts, exportRelationshipIdVersioned,
            exportableRelationshipIdPart, exportableRelationshipUuidIdPart, exportRelIdUuidList, TYPE.RELATIONSHIP);

        // base concept details.
        mockBaseDetails(relationshipTuple, version, date, positionConceptData, pathUuidList);

        // sctid uuid map details
        expect(termFactory.getConcept(activeStatusNid)).andReturn(activeConceptData);

        // relationship details
        expect(relationshipTuple.getCharacteristicId()).andReturn(characteristicNid).times(2);
        expect(termFactory.getUids(characteristicNid)).andReturn(characteristicUuidList).times(2);
        expect(relationshipTuple.getRelId()).andReturn(relNid).times(2);
        expect(termFactory.getUids(relNid)).andReturn(exportRelIdUuidList);
        expect(relationshipTuple.getC2Id()).andReturn(destinationNid);
        expect(termFactory.getUids(destinationNid)).andReturn(exportRelationshipDestinationUuidList);
        expect(relationshipTuple.getRefinabilityId()).andReturn(optionalRefinabilityNid);
        expect(termFactory.getUids(optionalRefinabilityNid)).andReturn(refinabilityUuidList);
        expect(relationshipTuple.getGroup()).andReturn(1);
        expect(relationshipTuple.getC1Id()).andReturn(conceptNid);
        expect(termFactory.getUids(conceptNid)).andReturn(conceptUuidList);
        expect(relationshipTuple.getTypeId()).andReturn(isANid);
        expect(termFactory.getUids(isANid)).andReturn(snomedIsAUuuidList);

        // Exportable relationships
        List<I_RelTuple> relationshipTuples = new ArrayList<I_RelTuple>();
        relationshipTuples.add(relationshipTuple);
        expect(concept.getSourceRelTuples(null, false, true)).andReturn(relationshipTuples);

        replay(destinationConceptData, exportableRelationshipIdPart, relationshipTuple, exportRelationshipIdVersioned,
            exportableRelationshipUuidIdPart);
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
    private void mockBaseExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid,
            int memberNid, int componentNid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            I_ThinExtByRefPart part, int extensionTypeNid, List<I_ThinExtByRefVersioned> conceptExtensions)
    throws TerminologyException, IOException {
        List<I_ThinExtByRefTuple> extensionConceptTuples = new ArrayList<I_ThinExtByRefTuple>();
        I_ThinExtByRefVersioned conceptExtension = createMock(I_ThinExtByRefVersioned.class);
        expect(conceptExtension.getRefsetId()).andReturn(refsetNid).times(2);
        expect(conceptExtension.getMemberId()).andReturn(memberNid).times(2);
        expect(conceptExtension.getComponentId()).andReturn(componentNid);
        expect(conceptExtension.getTuples(false, true)).andReturn(extensionConceptTuples);

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

        expect(incluesionRootConceptData.isParentOf(refsetConcept, false)).andReturn(true);
        expect(exclusionsRootConceptData.isParentOf(refsetConcept, false)).andReturn(false);

        expect(termFactory.getId(memberNid)).andReturn(memberIdVersioned);
        org.easymock.classextension.EasyMock.expect(exportPositionConceptData.getConceptId()).andReturn(exportPathNid);

        mockBaseDetails(part, version, date, exportPositionConceptData, pathUuidList);
        mockConceptId(part, version, date, memberIdParts, memberIdVersioned, memberIdPart, memberUuidIdPart,
            memberIdUuidList, TYPE.REFSET);
        expect(memberIdPart.getVersion()).andReturn(version);
        expect(part.getVersion()).andReturn(version);

        expect(part.getStatusId()).andReturn(activeStatusNid);
        expect(termFactory.getUids(activeStatusNid)).andReturn(activeUuidList);
        expect(part.getVersion()).andReturn(version);

        expect(termFactory.getUids(memberNid)).andReturn(memberIdUuidList);
        expect(termFactory.getUids(componentNid)).andReturn(conceptUuidList);

        conceptExtensions.add(conceptExtension);
        expect(termFactory.getAllExtensionsForComponent(componentNid)).andReturn(conceptExtensions);
        expect(conceptExtension.getTuples(false, true)).andReturn(extensionConceptTuples);

        replay(memberIdPart, memberUuidIdPart, memberIdVersioned, conceptExtension, conceptExtensionTuple);
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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, List<UUID> conceptUuidList, int pathNid, I_GetConceptData exportPositionConceptData,
            List<UUID> pathUuidList, I_GetConceptData incluesionRootConceptData,
            I_GetConceptData exclusionsRootConceptData, List<I_ThinExtByRefVersioned> conceptExtensions)
    throws TerminologyException, IOException {
        I_ThinExtByRefPartConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConcept.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);

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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, String stringValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptString conceptExtensionPart = createMock(I_ThinExtByRefPartConceptString.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptStringExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);
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
     * @param intValue Integer NB make sure you use Integer and not int or you'll get a concept concept extension and you don't want that.
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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, Integer intValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptInt conceptExtensionPart = createMock(I_ThinExtByRefPartConceptInt.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptIntExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);
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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, int component2Nid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConcept.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptConceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);
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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, int component2Nid, String stringValue, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConceptString conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConceptString.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptConceptStringExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);
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
    public void mockExtension(I_GetConceptData refsetConcept, int version, Date date, int refsetNid, int memberNid,
            int componentNid, int component2Nid, int component3Nid, List<UUID> conceptUuidList, int pathNid,
            I_GetConceptData exportPositionConceptData, List<UUID> pathUuidList,
            I_GetConceptData incluesionRootConceptData, I_GetConceptData exclusionsRootConceptData,
            List<I_ThinExtByRefVersioned> conceptExtensions)
            throws TerminologyException, IOException {
        I_ThinExtByRefPartConceptConceptConcept conceptExtensionPart = createMock(I_ThinExtByRefPartConceptConceptConcept.class);

        mockBaseExtension(refsetConcept, version, date, refsetNid, memberNid, componentNid, conceptUuidList, pathNid,
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData,
            conceptExtensionPart, conceptConceptConceptExtensionNid, conceptExtensions);

        expect(conceptExtensionPart.getC1id()).andReturn(componentNid);
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
    public void setNameSpaceId(List<I_IdPart> idParts, I_IdPart exportableConceptIdPart, TYPE type, int exportVersion) {
        expect(exportableConceptIdPart.getSource()).andReturn(snomedIntNid).times(6);
        expect(exportableConceptIdPart.getStatusId()).andReturn(activeStatusNid);
        expect(exportableConceptIdPart.getVersion()).andReturn(exportVersion).times(3);
        String sctid = SctIdGenerator.generate(sctSequence++, NAMESPACE.NEHTA, type);
        expect(exportableConceptIdPart.getSourceId()).andReturn(sctid).times(4);
        idParts.add(exportableConceptIdPart);
    }

    /**
     * Mocks a CONCEPT enum in ArchitectAuxilery RefsetAuxilery or LadiesAuxilery...
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
     */
    public I_GetConceptData mockConceptEnum(List<UUID> uuidList, I_ConceptEnumeration enumConcept, Integer nid)
            throws SecurityException, NoSuchFieldException, IOException, TerminologyException,
            IllegalArgumentException, IllegalAccessException {
        Field conceptLocalField = enumConcept.getClass().getDeclaredField("local");
        conceptLocalField.setAccessible(true);
        Field conceptUidsField = enumConcept.getClass().getDeclaredField("conceptUids");
        conceptUidsField.setAccessible(true);

        I_ConceptualizeLocally conceptualizeLocally = createMock(I_ConceptualizeLocally.class);
        I_GetConceptData conceptData = createMock(I_GetConceptData.class);

        expect(conceptualizeLocally.getUids()).andReturn(uuidList);
        expect(conceptualizeLocally.getNid()).andReturn(nid);
        conceptLocalField.set(enumConcept, conceptualizeLocally);
        conceptUidsField.set(enumConcept, uuidList);

        expect(termFactory.getConcept(uuidList.iterator().next())).andReturn(conceptData);
        expect(conceptData.getUids()).andReturn(uuidList).anyTimes();

        replay(conceptualizeLocally);

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
