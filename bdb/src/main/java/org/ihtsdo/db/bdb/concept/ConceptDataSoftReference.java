/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.DataVersionBinder;
import org.ihtsdo.db.bdb.concept.component.Revision;
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
import org.ihtsdo.db.util.ConcurrentSet;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

/**
 * File format:<br>
 * 
 * @author kec
 * 
 */
public class ConceptDataSoftReference implements I_ManageConceptData {

	private Concept enclosingConcept;
	protected I_GetNidData nidData;

	protected SoftReference<ConceptAttributes> attributesRef;
	protected SoftReference<ArrayList<Relationship>> srcRelsRef;
	protected SoftReference<ArrayList<Description>> descriptionsRef;
	protected SoftReference<ArrayList<Image>> imagesRef;
	protected SoftReference<ArrayList<RefsetMember<?, ?>>> refsetMembersRef;
	protected SoftReference<ArrayIntList> destRelNidTypeNidListRef;
	protected SoftReference<ArrayIntList> refsetNidMemberNidForConceptListRef;
	protected SoftReference<ArrayIntList> refsetNidMemberNidForDescriptionsListRef;
	protected SoftReference<ArrayIntList> refsetNidMemberNidForRelsListRef;
	protected SoftReference<ArrayIntList> refsetNidMemberNidForImagesListRef;
	protected SoftReference<ArrayIntList> refsetNidMemberNidForRefsetMembersListRef;
	protected SoftReference<IntSet> descNidsRef;
	protected SoftReference<IntSet> srcRelNidsRef;
	protected SoftReference<IntSet> imageNidsRef;
	protected SoftReference<HashMap<Integer, RefsetMember<?, ?>>> refsetMembersMapRef;
	/**
	 * If the concept is editable, add all changes to the strongReferences to
	 * ensure they don't get garbage collected inappropriately.
	 */
	private ConcurrentSet<Object> strongReferences;

	ConceptDataSoftReference(Concept enclosingConcept) throws IOException {
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
		if (enclosingConcept.isEditable()) {
			strongReferences = new ConcurrentSet<Object>(5);
		}
		nidData = new NidDataFromBdb(enclosingConcept.getNid(), Bdb
				.getConceptDb().getReadOnly(), Bdb.getConceptDb()
				.getReadWrite());
	}

	ConceptDataSoftReference(Concept enclosingConcept, DatabaseEntry data)
			throws IOException {
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
		if (enclosingConcept.isEditable()) {
			strongReferences = new ConcurrentSet<Object>(5);
		}
		nidData = new NidDataInMemory(new byte[] {}, data.getData());
	}

