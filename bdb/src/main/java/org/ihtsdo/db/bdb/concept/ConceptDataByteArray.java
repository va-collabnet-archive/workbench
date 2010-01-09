package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.DataVersionBinder;
import org.ihtsdo.db.bdb.concept.component.RelNidTypeNidBinder;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.image.ImageBinder;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

public class ConceptDataByteArray implements I_ManageConceptData {
	private Concept enclosingConcept;
	protected I_GetNidData nidData;

	protected ConceptAttributes attributes;
	protected ArrayList<Relationship> srcRels;
	protected ArrayList<Description> descriptions;
	protected ArrayList<Image> images;
	protected ArrayList<RefsetMember<?, ?>> refsetMembers;

	public ConceptDataByteArray(Concept concept, DatabaseEntry data) {
		enclosingConcept = concept;
		nidData = new NidDataInMemory(new byte[] {}, data.getData());
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getNid()
	 */
	public int getNid() {
		return enclosingConcept.getNid();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getReadWriteDataVersion()
	 */
	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException {
		DataVersionBinder binder = DataVersionBinder.getBinder();
		return binder.entryToObject(nidData.getReadWriteTupleInput());
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getSourceRels()
	 */
	public ArrayList<Relationship> getSourceRels() throws IOException {
		if (srcRels != null) {
			return srcRels;
		}
		try {
			srcRels = getList(new RelationshipBinder(), 
					OFFSETS.SOURCE_RELS, 
					enclosingConcept);
			return srcRels;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDescriptions()
	 */
	public List<Description> getDescriptions() throws IOException {
		if (descriptions != null) {
			return descriptions;
		}
		try {
			descriptions = getList(new DescriptionBinder(),
					OFFSETS.DESCRIPTIONS,
					enclosingConcept);
			return descriptions;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private <C extends ConceptComponent<V, C>, 
	         V extends Version<V, C>> ArrayList<C> 
		getList(ConceptComponentBinder<V, C> binder, 
				OFFSETS offset, Concept enclosingConcept)
			throws InterruptedException, ExecutionException, IOException {
		binder.setupBinder(enclosingConcept);
		ArrayList<C> componentList;
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() > 0) {
			checkFormatAndVersion(readOnlyInput);
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int listStart = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(listStart);			
			componentList = binder.entryToObject(readOnlyInput);
		} else {
			componentList = new ArrayList<C>();
		}
		assert componentList != null;
		binder.setTermComponentList(componentList);
		TupleInput readWriteInput = nidData.getReadWriteTupleInput();
		if (readWriteInput.available() > 0) {
			checkFormatAndVersion(readWriteInput);
			readWriteInput.mark(128);
			readWriteInput.skipFast(offset.offset);
			int listStart = readWriteInput.readInt();
			readWriteInput.reset();
			readWriteInput.skipFast(listStart);			
			componentList = binder.entryToObject(readWriteInput);
		}
		return componentList;
	}

	private void checkFormatAndVersion(TupleInput input)
			throws UnsupportedEncodingException {
		input.mark(128);
		int formatVersion = input.readInt();
		int dataVersion = input.readInt();
		if (formatVersion != OFFSETS.CURRENT_FORMAT_VERSION) {
			throw new UnsupportedEncodingException("No support for format version: " + formatVersion);
		}
		if (dataVersion != OFFSETS.CURRENT_DATA_VERSION) {
			throw new UnsupportedEncodingException("No support for data version: " + dataVersion);
		}
		input.reset();
	}

	private ArrayList<RefsetMember<?, ?>> getList(
			RefsetMemberBinder binder, OFFSETS offset, Concept enclosingConcept)
			throws InterruptedException, ExecutionException, IOException {
		binder.setupBinder(enclosingConcept);
		ArrayList<RefsetMember<?, ?>> componentList;
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() > 0) {
			checkFormatAndVersion(readOnlyInput);
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int listStart = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(listStart);			
			componentList = binder.entryToObject(readOnlyInput);
		} else {
			componentList = new ArrayList<RefsetMember<?, ?>>();
		}
		assert componentList != null;
		binder.setTermComponentList(componentList);
		TupleInput readWriteInput = nidData.getReadWriteTupleInput();
		if (readWriteInput.available() > 0) {
			readWriteInput.mark(128);
			checkFormatAndVersion(readWriteInput);
			readWriteInput.reset();
			readWriteInput.skipFast(offset.offset);
			int listStart = readWriteInput.readInt();
			readWriteInput.reset();
			readWriteInput.skipFast(listStart);			
			componentList = binder.entryToObject(readWriteInput);
		}
		return componentList;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getConceptAttributes()
	 */
	public ConceptAttributes getConceptAttributes() throws IOException {
		if (attributes != null) {
			return attributes;
		}
		try {
			ArrayList<ConceptAttributes> components = getList(
					new ConceptAttributesBinder(), 
					OFFSETS.ATTRIBUTES, 
					enclosingConcept);
			return components.get(0);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
	 */
	public List<Relationship> getDestRels() throws IOException {
		try {
			TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
			readOnlyInput
					.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap roRelNidToTypeNidMap = RelNidTypeNidBinder
					.getBinder().entryToObject(readOnlyInput);

			TupleInput readWriteInput = nidData.getReadWriteTupleInput();
			readWriteInput
					.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap rwRelNidToTypeNidMap = RelNidTypeNidBinder
					.getBinder().entryToObject(readWriteInput);
			if (rwRelNidToTypeNidMap.size() > roRelNidToTypeNidMap.size()) {
				for (int relNid : roRelNidToTypeNidMap.keys().elements()) {
					rwRelNidToTypeNidMap.put(relNid, roRelNidToTypeNidMap
							.get(relNid));
				}
			} else {
				for (int relNid : rwRelNidToTypeNidMap.keys().elements()) {
					roRelNidToTypeNidMap.put(relNid, rwRelNidToTypeNidMap
							.get(relNid));
				}
				rwRelNidToTypeNidMap = roRelNidToTypeNidMap;
			}
			List<Relationship> destRels = new ArrayList<Relationship>();
			for (int relNid : rwRelNidToTypeNidMap.keys().elements()) {
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getRefsetMembers()
	 */
	public List<RefsetMember<?, ?>> getRefsetMembers() throws IOException {
		if (refsetMembers != null) {
			return refsetMembers;
		}
		try {
			refsetMembers = getList(new RefsetMemberBinder(),
					OFFSETS.REFSET_MEMBERS, 
					enclosingConcept);
			return refsetMembers;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getImages()
	 */
	public List<Image> getImages() throws IOException {
		if (images != null) {
			return images;
		}
		try {
			images = getList(new ImageBinder(), OFFSETS.IMAGES, 
					enclosingConcept);
			return images;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}


	protected I_GetNidData getNidData() {
		return nidData;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#set(org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes)
	 */
	public void set(ConceptAttributes attr) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		if (attributes != null) {
			throw new IOException(
					"Attributes is already set. Please modify the exisiting attributes object.");
		}
		attributes = attr;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept.component.description.Description)
	 */
	public void add(Description desc) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getDescriptions().add(desc);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept.component.relationship.Relationship)
	 */
	public void add(Relationship rel) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getSourceRels().add(rel);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept.component.image.Image)
	 */
	public void add(Image img) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getImages().add(img);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept.component.refset.RefsetMember)
	 */
	public void add(RefsetMember<?, ?> refsetMember) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getRefsetMembers().add(refsetMember);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getAllNids()
	 */
	public int[] getAllNids() throws IOException {
		ArrayIntList allContainedNids = new ArrayIntList();
		allContainedNids.add(enclosingConcept.getNid());
		for (Description d: getDescriptions()) {
			allContainedNids.add(d.nid);
		}
		for (Relationship r: getSourceRels()) {
			allContainedNids.add(r.nid);
		}
		for (Image i: getImages()) {
			allContainedNids.add(i.nid);
		}
		for (RefsetMember<?, ?> r: getRefsetMembers()) {
			allContainedNids.add(r.nid);
		}
		return allContainedNids.toArray();
	}

	@Override
	public byte[] getReadOnlyBytes() throws InterruptedException, ExecutionException, IOException {
		return nidData.getReadOnlyBytes();
	}

	@Override
	public byte[] getReadWriteBytes() throws InterruptedException, ExecutionException {
		return nidData.getReadWriteBytes();
	}
	
	public SoftReference<ConceptAttributes> getAttributesRef() {
		return new SoftReference<ConceptAttributes>(attributes);
	}

	public SoftReference<ArrayList<Relationship>> getSrcRelsRef() {
		return new SoftReference<ArrayList<Relationship>>(srcRels);
	}

	public SoftReference<ArrayList<Description>> getDescriptionsRef() {
		return new SoftReference<ArrayList<Description>>(descriptions);
	}

	public SoftReference<ArrayList<Image>> getImagesRef() {
		return new SoftReference<ArrayList<Image>>(images);
	}

	public SoftReference<ArrayList<RefsetMember<?, ?>>> getRefsetMembersRef() {
		return new SoftReference<ArrayList<RefsetMember<?,?>>>(refsetMembers);
	}

	@Override
	public TupleInput getReadWriteTupleInput() throws InterruptedException,
			ExecutionException {
		return nidData.getReadWriteTupleInput();
	}

}