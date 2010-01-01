/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.NidData;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.DataVersionBinder;
import org.ihtsdo.db.bdb.concept.component.RelNidTypeNidBinder;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesVersion;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionVersion;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.refset.AbstractRefsetMember;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipVersion;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.TupleInput;

/**
 * File format:<br>
 * @author kec
 *
 */
public class ConceptData  {
	
	public enum OFFSETS {
		FORMAT_VERSION(4, null),
		DATA_VERSION(4, FORMAT_VERSION),
		ATTRIBUTES(4, DATA_VERSION),
		DESCRIPTIONS(4, ATTRIBUTES),
		SOURCE_RELS(4, DESCRIPTIONS),
		DEST_REL_ORIGIN_NID_TYPE_NIDS(4, SOURCE_RELS), // Binder done
		IMAGES(4, DEST_REL_ORIGIN_NID_TYPE_NIDS),
		REFSET_MEMBERS(4, IMAGES),
		REFSETNID_MEMBERNID_FOR_CONCEPT(4, REFSET_MEMBERS), // Binder done
		REFSETNID_MEMBERNID_COMPONENTNID_FOR_COMPONENTS(4, REFSETNID_MEMBERNID_FOR_CONCEPT), // Binder done
		IDENTIFIERS_FOR_CONCEPT(4, REFSETNID_MEMBERNID_COMPONENTNID_FOR_COMPONENTS),
		IDENTIFIERS_FOR_COMPONENTS(4, IDENTIFIERS_FOR_CONCEPT);
		
		private int offset;
		private int bytes;
		OFFSETS(int bytes, OFFSETS prev) {
			this.bytes = bytes;
			if (prev == null) {
				offset = 0;
			} else {
				offset = prev.offset + prev.bytes;
			}
		}
		
		int getOffset(byte[] data) {
			TupleInput offsetInput = new TupleInput(data);
			offsetInput.skipFast(offset);
			return offsetInput.readInt();
		}

		public int getOffset() {
			return offset;
		}

		public int getBytes() {
			return bytes;
		}
	}
	
	
	private int nid;
	private boolean editable;
	private NidData data;

	
	ConceptData(int nid, boolean editable) throws IOException {
		this.nid = nid;
		this.editable = editable;
		data = new NidData(nid, Bdb.getConceptDb().getReadOnly(),
				Bdb.getConceptDb().getReadWrite());
	}

	public int getNid() {
		return nid;
	}


	public int getReadWriteDataVersion() throws InterruptedException, ExecutionException {
		DataVersionBinder binder = DataVersionBinder.getBinder();
		return binder.entryToObject(data.getReadWriteTupleInput());
	}

	public ArrayList<Relationship> getSourceRels() throws IOException {
		ConceptComponentBinder<Relationship, RelationshipVersion> binder = RelationshipBinder.getBinder();
		binder.setupBinder(nid, editable);
		try {
			TupleInput readOnlyInput = data.getReadOnlyTupleInput();
			readOnlyInput.skipFast(OFFSETS.SOURCE_RELS.offset);
			ArrayList<Relationship> readOnlyRels = binder.entryToObject(readOnlyInput);

			TupleInput readWriteInput = data.getReadWriteTupleInput();
			readWriteInput.skipFast(OFFSETS.SOURCE_RELS.offset);
			binder.setTermComponentList(readOnlyRels);
			return binder.entryToObject(readWriteInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}
	
	public List<Description> getDescriptions() throws IOException {
		ConceptComponentBinder<Description, DescriptionVersion> binder = DescriptionBinder.getBinder();
		binder.setupBinder(nid, editable);
		try {
			TupleInput readOnlyInput = data.getReadOnlyTupleInput();
			readOnlyInput.skipFast(OFFSETS.DESCRIPTIONS.offset);
			ArrayList<Description> readOnlyDescriptions = binder.entryToObject(readOnlyInput);

			TupleInput readWriteInput = data.getReadWriteTupleInput();
			readWriteInput.skipFast(OFFSETS.DESCRIPTIONS.offset);
			binder.setTermComponentList(readOnlyDescriptions);
			return binder.entryToObject(readWriteInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}


	public ConceptAttributes getConceptAttributes()
			throws IOException {
		ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion> binder = 
				ConceptAttributesBinder.getBinder();
		binder.setupBinder(nid, editable);
		try {
			TupleInput readOnlyInput = data.getReadOnlyTupleInput();
			readOnlyInput.skipFast(OFFSETS.ATTRIBUTES.offset);
			ArrayList<ConceptAttributes> readOnlyComponents = binder.entryToObject(readOnlyInput);

			TupleInput readWriteInput = data.getReadWriteTupleInput();
			readWriteInput.skipFast(OFFSETS.ATTRIBUTES.offset);
			binder.setTermComponentList(readOnlyComponents);
			ArrayList<ConceptAttributes> components = binder.entryToObject(readWriteInput);
			if (components.size() == 1) {
				return components.get(0);
			}
			return null;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Destination rels are stored as a relid and a type id in an array. 
	 * @return
	 * @throws IOException
	 */
	public List<Relationship> getDestRels() throws IOException {
		try {
			TupleInput readOnlyInput = data.getReadOnlyTupleInput();
			readOnlyInput.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap roRelNidToTypeNidMap = RelNidTypeNidBinder.getBinder().entryToObject(readOnlyInput);

			TupleInput readWriteInput = data.getReadWriteTupleInput();
			readWriteInput.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap rwRelNidToTypeNidMap = RelNidTypeNidBinder.getBinder().entryToObject(readWriteInput);
			if (rwRelNidToTypeNidMap.size() > roRelNidToTypeNidMap.size()) {
				for (int relNid: roRelNidToTypeNidMap.keys().elements()) {
					rwRelNidToTypeNidMap.put(relNid, roRelNidToTypeNidMap.get(relNid));
				}
			} else {
				for (int relNid: rwRelNidToTypeNidMap.keys().elements()) {
					roRelNidToTypeNidMap.put(relNid, rwRelNidToTypeNidMap.get(relNid));
				}
				rwRelNidToTypeNidMap = roRelNidToTypeNidMap;
			}
			List<Relationship> destRels = new ArrayList<Relationship>();
			for (int relNid: rwRelNidToTypeNidMap.keys().elements()) {
				Concept c = Bdb.getConceptForComponent(relNid);
				destRels.add(c.getRelationship(relNid));
			}
			return destRels;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	public List<AbstractRefsetMember> getExtensions() throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<Image> getImages() throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

}