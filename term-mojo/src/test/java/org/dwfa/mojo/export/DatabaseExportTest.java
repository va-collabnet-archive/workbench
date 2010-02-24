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
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
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
    private I_GetConceptData incluesionRootConceptData;
    private I_GetConceptData exclusionsRootConceptData;
    private Class<? extends DatabaseExport> databaseExportClass;

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
        org.easymock.classextension.EasyMock.expect(positionDescriptor.getTimeString()).andReturn(AceDateFormat.getRf2TimezoneDateFormat().format(exportDate).replace("+1000", "Z"));

        PositionDescriptor[] exportPositions = new PositionDescriptor[]{positionDescriptor};
        Field positionsForExport = databaseExportClass.getDeclaredField("positionsForExport");
        positionsForExport.setAccessible(true);
        positionsForExport.set(databaseExport, exportPositions);

        //mock the include root
        ConceptDescriptor incluesionRoot = createMock(ConceptDescriptor.class);

        incluesionRootConceptData = createMock(I_GetConceptData.class);
        org.easymock.classextension.EasyMock.expect(incluesionRoot.getVerifiedConcept()).andReturn(incluesionRootConceptData);

        ConceptDescriptor[] includedConceptDescriptors = new ConceptDescriptor[]{incluesionRoot};
        Field inclusions = databaseExportClass.getDeclaredField("inclusions");
        inclusions.setAccessible(true);
        inclusions.set(databaseExport, includedConceptDescriptors);

        //mock the exclude root
        ConceptDescriptor exclusionsRoot = createMock(ConceptDescriptor.class);
        exclusionsRootConceptData = createMock(I_GetConceptData.class);
        org.easymock.classextension.EasyMock.expect(exclusionsRoot.getVerifiedConcept()).andReturn(exclusionsRootConceptData);

        ConceptDescriptor[] excludedConceptDescriptors = new ConceptDescriptor[]{exclusionsRoot};
        Field exclusions = databaseExportClass.getDeclaredField("exclusions");
        exclusions.setAccessible(true);
        exclusions.set(databaseExport, excludedConceptDescriptors);
        org.easymock.classextension.EasyMock.replay(incluesionRoot, exclusionsRoot, positionDescriptor, conceptDescriptor);

        //mock export specification constants
        activeUuidList = new ArrayList<UUID>();
        activeUuidList.add(UUID.randomUUID());
        activeConceptData = mockConceptEnum(activeUuidList, ArchitectonicAuxiliary.Concept.ACTIVE, activeStatusNid);
        expect(activeConceptData.isParentOf(activeConceptData, false)).andReturn(true).anyTimes();
        replay(activeConceptData);

        //setup the export output handler details
        Field exportDirectoryField = databaseExportClass.getDeclaredField("exportDirectory");
        exportDirectoryField.setAccessible(true);
        exportDirectoryField.set(databaseExport, exportDirectory);

        Field sctIdDbDirectoryField = databaseExportClass.getDeclaredField("SctIdDbDirectory");
        sctIdDbDirectoryField.setAccessible(true);
        sctIdDbDirectoryField.set(databaseExport, dbDirectory);

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
        /////////////////////////////////////
        // Create mock concept to process. //
        /////////////////////////////////////
        List<I_ThinExtByRefVersioned> conceptExtensions = new ArrayList<I_ThinExtByRefVersioned>();

        int exportConceptNid = 10;
        List<UUID> exportConceptUuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept = mockConcept(exportConceptNid, exportVersion, exportDate,
            exportConceptUuidList, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        int exportConcept2Nid = 11;
        List<UUID> exportConcept2UuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept2 = mockConcept(exportConcept2Nid, exportVersion, exportDate,
            exportConcept2UuidList, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        int exportConcept3Nid = 12;
        List<UUID> exportConcept3UuidList = new ArrayList<UUID>();
        I_GetConceptData exportableConcept3 = mockConcept(exportConcept3Nid, exportVersion, exportDate,
            exportConcept3UuidList, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        ///////////////////////////
        //Concept reference sets //
        ///////////////////////////
        int refsetNid = 1000;
        int memberNid = 10000;

        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_GetConceptData refsetConcept = mockConcept(refsetNid, exportVersion, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData);

        mockExtension(refsetConcept, exportVersion, exportDate, refsetNid, memberNid, exportConceptNid,
            exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData, conceptExtensions);

        //////////////////////////////////
        //Concept string reference sets //
        //////////////////////////////////
        int refsetStringNid = 1010;
        int memberStringNid = 10100;

        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_GetConceptData refsetStringConcept = mockConcept(refsetStringNid, exportVersion, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData);

        mockExtension(refsetStringConcept, exportVersion, exportDate, refsetStringNid, memberStringNid,
            exportConceptNid, "Woolie", exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetStringConcept);

        //////////////////////////////////
        //Concept Integer reference sets //
        //////////////////////////////////
        int refsetIntegerNid = 1020;
        int memberIntegerNid = 10200;

        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);
        I_GetConceptData refsetIntegerConcept = mockConcept(refsetIntegerNid, exportVersion, exportDate, new ArrayList<UUID>(),
            exportPositionConceptData, pathUuidList, incluesionRootConceptData, exclusionsRootConceptData);

        mockExtension(refsetIntegerConcept, exportVersion, exportDate, refsetIntegerNid, memberIntegerNid,
            exportConceptNid, new Integer("80085"), exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetIntegerConcept);

        ///////////////////////////////////
        //Concept Concept reference sets //
        ///////////////////////////////////
        int refsetCcNid = 1100;
        int memberCcNid = 11000;

        expect(exportableConcept2.getConceptId()).andReturn(exportConcept2Nid);
        I_GetConceptData refsetConceptConcept = mockConcept(refsetCcNid, exportVersion, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        mockExtension(refsetConceptConcept, exportVersion, exportDate, refsetCcNid, memberCcNid, exportConceptNid,
            exportConcept2Nid, exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConcept, exportableConcept2);

        ///////////////////////////////////
        //Concept Concept reference sets //
        ///////////////////////////////////
        int refsetCcsNid = 1110;
        int memberCcsNid = 11100;

        I_GetConceptData refsetConceptConceptString = mockConcept(refsetCcsNid, exportVersion, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        mockExtension(refsetConceptConceptString, exportVersion, exportDate, refsetCcsNid, memberCcsNid, exportConceptNid,
            exportConcept2Nid, "mamoth", exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConceptString);

        ///////////////////////////////////////////
        //Concept Concept Concept reference sets //
        ///////////////////////////////////////////
        int refsetCccNid = 1200;
        int memberCccNid = 12000;

        expect(exportableConcept3.getConceptId()).andReturn(exportConcept2Nid);
        I_GetConceptData refsetConceptConceptConcept = mockConcept(refsetCccNid, exportVersion, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        mockExtension(refsetConceptConcept, exportVersion, exportDate, refsetCccNid, memberCccNid, exportConceptNid,
            exportConcept2Nid, exportConcept3Nid, exportConceptUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, conceptExtensions);

        replay(refsetConceptConceptConcept, exportableConcept3);

        /////////////////////////
        //Concept descriptions //
        /////////////////////////
        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        List<UUID> exportFsnUuidList = new ArrayList<UUID>();

        int fsnDescriptionId = 100;
        I_DescriptionTuple exportableFsnDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid, fullySpecifiedDescriptionTypeUuidList,
            fullySpecifiedDescriptionConceptData, fullySpecifiedNameTypeNid, fsnDescriptionId, "FSN text", exportFsnUuidList);

        expect(termFactory.getAllExtensionsForComponent(fsnDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        //setup fsn
        List<I_DescriptionTuple> descriptionFsnTuples = new ArrayList<I_DescriptionTuple>();
        descriptionFsnTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, fsnIIntSet, null, true)).andReturn(descriptionFsnTuples);

        int ptDescriptionId = 101;
        List<UUID> exportPtUuidList = new ArrayList<UUID>();
        I_DescriptionTuple exportablepreferredDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid, preferredDescriptionTypeUuidList,
            preferredDescriptionTypeConceptData, preferredTypeNid, ptDescriptionId, "Preferred text", exportPtUuidList);

        descriptionTuples.add(exportablepreferredDescriptionTuple);
        descriptionTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(true)).andReturn(descriptionTuples);

        //refsets
        List<I_ThinExtByRefVersioned> descriptionExtensions = new ArrayList<I_ThinExtByRefVersioned>();
        int descRefsetNid = 2000;
        int descMemberNid = 20000;

        I_GetConceptData descriptionRefsetConcept = mockConcept(descRefsetNid, exportVersion, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        mockExtension(descriptionRefsetConcept, exportVersion, exportDate, descRefsetNid, descMemberNid,
            ptDescriptionId, exportPtUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, descriptionExtensions);

        expect(termFactory.getAllExtensionsForComponent(ptDescriptionId)).andReturn(descriptionExtensions);

        replay(descriptionRefsetConcept);
        ///////////////////////////
        // concept relationships //
        ///////////////////////////
        int relId = 200;

        List<UUID> exportRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(relId, exportVersion, exportDate, exportRelationshipUuidList, exportableConcept,
            exportConceptUuidList, exportConceptNid, ptDescriptionId, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicUuidList,
            definingCharacteristicNid, optionalRefinabilityUuidList);

        //Relationship extensions
        List<I_ThinExtByRefVersioned> relationshipExtensions = new ArrayList<I_ThinExtByRefVersioned>();
        int relRefsetNid = 3000;
        int relMemberNid = 30000;

        I_GetConceptData relationshipRefsetConcept = mockConcept(relRefsetNid, exportVersion, exportDate,
            new ArrayList<UUID>(), exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        mockExtension(relationshipRefsetConcept, exportVersion, exportDate, relRefsetNid, relMemberNid, relId,
            exportRelationshipUuidList, exportPathNid, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, relationshipExtensions);

        replay(refsetConcept, relationshipRefsetConcept);
        org.easymock.classextension.EasyMock.replay(incluesionRootConceptData, exclusionsRootConceptData, exportPositionConceptData);

        replay(termFactory);

        databaseExport.execute();

        Field exportSpecificationField = databaseExport.getClass().getDeclaredField("exportSpecification");
        exportSpecificationField.setAccessible(true);
        ExportSpecification exportSpecification = (ExportSpecification) exportSpecificationField.get(databaseExport);
        Field fsnIntTypeField = exportSpecification.getClass().getDeclaredField("fullySpecifiedDescriptionTypeIIntSet");
        fsnIntTypeField.setAccessible(true);
        fsnIntTypeField.set(exportSpecification, fsnIIntSet);

        replay(exportableConcept);

        databaseExport.processConcept(exportableConcept);

        Field rf2OutputHandlerField = databaseExportClass.getDeclaredField("rf2OutputHandler");
        rf2OutputHandlerField.setAccessible(true);
        rf2OutputHandler = (Rf2OutputHandler)rf2OutputHandlerField.get(databaseExport);
        Method finaliseMethod = rf2OutputHandler.getClass().getDeclaredMethod("finalize");
        finaliseMethod.setAccessible(true);
        finaliseMethod.invoke(rf2OutputHandler);
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
        I_GetConceptData exportableConcept = mockConcept(exportConceptNid, exportVersion, exportDate,
            exportConceptUuidList, exportPositionConceptData, pathUuidList, incluesionRootConceptData,
            exclusionsRootConceptData);

        expect(termFactory.getAllExtensionsForComponent(exportConceptNid)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        expect(exportableConcept.getConceptId()).andReturn(exportConceptNid);

        /////////////////////////
        //Concept descriptions //
        /////////////////////////
        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        List<UUID> exportFsnUuidList = new ArrayList<UUID>();

        int fsnDescriptionId = 100;
        I_DescriptionTuple exportableFsnDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid, fullySpecifiedDescriptionTypeUuidList,
            fullySpecifiedDescriptionConceptData, fullySpecifiedNameTypeNid, fsnDescriptionId, "FSN text", exportFsnUuidList);

        expect(termFactory.getAllExtensionsForComponent(fsnDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());
        //setup fsn
        List<I_DescriptionTuple> descriptionFsnTuples = new ArrayList<I_DescriptionTuple>();
        descriptionFsnTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(null, fsnIIntSet, null, true)).andReturn(descriptionFsnTuples);

        int ptDescriptionId = 101;
        List<UUID> exportPtUuidList = new ArrayList<UUID>();
        I_DescriptionTuple exportablepreferredDescriptionTuple = getMockDescription(exportPositionConceptData,
            exportVersion, exportDate, pathUuidList, exportConceptUuidList, exportConceptNid, preferredDescriptionTypeUuidList,
            preferredDescriptionTypeConceptData, preferredTypeNid, ptDescriptionId, "Preferred text", exportPtUuidList);

        expect(termFactory.getAllExtensionsForComponent(ptDescriptionId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());

        descriptionTuples.add(exportablepreferredDescriptionTuple);
        descriptionTuples.add(exportableFsnDescriptionTuple);
        expect(exportableConcept.getDescriptionTuples(true)).andReturn(descriptionTuples);

        ///////////////////////////
        // concept relationships //
        ///////////////////////////
        int relId = 200;

        List<UUID> exportRelationshipUuidList = new ArrayList<UUID>();
        mockRelationship(relId, exportVersion, exportDate, exportRelationshipUuidList, exportableConcept,
            exportConceptUuidList, exportConceptNid, ptDescriptionId, exportPositionConceptData, pathUuidList,
            incluesionRootConceptData, exclusionsRootConceptData, definingCharacteristicUuidList,
            definingCharacteristicNid, optionalRefinabilityUuidList);

        expect(termFactory.getAllExtensionsForComponent(relId)).andReturn(new ArrayList<I_ThinExtByRefVersioned>());



        org.easymock.classextension.EasyMock.replay(incluesionRootConceptData, exclusionsRootConceptData, exportPositionConceptData);

        replay(termFactory);

        databaseExport.execute();

        Field exportSpecificationField = databaseExport.getClass().getDeclaredField("exportSpecification");
        exportSpecificationField.setAccessible(true);
        ExportSpecification exportSpecification = (ExportSpecification) exportSpecificationField.get(databaseExport);
        Field fsnIntTypeField = exportSpecification.getClass().getDeclaredField("fullySpecifiedDescriptionTypeIIntSet");
        fsnIntTypeField.setAccessible(true);
        fsnIntTypeField.set(exportSpecification, fsnIIntSet);

        replay(exportableConcept);

        databaseExport.processConcept(exportableConcept);

        Field rf2OutputHandlerField = databaseExportClass.getDeclaredField("rf2OutputHandler");
        rf2OutputHandlerField.setAccessible(true);
        rf2OutputHandler = (Rf2OutputHandler)rf2OutputHandlerField.get(databaseExport);
        Method finaliseMethod = rf2OutputHandler.getClass().getDeclaredMethod("finalize");
        finaliseMethod.setAccessible(true);
        finaliseMethod.invoke(rf2OutputHandler);
    }
}
