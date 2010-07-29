/*
 *  Copyright 2010 matt.
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
package org.dwfa.mojo.export.file;

import org.dwfa.maven.sctid.UuidSctidMapDb;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Iterator;
import org.dwfa.mojo.file.ace.AceIdentifierReader;
import org.dwfa.mojo.file.ace.AceConceptReader;
import org.dwfa.mojo.file.ace.AceDescriptionReader;
import org.dwfa.mojo.file.ace.AceRelationshipReader;
import org.dwfa.mojo.file.ace.AceRelationshipRow;
import org.dwfa.mojo.file.ace.AceDescriptionRow;
import org.dwfa.mojo.file.ace.AceConceptRow;
import org.dwfa.mojo.file.ace.AceIdentifierRow;
import java.util.ArrayList;
import org.junit.Assert;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.RelationshipDto;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.util.Date;
import org.dwfa.dto.ComponentDto;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author matt
 */
public class AmtOutputHandlerTest {

    private Calendar aceTime = new GregorianCalendar();
    private static SnomedFileFormatOutputHandler aceOutputHandler;
    private static final File RESOURCES_DIR = new File("target/test-classes/org/dwfa/mojo/file/amt-output-handler");
    private static final File TEST_FILES_DIR = new File(RESOURCES_DIR, "amt-output-handler");
    private static final File DB_DIR = new File(RESOURCES_DIR, "test-id-db");
    private static final File EXPORT_DIR_ACE = new File(RESOURCES_DIR, "ace");
    private static final File EXPORT_DIR_FULL = new File(EXPORT_DIR_ACE, "full");

    public AmtOutputHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (System.getProperty(AceOutputHandlerTest.UUID_MAP_TEST_DATABASE_DRIVER) == null) {
            UuidSctidMapDb.setDatabaseProperties("org.apache.derby.jdbc.EmbeddedDriver",
                    "jdbc:derby:directory:" + DB_DIR.getCanonicalPath() + ";create=true;");

        } else {
            UuidSctidMapDb.setDatabaseProperties(System.getProperty(AceOutputHandlerTest.UUID_MAP_TEST_DATABASE_DRIVER),
                    System.getProperty(AceOutputHandlerTest.UUID_MAP_TEST_DATABASE_URL),
                    System.getProperty(AceOutputHandlerTest.UUID_MAP_TEST_DATABASE_USER),
                    System.getProperty(AceOutputHandlerTest.UUID_MAP_TEST_DATABASE_PASSWORD));
        }
        aceOutputHandler = new AmtOutputHandler(EXPORT_DIR_ACE, new HashMap<UUID, Map<UUID, Date>>());
        aceOutputHandler.failOnError = true;
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

    /**
     * Test of exportComponent method, of class AmtOutputHandler.
     */
    @Test
    public void testExportComponent() throws Exception {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);
        setConceptDtoData(conceptDto);
        conceptDto.getIdentifierDtos().add(setIdentifierDtoData(new IdentifierDto()));

        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setConceptId(conceptDto.getConceptId());
        componentDto.getDescriptionDtos().add(setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(1).setConceptId(conceptDto.getConceptId());

        componentDto.getRelationshipDtos().add(setRelationshipDto(new RelationshipDto()));

        aceOutputHandler.export(componentDto);

        aceOutputHandler.closeFiles();

        AceIdentifierReader aceIdentifierReader = new AceIdentifierReader(new File(EXPORT_DIR_FULL, "ids.txt"));
        assertIdentifierRow(conceptDto.getIdentifierDtos().get(0),
                aceIdentifierReader.iterator().next());

        AceConceptReader aceConceptReader = new AceConceptReader(new File(EXPORT_DIR_FULL, "concepts.txt"));
        AceConceptRow aceConceptRow = aceConceptReader.iterator().next();
        assertConceptRow(componentDto, aceConceptRow);

        AceDescriptionReader aceDescriptionReader = new AceDescriptionReader(new File(EXPORT_DIR_FULL, "descriptions.txt"));
        Iterator<AceDescriptionRow> descriptionIterator = aceDescriptionReader.iterator();
        assertDescriptionRow(componentDto.getDescriptionDtos().get(0), descriptionIterator.next());
        assertDescriptionRow(componentDto.getDescriptionDtos().get(1), descriptionIterator.next());

        AceRelationshipReader aceRelationshipReader = new AceRelationshipReader(new File(EXPORT_DIR_FULL, "relationships.txt"));

        AceRelationshipRow relationshipRow = aceRelationshipReader.iterator().next();
        RelationshipDto relationshipDto = componentDto.getRelationshipDtos().get(0);
        assertRelationshipRow(relationshipRow, relationshipDto);
    }

