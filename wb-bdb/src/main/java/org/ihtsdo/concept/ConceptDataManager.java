/**
 * 
 */
package org.ihtsdo.concept;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ComponentList;
import org.ihtsdo.concept.component.DataVersionBinder;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.util.NidPair;

import com.sleepycat.bind.tuple.TupleInput;

/**
 * File format:<br>
 * 
 * @author kec
 * 
 */
public abstract class ConceptDataManager implements I_ManageConceptData {

	public class AddDescriptionList extends ComponentList<Description> {

		private static final long serialVersionUID = 1L;

		public AddDescriptionList(Collection<? extends Description> c) {
			super(c);
		}

		@Override
		public boolean add(Description e) {
			try {
				boolean returnValue = super.add(e);
				processNewDesc(e);
				return returnValue;
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public class AddSrcRelList extends ComponentList<Relationship> {

		private static final long serialVersionUID = 1L;

		public AddSrcRelList(Collection<? extends Relationship> c) {
			super(c);
		}

		@Override
		public boolean add(Relationship e) {
			try {
			    if (e == null) {
			        AceLog.getAppLog().info("found it");
			    }
			    assert e != null: "Relationship is null processing:\n" + this;
				boolean returnValue = super.add(e);
				processNewRel(e);
				return returnValue;
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public class AddImageList extends ComponentList<Image> {

		private static final long serialVersionUID = 1L;

		public AddImageList(Collection<? extends Image> c) {
			super(c);
		}

		@Override
		public boolean add(Image e) {
			try {
				boolean returnValue = super.add(e);
				processNewImage(e);
				return returnValue;
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public class AddMemberList extends ComponentList<RefsetMember<?, ?>> {

		private static final long serialVersionUID = 1L;

		public AddMemberList(Collection<? extends RefsetMember<?, ?>> c) {
			super(c);
		}

		@Override
		public boolean add(RefsetMember<?, ?> e) {
			try {
				assert e != null: "Trying to add a null refset member to: " + this;
				boolean returnValue = super.add(e);
				processNewRefsetMember(e);
				return returnValue;
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	
	public class SetModifiedWhenChangedList extends CopyOnWriteArrayList<NidPair> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SetModifiedWhenChangedList() {
			super();
		}

		public SetModifiedWhenChangedList(Collection<NidPair> c) {
			super(c);
		}

		public SetModifiedWhenChangedList(NidPair[] toCopyIn) {
			super(toCopyIn);
		}

		@Override
		public void add(int index, NidPair element) {
			super.add(index, element);
			modified();
		}

		@Override
		public boolean add(NidPair e) {
			boolean returnValue = super.add(e);
			modified();
			return returnValue;
		}

		@Override
		public boolean addAll(Collection<? extends NidPair> c) {
			boolean returnValue = super.addAll(c);
			modified();
			return returnValue;
		}

		@Override
		public boolean addAll(int index, Collection<? extends NidPair> c) {
			boolean returnValue =  super.addAll(index, c);
			modified();
			return returnValue;
		}

		@Override
		public int addAllAbsent(Collection<? extends NidPair> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addIfAbsent(NidPair e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		public synchronized boolean forget(NidPair pair) {
			boolean removed = super.remove(pair);
			if (removed) {
	            modified();
			}
			return removed;
		}

		@Override
		public boolean remove(Object o) {
			return forget((NidPair) o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public NidPair remove(int index) {
			throw new UnsupportedOperationException();
		}
		
	}

	/**
	 * When the number of refset members are greater than this value, use a map
	 * for looking up members instead of iterating through a list.
	 */
	protected static int useMemberMapThreshold = 5;

	protected Concept enclosingConcept;
	protected I_GetNidData nidData;

	public ConceptDataManager(I_GetNidData nidData) throws IOException {
		super();
		this.nidData = nidData;
		this.lastChange = getDataVersion();
		this.lastWrite = this.lastChange;
	}
	
	public void resetNidData() {
		this.nidData.reset();
	}

	private long getDataVersion() throws IOException {
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		long dataVersion = Long.MIN_VALUE;
		if (readOnlyInput.available() > 0) {
			dataVersion = checkFormatAndVersion(readOnlyInput);
		}
		TupleInput readWriteInput = nidData.getMutableTupleInput();
		if (readWriteInput.available() > 0) {
			dataVersion = checkFormatAndVersion(readWriteInput);
		}
		return dataVersion;
	}

	protected long lastChange = Long.MIN_VALUE;
	protected long lastWrite = Long.MIN_VALUE;
	protected long lastExtinctRemoval = Long.MIN_VALUE;

	public void modified() {
		lastChange = Bdb.gVersion.incrementAndGet();
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
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
	 */
	public List<Relationship> getDestRels() throws IOException {

		List<Relationship> destRels = new ArrayList<Relationship>();
		for (NidPair pair: getDestRelNidTypeNidList()) {
			int relNid = pair.getNid1();
			int conceptNid = Bdb.getNidCNidMap().getCNid(relNid);
			Concept c = Bdb.getConceptForComponent(conceptNid);
			if (c != null) {
			    Relationship r = c.getRelationship(relNid);
			    if (r != null) {
		            destRels.add(r);
			    }
			}
		}
		return destRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.description.Description)
	 */
	public void add(Description desc) throws IOException {
		getDescriptions().addDirect(desc);
		getDescNids().add(desc.nid);
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.relationship.Relationship)
	 */
	public void add(Relationship rel) throws IOException {
		getSourceRels().addDirect(rel);
		getSrcRelNids().add(rel.nid);
		modified();
	}

	void processNewRel(Relationship rel) throws IOException {
        assert rel != null : "rel is null: " + this;
		assert rel.nid != 0 : "relNid is 0: " + this;
		assert rel.getTypeId() != 0 : "relTypeNid is 0: " + this;
		Concept dest = Concept.get(rel.getC2Id());
		assert Bdb.getConceptForComponent(rel.nid) != null : "No concept for component: "
				+ rel.nid
				+ "\nsourceConcept: "
				+ this.enclosingConcept.toLongString()
				+ "\ndestConcept: "
				+ dest.toLongString();
		List<NidPair> relNidTypeNidList = (List<NidPair>) dest.getData()
				.getDestRelNidTypeNidList();
		NidPair pair = new NidPair(rel.nid, rel.getTypeId());
		relNidTypeNidList.add(pair);
		BdbCommitManager.addUncommittedNoChecks(dest);
		getSrcRelNids().add(rel.nid);
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.image.Image)
	 */
	public void add(Image img) throws IOException {
		getImages().addDirect(img);
		getImageNids().add(img.nid);
		modified();
	}

	void processNewImage(Image img) throws IOException {
		assert img.nid != 0 : "imgNid is 0: " + this;
		getImageNids().add(img.nid);
		modified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
	 * .component.refset.RefsetMember)
	 */
	public void add(RefsetMember<?, ?> refsetMember) throws IOException {
		getRefsetMembers().addDirect(refsetMember);
		getMemberNids().add(refsetMember.nid);
		modified();
	}

	public abstract boolean hasComponent(int nid) throws IOException;

	void processNewRefsetMember(RefsetMember<?, ?> refsetMember)
			throws IOException {
		assert refsetMember != null : "refsetMember is null: " + this;
		assert refsetMember.nid != 0 : "memberNid is 0: " + this;
		assert refsetMember.getComponentId() != 0 : "componentNid is 0: "
				+ this;
		assert refsetMember.enclosingConceptNid != 0 : "refsetNid is 0: "
				+ this;
		getMemberNids().add(refsetMember.nid);
		addToMemberMap(refsetMember);
		Concept dest = ConceptDataManager.this.enclosingConcept;
		if (!hasComponent(refsetMember.getComponentId())) {
			dest = Bdb.getConceptForComponent(refsetMember.getComponentId());
		}
		if (dest != null) {
			dest.getData().addRefsetNidMemberNidForComponent(
					refsetMember.enclosingConceptNid, refsetMember.nid,
					refsetMember.getComponentId());
			if (dest.getNid() != this.enclosingConcept.getNid()) {
	            BdbCommitManager.addUncommittedNoChecks(dest);
			}
		}
		modified();
	}

	protected abstract void addToMemberMap(RefsetMember<?, ?> refsetMember);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getAllNids()
	 */
	public Collection<Integer> getAllNids() throws IOException {
		Collection<Integer> descNids = getDescNids();
		Collection<Integer> srcRelNids = getSrcRelNids();
		Collection<Integer> imgNids = getImageNids();
 		Collection<Integer> memberNids = getMemberNids();

		int size = 1 + descNids.size() + srcRelNids.size() + imgNids.size()
				+ memberNids.size();

		ArrayList<Integer> allContainedNids = new ArrayList<Integer>(size);
		allContainedNids.add(enclosingConcept.getNid());
        assert enclosingConcept.getNid() != 0;
        assert !descNids.contains(0);
		allContainedNids.addAll(descNids);
        assert !srcRelNids.contains(0);
		allContainedNids.addAll(srcRelNids);
	    assert !imgNids.contains(0);
		allContainedNids.addAll(imgNids);
        assert !memberNids.contains(0);
		allContainedNids.addAll(memberNids);
		return allContainedNids;
	}

	@Override
	public byte[] getReadOnlyBytes() throws IOException {
		return nidData.getReadOnlyBytes();
	}

	@Override
	public byte[] getReadWriteBytes() throws IOException {
		return nidData.getReadWriteBytes();
	}

	@Override
	public TupleInput getReadWriteTupleInput() throws IOException {
		return nidData.getMutableTupleInput();
	}

	@Override
	public void addRefsetNidMemberNidForComponent(int refsetNid, int memberNid,
			int componentNid) throws IOException {
		List<NidPair> list = null;
		if (componentNid == enclosingConcept.getNid()) {
			list = (List<NidPair>) getRefsetNidMemberNidForConceptList();
		} else if (getDescNids().contains(componentNid)) {
			list = (List<NidPair>) getRefsetNidMemberNidForDescriptionsList();
		} else if (getSrcRelNids().contains(componentNid)) {
			list = (List<NidPair>) getRefsetNidMemberNidForRelsList();
		} else if (getImageNids().contains(componentNid)) {
			list = (List<NidPair>) getRefsetNidMemberNidForImagesList();
		} else if (getMemberNids().contains(componentNid)) {
			list = (List<NidPair>) getRefsetNidMemberNidForRefsetMembersList();
		} else {
			AceLog.getAppLog().warning(
					"Cannot find component nid: " + componentNid
							+ " in concept: " + enclosingConcept);
		}
		if (list != null) {
			ArrayList<Integer> toAdd = new ArrayList<Integer>();
			toAdd.add(refsetNid);
			toAdd.add(memberNid);
			NidPair refsetNidMemberNidPair = new NidPair(refsetNid, memberNid);
			list.add(refsetNidMemberNidPair);
		}
		modified();
	}

	void processNewDesc(Description e) throws IOException {
		assert e.nid != 0 : "descNid is 0: " + this;
		getDescNids().add(e.nid);
		BdbCommitManager.addUncommittedDescNid(e.nid);
		modified();
	}

	@Override
	public String toString() {
		return enclosingConcept.toLongString();
	}

	public long getLastWrite() {
		return lastWrite;
	}

	public void setLastWrite(long lastWrite) {
		this.lastWrite = Math.max(this.lastWrite, lastWrite);
	}

	public long getLastChange() {
		return lastChange;
	}

	@Override
	public final boolean isUncommitted() {
	    if (lastChange > BdbCommitManager.getLastCommit()) {
	        return hasUncommittedComponents();
	    }
		return false;
	}
	
	public abstract boolean hasUncommittedComponents();

	@Override
	public final boolean isUnwritten() {
		return lastChange > lastWrite;
	}

	protected long checkFormatAndVersion(TupleInput input)
			throws UnsupportedEncodingException {
		input.mark(128);
		int formatVersion = input.readInt();
		long dataVersion = input.readLong();
		if (formatVersion != OFFSETS.CURRENT_FORMAT_VERSION) {
			throw new UnsupportedEncodingException(
					"No support for format version: " + formatVersion);
		}
		input.reset();
		return dataVersion;
	}
	@Override
	public boolean isPrimordial() throws IOException {
		return nidData.isPrimordial();
	}

}