	public ConceptDataSoftReference(Concept enclosingConcept, byte[] roBytes,
			byte[] mutableBytes) {
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
		if (enclosingConcept.isEditable()) {
			strongReferences = new ConcurrentSet<Object>(5);
		}
		nidData = new NidDataInMemory(roBytes, mutableBytes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getNid()
	 */
	public int getNid() {
		return enclosingConcept.getNid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#getReadWriteDataVersion()
	 */
	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException {
		DataVersionBinder binder = DataVersionBinder.getBinder();
		return binder.entryToObject(nidData.getMutableTupleInput());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getSourceRels()
	 */
	public ArrayList<Relationship> getSourceRels() throws IOException {
		ArrayList<Relationship> rels;
		if (srcRelsRef != null) {
			rels = srcRelsRef.get();
			if (rels != null) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(rels);
				}
				return rels;
			}
		}
		try {
			rels = getList(new RelationshipBinder(), OFFSETS.SOURCE_RELS,
					enclosingConcept);
			if (enclosingConcept.isEditable() && rels != null) {
				strongReferences.add(rels);
				srcRelsRef = new SoftReference<ArrayList<Relationship>>(rels);
			}
			return rels;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDescriptions()
	 */
	public List<Description> getDescriptions() throws IOException {
		ArrayList<Description> descList = null;
		if (descriptionsRef != null) {
			descList = descriptionsRef.get();
			if (descList != null) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(descList);
				}
				return descList;
			}
		}
		try {
			descList = getList(new DescriptionBinder(), OFFSETS.DESCRIPTIONS,
					enclosingConcept);
			if (enclosingConcept.isEditable()) {
				strongReferences.add(descList);
				descriptionsRef = new SoftReference<ArrayList<Description>>(
						descList);
			}
			return descList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private <C extends ConceptComponent<V, C>, V extends Revision<V, C>> ArrayList<C> getList(
			ConceptComponentBinder<V, C> binder, OFFSETS offset,
			Concept enclosingConcept) throws InterruptedException,
			ExecutionException, IOException {
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
		TupleInput readWriteInput = nidData.getMutableTupleInput();
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
			throw new UnsupportedEncodingException(
					"No support for format version: " + formatVersion);
		}
		if (dataVersion != OFFSETS.CURRENT_DATA_VERSION) {
			throw new UnsupportedEncodingException(
					"No support for data version: " + dataVersion);
		}
		input.reset();
	}

	private ArrayList<RefsetMember<?, ?>> getList(RefsetMemberBinder binder,
			OFFSETS offset, Concept enclosingConcept)
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
		TupleInput readWriteInput = nidData.getMutableTupleInput();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getConceptAttributes()
	 */
	public ConceptAttributes getConceptAttributes() throws IOException {
		ConceptAttributes attr;
		if (attributesRef != null) {
			attr = attributesRef.get();
			if (attr != null) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(attr);
				}
				return attr;
			}
		}
		try {
			ArrayList<ConceptAttributes> components = getList(
					new ConceptAttributesBinder(), OFFSETS.ATTRIBUTES,
					enclosingConcept);
			if (components != null && components.size() > 0) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(components.get(0));
				}
				attributesRef = new SoftReference<ConceptAttributes>(components.get(0));
				return components.get(0);
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
	 */
	public List<Relationship> getDestRels() throws IOException {
		ArrayIntList destRelNidTypeNidList = getDestRelNidTypeNidList();

		List<Relationship> destRels = new ArrayList<Relationship>();
		IntIterator itr = destRelNidTypeNidList.iterator();
		while (itr.hasNext()) {
			int relNid = itr.next();
			@SuppressWarnings("unused")
			int typeNid = itr.next();
			int conceptNid = Bdb.getNidCNidMap().getCNid(relNid);
			Concept c = Bdb.getConceptForComponent(conceptNid);
			destRels.add(c.getRelationship(relNid));
		}
		return destRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getRefsetMembers()
	 */
	public List<RefsetMember<?, ?>> getRefsetMembers() throws IOException {
		ArrayList<RefsetMember<?, ?>> refsetMemberList;
		if (refsetMembersRef != null) {
			refsetMemberList = refsetMembersRef.get();
			if (refsetMemberList != null) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(refsetMemberList);
				}
				return refsetMemberList;
			}
		}
		try {
			refsetMemberList = getList(new RefsetMemberBinder(),
					OFFSETS.REFSET_MEMBERS, enclosingConcept);
			if (enclosingConcept.isEditable() && refsetMemberList != null) {
				strongReferences.add(refsetMemberList);
				refsetMembersRef = new SoftReference<ArrayList<RefsetMember<?, ?>>>(
						refsetMemberList);
			}
			return refsetMemberList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getImages()
	 */
	public List<Image> getImages() throws IOException {
		ArrayList<Image> imgList;
		if (imagesRef != null) {
			imgList = imagesRef.get();
			if (imgList != null) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(imgList);
				}
				return imgList;
			}
		}
		try {
			imgList = getList(new ImageBinder(), OFFSETS.IMAGES,
					enclosingConcept);
			if (enclosingConcept.isEditable() && imgList != null) {
				strongReferences.add(imgList);
				imagesRef = new SoftReference<ArrayList<Image>>(imgList);
			}
			return imgList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	public SoftReference<ConceptAttributes> getAttributesRef() {
		return attributesRef;
	}

	public SoftReference<ArrayList<Relationship>> getSrcRelsRef() {
		return srcRelsRef;
	}

	public SoftReference<ArrayList<Description>> getDescriptionsRef() {
		return descriptionsRef;
	}

	public SoftReference<ArrayList<Image>> getImagesRef() {
		return imagesRef;
	}

	public SoftReference<ArrayList<RefsetMember<?, ?>>> getRefsetMembersRef() {
		return refsetMembersRef;
	}

	public I_GetNidData getNidData() {
		return nidData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#set(org.ihtsdo.db.bdb.concept
	 * .component.attributes.ConceptAttributes)
	 */
	public void set(ConceptAttributes attr) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		if (attributesRef != null) {
			throw new IOException(
					"Attributes is already set. Please modify the exisiting attributes object.");
		}
		attributesRef = new SoftReference<ConceptAttributes>(attr);
		strongReferences.add(attr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.description.Description)
	 */
	public void add(Description desc) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getDescNids().add(desc.nid);
		getDescriptions().add(desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.relationship.Relationship)
	 */
	public void add(Relationship rel) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getSrcRelNids().add(rel.nid);
		getSourceRels().add(rel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.image.Image)
	 */
	public void add(Image img) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getImageNids().add(img.nid);
		getImages().add(img);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.refset.RefsetMember)
	 */
	public void add(RefsetMember<?, ?> refsetMember) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getRefsetMembers().add(refsetMember);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getAllNids()
	 */
	public int[] getAllNids() throws IOException {
		ArrayIntList allContainedNids = new ArrayIntList();
		allContainedNids.add(enclosingConcept.getNid());
		for (Description d : getDescriptions()) {
			allContainedNids.add(d.nid);
		}
		for (Relationship r : getSourceRels()) {
			allContainedNids.add(r.nid);
		}
		for (Image i : getImages()) {
			allContainedNids.add(i.nid);
		}
		for (RefsetMember<?, ?> r : getRefsetMembers()) {
			allContainedNids.add(r.nid);
		}
		return allContainedNids.toArray();
	}

	@Override
	public byte[] getReadOnlyBytes() throws InterruptedException,
			ExecutionException, IOException {
		return nidData.getReadOnlyBytes();
	}

	@Override
	public byte[] getReadWriteBytes() throws InterruptedException,
			ExecutionException {
		return nidData.getReadWriteBytes();
	}

	@Override
	public TupleInput getReadWriteTupleInput() throws InterruptedException,
			ExecutionException {
		return nidData.getMutableTupleInput();
	}

	@Override
	public void setDestRelNidTypeNidList(ArrayIntList destRelNidTypeNidList)
			throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		destRelNidTypeNidListRef = new SoftReference<ArrayIntList>(
				destRelNidTypeNidList);
		strongReferences.add(destRelNidTypeNidList);
	}

	@Override
	public void setRefsetNidMemberNidForConceptList(
			ArrayIntList refsetNidMemberNidForConceptList) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		refsetNidMemberNidForConceptListRef = new SoftReference<ArrayIntList>(
				refsetNidMemberNidForConceptList);
		strongReferences.add(refsetNidMemberNidForConceptList);
	}

