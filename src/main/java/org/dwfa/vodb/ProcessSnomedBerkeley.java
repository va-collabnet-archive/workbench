package org.dwfa.vodb;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.activity.I_ShowActivity;
import org.dwfa.ace.activity.UpperInfoOnlyConsoleMonitor;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMEDExtension;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal snomed-berkley
 * 
 * @phase generate-resources
 */
public class ProcessSnomedBerkeley extends ProcessSnomed {

	/*
	DESCRIPTIONID	DESCRIPTIONSTATUS	CONCEPTID	TERM	INITIALCAPITALSTATUS	DESCRIPTIONTYPE	LANGUAGECODE
	220309016	0	138875005	SNOMED CT Concept	1	1	en
	*/
	//public static final long SNOMED_ROOT_CONCEPTID = 138875005L;
	//public static final long SNOMED_ROOT_DESCID = 220309016L;
	//public static final long SNOMED_ISA_REL = 116680003L;
	
			
		Map<Long, Integer> idMap = new HashMap<Long, Integer>();
		// Add a hash map
		public int getIntId(long snomedId, Date version) throws Exception {
			if (idMap.containsKey(snomedId)) {
				return idMap.get(snomedId);
			}
			int thinVers = ThinVersionHelper.convert(version.getTime());
			UUID snomedUid = Type3UuidFactory.fromSNOMED(snomedId);
			//We have not encountered the SNOMED ID yet, so generate a new one. 
			
			//See if the UUID generated a duplicate...
			//Check to be sure the UUID generated from nameUUID is unique. 
			I_IdVersioned dup = vodb.getId(snomedUid);
			if (dup != null) {
				boolean error = true;
				if (dup.getTuples().size() == 1) {
					if (SNOMEDExtension.getSnomedIdsUsed().contains(snomedUid)) {
						error = false;
					}
				}
				if (error) {
					StringBuffer buf = new StringBuffer();
					buf.append("Severe error: ");
					buf.append(snomedId);
					buf.append(" generates a duplicate type 3 UUID. ");
					buf.append(dup.toString());
					buf.append(" Extension ids: ");
					buf.append(SNOMEDExtension.getSnomedIdsUsed());
					throw new Exception(buf.toString());
				}
			}
			//No duplicate, so generate id...
			
			
			int newId = vodb.uuidToNativeWithGeneration(snomedUid, snomedType3UuidSource,
					snomedPath, thinVers);
			idMap.put(snomedId, newId);
			DatabaseEntry key = new DatabaseEntry(); 
			intBinder.objectToEntry(newId, key);
			DatabaseEntry value = new DatabaseEntry(); 
			I_IdVersioned idv = new ThinIdVersioned(newId, 2);
			//add 
			ThinIdPart idPart = new ThinIdPart();
			idPart.setIdStatus(currentId);
			idPart.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
			idPart.setSource(snomedType3UuidSource);
			idPart.setSourceId(snomedUid);
			idPart.setVersion(thinVers);
			idv.addVersion(idPart);

			idPart = new ThinIdPart();
			idPart.setIdStatus(currentId);
			idPart.setPathId(snomedPath.getConceptId());
			idPart.setSource(snomedIntIdSource);
			idPart.setSourceId(new Long(snomedId));
			idPart.setVersion(thinVers);
			idv.addVersion(idPart);

			idBinder.objectToEntry(idv, value);
			vodb.getIdDb().put(null, key, value);
			return newId;
		}
	
	
		
	private ThinIdVersionedBinding idBinder = new ThinIdVersionedBinding();
	private EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
	private ThinConVersionedBinding conBinder = new ThinConVersionedBinding();
	private ThinDescVersionedBinding descBinder = new ThinDescVersionedBinding();
	private ThinRelVersionedBinding relBinder = new ThinRelVersionedBinding();
	private VodbEnv vodb;
	//private Map<UUID, Integer> uuidToIntMap;	
	private Stopwatch timer;
	private I_ShowActivity monitor;
	private I_Path snomedPath;
	private int snomedType3UuidSource;
	private int snomedIntIdSource;
	private int currentId;

