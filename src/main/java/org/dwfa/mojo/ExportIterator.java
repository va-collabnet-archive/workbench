package org.dwfa.mojo;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

public class ExportIterator implements I_ProcessConcepts {

	private static final String DATE_FORMAT = "yyyy.mm.dd hh:mm:ss";

	private int totalConcepts = 0;

	private int conceptsMatched = 0;

	private int conceptsUnmatched = 0;

	private int conceptsSuppressed = 0;

	private int maxSuppressed = Integer.MAX_VALUE;

	private Writer errorWriter;

	private Set<I_Position> positions;

	private I_IntSet allowedStatus;

	private Writer conceptsWriter;
	private Writer descriptionsWriter;
	private Writer relationshipsWriter;
	private Writer idsWriter;
	
	private Log log;

	private String releaseDate;

	private ExportSpecification[] specs;

	private I_TermFactory termFactory;

	private I_IntList nameOrder;

	private final Writer idMapWriter;

	private Collection<UUID> snomedIdUuids;



	public ExportIterator(Writer concepts, Writer descriptions,
			Writer relationships, Writer idsWriter, Writer idMapWriter,
			Writer errorWriter, Set<I_Position> positions, I_IntSet allowedStatus,
			ExportSpecification[] specs, Log log) throws IOException, TerminologyException {
		super();
		this.idsWriter = idsWriter;
		this.idMapWriter = idMapWriter;
		this.errorWriter = errorWriter;
		this.positions = positions;
		this.allowedStatus = allowedStatus;
		this.conceptsWriter = concepts;
		this.descriptionsWriter = descriptions;
		this.relationshipsWriter = relationships;
		this.log = log;
		this.specs = specs;
		this.termFactory = LocalVersionedTerminology.get();
		
		snomedIdUuids = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids();
		
		nameOrder = termFactory.newIntList();
		nameOrder
				.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.localize().getNid());
		nameOrder
				.add(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE
						.localize().getNid());
		nameOrder.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
				.localize().getNid());
		nameOrder.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE
				.localize().getNid());
	}

	public int getTotals() {
		return totalConcepts;
	}

	public int getmatched() {
		return conceptsMatched;
	}

	public int getUnmatched() {
		return conceptsUnmatched;
	}

	public void processConcept(I_GetConceptData concept) throws Exception {

		if (conceptsSuppressed > maxSuppressed) {
			return;
		}

		// I_IntSet allowedTypes = termFactory.newIntSet();
		// allowedTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

		totalConcepts++;
		
		if (totalConcepts % 1000 == 0) {
			log.info("Iterated " + totalConcepts);
		}

		if (isExportable(concept)) {
			/*
			 * Get concept details
			 */
			if (writeUuidBasedConceptDetails(concept, allowedStatus)) {
				writeUuidBasedRelDetails(concept, allowedStatus, null);
				writeUuidBasedDescriptionDetails(concept, allowedStatus, null);
				writeUuidBasedIdDetails(concept.getId(), allowedStatus, null);
			}
		} else {
			conceptsSuppressed++;
			log.info("Suppressing: " + concept);
		}

	}// End method processConcept

	private void writeUuidBasedIdDetails(I_IdVersioned idVersioned,
			I_IntSet allowedStatus2, Object object) throws TerminologyException, Exception {

		Object[] idTuples = idVersioned.getTuples().toArray();
		Arrays.sort(idTuples, new Comparator<Object>() {

			public int compare(Object o1, Object o2) {
				return ((I_IdTuple) o1).getVersion() - ((I_IdTuple) o2).getVersion();
			}
			
		});
		
		String uuidString = "";
		boolean firstRun = true;
		for (UUID uuid : idVersioned.getUIDs()) {
			if (!firstRun) {
				uuidString += "\t" + uuid;
			} else {
				firstRun = false;
				uuidString += uuid;
			}
		}
		
		String snomedId = "";
		
		for (Object obj : idTuples) {
			I_IdTuple tuple = (I_IdTuple) obj;
			I_IdPart part = tuple.getPart();
			I_IdVersioned id = tuple.getIdVersioned();
			if (allowedStatus.contains(part.getIdStatus())
					&& isExportable(ConceptBean.get(part.getSource()))) {

				if (snomedSource(part) && !snomedId.equals(part.getSourceId())) {
					snomedId = (String) part.getSourceId();
					idMapWriter.write(uuidString);
					idMapWriter.write(System
							.getProperty("line.separator"));
					idMapWriter.write(snomedId);
					idMapWriter.write(System
							.getProperty("line.separator"));
				}
				
				StringBuilder stringBuilder = new StringBuilder();
				// primary UUID
				createRecord(stringBuilder, id.getUIDs().iterator().next());

				// source system UUID
				createRecord(stringBuilder, getFirstUuid(part.getSource()));

				// source id
				createRecord(stringBuilder, part.getSourceId());

				// status UUID
				createRecord(stringBuilder, getFirstUuid(part.getIdStatus()));

				// Effective time
				createVersion(part.getVersion(), stringBuilder);
				
				//Path Id
				createRecord(stringBuilder, getFirstUuid(part.getPathId()));
				
				createRecord(stringBuilder, System
						.getProperty("line.separator"));

				idsWriter.write(stringBuilder.toString());
			}
		}
	}
	
	private boolean snomedSource(I_IdPart idvPart) throws TerminologyException, IOException {
		if (termFactory.hasConcept(idvPart.getSource())) {
			for (UUID uuid : termFactory.getUids(idvPart.getSource())) {
				if (snomedIdUuids.contains(uuid)) {
					return true;
				}
			}
		} else {
			System.out.println("no concept for source, id was " + idvPart.getSourceId());
		}
		return false;
	}

	private boolean isExportable(I_GetConceptData concept) throws Exception {
		for (ExportSpecification spec : specs) {
			if (spec.test(concept)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return "prepareConceptData";
	}

	private boolean writeUuidBasedConceptDetails(I_GetConceptData concept,
			I_IntSet allowedStatus) throws IOException, TerminologyException {

		I_DescriptionTuple descForConceptFile = concept.getDescTuple(nameOrder,
				null, positions);
		if (descForConceptFile == null) {
			errorWriter.append("\n\nnull desc for: " + concept.getUids() + " "
					+ concept.getDescriptions());
			return false;
		} else {
			StringBuilder stringBuilder = new StringBuilder("");

			List<I_ConceptAttributeTuple> firstMatches = concept
					.getConceptAttributeTuples(null, positions);
			List<I_ConceptAttributeTuple> matches = new LinkedList<I_ConceptAttributeTuple>();
			for (int i = 0; i < firstMatches.size(); i++) {
				if (allowedStatus.contains(firstMatches.get(i)
						.getConceptStatus())) {
					matches.add(firstMatches.get(i));
				}
			}

			if (matches == null || matches.size() == 0) {
				return false;
			}
			conceptsMatched++;
			for (I_ConceptAttributeTuple attribTup : matches) {
				// Snomed core
				// ConceptId
				createRecord(stringBuilder, concept.getUids().get(0));

				// Concept status
				createRecord(stringBuilder, ArchitectonicAuxiliary
						.getSnomedConceptStatusId(termFactory.getUids(attribTup.getConceptStatus())));

				// Fully specified name
				createRecord(stringBuilder, descForConceptFile.getText());
				// createRecord(stringBuilder,
				// descriptionTuples.get(0).getText()
				// );

				// CTV3ID
				createRecord(stringBuilder, "null");

				// SNOMED 3 ID... We ignore this for now.
				createRecord(stringBuilder, "null");

				// IsPrimative value
				createRecord(stringBuilder, attribTup.isDefined() ? 0 : 1);

				// AMT added
				// Concept UUID
				createRecord(stringBuilder, concept.getUids().get(0));

				// ConceptStatusId
				createRecord(stringBuilder, getFirstUuid(attribTup.getConceptStatus()));

				// Effective time
				createVersion(attribTup.getPart().getVersion(), stringBuilder);
				
				//Path Id
				createRecord(stringBuilder, getFirstUuid(attribTup.getPart().getPathId()));
				
				// End record
				createRecord(stringBuilder, System
						.getProperty("line.separator"));
			}// End while loop
			conceptsWriter.write(stringBuilder.toString());

			return true;
		}// End method getUuidBasedConceptDetaiils
	}

	private void writeUuidBasedRelDetails(I_GetConceptData concept,
			I_IntSet allowedStatus, I_IntSet allowedTypes) throws Exception {

		List<I_RelTuple> tuples = concept.getSourceRelTuples(null, null,
				positions, false);
		int relId = 0;
		for (I_RelTuple tuple : tuples) {
			I_RelPart part = tuple.getPart();
			I_RelVersioned rel = tuple.getRelVersioned();
			if (allowedStatus.contains(part.getStatusId())
					&& isExportable(ConceptBean.get(rel.getC2Id()))
					&& isExportable(ConceptBean.get(part.getCharacteristicId()))
					&& isExportable(ConceptBean.get(part.getRefinabilityId()))
					&& isExportable(ConceptBean.get(part.getRelTypeId()))) {

				if (relId != tuple.getRelId()) {
					relId = tuple.getRelId();
					writeUuidBasedIdDetails(termFactory.getId(relId), allowedStatus, null);
				}
				
				StringBuilder stringBuilder = new StringBuilder();
				// Relationship ID
				createRecord(stringBuilder, getFirstUuid(rel.getRelId()));

				// Concept status
				createRecord(
						stringBuilder,
						ArchitectonicAuxiliary
								.getSnomedConceptStatusId(termFactory.getUids(part.getStatusId())));

				// Concept Id 1 UUID
				createRecord(stringBuilder, getFirstUuid((rel.getC1Id())));

				// Relationship type UUID
				createRecord(stringBuilder, getFirstUuid((part.getRelTypeId())));

				// Concept Id 2 UUID
				createRecord(stringBuilder, getFirstUuid(rel.getC2Id()));

				// (Characteristict Type integer)
				int snomedCharacter = ArchitectonicAuxiliary
						.getSnomedCharacteristicTypeId(termFactory.getUids(part.getCharacteristicId()));
				if (snomedCharacter == -1) {
					errorWriter.append("\nNo characteristic mapping for: "
							+ termFactory.getConcept(
									part.getCharacteristicId()));
				}
				createRecord(stringBuilder, snomedCharacter);

				// Refinability integer
				createRecord(stringBuilder, ArchitectonicAuxiliary
						.getSnomedRefinabilityTypeId(termFactory.getUids(part.getRefinabilityId())));

				// Relationship Group
				createRecord(stringBuilder, part.getGroup());

				// Amt added
				// Relationship UUID
				createRecord(stringBuilder, getFirstUuid((rel.getRelId())));

				// Concept1 UUID
				createRecord(stringBuilder, getFirstUuid(rel.getC1Id()));

				// Relationship type UUID
				createRecord(stringBuilder, getFirstUuid(part.getRelTypeId()));

				// Concept2 UUID
				createRecord(stringBuilder, getFirstUuid(rel.getC2Id()));

				// Characteristic Type UUID
				createRecord(stringBuilder, getFirstUuid(part.getCharacteristicId()));

				// Refinability UUID
				createRecord(stringBuilder, getFirstUuid(part.getRefinabilityId()));

				// Relationship status UUID
				createRecord(stringBuilder, getFirstUuid(part.getStatusId()));

				// Effective Time
				createVersion(part.getVersion(), stringBuilder);

				//Path Id
				createRecord(stringBuilder, getFirstUuid(part.getPathId()));
				
				createRecord(stringBuilder, System
						.getProperty("line.separator"));

				relationshipsWriter.write(stringBuilder.toString());
			}
		}
	}// End method getUuidBasedRelDetails

	private void createVersion(int version, StringBuilder stringBuilder) {
		if (releaseDate == null) {
			createRecord(stringBuilder, new SimpleDateFormat(DATE_FORMAT)
					.format(new Date(ThinVersionHelper.convert(version))));
		} else {
			createRecord(stringBuilder, releaseDate);
		}
	}

	private void writeUuidBasedDescriptionDetails(I_GetConceptData concept,
			I_IntSet allowedStatus, I_IntSet allowedTypes) throws Exception {

		List<I_DescriptionTuple> tuples = concept.getDescriptionTuples(null,
				null, positions);
		
		int descId = 0;
		for (I_DescriptionTuple tuple : tuples) {
			I_DescriptionPart part = tuple.getPart();
			if (allowedStatus.contains(part.getStatusId())
					&& isExportable(ConceptBean.get(part.getTypeId()))) {

				if (descId != tuple.getDescId()) {
					descId = tuple.getDescId();
					writeUuidBasedIdDetails(termFactory.getId(descId), allowedStatus, null);
				}
				
				StringBuilder stringBuilder = new StringBuilder("");
				createRecord(stringBuilder, termFactory
						.getConcept(tuple.getDescVersioned().getDescId())
						.getUids().get(0));

				// Description Status
				createRecord(
						stringBuilder,
						ArchitectonicAuxiliary
								.getSnomedDescriptionStatusId(termFactory.getUids(part.getStatusId())));

				// ConceptId
				createRecord(stringBuilder, concept.getUids().get(0));

				// Term
				createRecord(stringBuilder, part.getText());

				// Case sensitivity
				createRecord(stringBuilder,
						part.getInitialCaseSignificant() ? 1 : 0);

				// Initial Capital Status
				createRecord(stringBuilder,
						part.getInitialCaseSignificant() ? 1 : 0);

				// Description Type
				createRecord(stringBuilder, ArchitectonicAuxiliary
						.getSnomedDescriptionTypeId(termFactory.getUids(part.getTypeId())));

				// Language code
				createRecord(stringBuilder, part.getLang());

				// Language code for UUID
				createRecord(stringBuilder, part.getLang());

				// AMT added
				// Description UUID
				createRecord(stringBuilder, getFirstUuid(tuple.getDescVersioned().getDescId()));

				// Description status UUID
				createRecord(stringBuilder, getFirstUuid(part.getStatusId()));

				// Description type UUID
				createRecord(stringBuilder, getFirstUuid(part.getTypeId()));

				// ConceptId
				createRecord(stringBuilder, concept.getUids().get(0));

				// Effective time
				createVersion(part.getVersion(), stringBuilder);

				//Path Id
				createRecord(stringBuilder, getFirstUuid(part.getPathId()));
				
				// End record
				createRecord(stringBuilder, System
						.getProperty("line.separator"));

				descriptionsWriter.write(stringBuilder.toString());
			}
		}
	}// End method getUuidBasedDescriptionDetails

	private UUID getFirstUuid(int nid)
			throws TerminologyException, IOException {
		return termFactory.getUids(nid).iterator().next();
	}

	public int getConceptsSuppressed() {
		return conceptsSuppressed;
	}

	public void setConceptsSuppressed(int conceptsSuppressed) {
		this.conceptsSuppressed = conceptsSuppressed;
	}

	private void createRecord(StringBuilder stringBuilder, Object fieldData) {
		stringBuilder.append(fieldData);
		if (fieldData != System.getProperty("line.separator"))
			stringBuilder.append("\t");
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

}