	@Override
	public void setRefsetNidMemberNidForDescriptionsList(
			ArrayIntList refsetNidMemberNidForDescriptionsList)
			throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		refsetNidMemberNidForDescriptionsListRef = new SoftReference<ArrayIntList>(
				refsetNidMemberNidForDescriptionsList);
		strongReferences.add(refsetNidMemberNidForDescriptionsList);
	}

	@Override
	public void setRefsetNidMemberNidForRelsList(
			ArrayIntList refsetNidMemberNidForRelsList) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		refsetNidMemberNidForRelsListRef = new SoftReference<ArrayIntList>(
				refsetNidMemberNidForRelsList);
		strongReferences.add(refsetNidMemberNidForRelsList);
	}

	@Override
	public void setRefsetNidMemberNidForImagesList(
			ArrayIntList refsetNidMemberNidForImagesList) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		refsetNidMemberNidForImagesListRef = new SoftReference<ArrayIntList>(
				refsetNidMemberNidForImagesList);
		strongReferences.add(refsetNidMemberNidForImagesList);
	}


	@Override
	public void setRefsetNidMemberNidForRefsetMembersList(
			ArrayIntList refsetNidMemberNidForRefsetMembersList) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		refsetNidMemberNidForRefsetMembersListRef = new SoftReference<ArrayIntList>(
				refsetNidMemberNidForRefsetMembersList);
		strongReferences.add(refsetNidMemberNidForRefsetMembersListRef);
	}

	@Override
	public ArrayIntList getDestRelNidTypeNidList() throws IOException {
		ArrayIntList returnList = null;
		if (destRelNidTypeNidListRef != null) {
			returnList = destRelNidTypeNidListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS);
		destRelNidTypeNidListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}

	private ArrayIntList getArrayIntList(OFFSETS offset) throws IOException {
		try {
			ArrayIntList roList = getReadOnlyArrayIntList(offset);
			IntListPairsBinder binder = new IntListPairsBinder();
			binder.setReadOnlyList(roList);
			TupleInput readWriteInput = nidData.getMutableTupleInput();
			if (readWriteInput.available() < 4) {
				return roList;
			}
			readWriteInput.mark(128);
			readWriteInput.skipFast(offset.offset);
			int dataOffset = readWriteInput.readInt();
			readWriteInput.reset();
			readWriteInput.skipFast(dataOffset);
			return binder.entryToObject(readWriteInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private ArrayIntList getReadOnlyArrayIntList(OFFSETS offset)
			throws IOException {
		try {
			TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
			if (readOnlyInput.available() < 4) {
				return new ArrayIntList();
			}
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int dataOffset = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(dataOffset);
			IntListPairsBinder binder = new IntListPairsBinder();
			return binder.entryToObject(readOnlyInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private IntSet getReadOnlyIntSet(OFFSETS offset) throws IOException {
		try {
			TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
			if (readOnlyInput.available() < 4) {
				return new IntSet();
			}
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int dataOffset = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(dataOffset);
			IntSetBinder binder = new IntSetBinder();
			return binder.entryToObject(readOnlyInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private IntSet getMutableIntSet(OFFSETS offset) throws IOException {
		try {
			TupleInput mutableInput = nidData.getMutableTupleInput();
			if (mutableInput.available() < 4) {
				return new IntSet();
			}
			mutableInput.mark(128);
			mutableInput.skipFast(offset.offset);
			int dataOffset = mutableInput.readInt();
			mutableInput.reset();
			mutableInput.skipFast(dataOffset);
			IntSetBinder binder = new IntSetBinder();
			return binder.entryToObject(mutableInput);
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForConceptList()
			throws IOException {
		ArrayIntList returnList = null;
		if (refsetNidMemberNidForConceptListRef != null) {
			returnList = refsetNidMemberNidForConceptListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT);
		refsetNidMemberNidForConceptListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForDescriptionsList()
			throws IOException {
		ArrayIntList returnList = null;
		if (refsetNidMemberNidForDescriptionsListRef != null) {
			returnList = refsetNidMemberNidForDescriptionsListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS);
		refsetNidMemberNidForDescriptionsListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForRelsList() throws IOException {
		ArrayIntList returnList = null;
		if (refsetNidMemberNidForRelsListRef != null) {
			returnList = refsetNidMemberNidForRelsListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS);
		refsetNidMemberNidForRelsListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForImagesList() throws IOException {
		ArrayIntList returnList = null;
		if (refsetNidMemberNidForImagesListRef != null) {
			returnList = refsetNidMemberNidForImagesListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES);
		refsetNidMemberNidForImagesListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}


	@Override
	public ArrayIntList getRefsetNidMemberNidForRefsetMembersList() throws IOException {
		ArrayIntList returnList = null;
		if (refsetNidMemberNidForRefsetMembersListRef != null) {
			returnList = refsetNidMemberNidForRefsetMembersListRef.get();
		}
		if (returnList != null) {
			return returnList;
		}
		returnList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS);
		refsetNidMemberNidForRefsetMembersListRef = new SoftReference<ArrayIntList>(
				returnList);
		if (enclosingConcept.isEditable()) {
			strongReferences.add(returnList);
		}
		return returnList;
	}


	@Override
	public ArrayIntList getDestRelNidTypeNidListReadOnly() throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS);
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForConceptListReadOnly()
			throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT);
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForDescriptionsListReadOnly()
			throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS);
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForRelsListReadOnly()
			throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS);
	}

	@Override
	public ArrayIntList getRefsetNidMemberNidForRefsetMembersListReadOnly()
			throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS);
	}


	@Override
	public ArrayIntList getRefsetNidMemberNidForImagesListReadOnly()
			throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES);
	}

	@Override
	public ConceptComponent<?, ?> getComponent(int nid) throws IOException {
		if (getConceptAttributes() != null && getConceptAttributes().nid == nid) {
			return getConceptAttributes();
		}
		
		if (getDescNids().contains(nid)) {
			for (Description d : getDescriptions()) {
				if (d.getNid() == nid) {
					return d;
				}
			}
		}
		if (getSrcRelNids().contains(nid)) {
			for (Relationship r : getSourceRels()) {
				if (r.getNid() == nid) {
					return r;
				}
			}
		}
		if (getImageNids().contains(nid)) {
			for (Image i : getImages()) {
				if (i.getNid() == nid) {
					return i;
				}
			}
		}
		for (RefsetMember<?, ?> r : getRefsetMembers()) {
			if (r.getNid() == nid) {
				return r;
			}
		}
		return null;
	}

	/**
	 * TODO add call for getRefsetMembersForComponent(int refset, int componentNid);
	 * @throws IOException 
	 */
	@Override
	public List<RefsetMember<?, ?>> getExtensionsForComponent(int componentNid) throws IOException {
		if (componentNid == enclosingConcept.getConceptId()) {
			ArrayIntList conceptMembers = getRefsetNidMemberNidForConceptList();	
			return getRefsetMembers(conceptMembers);
		} else if (getDescNids().contains(componentNid)) {
			ArrayIntList descMembers = getRefsetNidMemberNidForDescriptionsList();
			return getRefsetMembers(descMembers);
		} else if (getSrcRelNids().contains(componentNid)) {
			ArrayIntList srcRelMembers = getRefsetNidMemberNidForRelsList();
			return getRefsetMembers(srcRelMembers);
		} else if (getImageNids().contains(componentNid)) {
			ArrayIntList imageMembers = getRefsetNidMemberNidForImagesList();
			return getRefsetMembers(imageMembers);
		} // must be in one of the refset members by elimination
		ArrayIntList refsetMembers = getRefsetNidMemberNidForRefsetMembersList();
		return getRefsetMembers(refsetMembers);
	}

	private List<RefsetMember<?, ?>> getRefsetMembers(
			ArrayIntList members) throws IOException {
		List<RefsetMember<?, ?>> refsetMembers = new ArrayList<RefsetMember<?,?>>();
		int i = 0;
		while (i < members.size()) {
			int refsetNid = members.get(i++);
			int memberNid = members.get(i++);
			Concept c = Bdb.getConceptDb().getConcept(refsetNid);
			refsetMembers.add(c.getRefsetMember(memberNid));
		}
		return refsetMembers;
	}

	@Override
	public IntSet getDescNids()  throws IOException {
		if (descNidsRef != null) {
			IntSet nids = descNidsRef.get();
			if (nids != null) {
				return nids;
			}
		}
		IntSet nids = getDescNidsReadOnly().addAll(
				getMutableIntSet(OFFSETS.DESC_NIDS).getSetValues());
		if (enclosingConcept.isEditable()) {
			strongReferences.add(nids);
		}
		descNidsRef = new SoftReference<IntSet>(nids);
		return nids;
	}

	@Override
	public IntSet getDescNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.DESC_NIDS);
	}

	@Override
	public IntSet getImageNids()  throws IOException {
		if (imageNidsRef != null) {
			IntSet nids = imageNidsRef.get();
			if (nids != null) {
				return nids;
			}
		}
		IntSet nids = getImageNidsReadOnly().addAll(
				getMutableIntSet(OFFSETS.IMAGE_NIDS).getSetValues());
		if (enclosingConcept.isEditable()) {
			strongReferences.add(nids);
		}
		imageNidsRef = new SoftReference<IntSet>(nids);
		return nids;
	}

	@Override
	public IntSet getImageNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.IMAGE_NIDS);
	}

	@Override
	public IntSet getSrcRelNids()  throws IOException {
		if (srcRelNidsRef != null) {
			IntSet nids = srcRelNidsRef.get();
			if (nids != null) {
				return nids;
			}
		}
		IntSet nids = getSrcRelNidsReadOnly().addAll(
				getMutableIntSet(OFFSETS.SRC_REL_NIDS).getSetValues());
		if (enclosingConcept.isEditable()) {
			strongReferences.add(nids);
		}
		srcRelNidsRef = new SoftReference<IntSet>(nids);
		return nids;
	}

	@Override
	public IntSet getSrcRelNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
	}

	public SoftReference<ArrayIntList> getRefsetNidMemberNidForImagesListRef() {
		return refsetNidMemberNidForImagesListRef;
	}

	public SoftReference<ArrayIntList> getRefsetNidMemberNidForRefsetMembersListRef() {
		return refsetNidMemberNidForRefsetMembersListRef;
	}

	public void setRefsetNidMemberNidForRefsetMembersListRef(
			SoftReference<ArrayIntList> refsetNidMemberNidForRefsetMembersListRef) {
		this.refsetNidMemberNidForRefsetMembersListRef = refsetNidMemberNidForRefsetMembersListRef;
	}


	@Override
	public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
		HashMap<Integer, RefsetMember<?, ?>> memberMap = null;
		if (refsetMembersMapRef != null) {
			memberMap = refsetMembersMapRef.get();
		}
		if (memberMap == null) {
			List<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();
			memberMap = new HashMap<Integer, RefsetMember<?,?>>(refsetMemberList.size());
			for (RefsetMember<?, ?> m: refsetMemberList) {
				memberMap.put(m.nid, m);
			}
			refsetMembersMapRef = 
				new SoftReference<HashMap<Integer,RefsetMember<?,?>>>(memberMap);
		}
		return memberMap.get(memberNid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isUncommitted() {
		if (strongReferences == null || strongReferences.size() == 0) {
			return false;
		}
		for (Object o: strongReferences) {
			if (Collection.class.isAssignableFrom(o.getClass())) {
				Collection<ConceptComponent<?,?>>  coll = 
					(Collection<ConceptComponent<?,?>>) o;
				for (ConceptComponent<?,?> component: coll) {
					if (component.isUncommitted() == true) {
						return true;
					}
				}
			} else if (ConceptComponent.class.isAssignableFrom(o.getClass())) {
				ConceptComponent<?,?> component = (ConceptComponent<?,?>) o;
				if (component.isUncommitted() == true) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void makeWritable() {
		if (strongReferences == null) {
			strongReferences = new ConcurrentSet<Object>(5);
		}
	}

}