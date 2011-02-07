package org.dwfa.mojo.export.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.file.rf2.Rf2ConceptReader;
import org.dwfa.mojo.file.rf2.Rf2ConceptRow;
import org.dwfa.mojo.file.rf2.Rf2DescriptionReader;
import org.dwfa.mojo.file.rf2.Rf2DescriptionRow;
import org.dwfa.mojo.file.rf2.Rf2IdentifierReader;
import org.dwfa.mojo.file.rf2.Rf2IdentifierRow;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetReader;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetRow;
import org.dwfa.mojo.file.rf2.Rf2RelationshipReader;
import org.dwfa.mojo.file.rf2.Rf2RelationshipRow;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the Rf2OutputHandler - tests will be executed against a Derby embedded database unless the following system
 * properties are set
 * <ul>
 * <li>uuid.map.test.database.password</li>
 * <li>uuid.map.test.database.user</li>
 * <li>uuid.map.test.database.url</li>
 * <li>uuid.map.test.database.driver</li>
 * </ul>
 */
public class Rf2OutputHandlerTest {

    public static final String UUID_MAP_TEST_DATABASE_PASSWORD = "uuid.map.test.database.password";
    public static final String UUID_MAP_TEST_DATABASE_USER = "uuid.map.test.database.user";
    public static final String UUID_MAP_TEST_DATABASE_URL = "uuid.map.test.database.url";
    public static final String UUID_MAP_TEST_DATABASE_DRIVER = "uuid.map.test.database.driver";
    static Rf2OutputHandler rf2OutputHandler;
    static File dbDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "test-id-db");
    static File exportDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "rf2");
    static File exportDirectoryFull = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "rf2" +File.separatorChar + "full");
    static File exportClinicalRefsetDirectory = new File("target" + File.separatorChar + "test-classes"
        + File.separatorChar + "rf2" + File.separatorChar + "full" + File.separatorChar + "refsets" + File.separatorChar + "clinical");
    static File exportStructuralRefsetDirectory = new File("target" + File.separatorChar + "test-classes"
        + File.separatorChar + "rf2" + File.separatorChar + "full" + File.separatorChar + "refsets" + File.separatorChar + "structural"
        + File.separatorChar);

    @Before
    public void setUp() throws Exception {

        exportDirectory.mkdirs();
        if (System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER) == null) {
            UuidSctidMapDb.setDatabaseProperties("org.apache.derby.jdbc.EmbeddedDriver",
                "jdbc:derby:directory:" + dbDirectory.getCanonicalPath() + ";create=true;");

        } else {
            UuidSctidMapDb.setDatabaseProperties(System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER),
                System.getProperty(UUID_MAP_TEST_DATABASE_URL),
                System.getProperty(UUID_MAP_TEST_DATABASE_USER),
                System.getProperty(UUID_MAP_TEST_DATABASE_PASSWORD));
        }

        rf2OutputHandler = new Rf2OutputHandler(exportDirectory, new HashMap<UUID, Map<UUID, Date>>(0));
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void testExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);
        conceptDto.getIdentifierDtos().add(setIdentifierDtoData(new IdentifierDto(), 325700321000036104l));
        conceptDto.setConceptId(getIdMap(
            conceptDto.getIdentifierDtos().get(0).getConceptId().keySet().iterator().next(), null));

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setFullySpecifiedName("Ducks!");
        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(1).setValue(null);
        componentDto.getConceptExtensionDtos().get(1).setFullySpecifiedName("Moreducks");

        componentDto.getDescriptionExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getDescriptionExtensionDtos().get(0).setConcept2Id(getIdMap(UUID.randomUUID(), null));
        componentDto.getDescriptionExtensionDtos().get(0).setFullySpecifiedName("Descriptionduck");

        componentDto.getRelationshipExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getRelationshipExtensionDtos().get(0).setConcept2Id(getIdMap(UUID.randomUUID(), null));
        componentDto.getRelationshipExtensionDtos().get(0).setConcept3Id(getIdMap(UUID.randomUUID(), null));
        componentDto.getRelationshipExtensionDtos().get(0).setFullySpecifiedName("Duckdelations");

        rf2OutputHandler.export(componentDto);

        rf2OutputHandler.closeFiles();

        Rf2IdentifierReader rf2IdentifierReader = new Rf2IdentifierReader(new File(exportDirectoryFull, "ids.rf2.txt"));
        rf2IdentifierReader.setHasHeader(true);
        assertIdentifierRow(conceptDto.getIdentifierDtos().get(0),
            rf2IdentifierReader.iterator().next());

        Rf2ConceptReader rf2ConceptReader = new Rf2ConceptReader(new File(exportDirectoryFull, "concepts.rf2.txt"));
        rf2ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf2ConceptReader.iterator().next());

        Rf2DescriptionReader rf2DescriptionReader = new Rf2DescriptionReader(new File(exportDirectoryFull, "descriptions.rf2.txt"));
        rf2DescriptionReader.setHasHeader(true);
        Iterator<Rf2DescriptionRow> descriptionIterator = rf2DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf2RelationshipReader rf2RelationshipReader = new Rf2RelationshipReader(new File(exportDirectoryFull, "relationships.rf2.txt"));
        rf2RelationshipReader.setHasHeader(true);

        Rf2RelationshipRow relationshipRow = rf2RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);

        for (ExtensionDto extensionDto : componentDto.getConceptExtensionDtos()) {
            String referenceSetName = extensionDto.getFullySpecifiedName();
            Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
            rf2ReferenceSetReader.setHasHeader(true);

            for (Rf2ReferenceSetRow rf2ReferenceSetRow : rf2ReferenceSetReader) {
                assertRefsetRow(rf2ReferenceSetRow, extensionDto);
            }
        }

        for (ExtensionDto extensionDto : componentDto.getDescriptionExtensionDtos()) {
            String referenceSetName = extensionDto.getFullySpecifiedName();
            Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
            rf2ReferenceSetReader.setHasHeader(true);

            for (Rf2ReferenceSetRow rf2ReferenceSetRow : rf2ReferenceSetReader) {
                assertRefsetRow(rf2ReferenceSetRow, extensionDto);
            }
        }

        for (ExtensionDto extensionDto : componentDto.getRelationshipExtensionDtos()) {
            String referenceSetName = extensionDto.getFullySpecifiedName();
            Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
            rf2ReferenceSetReader.setHasHeader(true);

            for (Rf2ReferenceSetRow rf2ReferenceSetRow : rf2ReferenceSetReader) {
                assertRefsetRow(rf2ReferenceSetRow, extensionDto);
            }
        }
    }

    @Test
    public void testExportConceptRefset() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageCode("en-AU");
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setIsClinical(false);
        componentDto.getConceptExtensionDtos().get(0).setValue(null);

        rf2OutputHandler.export(componentDto);

        rf2OutputHandler.closeFiles();

        Rf2ConceptReader rf2ConceptReader = new Rf2ConceptReader(new File(exportDirectoryFull, "concepts.rf2.txt"));
        rf2ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf2ConceptReader.iterator().next());

        Rf2DescriptionReader rf2DescriptionReader = new Rf2DescriptionReader(new File(exportDirectoryFull, "descriptions.rf2.txt"));
        rf2DescriptionReader.setHasHeader(true);
        Iterator<Rf2DescriptionRow> descriptionIterator = rf2DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf2RelationshipReader rf2RelationshipReader = new Rf2RelationshipReader(new File(exportDirectoryFull, "relationships.rf2.txt"));
        rf2RelationshipReader.setHasHeader(true);

        Rf2RelationshipRow relationshipRow = rf2RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);

        String referenceSetName = componentDto.getConceptExtensionDtos().get(0).getFullySpecifiedName();
        Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportStructuralRefsetDirectory, referenceSetName+ ".txt"));
        rf2ReferenceSetReader.setHasHeader(true);

        Rf2ReferenceSetRow referenceSetRow = rf2ReferenceSetReader.iterator().next();
        ExtensionDto extensionDto = componentDto.getConceptExtensionDtos().get(0);
        assertRefsetRow(referenceSetRow, extensionDto);
    }

    @Test
    public void testComponetComponentRefsetExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setValue(null);
        componentDto.getConceptExtensionDtos().get(0).setConcept2Id(getIdMap(UUID.randomUUID(), null));

        rf2OutputHandler.export(componentDto);

        rf2OutputHandler.closeFiles();

        Rf2ConceptReader rf2ConceptReader = new Rf2ConceptReader(new File(exportDirectoryFull, "concepts.rf2.txt"));
        rf2ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf2ConceptReader.iterator().next());

        Rf2DescriptionReader rf2DescriptionReader = new Rf2DescriptionReader(new File(exportDirectoryFull, "descriptions.rf2.txt"));
        rf2DescriptionReader.setHasHeader(true);
        Iterator<Rf2DescriptionRow> descriptionIterator = rf2DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf2RelationshipReader rf2RelationshipReader = new Rf2RelationshipReader(new File(exportDirectoryFull, "relationships.rf2.txt"));
        rf2RelationshipReader.setHasHeader(true);

        Rf2RelationshipRow relationshipRow = rf2RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);

        String referenceSetName = componentDto.getConceptExtensionDtos().get(0).getFullySpecifiedName();
        Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
        rf2ReferenceSetReader.setHasHeader(true);

        Rf2ReferenceSetRow referenceSetRow = rf2ReferenceSetReader.iterator().next();
        ExtensionDto extensionDto = componentDto.getConceptExtensionDtos().get(0);
        assertRefsetRow(referenceSetRow, extensionDto);

        Assert.assertEquals(getSctId(extensionDto.getConcept2Id(), extensionDto), referenceSetRow.getComponentId2());
    }

    @Test
    public void testComponetComponentComponentRefsetExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setValue(null);
        componentDto.getConceptExtensionDtos().get(0).setConcept2Id(getIdMap(UUID.randomUUID(), null));
        componentDto.getConceptExtensionDtos().get(0).setConcept3Id(getIdMap(UUID.randomUUID(), null));

        rf2OutputHandler.export(componentDto);

        rf2OutputHandler.closeFiles();

        Rf2ConceptReader rf2ConceptReader = new Rf2ConceptReader(new File(exportDirectoryFull, "concepts.rf2.txt"));
        rf2ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf2ConceptReader.iterator().next());

        Rf2DescriptionReader rf2DescriptionReader = new Rf2DescriptionReader(new File(exportDirectoryFull, "descriptions.rf2.txt"));
        rf2DescriptionReader.setHasHeader(true);
        Iterator<Rf2DescriptionRow> descriptionIterator = rf2DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf2RelationshipReader rf2RelationshipReader = new Rf2RelationshipReader(new File(exportDirectoryFull, "relationships.rf2.txt"));
        rf2RelationshipReader.setHasHeader(true);

        Rf2RelationshipRow relationshipRow = rf2RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);

        String referenceSetName = componentDto.getConceptExtensionDtos().get(0).getFullySpecifiedName();
        Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
        rf2ReferenceSetReader.setHasHeader(true);

        Rf2ReferenceSetRow referenceSetRow = rf2ReferenceSetReader.iterator().next();
        ExtensionDto extensionDto = componentDto.getConceptExtensionDtos().get(0);
        assertRefsetRow(referenceSetRow, extensionDto);

        Assert.assertEquals(getSctId(extensionDto.getConcept2Id(), extensionDto), referenceSetRow.getComponentId2());
        Assert.assertEquals(getSctId(extensionDto.getConcept3Id(), extensionDto), referenceSetRow.getComponentId3());
    }

    @Test
    public void testComponetComponentComponentStringRefsetExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setConcept2Id(getIdMap(UUID.randomUUID(), null));
        componentDto.getConceptExtensionDtos().get(0).setConcept3Id(getIdMap(UUID.randomUUID(), null));

        rf2OutputHandler.export(componentDto);

        rf2OutputHandler.closeFiles();

        Rf2ConceptReader rf2ConceptReader = new Rf2ConceptReader(new File(exportDirectoryFull, "concepts.rf2.txt"));
        rf2ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf2ConceptReader.iterator().next());

        Rf2DescriptionReader rf2DescriptionReader = new Rf2DescriptionReader(new File(exportDirectoryFull, "descriptions.rf2.txt"));
        rf2DescriptionReader.setHasHeader(true);
        Iterator<Rf2DescriptionRow> descriptionIterator = rf2DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf2RelationshipReader rf2RelationshipReader = new Rf2RelationshipReader(new File(exportDirectoryFull, "relationships.rf2.txt"));
        rf2RelationshipReader.setHasHeader(true);

        Rf2RelationshipRow relationshipRow = rf2RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);

        String referenceSetName = componentDto.getConceptExtensionDtos().get(0).getFullySpecifiedName();
        Rf2ReferenceSetReader rf2ReferenceSetReader = new Rf2ReferenceSetReader(new File(exportClinicalRefsetDirectory, referenceSetName + ".txt"));
        rf2ReferenceSetReader.setHasHeader(true);

        Rf2ReferenceSetRow referenceSetRow = rf2ReferenceSetReader.iterator().next();
        ExtensionDto extensionDto = componentDto.getConceptExtensionDtos().get(0);
        assertRefsetRow(referenceSetRow, extensionDto);

        Assert.assertEquals(getSctId(extensionDto.getConcept2Id(), extensionDto), referenceSetRow.getComponentId2());
        Assert.assertEquals(getSctId(extensionDto.getConcept3Id(), extensionDto), referenceSetRow.getComponentId3());
        Assert.assertEquals(extensionDto.getValue(), referenceSetRow.getValue());
    }

    private void assertRefsetRow(Rf2ReferenceSetRow referenceSetRow, ExtensionDto extensionDto) throws Exception {
        Assert.assertEquals(getSctId(extensionDto.getMemberId(), extensionDto, TYPE.REFSET), referenceSetRow.getMemberId());
        Assert.assertEquals(getSctId(extensionDto.getReferencedConceptId(), extensionDto), referenceSetRow.getReferencedComponentId());
        Assert.assertEquals(getSctId(extensionDto.getConcept1Id(), extensionDto), referenceSetRow.getComponentId1());
        Assert.assertEquals(getSctId(extensionDto.getConceptId(), extensionDto), referenceSetRow.getRefsetId());
        Assert.assertEquals((extensionDto.isActive()) ? "1" : "0", referenceSetRow.getActive());
        Assert.assertEquals(rf2OutputHandler.getReleaseDate(extensionDto, extensionDto.getRf2DateTime()), referenceSetRow.getEffectiveTime());
        Assert.assertEquals(getSctId(extensionDto.getPathId(), extensionDto), referenceSetRow.getModuleId());
    }

    private void assetRelationshipRow(Rf2RelationshipRow relationshipRow, RelationshipDto relationshipDto)
            throws Exception {
        Assert.assertEquals(getSctId(relationshipDto.getConceptId(), relationshipDto),
            relationshipRow.getRelationshipSctId());
        Assert.assertEquals((relationshipDto.isActive()) ? "1" : "0", relationshipRow.getActive());
        Assert.assertEquals(getSctId(relationshipDto.getCharacteristicTypeId(), relationshipDto),
            relationshipRow.getCharacteristicSctId());
        Assert.assertEquals(getSctId(relationshipDto.getDestinationId(), relationshipDto),
            relationshipRow.getDestinationSctId());
        Assert.assertEquals(rf2OutputHandler.getReleaseDate(relationshipDto, relationshipDto.getRf2DateTime()),
            relationshipRow.getEffectiveTime());
        Assert.assertEquals(getSctId(relationshipDto.getModifierId(), relationshipDto),
            relationshipRow.getModifierSctId());
        Assert.assertEquals(getSctId(relationshipDto.getPathId(), relationshipDto),
            relationshipRow.getModuleSctId());
        Assert.assertEquals(getSctId(relationshipDto.getSourceId(), relationshipDto),
            relationshipRow.getSourceSctId());
        Assert.assertEquals(getSctId(relationshipDto.getTypeId(), relationshipDto),
            relationshipRow.getTypeSctId());
    }

    private void assertDescriptionRow(DescriptionDto descriptionDto, Rf2DescriptionRow descriptionRow) throws Exception {
        Assert.assertEquals(getSctId(descriptionDto.getConceptId(), descriptionDto),
            descriptionRow.getConceptSctId());
        Assert.assertEquals(getSctId(descriptionDto.getDescriptionId(), descriptionDto),
            descriptionRow.getDescriptionSctId());
        Assert.assertEquals((descriptionDto.isActive()) ? "1" : "0", descriptionRow.getActive());
        Assert.assertEquals(rf2OutputHandler.getReleaseDate(descriptionDto, descriptionDto.getRf2DateTime()),
            descriptionRow.getEffectiveTime());
        Assert.assertEquals(getSctId(descriptionDto.getCaseSignificanceId(), descriptionDto),
            descriptionRow.getCaseSignificaceSctId());
        Assert.assertEquals(getSctId(descriptionDto.getPathId(), descriptionDto),
            descriptionRow.getModuleSctId());
        Assert.assertEquals(descriptionDto.getDescription(), descriptionRow.getTerm());
        Assert.assertEquals(descriptionRow.getLanaguageCode(), "en");
        Assert.assertEquals(getSctId(descriptionDto.getRf2TypeId(), descriptionDto),
            descriptionRow.getTypeSctId());
    }


    private void assertIdentifierRow(IdentifierDto identifierDto, Rf2IdentifierRow rf2IdentifierRow) throws Exception {
        Assert.assertEquals(identifierDto.getConceptId().keySet().iterator().next().toString(), rf2IdentifierRow.getAlternateIdentifier().toString());
        Assert.assertEquals(rf2OutputHandler.getReleaseDate(identifierDto, identifierDto.getRf2DateTime()), rf2IdentifierRow.getEffectiveTime());
        Assert.assertEquals((identifierDto.isActive())?"1":"0", rf2IdentifierRow.getActive());
        Assert.assertEquals(getSctId(identifierDto.getPathId(), identifierDto), rf2IdentifierRow.getModuleSctId());
        Assert.assertEquals(identifierDto.getReferencedSctId().toString(), rf2IdentifierRow.getReferencedComponentSctId());
        Assert.assertEquals(getSctId(identifierDto.getIdentifierSchemeUuid(), identifierDto), rf2IdentifierRow.getIdentifierSchemeSctId());
    }

    private void assertConceptRow(ComponentDto componentDto, Rf2ConceptRow rf2ConceptRow) throws Exception {
        ConceptDto conceptDto = componentDto.getConceptDtos().get(0);

        Assert.assertEquals(getSctId(conceptDto.getConceptId(), conceptDto),
            rf2ConceptRow.getConceptSctId());
        Assert.assertEquals(rf2OutputHandler.getReleaseDate(conceptDto, conceptDto.getRf2DateTime()), rf2ConceptRow.getEffectiveTime());
        Assert.assertEquals((conceptDto.isActive())?"1":"0", rf2ConceptRow.getActive());
        Assert.assertEquals(getSctId(conceptDto.getPathId(), conceptDto), rf2ConceptRow.getModuleSctId());
    }

    private String getSctId(UUID id, Concept concept, TYPE type) throws Exception {
        String sctId = null;

        sctId = UuidSnomedDbMapHandler.getInstance().getWithoutGeneration(id, concept.getNamespace(), type).toString();

        return sctId;
    }

    private String getSctId(UUID id, Concept concept) throws Exception {
        return getSctId(id, concept, concept.getType());
    }

    private String getSctId(Map<UUID, Long> idMap, Concept concept) throws Exception {
        return getSctId(idMap.keySet().iterator().next(), concept, concept.getType());
    }

    @Test
    public void testExportMissingConceptDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);

        setConceptDtoData(conceptDto);
        conceptDto.setConceptId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a concepts id");
        } catch (Exception e) {

        }

        setConceptDtoData(conceptDto);
        conceptDto.setRf2DateTime(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a RF2 date");
        } catch (Exception e) {

        }

        setConceptDtoData(conceptDto);
        conceptDto.setPathId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a path id");
        } catch (Exception e) {

        }

        setConceptDtoData(conceptDto);
        conceptDto.setStatusId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a status");
        } catch (Exception e) {

        }

        setConceptDtoData(conceptDto);
        conceptDto.setType(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a type");
        } catch (Exception e) {

        }

        setConceptDtoData(conceptDto);
        conceptDto.setNamespace(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Namespace");
        } catch (Exception e) {

        }
    }

    @Test
    public void testExportMissingDescriptionValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));


        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have 2 or more descriptions for a concept");
        } catch (Exception e) {

        }

    }

    @Test
    public void testExportMissingDescriptionDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));

        componentDto.getDescriptionDtos().get(0).setDescription(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a description");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a description id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setCaseSignificanceId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Case Significance Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionTypeCode(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Description Type Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setInitialCapitalStatusCode(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Initial Capital Status Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageCode(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Language Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Language Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setTypeId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Type Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setRf2TypeId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have Type Id");
        } catch (Exception e) {

        }
    }

    @Test
    public void testExportMissingRelationshipDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));

        componentDto.setRelationshipDtos(new ArrayList<RelationshipDto>());
        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.getRelationshipDtos().get(0).setCharacteristicTypeCode(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setCharacteristicTypeId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setModifierId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Modifier Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setRelationshipGroup(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Relationship Group Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setSourceId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Source Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setDestinationId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Destination Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setTypeId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a Type Id");
        } catch (Exception e) {

        }
    }

    @Test
    public void testExportMissingExtensionDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);

        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));

        componentDto.setRelationshipDtos(new ArrayList<RelationshipDto>());
        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        componentDto.setConceptExtensionDtos(new ArrayList<ExtensionDto>());
        componentDto.getConceptExtensionDtos().add(setExtensionDto(new ExtensionDto()));

        componentDto.getConceptExtensionDtos().get(0).setConcept1Id(null);
        componentDto.getConceptExtensionDtos().get(0).setValue(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a referenced component Id");
        } catch (Exception e) {

        }

        componentDto.getConceptExtensionDtos().set(0, setExtensionDto(new ExtensionDto()));
        componentDto.getConceptExtensionDtos().get(0).setMemberId(null);
        try{
            rf2OutputHandler.export(componentDto);
            Assert.fail("Must have a member Id");
        } catch (Exception e) {

        }
    }

    private IdentifierDto setIdentifierDtoData(IdentifierDto identifierDto, long sctid) {
        identifierDto.setActive(true);
        identifierDto.setReferencedSctId(sctid);
        identifierDto.setConceptId(getIdMap(UUID.randomUUID(), null));
        identifierDto.setDateTime(new Date());
        identifierDto.setRf2DateTime(new Date());
        identifierDto.setIdentifierSchemeUuid(UUID.randomUUID());
        identifierDto.setPathId(UUID.randomUUID());
        identifierDto.setNamespace(NAMESPACE.NEHTA);
        identifierDto.setProject(PROJECT.AU);
        identifierDto.setStatusId(UUID.randomUUID());
        identifierDto.setType(TYPE.CONCEPT);

        return identifierDto;
    }

    private ConceptDto setConceptDtoData(ConceptDto conceptDto) {
        conceptDto.setActive(true);
        conceptDto.setConceptId(getIdMap(UUID.randomUUID(), null));
        conceptDto.setDateTime(new Date());
        conceptDto.setRf2DateTime(new Date());
        conceptDto.setFullySpecifiedName("Flamingducks");
        conceptDto.setNamespace(NAMESPACE.NEHTA);
        conceptDto.setProject(PROJECT.AU);
        conceptDto.setPathId(UUID.randomUUID());
        conceptDto.setPrimative(false);
        conceptDto.setDefinitionStatusUuid(UUID.randomUUID());
        conceptDto.setStatusId(UUID.randomUUID());
        conceptDto.setType(TYPE.CONCEPT);

        return conceptDto;
    }

    private DescriptionDto setDescriptionDto(DescriptionDto descriptionDto) {
        setConceptDtoData(descriptionDto);

        descriptionDto.setCaseSignificanceId(UUID.randomUUID());
        descriptionDto.setDescription("Description of flaming ducks");
        descriptionDto.setDescriptionId(UUID.randomUUID());
        descriptionDto.setDescriptionTypeCode('0');
        descriptionDto.setFullySpecifiedName("FSN");
        descriptionDto.setInitialCapitalStatusCode('1');
        descriptionDto.setLanguageCode("en");
        descriptionDto.setLanguageId(UUID.randomUUID());
        descriptionDto.setTypeId(UUID.randomUUID());
        descriptionDto.setRf2TypeId(UUID.randomUUID());

        return descriptionDto;
    }

    private RelationshipDto setRelationshipDto(RelationshipDto relationshipDto) {
        setConceptDtoData(relationshipDto);

        relationshipDto.setSourceId(UUID.randomUUID());
        relationshipDto.setDestinationId(getIdMap(UUID.randomUUID(), null));
        relationshipDto.setCharacteristicTypeCode('0');
        relationshipDto.setCharacteristicTypeId(UUID.randomUUID());
        relationshipDto.setModifierId(UUID.randomUUID());
        relationshipDto.setRefinable('1');
        relationshipDto.setRelationshipGroup(1);
        relationshipDto.setTypeId(UUID.randomUUID());

        return relationshipDto;
    }

    private ExtensionDto setExtensionDto(ExtensionDto extensionDto) {
        setConceptDtoData(extensionDto);

        extensionDto.setReferencedConceptId(getIdMap(UUID.randomUUID(), null));
        extensionDto.setConcept1Id(getIdMap(UUID.randomUUID(), null));
        extensionDto.setValue("Test String");
        extensionDto.getIdentifierDtos().add(setIdentifierDtoData(new IdentifierDto(), 44949371000036165l));
        extensionDto.setMemberId(extensionDto.getIdentifierDtos().get(0).getConceptId().keySet().iterator().next());

        return extensionDto;
    }

    private Map<UUID, Long> getIdMap(UUID uuid, Long sctId) {
        Map<UUID, Long> map = new HashMap<UUID, Long>(1);

        map.put(uuid, sctId);

        return map;
    }
}
