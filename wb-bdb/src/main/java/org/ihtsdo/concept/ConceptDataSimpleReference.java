package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ComponentList;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponentBinder;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.image.ImageBinder;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.util.GCValueComponentMap;
import org.ihtsdo.db.util.ReferenceType;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptDataSimpleReference extends ConceptDataManager {

	private static GCValueComponentMap componentMap = new GCValueComponentMap(ReferenceType.WEAK);

	private ConceptAttributes attributes;
	private AddSrcRelList srcRels;
	private AddDescriptionList descriptions;
	private AddImageList images;
	private AddMemberList refsetMembers;
	private SetModifiedWhenChangedList destRelNidTypeNidList;
	private SetModifiedWhenChangedList refsetNidMemberNidForConceptList;
	private SetModifiedWhenChangedList refsetNidMemberNidForDescriptionsList;
	private SetModifiedWhenChangedList refsetNidMemberNidForRelsList;
	private SetModifiedWhenChangedList refsetNidMemberNidForImagesList;
	private SetModifiedWhenChangedList refsetNidMemberNidForRefsetMembersList;
	private CopyOnWriteArraySet<Integer> descNids;
	private CopyOnWriteArraySet<Integer> srcRelNids;
	private CopyOnWriteArraySet<Integer> imageNids;
	private CopyOnWriteArraySet<Integer> memberNids;
	private ConcurrentHashMap<Integer,RefsetMember<?,?>> refsetMembersMap;


	public ConceptDataSimpleReference(Concept enclosingConcept) throws IOException {
		super(new NidDataFromBdb(enclosingConcept.getNid(), Bdb
				.getConceptDb().getReadOnly(), Bdb.getConceptDb()
				.getReadWrite()));
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
	}

	public ConceptDataSimpleReference(Concept enclosingConcept, byte[] roBytes,
			byte[] mutableBytes) throws IOException {
		super(new NidDataInMemory(roBytes, mutableBytes));
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
	}


	public AddSrcRelList getSourceRels() throws IOException {
		if (srcRels == null) {
			srcRels = new AddSrcRelList(getList(new RelationshipBinder(),
					OFFSETS.SOURCE_RELS, enclosingConcept));
		}
		return srcRels;
	}

	public AddDescriptionList getDescriptions() throws IOException {
		if (descriptions == null) {
			descriptions = new AddDescriptionList(getList(new DescriptionBinder(),
					OFFSETS.DESCRIPTIONS, enclosingConcept));
		}
		return descriptions;
	}

	private <C extends ConceptComponent<V, C>, V extends Revision<V, C>> ArrayList<C> getList(
			ConceptComponentBinder<V, C> binder, OFFSETS offset,
			Concept enclosingConcept) throws IOException {
		binder.setupBinder(enclosingConcept, componentMap);
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

	private Collection<RefsetMember<?, ?>> getList(RefsetMemberBinder binder,
			OFFSETS offset, Concept enclosingConcept)
			throws IOException {
		binder.setupBinder(enclosingConcept, componentMap);
		Collection<RefsetMember<?, ?>> componentList;
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

	public ConceptAttributes getConceptAttributes() throws IOException {
		if (attributes == null) {
			ArrayList<ConceptAttributes> components = getList(
					new ConceptAttributesBinder(), OFFSETS.ATTRIBUTES,
					enclosingConcept);
			if (components != null && components.size() > 0) {
				attributes = components.get(0);
			}		
			
		}
		return attributes;
	}

	public AddMemberList getRefsetMembers() throws IOException {
		if (refsetMembers == null) {
			refsetMembers = new AddMemberList(getList(
					new RefsetMemberBinder(), OFFSETS.REFSET_MEMBERS,
					enclosingConcept));
		}
		return refsetMembers;
	}

	public AddImageList getImages() throws IOException {
		if (images == null) {
			images = new AddImageList(getList(new ImageBinder(),
					OFFSETS.IMAGES, enclosingConcept));
		}
		return images;
	}
	
	public I_GetNidData getNidData() {
		return nidData;
	}

	
	protected SetModifiedWhenChangedList getArrayIntList(OFFSETS offset)
			throws IOException {
		SetModifiedWhenChangedList roList = getReadOnlyArrayIntList(offset);
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
		return new SetModifiedWhenChangedList(binder.entryToObject(readWriteInput));
	}

	protected SetModifiedWhenChangedList getReadOnlyArrayIntList(
			OFFSETS offset) throws IOException {
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() < 4) {
			return new SetModifiedWhenChangedList();
		}
		readOnlyInput.mark(128);
		readOnlyInput.skipFast(offset.offset);
		int dataOffset = readOnlyInput.readInt();
		readOnlyInput.reset();
		readOnlyInput.skipFast(dataOffset);
		IntListPairsBinder binder = new IntListPairsBinder();
		return new SetModifiedWhenChangedList(binder.entryToObject(readOnlyInput));
	}

	protected CopyOnWriteArraySet<Integer> getReadOnlyIntSet(OFFSETS offset)
			throws IOException {
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() < 4) {
			return new CopyOnWriteArraySet<Integer>();
		}
		readOnlyInput.mark(128);
		readOnlyInput.skipFast(offset.offset);
		int dataOffset = readOnlyInput.readInt();
		readOnlyInput.reset();
		readOnlyInput.skipFast(dataOffset);
		IntSetBinder binder = new IntSetBinder();
		return binder.entryToObject(readOnlyInput);
	}

	protected CopyOnWriteArraySet<Integer> getMutableIntSet(OFFSETS offset)
			throws IOException {
		TupleInput mutableInput = nidData.getMutableTupleInput();
		if (mutableInput.available() < 4) {
			return new CopyOnWriteArraySet<Integer>();
		}
		mutableInput.mark(128);
		mutableInput.skipFast(offset.offset);
		int dataOffset = mutableInput.readInt();
		mutableInput.reset();
		mutableInput.skipFast(dataOffset);
		IntSetBinder binder = new IntSetBinder();
		return binder.entryToObject(mutableInput);
	}

	protected List<RefsetMember<?, ?>> getRefsetMembers(
			SetModifiedWhenChangedList members) throws IOException {
		List<RefsetMember<?, ?>> refsetMembers = new ArrayList<RefsetMember<?, ?>>();
		int i = 0;
		while (i < members.size()) {
			int refsetNid = members.get(i++);
			int memberNid = members.get(i++);
			Concept refsetConcept = Bdb.getConceptDb().getConcept(refsetNid);
			RefsetMember<?, ?> member = refsetConcept
					.getRefsetMember(memberNid);
			if (member != null) {
				refsetMembers.add(refsetConcept.getRefsetMember(memberNid));
			} else {
				StringBuffer buff = new StringBuffer();
				buff.append("Unable to find extension. RefsetNid: ");
				buff.append(refsetNid);
				buff.append(" MemberNid: ");
				buff.append(memberNid);
				buff.append("\n\nReferenced from concept: ");
				buff.append(enclosingConcept.toLongString());
				buff.append("\n\nRefset concept: ");
				buff.append(refsetConcept.toLongString());
				AceLog.getAppLog().warning(buff.toString());
			}
		}
		return refsetMembers;
	}

	@Override
	public void setDestRelNidTypeNidList(List<Integer> destRelNidTypeNidList) throws IOException {
		this.destRelNidTypeNidList = new SetModifiedWhenChangedList(destRelNidTypeNidList);
		enclosingConcept.modified();
	}

	public void set(ConceptAttributes attr) throws IOException {
		if (attributes != null) {
			throw new IOException(
					"Attributes is already set. Please modify the exisiting attributes object.");
		}
		attributes = attr;
		enclosingConcept.modified();
	}

	@Override
	public void setRefsetNidMemberNidForConceptList(
			List<Integer> refsetNidMemberNidForConceptList) throws IOException {
		this.refsetNidMemberNidForConceptList = new SetModifiedWhenChangedList(
				refsetNidMemberNidForConceptList);
		enclosingConcept.modified();
	}

	@Override
	public void setRefsetNidMemberNidForDescriptionsList(List<Integer> refsetNidMemberNidForDescriptionsList)
	throws IOException {
		this.refsetNidMemberNidForDescriptionsList = new SetModifiedWhenChangedList(refsetNidMemberNidForDescriptionsList);
		enclosingConcept.modified();
	}

	@Override
	public void setRefsetNidMemberNidForRelsList(List<Integer> refsetNidMemberNidForRelsList)
	throws IOException {
		this.refsetNidMemberNidForRelsList = new SetModifiedWhenChangedList(refsetNidMemberNidForRelsList);
		enclosingConcept.modified();
	}

	@Override
	public void setRefsetNidMemberNidForImagesList(List<Integer> refsetNidMemberNidForImagesList)
	throws IOException {
		this.refsetNidMemberNidForImagesList = new SetModifiedWhenChangedList(refsetNidMemberNidForImagesList);
		enclosingConcept.modified();
	}

	@Override
	public void setRefsetNidMemberNidForRefsetMembersList(List<Integer> refsetNidMemberNidForRefsetMembersList)
	throws IOException {
		this.refsetNidMemberNidForRefsetMembersList = new SetModifiedWhenChangedList(refsetNidMemberNidForRefsetMembersList);
		enclosingConcept.modified();
	}

	@Override
	public SetModifiedWhenChangedList getDestRelNidTypeNidList() throws IOException {
		if (destRelNidTypeNidList == null) {
			destRelNidTypeNidList = getArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS);
		}
		return destRelNidTypeNidList;
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForConceptList() throws IOException {
		if (refsetNidMemberNidForConceptList == null) {
			refsetNidMemberNidForConceptList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT);
		}
		return refsetNidMemberNidForConceptList;
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForDescriptionsList()
			throws IOException {
		if (refsetNidMemberNidForDescriptionsList == null) {
			refsetNidMemberNidForDescriptionsList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS);
		}
		return refsetNidMemberNidForDescriptionsList;
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForRelsList() throws IOException {
		if (refsetNidMemberNidForRelsList == null) {
			refsetNidMemberNidForRelsList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS);;
		}
		return refsetNidMemberNidForRelsList;
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesList() throws IOException {
		if (refsetNidMemberNidForImagesList == null) {
			refsetNidMemberNidForImagesList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES);
		}
		return refsetNidMemberNidForImagesList;
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersList()
	throws IOException {
		if (refsetNidMemberNidForRefsetMembersList == null) {
			refsetNidMemberNidForRefsetMembersList = getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS);
		}
		return refsetNidMemberNidForRefsetMembersList;
	}

	@Override
	public SetModifiedWhenChangedList getDestRelNidTypeNidListReadOnly() throws IOException {
		return getReadOnlyArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS);
	}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForConceptListReadOnly()
			throws IOException {
				return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT);
			}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForDescriptionsListReadOnly()
			throws IOException {
				return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS);
			}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForRelsListReadOnly()
			throws IOException {
				return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS);
			}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersListReadOnly()
			throws IOException {
				return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS);
			}

	@Override
	public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesListReadOnly()
			throws IOException {
				return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES);
			}

	/**
	 * TODO add call for getRefsetMembersForComponent(int refset, int componentNid);
	 * @throws IOException 
	 */
	@Override
	public List<RefsetMember<?, ?>> getExtensionsForComponent(int componentNid) throws IOException {
		if (componentNid == enclosingConcept.getConceptId()) {
			SetModifiedWhenChangedList conceptMembers = getRefsetNidMemberNidForConceptList();	
			return getRefsetMembers(conceptMembers);
		} else if (getDescNids().contains(componentNid)) {
			SetModifiedWhenChangedList descMembers = getRefsetNidMemberNidForDescriptionsList();
			return getRefsetMembers(descMembers);
		} else if (getSrcRelNids().contains(componentNid)) {
			SetModifiedWhenChangedList srcRelMembers = getRefsetNidMemberNidForRelsList();
			return getRefsetMembers(srcRelMembers);
		} else if (getImageNids().contains(componentNid)) {
			SetModifiedWhenChangedList imageMembers = getRefsetNidMemberNidForImagesList();
			return getRefsetMembers(imageMembers);
		} // must be in one of the refset members by elimination
		SetModifiedWhenChangedList refsetMembers = getRefsetNidMemberNidForRefsetMembersList();
		return getRefsetMembers(refsetMembers);
	}

	@Override
	public Set<Integer> getDescNids() throws IOException {
		if (descNids == null) {
		    CopyOnWriteArraySet<Integer> temp = 
                new CopyOnWriteArraySet<Integer>(getDescNidsReadOnly());
			temp.addAll(getMutableIntSet(OFFSETS.DESC_NIDS));
			descNids = temp;
		}
		return descNids;
	}

	@Override
	public Set<Integer> getDescNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.DESC_NIDS);
	}

	@Override
	public CopyOnWriteArraySet<Integer> getImageNids() throws IOException {
		if (imageNids == null) {
		    CopyOnWriteArraySet<Integer> temp = 
                new CopyOnWriteArraySet<Integer>(getImageNidsReadOnly());
			temp.addAll(getMutableIntSet(OFFSETS.IMAGE_NIDS));
			imageNids = temp;
		}
		return imageNids;
	}

	@Override
	public Set<Integer> getImageNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.IMAGE_NIDS);
	}

	@Override
	public CopyOnWriteArraySet<Integer> getSrcRelNids() throws IOException {
		if (srcRelNids == null) {
		    CopyOnWriteArraySet<Integer> temp = 
				new CopyOnWriteArraySet<Integer>(getSrcRelNidsReadOnly());
		    temp.addAll(getMutableIntSet(OFFSETS.SRC_REL_NIDS));
		    
		    srcRelNids = temp;
		}
		return srcRelNids;
	}

	@Override
	public CopyOnWriteArraySet<Integer> getSrcRelNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
	}

	@Override
	public CopyOnWriteArraySet<Integer> getMemberNids() throws IOException {
		if (memberNids == null) {
		    CopyOnWriteArraySet<Integer> temp = 
                new CopyOnWriteArraySet<Integer>(getMemberNidsReadOnly());
			temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
			memberNids = temp;
		}
		return memberNids;
	}

	@Override
	public CopyOnWriteArraySet<Integer> getMemberNidsReadOnly() throws IOException {
		return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
	}

	public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesListRef() {
		return refsetNidMemberNidForImagesList;
	}

	public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersListRef() {
		return refsetNidMemberNidForRefsetMembersList;
	}

	public void setRefsetNidMemberNidForRefsetMembersListRef(SetModifiedWhenChangedList refsetNidMemberNidForRefsetMembersListRef) {
		this.refsetNidMemberNidForRefsetMembersList = refsetNidMemberNidForRefsetMembersListRef;
		enclosingConcept.modified();
	}

	@Override
	public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
	    Collection<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();
		if (refsetMemberList.size() < useMemberMapThreshold) {
			for (RefsetMember<?, ?> member: refsetMemberList) {
				if (member.nid == memberNid) {
					return member;
				}
			}
			return null;
		}
		if (refsetMembersMap == null) {
			setupMemberMap(refsetMemberList);
		}
		return refsetMembersMap.get(memberNid);
	}

	private synchronized void setupMemberMap(Collection<RefsetMember<?, ?>> refsetMemberList) {
		if (refsetMembersMap == null) {
			refsetMembersMap = new ConcurrentHashMap<Integer, RefsetMember<?,?>>(refsetMemberList.size(), 0.75f, 2);
			for (RefsetMember<?, ?> m: refsetMemberList) {
				refsetMembersMap.put(m.nid, m);
			}
		}
	}


	@Override
	public ConceptAttributes getConceptAttributesIfChanged() throws IOException {
		if (attributes == null) {
			attributes = getConceptAttributes();
		}
		return attributes;
	}

	@Override
	public ComponentList<Description> getDescriptionsIfChanged() throws IOException {
		if (descriptions == null) {
			descriptions = getDescriptions();
		}
		return descriptions;
	}

	@Override
	public ComponentList<Image> getImagesIfChanged() throws IOException {
		if (images == null) {
			images = getImages();
		}
		return images;
	}

	@Override
	public ComponentList<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException {
		if (refsetMembers == null) {
			refsetMembers = getRefsetMembers();
		}
		return refsetMembers;
	}

	@Override
	public ComponentList<Relationship> getSourceRelsIfChanged() throws IOException {
		if (srcRels == null) {
			srcRels = getSourceRels();
		}
		return srcRels;
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
		if (getMemberNids().contains(nid)) {
		for (RefsetMember<?, ?> r : getRefsetMembers()) {
			if (r.getNid() == nid) {
				return r;
			}
		}
		}
		return null;
	}

	protected void addToMemberMap(RefsetMember<?, ?> refsetMember) {
		if (refsetMembersMap != null) {
			refsetMembersMap.put(refsetMember.nid, refsetMember);
		}
	}

	public boolean hasComponent(int nid) throws IOException {
		if (getNid() == nid) {
			return true;
		}
		if (getDescNids().contains(nid)) {
			return true;
		}
		if (getSrcRelNids().contains(nid)) {
			return true;
		}
		if (getImageNids().contains(nid)) {
			return true;
		}
		if (getMemberNids().contains(nid)) {
			return true;
		}
		return false;
	}

}