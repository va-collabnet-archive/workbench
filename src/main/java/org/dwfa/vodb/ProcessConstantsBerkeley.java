package org.dwfa.vodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.IntSet;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
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
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ProcessConstantsBerkeley extends ProcessConstants {

	private class IdMapper {
				
		private Map<UUID, Integer> uuidToInt = new HashMap<UUID, Integer>();
		
		public int getIntId(Collection<UUID> uids, Path idPath, int version) throws Exception {
			for (UUID uid: uids) {
				try {
					return getIntId(uid, idPath, version);
				} catch (Exception e) {
					// continue
				}				
			}
			throw new Exception("Can't find id for: " + uids);
		}
		public int getIntId(UUID uid, Path idPath, int version) throws Exception {
			if (uuidToInt.containsKey(uid)) {
				return uuidToInt.get(uid);
			}
			int encodingSource = getIntId(PrimordialId.ACE_AUX_ENCODING_ID.getUids(), idPath, version);
			int newId = vodb.uuidToNativeWithGeneration(uid, encodingSource, idPath, version);
			uuidToInt.put(uid, newId);
			
			DatabaseEntry key = new DatabaseEntry(); 
			intBinder.objectToEntry(newId, key);
			DatabaseEntry value = new DatabaseEntry(); 
			ThinIdVersioned idv = new ThinIdVersioned(newId, 1);
			ThinIdPart idPart = new ThinIdPart();
			idPart.setIdStatus(vodb.uuidToNativeWithGeneration(ArchitectonicAuxiliary.Concept.CURRENT.getUids(), 
					encodingSource, idPath, version));
			idPart.setPathId(vodb.uuidToNativeWithGeneration(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(), 
					encodingSource, idPath, version));
			idPart.setSource(encodingSource);
			idPart.setSourceId(uid);
			idPart.setVersion(version);
			idv.addVersion(idPart);

			idBinder.objectToEntry(idv, value);
			vodb.getIdDb().put(null, key, value);
			return newId;
		}
	}
	
	private EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
	private ThinIdVersionedBinding idBinder = new ThinIdVersionedBinding();
	private ThinConVersionedBinding conBinder = new ThinConVersionedBinding();
	private ThinDescVersionedBinding descBinder = new ThinDescVersionedBinding();
	private ThinRelVersionedBinding relBinder = new ThinRelVersionedBinding();

	private VodbEnv vodb;
	private IdMapper map;
	
	Path aceAuxPath;

	public ProcessConstantsBerkeley(VodbEnv vodb) throws DatabaseException {
		super();
		map = new IdMapper();
		this.vodb = vodb;
		Class nativeIdClass = this.vodb.getNativeIdClass();
		if (nativeIdClass.equals(Integer.class)) {
			for (PrimordialId primId: PrimordialId.values()) {
				for (UUID uid:  primId.getUids()) {
					ThinIdVersioned thinId = new ThinIdVersioned(primId.getNativeId(Integer.MIN_VALUE), 1);
					ThinIdPart idPart = new ThinIdPart();
					idPart.setIdStatus(PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE));
					idPart.setPathId(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE));
					idPart.setSource(PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE));
					idPart.setSourceId(uid);
					idPart.setVersion(Integer.MIN_VALUE);
					thinId.addVersion(idPart);
					vodb.writeId(thinId);
					map.uuidToInt.put(uid, primId.getNativeId(Integer.MIN_VALUE));
				}
			}
		} else {
			throw new UnsupportedOperationException("Long native id type is not currently supported. ");
		}
		aceAuxPath = new Path(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE),
				new ArrayList<Position>());
	}

	@Override
	public Logger getLog() {
		return vodb.getLogger();
	}
	
	@Override
	public void cleanup(IntSet relsToIgnore) throws Exception {
		//Nothing to do...
	}


	@Override
	public void writeConcept(Date releaseDate, Object conceptKey,
			Object conceptStatus, boolean defChar) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinConPart con = new ThinConPart();
		con.setPathId(map.getIntId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(), aceAuxPath, version));
		con.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		con.setConceptStatus(map.getIntId((UUID) conceptStatus, aceAuxPath, version));
		con.setDefined(defChar);
		DatabaseEntry key = new DatabaseEntry(); 
		
		intBinder.objectToEntry(map.getIntId((UUID) conceptKey, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		ThinConVersioned vcon;
		if (vodb.getConceptDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vcon = (ThinConVersioned) conBinder.entryToObject(value);
		} else {
			vcon = new ThinConVersioned(map.getIntId((UUID) conceptKey, aceAuxPath, version), 1);
		}
		if (vcon.addVersion(con)) {
			value = new DatabaseEntry(); 
			conBinder.objectToEntry(vcon, value);
			vodb.getConceptDb().put(null, key, value);
		}
	}

	@Override
	public void writeDescription(Date releaseDate, Object descriptionId,
			Object status, Object conceptId, String text, boolean capStatus,
			Object typeInt, String lang) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinDescPart desc = new ThinDescPart();
		desc.setPathId(map.getIntId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(), aceAuxPath, version));
		desc.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		desc.setStatusId(map.getIntId((UUID) status, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));
		desc.setInitialCaseSignificant(capStatus);
		desc.setLang(lang);
		desc.setText(text);
		desc.setTypeId(map.getIntId((UUID) typeInt, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));
		
		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(map.getIntId((UUID) descriptionId, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		ThinDescVersioned vdesc;
		if (vodb.getDescDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vdesc = (ThinDescVersioned) descBinder.entryToObject(value);
		} else {
			vdesc = new ThinDescVersioned(map.getIntId((UUID) descriptionId, aceAuxPath, version), 
					map.getIntId((UUID) conceptId, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())), 1);
		}
		if (vdesc.addVersion(desc)) {
			value = new DatabaseEntry(); 
			descBinder.objectToEntry(vdesc, value);
			vodb.getDescDb().put(null, key, value);
		}	
	}

	@Override
	public void writeRelationship(Date releaseDate, Object relID,
			Object conceptOneID, Object relationshipTypeConceptID,
			Object conceptTwoID, Object characteristic, Object refinability,
			int group) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinRelPart rel = new ThinRelPart();
		rel.setPathId(map.getIntId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(), aceAuxPath, version));
		rel.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		rel.setStatusId(map.getIntId(ArchitectonicAuxiliary.Concept.CURRENT.getUids(), aceAuxPath, version));
		rel.setCharacteristicId(map.getIntId((UUID) characteristic, aceAuxPath, version));
		rel.setGroup(group);
		rel.setRefinabilityId(map.getIntId((UUID) refinability, aceAuxPath, version));
		rel.setRelTypeId(map.getIntId((UUID) relationshipTypeConceptID, aceAuxPath, version));

		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(map.getIntId((UUID) relID, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		ThinRelVersioned vrel;
		if (vodb.getRelDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vrel = (ThinRelVersioned) relBinder.entryToObject(value);
		} else {
			vrel = new ThinRelVersioned(map.getIntId((UUID) relID, aceAuxPath, version),
					map.getIntId((UUID) conceptOneID, aceAuxPath, version),
					map.getIntId((UUID) conceptTwoID, aceAuxPath, version),
					1);
		}
		if (vrel.addVersionNoRedundancyCheck(rel)) {
			value = new DatabaseEntry(); 
			relBinder.objectToEntry(vrel, value);
			vodb.getRelDb().put(null, key, value);
		}
	}

	public Map<UUID, Integer> getConstantToIntMap() {
		return map.uuidToInt;
	}


}
