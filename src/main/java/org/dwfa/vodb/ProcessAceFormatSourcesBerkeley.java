package org.dwfa.vodb;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;
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

public class ProcessAceFormatSourcesBerkeley extends ProcessAceFormatSources {

	private class IdMapper {
				
		private Map<UUID, Integer> uuidToInt = new HashMap<UUID, Integer>();
		
		public int getIntId(Collection<UUID> uids, I_Path idPath, int version) throws Exception {
			for (UUID uid: uids) {
				try {
					return getIntId(uid, idPath, version);
				} catch (Exception e) {
					// continue
				}				
			}
			throw new Exception("Can't find id for: " + uids);
		}
		public int getIntId(UUID uid, I_Path idPath, int version) throws Exception {
			if (uuidToInt.containsKey(uid)) {
				return uuidToInt.get(uid);
			}
			int encodingSource = getIntId(PrimordialId.ACE_AUX_ENCODING_ID.getUids(), idPath, version);
			int newId = vodb.uuidToNativeWithGeneration(uid, encodingSource, idPath, version);
			uuidToInt.put(uid, newId);
			
			DatabaseEntry key = new DatabaseEntry(); 
			intBinder.objectToEntry(newId, key);
			DatabaseEntry value = new DatabaseEntry(); 
			I_IdVersioned idv = new ThinIdVersioned(newId, 1);
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
	
	I_Path aceAuxPath;

	public ProcessAceFormatSourcesBerkeley(VodbEnv vodb) throws DatabaseException {
		super();
		map = new IdMapper();
		this.vodb = vodb;
		Class nativeIdClass = this.vodb.getNativeIdClass();
		if (nativeIdClass.equals(Integer.class)) {
			for (PrimordialId primId: PrimordialId.values()) {
				for (UUID uid:  primId.getUids()) {
					I_IdVersioned thinId = new ThinIdVersioned(primId.getNativeId(Integer.MIN_VALUE), 1);
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
				new ArrayList<I_Position>());
	}

	@Override
	public Logger getLog() {
		return vodb.getLogger();
	}
	
	@Override
	public void cleanup(I_IntSet relsToIgnore) throws Exception {
		optimizeLicitWords();
	}


	@SuppressWarnings("unchecked")
	@Override
	public void writeConcept(Date releaseDate, Object conceptKey,
			Object conceptStatus, boolean defChar, Object pathId) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinConPart con = new ThinConPart();
		con.setPathId(map.getIntId((Collection<UUID>) pathId, aceAuxPath, version));
		con.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		con.setConceptStatus(map.getIntId((UUID) conceptStatus, aceAuxPath, version));
		con.setDefined(defChar);
		DatabaseEntry key = new DatabaseEntry(); 
		
		intBinder.objectToEntry(map.getIntId((UUID) conceptKey, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		I_ConceptAttributeVersioned vcon;
		if (vodb.getConceptDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vcon = (I_ConceptAttributeVersioned) conBinder.entryToObject(value);
		} else {
			vcon = new ThinConVersioned(map.getIntId((UUID) conceptKey, aceAuxPath, version), 1);
		}
		if (vcon.addVersion(con)) {
			value = new DatabaseEntry(); 
			conBinder.objectToEntry(vcon, value);
			vodb.getConceptDb().put(null, key, value);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeDescription(Date releaseDate, Object descriptionId,
			Object status, Object conceptId, String text, boolean capStatus,
			Object typeInt, String lang, Object pathID) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinDescPart desc = new ThinDescPart();
		desc.setPathId(map.getIntId((Collection<UUID>) pathID, aceAuxPath, version));
		desc.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		desc.setStatusId(map.getIntId((UUID) status, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));
		desc.setInitialCaseSignificant(capStatus);
		desc.setLang(lang);
		desc.setText(text);
		desc.setTypeId(map.getIntId((UUID) typeInt, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));
		
		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(map.getIntId((UUID) descriptionId, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		
		I_DescriptionVersioned vdesc;
		if (vodb.getDescDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vdesc = (I_DescriptionVersioned) descBinder.entryToObject(value);
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

	@SuppressWarnings("unchecked")
	@Override
	public void writeRelationship(Date releaseDate, Object relID, Object statusId, 
			Object conceptOneID, Object relationshipTypeConceptID,
			Object conceptTwoID, Object characteristic, Object refinability,
			int group, Object pathId) throws Exception {
		int version = ThinVersionHelper.convert(releaseDate.getTime());
		ThinRelPart part = new ThinRelPart();
      int c1id = map.getIntId((UUID) conceptOneID, aceAuxPath, version);
      int c2id = map.getIntId((UUID) conceptTwoID, aceAuxPath, version);
      if (c1id == c2id) {
         // log for now, throw exception later
         AceLog.getEditLog().log(Level.SEVERE, "*RECURSION* Rel points a concept to itself: " + relID + 
               " c one id: " + conceptOneID + 
               " c two id: " + conceptTwoID);
         throw new Exception("*RECURSION* Rel points a concept to itself: " + relID + 
                 " c one id: " + conceptOneID + 
                 " c two id: " + conceptTwoID);
         
      }
		part.setPathId(map.getIntId((Collection<UUID>) pathId, aceAuxPath, version));
		part.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
		part.setStatusId(map.getIntId((Collection<UUID>) statusId, aceAuxPath, version));
		part.setCharacteristicId(map.getIntId((UUID) characteristic, aceAuxPath, version));
		part.setGroup(group);
		part.setRefinabilityId(map.getIntId((UUID) refinability, aceAuxPath, version));
		part.setRelTypeId(map.getIntId((UUID) relationshipTypeConceptID, aceAuxPath, version));

		DatabaseEntry key = new DatabaseEntry(); 
		intBinder.objectToEntry(map.getIntId((UUID) relID, aceAuxPath, version), key);
		DatabaseEntry value = new DatabaseEntry(); 
		I_RelVersioned vrel;
		if (vodb.getRelDb().get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			 vrel = (I_RelVersioned) relBinder.entryToObject(value);
          if ((vrel.getC1Id() == c1id) && (vrel.getC2Id() == c2id)) {
             // rel ok
          } else {
             // log for now, throw exception later
            AceLog.getEditLog().log(Level.SEVERE, "Duplicate rels with different c1 and c2 for: " + relID);
            vrel = new ThinRelVersioned(map.getIntId(UUID.randomUUID(), aceAuxPath, version),
                  map.getIntId((UUID) conceptOneID, aceAuxPath, version),
                  map.getIntId((UUID) conceptTwoID, aceAuxPath, version),
                  1);
             
          }
		} else {
			vrel = new ThinRelVersioned(map.getIntId((UUID) relID, aceAuxPath, version),
					map.getIntId((UUID) conceptOneID, aceAuxPath, version),
					map.getIntId((UUID) conceptTwoID, aceAuxPath, version),
					1);
		}
		if (vrel.addVersionNoRedundancyCheck(part)) {
			value = new DatabaseEntry(); 
			relBinder.objectToEntry(vrel, value);
			vodb.getRelDb().put(null, key, value);
		}
	}

	public Map<UUID, Integer> getConstantToIntMap() {
		return map.uuidToInt;
	}

	@Override
	public void writeIllicitWord(String word) throws IOException {
		vodb.writeIllicitWord(word);
		
	}

	@Override
	public void writeLicitWord(String word)  throws IOException {
		vodb.writeLicitWord(word);
		
	}

	@Override
	public void optimizeLicitWords() throws IOException {
		vodb.optimizeLicitWords();
	}

    @Override
    public void writeId(UUID primaryUuid, UUID sourceSystemUuid, Object sourceId, UUID statusUuid, Date statusDate, UUID pathUuid) throws Exception {
        map.getIntId(Arrays.asList(new UUID[] {primaryUuid}), aceAuxPath, ThinVersionHelper.convert(statusDate.getTime()));
        
        ThinIdVersioned idv = ((VodbEnv)LocalVersionedTerminology.get()).getId(primaryUuid);
        ThinIdPart idPart = new ThinIdPart();
        idPart.setIdStatus(vodb.uuidToNative(statusUuid));
        idPart.setPathId(vodb.uuidToNative(pathUuid));
        idPart.setSource(vodb.uuidToNative(sourceSystemUuid));
        idPart.setSourceId(sourceId);
        idPart.setVersion(ThinVersionHelper.convert(statusDate.getTime()));
        if (idv.getVersions().contains(idPart) == false) {
            idv.addVersion(idPart);
            vodb.writeId(idv);
        }
        
    }
	protected void readBooleanMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws Exception {

	    
		
		st.nextToken();
		boolean booleanValue = st.sval.toLowerCase().startsWith("t");

		
		int version = ThinVersionHelper.convert(statusDate.getTime());
	    int refsetId = map.getIntId((UUID) refsetUuid, aceAuxPath, version);
	    int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);
	    int statusId = map.getIntId((UUID) statusUuid, aceAuxPath, version);
	    int componentId = map.getIntId((UUID) componentUuid, aceAuxPath, version);
	    int pathId = map.getIntId((UUID) pathUuid, aceAuxPath, version);
	    int typeId = ThinExtBinder.getExtensionType(EXT_TYPE.BOOLEAN);

		VodbEnv tf = (VodbEnv) LocalVersionedTerminology.get();
		I_ThinExtByRefVersioned ext = tf.newExtension(refsetId, memberId, componentId, typeId);
		if (tf.hasExtension(memberId)) {
			ext = tf.getExtension(memberId);
		} 
		ThinExtByRefPartBoolean part = new ThinExtByRefPartBoolean();
		part.setPathId(pathId);
		part.setStatus(statusId);
		part.setValue(booleanValue);
		part.setVersion(version);
		ext.addVersion(part);

	}

	@Override
	protected void readConIntMember(StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid)
			throws Exception {
		st.nextToken();
		UUID conceptUuid = (UUID) getId(st);
		st.nextToken();
		int intValue = Integer.parseInt(st.sval);

		
		int version = ThinVersionHelper.convert(statusDate.getTime());
	    int refsetId = map.getIntId((UUID) refsetUuid, aceAuxPath, version);
	    int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);
	    int statusId = map.getIntId((UUID) statusUuid, aceAuxPath, version);
	    int componentId = map.getIntId((UUID) componentUuid, aceAuxPath, version);
	    int pathId = map.getIntId((UUID) pathUuid, aceAuxPath, version);
	    int typeId = ThinExtBinder.getExtensionType(EXT_TYPE.BOOLEAN);
	    int conceptId =  map.getIntId((UUID) conceptUuid, aceAuxPath, version);

		VodbEnv tf = (VodbEnv) LocalVersionedTerminology.get();
		I_ThinExtByRefVersioned ext = tf.newExtension(refsetId, memberId, componentId, typeId);
		if (tf.hasExtension(memberId)) {
			ext = tf.getExtension(memberId);
		} 
		ThinExtByRefPartConceptInt part = new ThinExtByRefPartConceptInt();
		part.setPathId(pathId);
		part.setStatus(statusId);
		part.setConceptId(conceptId);
		part.setIntValue(intValue);
		part.setVersion(version);
		ext.addVersion(part);
		
	}

	@Override
	protected void readConceptMember(StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid)
			throws Exception {
		st.nextToken();
		UUID conceptUuid = (UUID) getId(st);

		
		int version = ThinVersionHelper.convert(statusDate.getTime());
	    int refsetId = map.getIntId((UUID) refsetUuid, aceAuxPath, version);
	    int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);
	    int statusId = map.getIntId((UUID) statusUuid, aceAuxPath, version);
	    int componentId = map.getIntId((UUID) componentUuid, aceAuxPath, version);
	    int pathId = map.getIntId((UUID) pathUuid, aceAuxPath, version);
	    int typeId = ThinExtBinder.getExtensionType(EXT_TYPE.BOOLEAN);
	    int conceptId =  map.getIntId((UUID) conceptUuid, aceAuxPath, version);

		VodbEnv tf = (VodbEnv) LocalVersionedTerminology.get();
		I_ThinExtByRefVersioned ext = tf.newExtension(refsetId, memberId, componentId, typeId);
		if (tf.hasExtension(memberId)) {
			ext = tf.getExtension(memberId);
		} 
		ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
		part.setPathId(pathId);
		part.setStatus(statusId);
		part.setConceptId(conceptId);
		part.setVersion(version);
		ext.addVersion(part);

	}
		


}
