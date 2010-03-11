package org.dwfa.mojo.export;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.export.file.Rf2OutputHandler;
import org.dwfa.util.AceDateFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseExportTest extends ConceptMockery {

    static File dbDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "test-id-db");
    static File exportDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "rf2");

    int exportVersion = 0;

    Date exportDate = new Date();

    Rf2OutputHandler rf2OutputHandler;
    private I_GetConceptData exportPositionConceptData;
    private List<UUID> pathUuidList;
    private Class<? extends DatabaseExport> databaseExportClass;

    int incluesionRootConceptDataNid = 1;
    int exclusionsRootConceptDataNid = 2;

    public DatabaseExportTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        exportDirectory.mkdirs();
        databaseExportClass = databaseExport.getClass();

        snomedIsAUuuidList.add(ConceptConstants.SNOMED_IS_A.getUuids()[0]);

        //mock the LocalVersionedTerminology
        Field factoryField = LocalVersionedTerminology.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(null, termFactory);

        databaseExport.setTermFactory(termFactory);

        pathUuidList = new ArrayList<UUID>();
        pathUuidList.add(UUID.randomUUID());
        exportPositionConceptData = createMock(I_GetConceptData.class);
        expect(exportPositionConceptData.getUids()).andReturn(pathUuidList);

        PositionDescriptor positionDescriptor = createMock(PositionDescriptor.class);
        ConceptDescriptor conceptDescriptor = createMock(ConceptDescriptor.class);

        org.easymock.classextension.EasyMock.expect(conceptDescriptor.getVerifiedConcept()).andReturn(exportPositionConceptData);
        org.easymock.classextension.EasyMock.expect(positionDescriptor.getPath()).andReturn(conceptDescriptor);
        org.easymock.classextension.EasyMock.expect(positionDescriptor.getTimeString()).andReturn("latest").times(1, 2);

        PositionDescriptor[] exportPositions = new PositionDescriptor[]{positionDescriptor};
        Field positionsForExport = databaseExportClass.getDeclaredField("positionsForExport");
        positionsForExport.setAccessible(true);
        positionsForExport.set(databaseExport, exportPositions);

        //mock the include root
        ConceptDescriptor incluesionRoot = createMock(ConceptDescriptor.class);

        org.easymock.classextension.EasyMock.expect(incluesionRoot.getVerifiedConcept()).andReturn(incluesionRootConceptData);
        expect(incluesionRootConceptData.getNid()).andReturn(incluesionRootConceptDataNid).anyTimes();

        ConceptDescriptor[] includedConceptDescriptors = new ConceptDescriptor[]{incluesionRoot};
        Field inclusions = databaseExportClass.getDeclaredField("inclusions");
        inclusions.setAccessible(true);
        inclusions.set(databaseExport, includedConceptDescriptors);

        //mock the exclude root
        ConceptDescriptor exclusionsRoot = createMock(ConceptDescriptor.class);
        expect(exclusionsRootConceptData.getNid()).andReturn(exclusionsRootConceptDataNid).anyTimes();
        org.easymock.classextension.EasyMock.expect(exclusionsRoot.getVerifiedConcept()).andReturn(exclusionsRootConceptData);

        ConceptDescriptor[] excludedConceptDescriptors = new ConceptDescriptor[]{exclusionsRoot};
        Field exclusions = databaseExportClass.getDeclaredField("exclusions");
        exclusions.setAccessible(true);
        exclusions.set(databaseExport, excludedConceptDescriptors);
        org.easymock.classextension.EasyMock.replay(incluesionRoot, exclusionsRoot, positionDescriptor, conceptDescriptor);

        //setup the export output handler details
        Field exportDirectoryField = databaseExportClass.getDeclaredField("exportDirectory");
        exportDirectoryField.setAccessible(true);
        exportDirectoryField.set(databaseExport, exportDirectory);

        Field sctIdDbDirectoryField = databaseExportClass.getDeclaredField("sctIdDbDirectory");
        sctIdDbDirectoryField.setAccessible(true);
        sctIdDbDirectoryField.set(databaseExport, dbDirectory);

        //set the default namespace
        Field defaultNamespaceField = databaseExportClass.getDeclaredField("defaultNamespace");
        defaultNamespaceField.setAccessible(true);
        defaultNamespaceField.set(databaseExport, NAMESPACE.NEHTA.getDigits());

        //setup 1 call to iterate concepts
        termFactory.iterateConcepts(databaseExport);
    }

    @After
    public void tearDown() {
        resetAll();
    }

    /**
     * Test all parts of an exportable concept and extension types.
     * @throws Exception
     */
    @Test
    public void testExportableConceptWithAllExtensionTypes() throws Exception {
        List<I_GetConceptData> exportableConcepts = new ArrayList<I_GetConceptData>();
        /////////////////////////////////////
        // Create mock concept to process. //
        /////////////////////////////////////
        List<I_ThinExtByRefVersioned> conceptExtensions = new ArrayList<I_ThinExtByRefVersioned>();

        int exportConceptNid = 10;
        List<UUID> exportConceptUuidList = new ArrayList<UUID>();
        I_IdVersioned exportIdVersioned = createMock(I_IdVersioned.class);
        List<I_IdPart> conceptIdParts = new ArrayList<I_IdPart>();
        I_GetConceptData exportableConcept = mockConcept(exportConceptNid, exportVersion, currentStatusNid, exportDate,
            exportConceptUuidList, exportPositionConceptData, pathUuidList, exportIdVersioned, conceptIdParts, incluesionRootConceptData,
            exclusionsRootConceptData);
        exportableConcepts.add(exportableConcept);

        I_IdPart exportableConceptSnomedRtIdPart = setSnomedRtId(exportIdVersioned, conceptIdParts, exportableConcept,
            incluesionRootConceptData, exclusionsRootConceptData, exportVersion);
        I_IdPart exportableConceptSnomedCtv3IdPart = setCtv3Id(exportIdVersioned, conceptIdParts, exportableConcept,
            incluesionRootConceptData, exclusionsRootConceptData, exportVersion);

        //for concept concept refsets and relationship
        int exportConcept2Nid = 11;
        List<UUID> exportConcept2UuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept2 = createExportableConcept(exportConcept2Nid, exportConcept2UuidList, exportConcept2Nid + 1, activeStatusNid);
        exportableConcepts.add(exportableConcept2);

        //for concept concept concept refsets
        int exportConcept3Nid = 13;
        List<UUID> exportConcept3UuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept3 = createExportableConcept(exportConcept3Nid, exportConcept3UuidList, exportConcept3Nid + 1, activeStatusNid);
        exportableConcepts.add(exportableConcept3);

        //for inactive concept
        int exportConcept4Nid = 15;
        List<UUID> exportConcept4UuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept4 = createExportableConcept(exportConcept4Nid, exportConcept4UuidList, exportConcept4Nid + 1, aceAmbiguousStatusNId);
        exportableConcepts.add(exportableConcept4);

        replay(exportIdVersioned, exportableConceptSnomedRtIdPart, exportableConceptSnomedCtv3IdPart, exportableConcept4);

        ///////////////////////////
        //Concept reference sets //
        ///////////////////////////
        int refsetNid = 1000000;
        int memberNid = 10000000;

        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_IdVersioned exportRefsetIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData refsetConcept = mockConcept(refsetNid, exportVersion, activeStatusNid, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, exportRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData, exclusionsRootConceptData);
        expect(refsetConcept.getInitialText()).andReturn("concept_refset").anyTimes();

        List<UUID> refsetUuidsList = new ArrayList<UUID>();
        refsetUuidsList.add(UUID.randomUUID());
        mockExtension(refsetConcept, exportVersion, activeStatusNid, exportDate, refsetNid, refsetUuidsList, memberNid, exportConceptNid,
            exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensions);

        replay(refsetConcept, exportRefsetIdVersioned);
        //////////////////////////////////
        //Concept string reference sets //
        //////////////////////////////////
        int refsetStringNid = 1000010;
        int memberStringNid = 10000100;

        I_IdVersioned exportStrRefsetIdVersioned = createMock(I_IdVersioned.class);
        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_GetConceptData refsetStringConcept = mockConcept(refsetStringNid, exportVersion, activeStatusNid, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, exportStrRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData, exclusionsRootConceptData);
        expect(refsetStringConcept.getInitialText()).andReturn("concept_string_refset").anyTimes();

        List<UUID> refsetStringUuidsList = new ArrayList<UUID>();
        refsetStringUuidsList.add(UUID.randomUUID());
        mockExtension(refsetStringConcept, exportVersion, activeStatusNid, exportDate, refsetStringNid, refsetStringUuidsList, memberStringNid,
            exportConceptNid, "Woolie", exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetStringConcept, exportStrRefsetIdVersioned);

        //////////////////////////////////
        //Concept Integer reference sets //
        //////////////////////////////////
        int refsetIntegerNid = 1000020;
        int memberIntegerNid = 10000200;

        I_IdVersioned exportIntegerRefsetIdVersioned = createMock(I_IdVersioned.class);
        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_GetConceptData refsetIntegerConcept = mockConcept(refsetIntegerNid, exportVersion, activeStatusNid, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, exportIntegerRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData, exclusionsRootConceptData);
        expect(refsetIntegerConcept.getInitialText()).andReturn("concept_int_refset").anyTimes();

        List<UUID> refsetIntegerUuidsList = new ArrayList<UUID>();
        refsetIntegerUuidsList.add(UUID.randomUUID());
        mockExtension(refsetIntegerConcept, exportVersion, activeStatusNid, exportDate, refsetIntegerNid, refsetIntegerUuidsList, memberIntegerNid,
            exportConceptNid, new Integer("80085"), exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetIntegerConcept, exportIntegerRefsetIdVersioned);

        ///////////////////////////////////
        //Concept Concept reference sets //
        ///////////////////////////////////
        int refsetCcNid = 1000100;
        int memberCcNid = 10001000;

        I_IdVersioned exportCcRefsetIdVersioned = createMock(I_IdVersioned.class);
        expect(exportableConcept2.getConceptId()).andReturn(exportConcept2Nid);
        I_GetConceptData refsetConceptConcept = mockConcept(refsetCcNid, exportVersion, activeStatusNid, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, exportCcRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData,
            exclusionsRootConceptData);
        expect(refsetConceptConcept.getInitialText()).andReturn("concept_concept_refset").anyTimes();

        List<UUID> refsetCcUuidsList = new ArrayList<UUID>();
        refsetCcUuidsList.add(UUID.randomUUID());
        mockExtension(refsetConceptConcept, exportVersion, activeStatusNid, exportDate, refsetCcNid, refsetCcUuidsList, memberCcNid, exportConceptNid,
            exportConcept2Nid, exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConcept, exportableConcept2, exportCcRefsetIdVersioned);

        //////////////////////////////////////////
        //Concept Concept String reference sets //
        //////////////////////////////////////////
        int refsetCcsNid = 1000110;
        int memberCcsNid = 10001100;

        I_IdVersioned exportCcStrRefsetIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData refsetConceptConceptString = mockConcept(refsetCcsNid, exportVersion, activeStatusNid, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, exportCcStrRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData,
            exclusionsRootConceptData);
        expect(refsetConceptConceptString.getInitialText()).andReturn("concept_concept_string_refset").anyTimes();

        List<UUID> refsetCcsUuidsList = new ArrayList<UUID>();
        refsetCcsUuidsList.add(UUID.randomUUID());
        mockExtension(refsetConceptConceptString, exportVersion, activeStatusNid, exportDate, refsetCcsNid, refsetCcsUuidsList, memberCcsNid, exportConceptNid,
            exportConcept2Nid, "mamoth", exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConceptString, exportCcStrRefsetIdVersioned);

        ////////////////////////////////////////////
        // Concept Concept Concept reference sets //
        ////////////////////////////////////////////
        int refsetCccNid = 1000200;
        int memberCccNid = 10002000;

        I_IdVersioned exportCccRefsetIdVersioned = createMock(I_IdVersioned.class);
        expect(exportableConcept3.getConceptId()).andReturn(exportConcept2Nid);
        I_GetConceptData refsetConceptConceptConcept = mockConcept(refsetCccNid, exportVersion, activeStatusNid, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, exportCccRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData,
            exclusionsRootConceptData);
        expect(refsetConceptConceptConcept.getInitialText()).andReturn("concept_concept_concept_string_refset").anyTimes();

        List<UUID> refsetCccUuidsList = new ArrayList<UUID>();
        refsetCccUuidsList.add(UUID.randomUUID());
        mockExtension(refsetConceptConcept, exportVersion, activeStatusNid, exportDate, refsetCccNid, refsetCccUuidsList, memberCccNid, exportConceptNid,
            exportConcept2Nid, exportConcept3Nid, exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConceptConcept, exportableConcept3, exportCccRefsetIdVersioned);

        /////////////////////////
        //Concept descriptions //
        /////////////////////////
        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        List<UUID> exportFsnUuidList = new ArrayList<UUID>();
        exportFsnUuidList.add(UUID.randomUUID());

        int fsnDescriptionId = 100;
        I_DescriptionTuple exportableFsnDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, activeStatusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            fullySpecifiedDescriptionConceptData, fullySpecifiedNameTypeNid, fsnDescriptionId, "FSN text", exportFsnUuidList);

        expect(termFactory.getAllExtensionsForComponent(fsnDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        //setup fsn
        List<I_DescriptionTuple> descriptionFsnTuples = new ArrayList<I_DescriptionTuple>();
        descriptionFsnTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, fsnIIntSet, null, true)).andReturn(descriptionFsnTuples);

        int ptDescriptionId = 101;
        List<UUID> exportPtUuidList = new ArrayList<UUID>();
        exportPtUuidList.add(UUID.randomUUID());
        I_DescriptionTuple exportablepreferredDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, activeStatusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            preferredDescriptionTypeConceptData, preferredDescriptionTypeNid, ptDescriptionId, "Preferred text", exportPtUuidList);

        descriptionTuples.add(exportablepreferredDescriptionTuple);
        descriptionTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, null, null, true)).andReturn(descriptionTuples);

        //refsets
        List<I_ThinExtByRefVersioned> descriptionExtensions = new ArrayList<I_ThinExtByRefVersioned>();
        int descRefsetNid = 2000;
        int descMemberNid = 20000;

        I_IdVersioned exportDescriptionRefsetIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData descriptionRefsetConcept = mockConcept(descRefsetNid, exportVersion, activeStatusNid, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, exportDescriptionRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData,
            exclusionsRootConceptData);
        expect(descriptionRefsetConcept.getInitialText()).andReturn("description_refset").anyTimes();

        List<UUID> refsetDescriptionUuidsList = new ArrayList<UUID>();
        refsetDescriptionUuidsList.add(UUID.randomUUID());
        mockExtension(descriptionRefsetConcept, exportVersion, activeStatusNid, exportDate, descRefsetNid, refsetDescriptionUuidsList, descMemberNid,
            ptDescriptionId, exportPtUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, descriptionExtensions);

        expect(termFactory.getAllExtensionsForComponent(ptDescriptionId)).andReturn(descriptionExtensions);

        replay(descriptionRefsetConcept, exportDescriptionRefsetIdVersioned);
        ///////////////////////////
        // concept relationships //
        ///////////////////////////
        // Exportable relationships
        List<I_RelTuple> relationshipTuples = new ArrayList<I_RelTuple>();
        expect(exportableConcept.getSourceRelTuples(null, null, null, false, true)).andReturn(relationshipTuples);

        int relId = 200;

        List<UUID> exportRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(relId, relationshipTuples, exportVersion, activeStatusNid, exportDate, exportRelationshipUuidList,
            exportableConcept, exportConceptUuidList, exportConceptNid, exportConcept2UuidList, exportConcept2Nid, exportPositionConceptData,
            pathUuidList, incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicNid, isANid);

        int historyRelId = 300;

        List<UUID> exportHistoryRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(historyRelId, relationshipTuples, exportVersion, activeStatusNid, exportDate, exportHistoryRelationshipUuidList,
            exportableConcept, exportConceptUuidList, exportConceptNid, exportConcept3UuidList, exportConcept3Nid, exportPositionConceptData,
            pathUuidList, incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicNid, wasAHistoryNid);

        expect(termFactory.getAllExtensionsForComponent(historyRelId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        //Relationship extensions
        List<I_ThinExtByRefVersioned> relationshipExtensions = new ArrayList<I_ThinExtByRefVersioned>();
        int relRefsetNid = 3000000;
        int relMemberNid = 30000000;

        I_IdVersioned exportRelationshipRefsetIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData relationshipRefsetConcept = mockConcept(relRefsetNid, exportVersion, activeStatusNid, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, exportRelationshipRefsetIdVersioned, new ArrayList<I_IdPart>(), incluesionRootConceptData,
            exclusionsRootConceptData);
        expect(relationshipRefsetConcept.getInitialText()).andReturn("relationship_refset").anyTimes();


        List<UUID> refsetRelationshipUuidsList = new ArrayList<UUID>();
        refsetRelationshipUuidsList.add(UUID.randomUUID());
        mockExtension(relationshipRefsetConcept, exportVersion, activeStatusNid, exportDate, relRefsetNid, refsetRelationshipUuidsList,
            relMemberNid, relId, exportRelationshipUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, relationshipExtensions);

        replay(relationshipRefsetConcept, exportRelationshipRefsetIdVersioned);
        org.easymock.classextension.EasyMock.replay(incluesionRootConceptData, exclusionsRootConceptData, exportPositionConceptData);

        //is the export path on the internation release path.
        expect(snomedCoreConcept.isParentOf(exportPositionConceptData, null, null, null, false)).andReturn(false).anyTimes();
        replay(snomedCoreConcept);
        replay(termFactory);

        Field testingField = databaseExport.getClass().getDeclaredField("testing");
        testingField.setAccessible(true);
        testingField.set(databaseExport, true);

        databaseExport.execute();
        testingField.set(databaseExport, false);

        Field exportSpecificationField = databaseExport.getClass().getDeclaredField("exportSpecification");
        exportSpecificationField.setAccessible(true);
        ExportSpecification exportSpecification = (ExportSpecification) exportSpecificationField.get(databaseExport);
        Field fsnIntTypeField = exportSpecification.getClass().getDeclaredField("fullySpecifiedDescriptionTypeIIntSet");
        fsnIntTypeField.setAccessible(true);
        fsnIntTypeField.set(exportSpecification, fsnIIntSet);

        replay(exportableConcept);

        for (I_GetConceptData conceptData : exportableConcepts) {
            databaseExport.processConcept(conceptData);
        }

        Field rf2OutputHandlerField = databaseExportClass.getDeclaredField("rf2OutputHandler");
        rf2OutputHandlerField.setAccessible(true);
        rf2OutputHandler = (Rf2OutputHandler)rf2OutputHandlerField.get(databaseExport);
        Method closeFilesMethod = rf2OutputHandler.getClass().getDeclaredMethod("closeFiles");
        closeFilesMethod.setAccessible(true);
        closeFilesMethod.invoke(rf2OutputHandler);
    }

    /**
     * Minimum core concept export. More complex testing can be based from hear
     *
     * @throws Exception
     */
    @Test
    public void testExportableConcept() throws Exception {
        /////////////////////////////////////
        // Create mock concept to process. //
        /////////////////////////////////////

        int exportConceptNid = 10;
        List<UUID> exportConceptUuidList = new ArrayList<UUID>();

        I_IdVersioned exportIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData exportableConcept = mockConcept(exportConceptNid, exportVersion, activeStatusNid, exportDate,
            exportConceptUuidList, exportPositionConceptData, pathUuidList, exportIdVersioned,
            new ArrayList<I_IdPart>(), incluesionRootConceptData, exclusionsRootConceptData);

        expect(termFactory.getAllExtensionsForComponent(exportConceptNid)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);

        int exportConcept2Nid = 11;
        List<UUID> exportConcept2UuidList = new ArrayList<UUID>();
        List<I_IdPart> conceptIdParts2 = new ArrayList<I_IdPart>();
        I_IdVersioned exportIdVersioned2 = createMock(I_IdVersioned.class);
        I_GetConceptData exportableConcept2 = mockConcept(exportConcept2Nid, exportVersion, activeStatusNid, exportDate,
            exportConcept2UuidList, exportPositionConceptData, pathUuidList, exportIdVersioned2,
            conceptIdParts2, incluesionRootConceptData, exclusionsRootConceptData);

        /////////////////////////
        //Concept descriptions //
        /////////////////////////
        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        List<UUID> exportFsnUuidList = new ArrayList<UUID>();
        exportFsnUuidList.add(UUID.randomUUID());

        int fsnDescriptionId = 100;
        I_DescriptionTuple exportableFsnDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, activeStatusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            fullySpecifiedDescriptionConceptData, fullySpecifiedNameTypeNid, fsnDescriptionId, "FSN text", exportFsnUuidList);

        expect(termFactory.getAllExtensionsForComponent(fsnDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        //setup fsn
        List<I_DescriptionTuple> descriptionFsnTuples = new ArrayList<I_DescriptionTuple>();
        descriptionFsnTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, fsnIIntSet, null, true)).andReturn(descriptionFsnTuples);

        int ptDescriptionId = 101;
        List<UUID> exportPtUuidList = new ArrayList<UUID>();
        exportPtUuidList.add(UUID.randomUUID());
        I_DescriptionTuple exportablepreferredDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, activeStatusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            preferredDescriptionTypeConceptData, preferredDescriptionTypeNid, ptDescriptionId, "Preferred text", exportPtUuidList);

        expect(termFactory.getAllExtensionsForComponent(ptDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        descriptionTuples.add(exportablepreferredDescriptionTuple);
        descriptionTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, null, null, true)).andReturn(descriptionTuples);

        ///////////////////////////
        // concept relationships //
        ///////////////////////////
        List<I_RelTuple> relationshipTuples = new ArrayList<I_RelTuple>();
        expect(exportableConcept.getSourceRelTuples(null, null, null, false, true)).andReturn(relationshipTuples);
        int relId = 200;

        List<UUID> exportRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(relId, relationshipTuples, exportVersion, activeStatusNid, exportDate, exportRelationshipUuidList, exportableConcept,
            exportConceptUuidList, exportConceptNid, exportConcept2UuidList, exportConcept2Nid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicNid, isANid);

        expect(termFactory.getAllExtensionsForComponent(relId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        org.easymock.classextension.EasyMock.replay(incluesionRootConceptData, exclusionsRootConceptData, exportPositionConceptData);

        replay(termFactory);

        Field testingField = databaseExport.getClass().getDeclaredField("testing");
        testingField.setAccessible(true);
        testingField.set(databaseExport, true);

        databaseExport.execute();
        testingField.set(databaseExport, false);

        Field exportSpecificationField = databaseExport.getClass().getDeclaredField("exportSpecification");
        exportSpecificationField.setAccessible(true);
        ExportSpecification exportSpecification = (ExportSpecification) exportSpecificationField.get(databaseExport);
        Field fsnIntTypeField = exportSpecification.getClass().getDeclaredField("fullySpecifiedDescriptionTypeIIntSet");
        fsnIntTypeField.setAccessible(true);
        fsnIntTypeField.set(exportSpecification, fsnIIntSet);

        replay(exportableConcept, exportIdVersioned, exportableConcept2, exportIdVersioned2);

        databaseExport.processConcept(exportableConcept);

        Field rf2OutputHandlerField = databaseExportClass.getDeclaredField("rf2OutputHandler");
        rf2OutputHandlerField.setAccessible(true);
        rf2OutputHandler = (Rf2OutputHandler)rf2OutputHandlerField.get(databaseExport);
        Method closeFilesMethod = rf2OutputHandler.getClass().getDeclaredMethod("closeFiles");
        closeFilesMethod.setAccessible(true);
        closeFilesMethod.invoke(rf2OutputHandler);
    }

    private I_GetConceptData createExportableConcept(int exportConceptNid, List<UUID> exportConceptUuidList, int exportConcept2Nid, int statusNid) throws Exception {
        /////////////////////////////////////
        // Create mock concept to process. //
        /////////////////////////////////////

        I_IdVersioned exportIdVersioned = createMock(I_IdVersioned.class);
        I_GetConceptData exportableConcept = mockConcept(exportConceptNid, exportVersion, statusNid, exportDate,
            exportConceptUuidList, exportPositionConceptData, pathUuidList, exportIdVersioned,
            new ArrayList<I_IdPart>(), incluesionRootConceptData, exclusionsRootConceptData);

        expect(termFactory.getAllExtensionsForComponent(exportConceptNid)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);

        List<UUID> exportConcept2UuidList = new ArrayList<UUID>();
        List<I_IdPart> conceptIdParts2 = new ArrayList<I_IdPart>();
        I_IdVersioned exportIdVersioned2 = createMock(I_IdVersioned.class);
        I_GetConceptData exportableConcept2 = mockConcept(exportConcept2Nid, exportVersion, statusNid, exportDate,
            exportConcept2UuidList, exportPositionConceptData, pathUuidList, exportIdVersioned2,
            conceptIdParts2, incluesionRootConceptData, exclusionsRootConceptData);

        /////////////////////////
        //Concept descriptions //
        /////////////////////////
        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        List<UUID> exportFsnUuidList = new ArrayList<UUID>();
        exportFsnUuidList.add(UUID.randomUUID());

        int fsnDescriptionId = exportConceptNid * 10000;
        I_DescriptionTuple exportableFsnDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, statusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            fullySpecifiedDescriptionConceptData, fullySpecifiedNameTypeNid, fsnDescriptionId, "FSN text", exportFsnUuidList);

        expect(termFactory.getAllExtensionsForComponent(fsnDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        //setup fsn
        List<I_DescriptionTuple> descriptionFsnTuples = new ArrayList<I_DescriptionTuple>();
        descriptionFsnTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, fsnIIntSet, null, true)).andReturn(descriptionFsnTuples);

        int ptDescriptionId = exportConceptNid * 10000 + 1;
        List<UUID> exportPtUuidList = new ArrayList<UUID>();
        exportPtUuidList.add(UUID.randomUUID());
        I_DescriptionTuple exportablepreferredDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, statusNid, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid,
            preferredDescriptionTypeConceptData, preferredDescriptionTypeNid, ptDescriptionId, "Preferred text", exportPtUuidList);

        expect(termFactory.getAllExtensionsForComponent(ptDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        descriptionTuples.add(exportablepreferredDescriptionTuple);
        descriptionTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, null, null, true)).andReturn(descriptionTuples);

        ///////////////////////////
        // concept relationships //
        ///////////////////////////
        List<I_RelTuple> relationshipTuples = new ArrayList<I_RelTuple>();
        expect(exportableConcept.getSourceRelTuples(null, null, null, false, true)).andReturn(relationshipTuples);
        int relId = exportConcept2Nid * 20000;

        List<UUID> exportRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(relId, relationshipTuples, exportVersion, statusNid, exportDate, exportRelationshipUuidList, exportableConcept,
            exportConceptUuidList, exportConceptNid, exportConcept2UuidList, exportConcept2Nid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicNid, isANid);

        expect(termFactory.getAllExtensionsForComponent(relId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        replay(exportIdVersioned, exportableConcept2, exportIdVersioned2);

        return exportableConcept;
    }
}
