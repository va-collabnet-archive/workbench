package org.dwfa.mojo.export.file;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import junit.framework.Assert;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.file.rf1.Rf1ConceptReader;
import org.dwfa.mojo.file.rf1.Rf1ConceptRow;
import org.dwfa.mojo.file.rf1.Rf1DescriptionReader;
import org.dwfa.mojo.file.rf1.Rf1DescriptionRow;
import org.dwfa.mojo.file.rf1.Rf1RelationshipReader;
import org.dwfa.mojo.file.rf1.Rf1RelationshipRow;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class Rf1OutputHandlerTest {

    public static final String UUID_MAP_TEST_DATABASE_PASSWORD = "uuid.map.test.database.password";
    public static final String UUID_MAP_TEST_DATABASE_USER = "uuid.map.test.database.user";
    public static final String UUID_MAP_TEST_DATABASE_URL = "uuid.map.test.database.url";
    public static final String UUID_MAP_TEST_DATABASE_DRIVER = "uuid.map.test.database.driver";
    static Rf1OutputHandler rf1OutputHandler;
    static File dbDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "test-id-db");
    static File exportDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "rf1");

    @Before
    public void setUp() throws IOException, SQLException, ClassNotFoundException {
        if (System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER) == null) {
            UuidSctidMapDb.setDatabaseProperties("org.apache.derby.jdbc.EmbeddedDriver", 
                "jdbc:derby:directory:" + dbDirectory.getCanonicalPath() + ";create=true;");

        } else {
            UuidSctidMapDb.setDatabaseProperties(System.getProperty(UUID_MAP_TEST_DATABASE_DRIVER), 
                System.getProperty(UUID_MAP_TEST_DATABASE_URL), 
                System.getProperty(UUID_MAP_TEST_DATABASE_USER), 
                System.getProperty(UUID_MAP_TEST_DATABASE_PASSWORD));
        }
        rf1OutputHandler = new Rf1OutputHandler(exportDirectory);
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void testExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());
        setConceptDtoData(componentDto.getConceptDto());

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(componentDto.getConceptDto().getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(componentDto.getConceptDto().getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        rf1OutputHandler.export(componentDto);

        rf1OutputHandler.closeFiles();

        Rf1ConceptReader rf1ConceptReader = new Rf1ConceptReader(new File(exportDirectory, "concepts.rf1.txt"));
        rf1ConceptReader.setHasHeader(true);
        assertConceptRow(componentDto, rf1ConceptReader.iterator().next());

        Rf1DescriptionReader Rf1DescriptionReader = new Rf1DescriptionReader(new File(exportDirectory, "descriptions.rf1.txt"));
        Rf1DescriptionReader.setHasHeader(true);
        Iterator<Rf1DescriptionRow> descriptionIterator = Rf1DescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        Rf1RelationshipReader Rf1RelationshipReader = new Rf1RelationshipReader(new File(exportDirectory, "relationships.rf1.txt"));
        Rf1RelationshipReader.setHasHeader(true);

        Rf1RelationshipRow relationshipRow = Rf1RelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);
    }

    private void assetRelationshipRow(Rf1RelationshipRow relationshipRow, RelationshipDto relationshipDto)
            throws Exception {
        Assert.assertEquals(getSctId(relationshipDto.getConceptId(), relationshipDto),
            relationshipRow.getRelationshipSctId());
        Assert.assertEquals(getSctId(relationshipDto.getDestinationId(), relationshipDto),
            relationshipRow.getDestinationSctId());
        Assert.assertEquals(getSctId(relationshipDto.getSourceId(), relationshipDto), relationshipRow.getSourceSctId());
        Assert.assertEquals(getSctId(relationshipDto.getTypeId(), relationshipDto),
            relationshipRow.getRelationshipType());
        Assert.assertEquals(relationshipDto.getCharacteristicTypeCode().toString(),
            relationshipRow.getCharacteristicType());
        Assert.assertEquals(relationshipDto.getRefinable().toString(), relationshipRow.getRefinability());
        Assert.assertEquals(relationshipDto.getRelationshipGroupCode().toString(),
            relationshipRow.getRelationshipGroup());
    }

    private void assertDescriptionRow(DescriptionDto descriptionDto, Rf1DescriptionRow descriptionRow) throws Exception {

        Assert.assertEquals(getSctId(descriptionDto.getConceptId(), descriptionDto), descriptionRow.getConceptSctId());
        Assert.assertEquals(getSctId(descriptionDto.getDescriptionId(), descriptionDto),
            descriptionRow.getDescriptionSctId());
        Assert.assertEquals(descriptionDto.getDescription(), descriptionRow.getTerm());
        Assert.assertEquals(descriptionDto.getStatusCode(), descriptionRow.getDescriptionStatus());
        Assert.assertEquals(descriptionDto.getInitialCapitalStatusCode().toString(), descriptionRow.getInitialCapitalStatus());
        Assert.assertEquals(descriptionDto.getDescriptionTypeCode().toString(), descriptionRow.getDescriptionType());
        Assert.assertEquals(descriptionDto.getLanguageCode(), descriptionRow.getLanaguageCode());
    }

    private void assertConceptRow(ComponentDto componentDto, Rf1ConceptRow rf1ConceptRow) throws Exception {
        Assert.assertEquals(getSctId(componentDto.getConceptDto().getConceptId(), componentDto.getConceptDto()),
            rf1ConceptRow.getConceptSctId());

        ConceptDto conceptDto = componentDto.getConceptDto();

        Assert.assertEquals(getSctId(conceptDto.getConceptId(), conceptDto).toString(), rf1ConceptRow.getConceptSctId());
        Assert.assertEquals(conceptDto.getStatusCode(), rf1ConceptRow.getConceptStatus());
        Assert.assertEquals(conceptDto.getCtv3Id(), rf1ConceptRow.getCtv3Id());
        Assert.assertEquals(conceptDto.getSnomedId(), rf1ConceptRow.getSnomedId());
        Assert.assertEquals(conceptDto.getFullySpecifiedName(), rf1ConceptRow.getFullySpecifiedName());
        Assert.assertEquals((conceptDto.isPrimative()) ? "1" : "0", rf1ConceptRow.getIsPrimitve());

    }

    private String getSctId(UUID id, Concept concept, TYPE type) throws Exception {
        String sctId = null;

        sctId = rf1OutputHandler.snomedIdHandler.getWithoutGeneration(id, concept.getNamespace(), type).toString();

        return sctId;
    }

    private String getSctId(UUID id, Concept concept) throws Exception {
        return getSctId(id, concept, concept.getType());
    }

    @Test
    public void testExportMissingConceptDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setConceptId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a concepts id");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setDateTime(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a date");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setPathId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a path id");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setStatusId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a status");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setType(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a type");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setNamespace(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Namespace");
        } catch (Exception e) {

        }
    }

    @Test
    public void testExportMissingDescriptionValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());
        setConceptDtoData(componentDto.getConceptDto());

        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));


        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have 2 or more descriptions for a concept");
        } catch (Exception e) {

        }

    }

    @Test
    public void testExportMissingDescriptionDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());
        setConceptDtoData(componentDto.getConceptDto());

        componentDto.setDescriptionDtos(new ArrayList<DescriptionDto>());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));

        componentDto.getDescriptionDtos().get(0).setDescription(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a description");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a description id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setCaseSignificanceId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have Case Significance Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionTypeCode(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have Description Type Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setInitialCapitalStatusCode(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have Initial Capital Status Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageCode(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have Language Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have Language Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setTypeId(null);
        try{
            rf1OutputHandler.export(componentDto);
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
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setCharacteristicTypeId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setModifierId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Modifier Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setRelationshipGroupCode(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Relationship Group Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setSourceId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Source Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setDestinationId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Destination Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setTypeId(null);
        try{
            rf1OutputHandler.export(componentDto);
            Assert.fail("Must have a Type Id");
        } catch (Exception e) {

        }
    }

    private ConceptDto setConceptDtoData(ConceptDto conceptDto) {
        conceptDto.setActive(true);
        conceptDto.setConceptId(UUID.randomUUID());
        conceptDto.setDateTime(new Date());
        conceptDto.setFullySpecifiedName("Flamingducks");
        conceptDto.setNamespace(NAMESPACE.NEHTA);
        conceptDto.setPathId(UUID.randomUUID());
        conceptDto.setPrimative(false);
        conceptDto.setStatusId(UUID.randomUUID());
        conceptDto.setType(TYPE.CONCEPT);
        conceptDto.setSnomedId("23423");
        conceptDto.setCtv3Id("iiyu34");
        conceptDto.setPrimative(true);
        conceptDto.setStatusCode("2");

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
        descriptionDto.setInitialCapitalStatusCode('2');
        descriptionDto.setDescriptionTypeCode('1');
        descriptionDto.setLanguageCode("en-OZ");

        return descriptionDto;
    }

    private RelationshipDto setRelationshipDto(RelationshipDto relationshipDto) {
        setConceptDtoData(relationshipDto);

        relationshipDto.setSourceId(UUID.randomUUID());
        relationshipDto.setDestinationId(UUID.randomUUID());
        relationshipDto.setCharacteristicTypeCode('0');
        relationshipDto.setCharacteristicTypeId(UUID.randomUUID());
        relationshipDto.setModifierId(UUID.randomUUID());
        relationshipDto.setRefinable('1');
        relationshipDto.setRelationshipGroupCode('1');
        relationshipDto.setTypeId(UUID.randomUUID());
        relationshipDto.setCharacteristicTypeCode('0');
        relationshipDto.setRefinable('0');

        return relationshipDto;
    }
}