    private void assertRelationshipRow(AceRelationshipRow relationshipRow, RelationshipDto relationshipDto)
            throws Exception {
        Assert.assertEquals(relationshipDto.getConceptId().keySet().iterator().next().toString(), relationshipRow.
                getRelationshipUuid());
        Assert.assertEquals(relationshipDto.getDestinationId().keySet().iterator().next().toString(), relationshipRow.
                getConceptUuid2());
        Assert.assertEquals(relationshipDto.getSourceId().toString(), relationshipRow.getConceptUuid1());
        Assert.assertEquals(relationshipDto.getTypeId().toString(), relationshipRow.getRelationshiptypeUuid());
        Assert.assertEquals(relationshipDto.getPathId().toString(), relationshipRow.getPathUuid());
        Assert.assertEquals(relationshipDto.getRefinabilityId().toString(), relationshipRow.getRefinabilityUuid());
        Assert.assertEquals(relationshipDto.getRelationshipGroup().toString(),
                relationshipRow.getRelationshipGroup());
        Assert.assertEquals(relationshipDto.getStatusId().toString(), relationshipRow.getRelationshipstatusUuid());
        Assert.assertEquals(relationshipDto.getTypeId().toString(), relationshipRow.getRelationshiptypeUuid());
    }

    private void assertDescriptionRow(DescriptionDto descriptionDto, AceDescriptionRow descriptionRow) throws Exception {

        Assert.assertEquals(descriptionDto.getConceptId().keySet().iterator().next().toString(), descriptionRow.
                getConceptUuid());
        Assert.assertEquals(descriptionDto.getDescriptionId().toString(), descriptionRow.getDescriptionUuid());
        Assert.assertEquals(descriptionDto.getDescription(), descriptionRow.getTerm());
        Assert.assertEquals(descriptionDto.getStatusId().toString(), descriptionRow.getDescriptionstatusUuid());
        Assert.assertEquals(descriptionDto.getTypeId().toString(), descriptionRow.getDescriptiontypeUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(descriptionDto), descriptionRow.getEffectiveTime());
        Assert.assertEquals(descriptionDto.getLanguageCode(), descriptionRow.getLanguageCode());
        Assert.assertEquals(descriptionDto.getPathId().toString(), descriptionRow.getPathUuid());
    }

    private void assertConceptRow(ComponentDto componentDto, AceConceptRow aceConceptRow) throws Exception {
        ConceptDto conceptDto = componentDto.getConceptDtos().get(0);

        Assert.assertEquals(conceptDto.getConceptId().keySet().iterator().next().toString(), aceConceptRow.
                getConceptUuid());
        Assert.assertEquals(conceptDto.getStatusId().toString(), aceConceptRow.getConceptStatusUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(conceptDto), aceConceptRow.getEffectiveTime());
        Assert.assertEquals(conceptDto.getPathId().toString(), aceConceptRow.getPathUuid());
        Assert.assertEquals((conceptDto.isPrimative()) ? "1" : "0", aceConceptRow.getIsPrimitve());
    }

    private void assertIdentifierRow(IdentifierDto identifierDto, AceIdentifierRow aceIdentifierRow) throws Exception {
        Assert.assertEquals(identifierDto.getConceptId().keySet().iterator().next().toString(), aceIdentifierRow.
                getPrimaryUuid());
        Assert.assertEquals(aceOutputHandler.getReleaseDate(identifierDto), aceIdentifierRow.getEffectiveDate());
        Assert.assertEquals(identifierDto.getPathId().toString(), aceIdentifierRow.getPathUuid());
        Assert.assertEquals(identifierDto.getReferencedSctId().toString(), aceIdentifierRow.getSourceId());
        Assert.assertEquals(identifierDto.getIdentifierSchemeUuid().toString(), aceIdentifierRow.getSourceSystemUuid());
        Assert.assertEquals(identifierDto.getStatusId().toString(), aceIdentifierRow.getStatusUuid());
    }