	public ProcessSnomedBerkeley(VodbEnv vodb, Map<UUID, Integer> constantToIntMap, int constantDate) throws DatabaseException, TerminologyException, IOException  {
		super(constantDate);
		this.vodb = vodb;
		//this.uuidToIntMap = constantToIntMap;
		this.timer = new Stopwatch();
		this.timer.start();
		monitor = new UpperInfoOnlyConsoleMonitor();
		snomedPath = new Path(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()),
				new ArrayList<I_Position>());
		snomedType3UuidSource = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID.getUids());
		snomedIntIdSource = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
		currentId = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
	}

	public void cleanup(I_IntSet relsToIgnore) throws Exception {
		printElapsedTime();
		AceLog.getAppLog().info("Creating concept->desc map.");
		vodb.getConceptDescMap();
		//Update the history records for the relationships...
		printElapsedTime();
		AceLog.getAppLog().info("Starting rel history update.");
		Cursor relC = vodb.getRelDb().openCursor(null, null);
		DatabaseEntry relKey = new DatabaseEntry();
		DatabaseEntry relValue = new DatabaseEntry();
		int compressedRels = 0;
		int retiredRels = 0;
		int currentRels = 0;
		int totalRels = 0;
		int[] releases = getReleaseDates();
		Arrays.sort(releases);
		while (relC.getNext(relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			totalRels++;
			I_RelVersioned vrel = (I_RelVersioned) relBinder.entryToObject(relValue);
			if (relsToIgnore.contains(vrel.getRelId()) == false) {
				boolean addRetired = vrel.addRetiredRec(releases,
						vodb.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
				boolean removeRedundant = vrel.removeRedundantRecs();
				if (addRetired && removeRedundant) {
					relBinder.objectToEntry(vrel, relValue);
					relC.put(relKey, relValue);
					retiredRels++;
					compressedRels++;
				} else if (addRetired) {
					relBinder.objectToEntry(vrel, relValue);
					relC.put(relKey, relValue);
					retiredRels++;
				} else if (removeRedundant) {
					relBinder.objectToEntry(vrel, relValue);
					relC.put(relKey, relValue);
					compressedRels++;
					currentRels++;
				} else {
					currentRels++;
				}
			}			
		}
		relC.close();
		AceLog.getAppLog().info("Total rels: " + totalRels);
		AceLog.getAppLog().info("Compressed rels: " + compressedRels);
		AceLog.getAppLog().info("Retired rels: " + retiredRels);
		AceLog.getAppLog().info("Current rels: " + currentRels);
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting c1RelMap.");
		vodb.createC1RelMap();
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting c2RelMap.");
		vodb.createC2RelMap();
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting createIdMaps.");
		vodb.createIdMaps();
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting createConceptImageMap.");
		vodb.createConceptImageMap();
		monitor.setProgressInfoUpper("Starting populateTimeBranchDb().");
		vodb.populateTimeBranchDb();
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting makeLuceneIndex().");
		vodb.makeLuceneIndex();
		printElapsedTime();
		monitor.setProgressInfoUpper("Starting cleanup.");
		vodb.close();
		printElapsedTime();

	}

	private void printElapsedTime() {
		Date end = new Date();
		long elapsed = timer.getElapsedTime();
		elapsed = elapsed / 1000;
		AceLog.getAppLog().info("Elapsed sec: " + elapsed);
		elapsed = elapsed / 60;
		AceLog.getAppLog().info("Elapsed min: " + elapsed);
		AceLog.getAppLog().info(end.toString());
	}

	
	public void writeConcept(Date releaseDate, Object conceptKey,
			Object conceptStatus, boolean defChar, Object ignoredPath) throws Exception {
		ThinConPart con = new ThinConPart();
		con.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
		con.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		
		con.setConceptStatus(getNativeStatus((Integer) conceptStatus));
		con.setDefined(defChar);
		DatabaseEntry key = new DatabaseEntry(); 
		
		intBinder.objectToEntry(getIntId((Long) conceptKey, releaseDate), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		I_ConceptAttributeVersioned vcon;
		if (vodb.getConceptDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vcon = (I_ConceptAttributeVersioned) conBinder.entryToObject(value);
		} else {
			vcon = new ThinConVersioned(getIntId((Long) conceptKey, releaseDate), 1);
		}
		if (vcon.addVersion(con)) {
			value = new DatabaseEntry(); 
			conBinder.objectToEntry(vcon, value);
			vodb.getConceptDb().put(null, key, value);
		}
		
	}

	private int getNativeStatus(Integer conceptStatus) throws IOException, TerminologyException {
		I_ConceptualizeUniversally status = ArchitectonicAuxiliary.getStatusFromId((Integer) conceptStatus);
		int statusNativeId = vodb.uuidToNative(status.getUids());
		return statusNativeId;
	}

	private int getNativeDescType(Integer typeId) throws IOException, TerminologyException {
		I_ConceptualizeUniversally type = ArchitectonicAuxiliary.getSnomedDescriptionType((Integer) typeId);
		int typeNativeId = vodb.uuidToNative(type.getUids());
		return typeNativeId;
	}

	private int getNativeCharacteristicType(Integer characteristicId) throws IOException, TerminologyException {
		I_ConceptualizeUniversally characteristic = ArchitectonicAuxiliary.getSnomedCharacteristicType((Integer) characteristicId);
		int characteristicNativeId = vodb.uuidToNative(characteristic.getUids());
		return characteristicNativeId;
	}

	private int getNativeRefinability(Integer refinabilityId) throws IOException, TerminologyException {
		I_ConceptualizeUniversally refinability = ArchitectonicAuxiliary.getSnomedRefinabilityType((Integer) refinabilityId);
		int refinabilityNativeId = vodb.uuidToNative(refinability.getUids());
		return refinabilityNativeId;
	}

	public void writeDescription(Date releaseDate, Object descriptionId,
			Object status, Object conceptId, String text, boolean capStatus,
			Object typeInt, String lang, Object ignoredPath) throws Exception {
		
		
		text = new String(text.getBytes(), "UTF-8");
		if (text.getBytes()[0] < 0) {
			AceLog.getAppLog().info("********\n" + text + "\n length: " + text.length());
			StringBuffer buff = new StringBuffer();
			for (byte b: text.getBytes()) {
				buff.append((int)b);
				buff.append(' ');
			}
			AceLog.getAppLog().info("\n\nUTF 8:");
			for (byte b: text.getBytes("UTF-8")) {
				buff.append((int)b);
				buff.append(' ');
			}
			AceLog.getAppLog().info("\n\nUTF 16:");
			for (char c: text.toCharArray()) {
				buff.append(c);
				buff.append(' ');
			}
			AceLog.getAppLog().info(buff.toString());
		}
		ThinDescPart desc = new ThinDescPart();
		desc.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
		desc.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		desc.setStatusId(getNativeStatus((Integer) status));
		desc.setInitialCaseSignificant(capStatus);
		desc.setLang(lang);
		desc.setText(text);
		desc.setTypeId(getNativeDescType((Integer) typeInt));
		
		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(getIntId((Long) descriptionId, releaseDate), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		I_DescriptionVersioned vdesc;
		if (vodb.getDescDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vdesc = (I_DescriptionVersioned) descBinder.entryToObject(value);
		} else {
			vdesc = new ThinDescVersioned(getIntId((Long) descriptionId, releaseDate), 
					getIntId((Long) conceptId, releaseDate), 1);
		}
		if (vdesc.addVersion(desc)) {
			value = new DatabaseEntry(); 
			descBinder.objectToEntry(vdesc, value);
			vodb.getDescDb().put(null, key, value);
		}	
	}

	public void writeRelationship(Date releaseDate, Object relID, Object statusIdIgnored,
			Object conceptOneID, Object relationshipTypeConceptID,
			Object conceptTwoID, Object characteristic, Object refinability, int group, Object ignoredPath)
			throws Exception {
		ThinRelPart rel = new ThinRelPart();
		rel.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
		rel.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		rel.setStatusId(getNativeStatus(0));
		rel.setCharacteristicId(getNativeCharacteristicType((Integer) characteristic));
		rel.setGroup(group);
		rel.setRefinabilityId(getNativeRefinability((Integer) refinability));
		rel.setRelTypeId(getIntId((Long) relationshipTypeConceptID, releaseDate));

		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(getIntId((Long) relID, releaseDate), key);
		DatabaseEntry value = new DatabaseEntry(); 
		I_RelVersioned vrel;
		if (vodb.getRelDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vrel = (I_RelVersioned) relBinder.entryToObject(value);
		} else {
			vrel = new ThinRelVersioned(getIntId((Long) relID, releaseDate),
					getIntId((Long) conceptOneID, releaseDate),
					getIntId((Long) conceptTwoID, releaseDate),
					1);
		}
		if (vrel.addVersionNoRedundancyCheck(rel)) {
			value = new DatabaseEntry(); 
			relBinder.objectToEntry(vrel, value);
			vodb.getRelDb().put(null, key, value);
		}
		
	}
	@Override
	public Logger getLog() {
		return vodb.getLogger();
	}

	@Override
	public void iterateRelationships(MakeRelSet oldRelItr) throws Exception {
		vodb.iterateRelationships(oldRelItr);
		
	}

	
}