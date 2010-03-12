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
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.file.ace.AceConceptReader;
import org.dwfa.mojo.file.ace.AceConceptRow;
import org.dwfa.mojo.file.ace.AceDescriptionReader;
import org.dwfa.mojo.file.ace.AceDescriptionRow;
import org.dwfa.mojo.file.ace.AceIdentifierReader;
import org.dwfa.mojo.file.ace.AceIdentifierRow;
import org.dwfa.mojo.file.ace.AceRelationshipReader;
import org.dwfa.mojo.file.ace.AceRelationshipRow;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class AceOutputHandlerTest {

    static AceOutputHandler aceOutputHandler;
    static File dbDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "test-id-db");
    static File exportDirectory = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "ace");

    @Before
    public void setUp() throws IOException, SQLException, ClassNotFoundException {
        aceOutputHandler = new AceOutputHandler(exportDirectory, dbDirectory);
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void testExport() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());
        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().getIdentifierDtos().add(setIdentifierDtoData(new IdentifierDto()));

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(componentDto.getConceptDto().getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(componentDto.getConceptDto().getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        aceOutputHandler.export(componentDto);

        aceOutputHandler.closeFiles();

        AceIdentifierReader aceIdentifierReader = new AceIdentifierReader(new File(exportDirectory, "ids.ace.txt"));
        aceIdentifierReader.setHasHeader(true);
        assertIdentifierRow(componentDto.getConceptDto().getIdentifierDtos().get(0),
            aceIdentifierReader.iterator().next());

        AceConceptReader aceConceptReader = new AceConceptReader(new File(exportDirectory, "concepts.ace.txt"));
        aceConceptReader.setHasHeader(true);
        AceConceptRow aceConceptRow = aceConceptReader.iterator().next();
        assertConceptRow(componentDto, aceConceptRow);

        AceDescriptionReader aceDescriptionReader = new AceDescriptionReader(new File(exportDirectory, "descriptions.ace.txt"));
        aceDescriptionReader.setHasHeader(true);
        Iterator<AceDescriptionRow> descriptionIterator = aceDescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        AceRelationshipReader aceRelationshipReader = new AceRelationshipReader(new File(exportDirectory, "relationships.ace.txt"));
        aceRelationshipReader.setHasHeader(true);

        AceRelationshipRow relationshipRow = aceRelationshipReader.iterator().next();
        RelationshipDto relationshipDto =  componentDto.getRelationshipDtos().get(0);
        assetRelationshipRow(relationshipRow, relationshipDto);
    }

    private void assetRelationshipRow(AceRelationshipRow relationshipRow, RelationshipDto relationshipDto)
            throws Exception {
        Assert.assertEquals(relationshipDto.getConceptId().toString(), relationshipRow.getRelationshipUuid());
        Assert.assertEquals(relationshipDto.getDestinationId().toString(), relationshipRow.getConceptUuid2());
        Assert.assertEquals(relationshipDto.getSourceId().toString(), relationshipRow.getConceptUuid1());
        Assert.assertEquals(relationshipDto.getTypeId().toString(), relationshipRow.getRelationshiptypeUuid());
        Assert.assertEquals(relationshipDto.getPathId().toString(), relationshipRow.getPathUuid());
        Assert.assertEquals(relationshipDto.getRefinabilityId().toString(), relationshipRow.getRefinabilityUuid());
        Assert.assertEquals(relationshipDto.getRelationshipGroupCode().toString(),
            relationshipRow.getRelationshipGroup());
        Assert.assertEquals(relationshipDto.getStatusId().toString(), relationshipRow.getRelationshipstatusUuid());
        Assert.assertEquals(relationshipDto.getTypeId().toString(), relationshipRow.getRelationshiptypeUuid());
    }

    private void assertDescriptionRow(DescriptionDto descriptionDto, AceDescriptionRow descriptionRow) throws Exception {

        Assert.assertEquals(descriptionDto.getConceptId().toString(), descriptionRow.getConceptUuid());
        Assert.assertEquals(descriptionDto.getDescriptionId().toString(), descriptionRow.getDescriptionUuid());
        Assert.assertEquals(descriptionDto.getDescription(), descriptionRow.getTerm());
        Assert.assertEquals(descriptionDto.getStatusId().toString(), descriptionRow.getDescriptionstatusUuid());
        Assert.assertEquals(descriptionDto.getTypeId().toString(), descriptionRow.getDescriptiontypeUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(descriptionDto), descriptionRow.getEffectiveTime());
        Assert.assertEquals(descriptionDto.getLanguageId().toString(), descriptionRow.getLanguageUuid());
        Assert.assertEquals(descriptionDto.getPathId().toString(), descriptionRow.getPathUuid());
    }

    private void assertConceptRow(ComponentDto componentDto, AceConceptRow aceConceptRow) throws Exception {
        ConceptDto conceptDto = componentDto.getConceptDto();

        Assert.assertEquals(conceptDto.getConceptId().toString(), aceConceptRow.getConceptUuid());
        Assert.assertEquals(conceptDto.getStatusId().toString(), aceConceptRow.getConceptStatusUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(conceptDto), aceConceptRow.getEffectiveTime());
        Assert.assertEquals(conceptDto.getPathId().toString(), aceConceptRow.getPathUuid());
        Assert.assertEquals((conceptDto.isPrimative()) ? "1" : "0", aceConceptRow.getIsPrimitve());
    }

    private void assertIdentifierRow(IdentifierDto identifierDto, AceIdentifierRow aceIdentifierRow) throws Exception {
        Assert.assertEquals(identifierDto.getConceptId().toString(), aceIdentifierRow.getPrimaryUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(identifierDto), aceIdentifierRow.getEffectiveDate());
        Assert.assertEquals(identifierDto.getPathId().toString(), aceIdentifierRow.getPathUuid());
        Assert.assertEquals(identifierDto.getReferencedSctId().toString(), aceIdentifierRow.getSourceId());
        Assert.assertEquals(identifierDto.getIdentifierSchemeUuid().toString(), aceIdentifierRow.getSourceSystemUuid());
        Assert.assertEquals(identifierDto.getStatusId().toString(), aceIdentifierRow.getStatusUuid());
    }

    @Test
    public void testExportMissingConceptDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();

        componentDto.setConceptDto(new ConceptDto());

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setConceptId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a concepts id");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setDateTime(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a date");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setPathId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a path id");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setStatusId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a status");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setType(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a type");
        } catch (Exception e) {

        }

        setConceptDtoData(componentDto.getConceptDto());
        componentDto.getConceptDto().setNamespace(null);
        try{
            aceOutputHandler.export(componentDto);
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
            aceOutputHandler.export(componentDto);
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
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a description");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a description id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setCaseSignificanceId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Case Significance Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionTypeCode(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Description Type Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setInitialCapitalStatusCode(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Initial Capital Status Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageCode(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Language Code");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Language Id");
        } catch (Exception e) {

        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setTypeId(null);
        try{
            aceOutputHandler.export(componentDto);
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
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setCharacteristicTypeId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setModifierId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Modifier Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setRelationshipGroupCode(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Relationship Group Code");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setSourceId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Source Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setDestinationId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Destination Id");
        } catch (Exception e) {

        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setTypeId(null);
        try{
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Type Id");
        } catch (Exception e) {

        }
    }

    private IdentifierDto setIdentifierDtoData(IdentifierDto identifierDto) {
        identifierDto.setActive(true);
        identifierDto.setReferencedSctId(900000000000960019l);
        identifierDto.setConceptId(UUID.randomUUID());
        identifierDto.setDateTime(new Date());
        identifierDto.setIdentifierSchemeUuid(UUID.randomUUID());
        identifierDto.setPathId(UUID.randomUUID());
        identifierDto.setNamespace(NAMESPACE.NEHTA);
        identifierDto.setStatusId(UUID.randomUUID());
        identifierDto.setType(TYPE.CONCEPT);

        return identifierDto;
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
        relationshipDto.setRefinabilityId(UUID.randomUUID());
        relationshipDto.setRelationshipGroupCode('1');
        relationshipDto.setTypeId(UUID.randomUUID());
        relationshipDto.setCharacteristicTypeCode('0');
        relationshipDto.setRefinable('0');

        return relationshipDto;
    }
}