    @Test
    public void testExportMissingConceptDetailsValidation() throws Throwable {
        ComponentDto componentDto = new ComponentDto();
        ConceptDto conceptDto = new ConceptDto();

        componentDto.getConceptDtos().add(conceptDto);

        setConceptDtoData(conceptDto);
        conceptDto.setConceptId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a concepts id");
        } catch (Exception e) {
        }

        setConceptDtoData(conceptDto);
        conceptDto.setDateTime(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a date");
        } catch (Exception e) {
        }

        setConceptDtoData(conceptDto);
        conceptDto.setPathId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a path id");
        } catch (Exception e) {
        }

        setConceptDtoData(conceptDto);
        conceptDto.setStatusId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a status");
        } catch (Exception e) {
        }

        setConceptDtoData(conceptDto);
        conceptDto.setType(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a type");
        } catch (Exception e) {
        }

        setConceptDtoData(conceptDto);
        conceptDto.setNamespace(null);
        try {
            aceOutputHandler.export(componentDto);
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


        try {
            aceOutputHandler.export(componentDto);
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
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a description");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a description id");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setCaseSignificanceId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Case Significance Id");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setDescriptionTypeCode(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Description Type Code");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setInitialCapitalStatusCode(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Initial Capital Status Code");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageCode(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Language Code");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setLanguageId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have Language Id");
        } catch (Exception e) {
        }

        componentDto.getDescriptionDtos().set(0, setDescriptionDto(new DescriptionDto()));
        componentDto.getDescriptionDtos().get(0).setTypeId(null);
        try {
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
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Code");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setCharacteristicTypeId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Characteristic Type Id");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setModifierId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Modifier Id");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setRelationshipGroup(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Relationship Group Code");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setSourceId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Source Id");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setDestinationId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Destination Id");
        } catch (Exception e) {
        }

        componentDto.getRelationshipDtos().set(0, setRelationshipDto(new RelationshipDto()));
        componentDto.getRelationshipDtos().get(0).setTypeId(null);
        try {
            aceOutputHandler.export(componentDto);
            Assert.fail("Must have a Type Id");
        } catch (Exception e) {
        }
    }

    private IdentifierDto setIdentifierDtoData(IdentifierDto identifierDto) {
        identifierDto.setActive(true);
        identifierDto.setLatest(true);
        identifierDto.setReferencedSctId(900000000000960019l);
        identifierDto.setConceptId(getIdMap(UUID.randomUUID(), null));
        identifierDto.setDateTime(getDate());
        identifierDto.setIdentifierSchemeUuid(UUID.randomUUID());
        identifierDto.setPathId(UUID.randomUUID());
        identifierDto.setNamespace(NAMESPACE.NEHTA);
        identifierDto.setStatusId(UUID.randomUUID());
        identifierDto.setType(TYPE.CONCEPT);

        return identifierDto;
    }

    private ConceptDto setConceptDtoData(ConceptDto conceptDto) {
        conceptDto.setActive(true);
        conceptDto.setLatest(true);
        conceptDto.setConceptId(getIdMap(UUID.randomUUID(), null));
        conceptDto.setDateTime(getDate());
        conceptDto.setFullySpecifiedName("Flamingducks");
        conceptDto.setNamespace(NAMESPACE.NEHTA);
        conceptDto.setProject(PROJECT.AMT);
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
        descriptionDto.setRf2TypeId(UUID.randomUUID());
        descriptionDto.setInitialCapitalStatusCode('2');
        descriptionDto.setDescriptionTypeCode('1');
        descriptionDto.setLanguageCode("en-OZ");

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
        relationshipDto.setRefinabilityId(UUID.randomUUID());
        relationshipDto.setRelationshipGroup(1);
        relationshipDto.setTypeId(UUID.randomUUID());
        relationshipDto.setTypeId(UUID.randomUUID());
        relationshipDto.setCharacteristicTypeCode('0');
        relationshipDto.setRefinable('0');

        return relationshipDto;
    }

    private Map<UUID, Long> getIdMap(UUID uuid, Long sctId) {
        Map<UUID, Long> map = new HashMap<UUID, Long>(1);

        map.put(uuid, sctId);

        return map;
    }

    private Date getDate() {
        aceTime.setTime(new Date());
        aceTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        aceTime.set(Calendar.HOUR, 0);
        aceTime.set(Calendar.MINUTE, 0);
        aceTime.set(Calendar.SECOND, 0);

        return aceTime.getTime();
    }
}
