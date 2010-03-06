package org.ihtsdo.concept;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.ConceptDataManager.SetModifiedWhenChangedList;
import org.ihtsdo.concept.component.ComponentList;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.Description.Version;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.computer.kindof.KindOfSpec;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.db.util.GCValueConceptMap;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.ReferenceType;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.I_ConceptualizeExternally;
import org.ihtsdo.lucene.LuceneManager;

public class Concept implements I_Transact, I_GetConceptData {

	public static ReferenceType refType = ReferenceType.WEAK;

	public static GCValueConceptMap concepts = new GCValueConceptMap(
			refType);

	public static Concept mergeAndWrite(EConcept eConcept) throws IOException {
		int conceptNid = Bdb.uuidToNid(eConcept.getPrimordialUuid());
		assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";
		Concept c = get(conceptNid);
		if (c.isPrimordial()) {
			populateFromEConcept(eConcept, c);
			LuceneManager.writeToLucene(c.getDescriptions());
			BdbCommitManager.addUncommittedNoChecks(c);
		} else {
			mergeWithEConcept(eConcept, c);
			BdbCommitManager.addUncommittedNoChecks(c);
		}
		return c;
	}
	
	private boolean isPrimordial() throws IOException {
		return data.isPrimordial();
	}

	public static Concept get(EConcept eConcept) throws IOException {
		int conceptNid = Bdb.uuidToNid(eConcept.getConceptAttributes()
				.getPrimordialComponentUuid());
		assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";
		Concept c = get(conceptNid);
		return populateFromEConcept(eConcept, c);
	}
	
	private static Concept mergeWithEConcept(EConcept eConcept, Concept c)
			throws IOException {
		if (eConcept.getPrimordialUuid().equals(
				UUID.fromString("a902562c-4768-4809-9859-a613d3914fe3"))) {
			AceLog.getAppLog().info("developer origin: Concept: \n" + 
					c.toLongString() + "\n\n" + eConcept);
		}
		EConceptAttributes eAttr = eConcept.getConceptAttributes();
		if (eAttr != null) {
			if (c.getConceptAttributes() == null) {
				setAttributesFromEConcept(c, eAttr);
			} else {
				ConceptAttributes ca = c.getConceptAttributes();
				ca.merge(new ConceptAttributes(eAttr, c));
			}
		}
		if (eConcept.getDescriptions() != null && 
				eConcept.getDescriptions().size() != 0) {
			if (c.getDescriptions() == null || c.getDescriptions().size() == 0) {
				setDescriptionsFromEConcept(eConcept, c);
			} else {
				Set<Integer> currentDNids = c.data.getDescNids();
				for (EDescription ed: eConcept.getDescriptions()) {
					int dNid = Bdb.uuidToNid(ed.primordialUuid);
					Description d = c.getDescription(dNid);
					if (currentDNids.contains(dNid)) {
						d.merge(new Description(ed, c));
					} else {
						c.getDescriptions().add(d);
					}
				}
			}
			LuceneManager.writeToLucene(c.getDescriptions());
		}
		if (eConcept.getRelationships() != null && 
				eConcept.getRelationships().size() != 0) {
			if (c.getSourceRels() == null || c.getSourceRels().size() == 0) {
				setRelationshipsFromEConcept(eConcept, c);
			} else {
				Set<Integer> currentSrcRelNids = c.data.getSrcRelNids();
				for (ERelationship er: eConcept.getRelationships()) {
					int rNid = Bdb.uuidToNid(er.primordialUuid);
					Relationship r = c.getSourceRel(rNid);
					if (currentSrcRelNids.contains(rNid)) {
						r.merge(new Relationship(er, c));
					} else {
						c.getSourceRels().add(r);
					}
				}
			}
		}
		if (eConcept.getImages() != null && 
				eConcept.getImages().size() != 0) {
			if (c.getImages() == null || c.getImages().size() == 0) {
				setImagesFromEConcept(eConcept, c);
			} else {
				Set<Integer> currentImageNids = c.data.getImageNids();
				for (EImage er: eConcept.getImages()) {
					int iNid = Bdb.uuidToNid(er.primordialUuid);
					Image r = c.getImage(iNid);
					if (currentImageNids.contains(iNid)) {
						r.merge(new Image(er, c));
					} else {
						c.getImages().add(r);
					}
				}
			}
		}
		if (eConcept.getRefsetMembers() != null && 
				eConcept.getRefsetMembers().size() != 0) {
			if (c.getRefsetMembers() == null || c.getRefsetMembers().size() == 0) {
				setRefsetMembersFromEConcept(eConcept, c);
			} else {
				Set<Integer> currentMemberNids = c.data.getMemberNids();
				for (ERefsetMember<?> er: eConcept.getRefsetMembers()) {
					int rNid = Bdb.uuidToNid(er.primordialUuid);
					RefsetMember<?, ?> r = c.getRefsetMember(rNid);
					if (currentMemberNids.contains(rNid)) {
						r.merge(RefsetMemberFactory.create(er, c));
					} else {
						c.getRefsetMembers().add(RefsetMemberFactory.create(er, c));
					}
				}
			}
		}

		if (eConcept.getDestRelUuidTypeUuids() != null && 
				eConcept.getDestRelUuidTypeUuids().size() != 0) {
			if (c.getData().getDestRelNidTypeNidList() == null || 
					c.getData().getDestRelNidTypeNidList().size() == 0) {
				setDestRelNidTypeNidFromEConcept(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getDestRelUuidTypeUuids(),
						c.getData().getDestRelNidTypeNidList());
				c.data.setDestRelNidTypeNidList(nidList);
			}		
		}
		if (eConcept.getRefsetUuidMemberUuidForConcept() != null && 
				eConcept.getRefsetUuidMemberUuidForConcept().size() != 0) {
			if (c.getData().getRefsetNidMemberNidForConceptList() == null || 
					c.getData().getRefsetNidMemberNidForConceptList().size() == 0) {
				setRefsetNidMemberNidForConceptFromEConcept(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getRefsetUuidMemberUuidForConcept(),
						c.getData().getRefsetNidMemberNidForConceptList());
				c.data.setRefsetNidMemberNidForConceptList(nidList);
			}		
		}
		if (eConcept.getRefsetUuidMemberUuidForDescriptions() != null && 
				eConcept.getRefsetUuidMemberUuidForDescriptions().size() != 0) {
			if (c.getData().getRefsetNidMemberNidForDescriptionsList() == null || 
					c.getData().getRefsetNidMemberNidForDescriptionsList().size() == 0) {
				setRefsetNidMemberNidForDescriptions(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getRefsetUuidMemberUuidForDescriptions(),
						c.getData().getRefsetNidMemberNidForDescriptionsList());
				c.data.setRefsetNidMemberNidForDescriptionsList(nidList);
			}		
		}
		if (eConcept.getRefsetUuidMemberUuidForRels() != null && 
				eConcept.getRefsetUuidMemberUuidForRels().size() != 0) {
			if (c.getData().getRefsetNidMemberNidForRelsList() == null || 
					c.getData().getRefsetNidMemberNidForRelsList().size() == 0) {
				getRefsetNidMemberNidForRels(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getRefsetUuidMemberUuidForRels(),
						c.getData().getRefsetNidMemberNidForRelsList());
				c.data.setRefsetNidMemberNidForRelsList(nidList);
			}		
		}

		if (eConcept.getRefsetUuidMemberUuidForImages() != null && 
				eConcept.getRefsetUuidMemberUuidForImages().size() != 0) {
			if (c.getData().getRefsetNidMemberNidForImagesList() == null || 
					c.getData().getRefsetNidMemberNidForImagesList().size() == 0) {
				getRefsetNidMemberNidForImages(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getRefsetUuidMemberUuidForImages(),
						c.getData().getRefsetNidMemberNidForImagesList());
				c.data.setRefsetNidMemberNidForImagesList(nidList);
			}		
		}

		if (eConcept.getRefsetUuidMemberUuidForRefsetMembers() != null && 
				eConcept.getRefsetUuidMemberUuidForRefsetMembers().size() != 0) {
			if (c.getData().getRefsetNidMemberNidForRefsetMembersList() == null || 
					c.getData().getRefsetNidMemberNidForRefsetMembersList().size() == 0) {
				getRefsetNidMemberNidForRefsetMembers(eConcept, c);
			} else {
				ArrayList<Integer> nidList = mergeNidLists(c, 
						eConcept.getRefsetUuidMemberUuidForRefsetMembers(),
						c.getData().getRefsetNidMemberNidForRefsetMembersList());
				c.data.setRefsetNidMemberNidForRefsetMembersList(nidList);
			}		
		}
		if (eConcept.getPrimordialUuid().equals(
				UUID.fromString("e89c2b90-c85a-3dfb-978e-8df49046592b"))) {
			AceLog.getAppLog().info("Finished merge: Concept: \n" + 
					c.toLongString() + "\n\n" + eConcept);
		}
		return c;
	}

	private static ArrayList<Integer> mergeNidLists(Concept c,
			List<UUID> uuidPairList, List<? extends Integer> intPairList)
			throws IOException {
		HashSet<NidPair> pairSet = new HashSet<NidPair>(
				c.getData().getRefsetNidMemberNidForRefsetMembersList().size());
		Iterator<? extends Integer> nidItr = intPairList.iterator();
		while (nidItr.hasNext()) {
			pairSet.add(new NidPair(nidItr.next(), nidItr.next()));
		}

		Iterator<UUID> uuidIterator = uuidPairList.iterator();
		while (uuidIterator.hasNext()) {
			pairSet.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
					Bdb.uuidToNid(uuidIterator.next())));
		}
		ArrayList<Integer> nidList = new ArrayList<Integer>(pairSet.size() * 2);
		for (NidPair pair: pairSet) {
			pair.addToList(nidList);
		}
		return nidList;
	}

	private static void getRefsetNidMemberNidForRefsetMembers(
			EConcept eConcept, Concept c) throws IOException {
		ArrayList<Integer> refsetNidMemberNidForRefsetMembersList = new ArrayList<Integer>(
				eConcept.getRefsetUuidMemberUuidForRefsetMembers().size());
		for (UUID uuid : eConcept.getRefsetUuidMemberUuidForRefsetMembers()) {
			refsetNidMemberNidForRefsetMembersList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setRefsetNidMemberNidForRefsetMembersList(refsetNidMemberNidForRefsetMembersList);
	}

	private static void getRefsetNidMemberNidForImages(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<Integer> refsetNidMemberNidForImagesList = new ArrayList<Integer>(
				eConcept.getRefsetUuidMemberUuidForImages().size());
		for (UUID uuid : eConcept.getRefsetUuidMemberUuidForImages()) {
			refsetNidMemberNidForImagesList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setRefsetNidMemberNidForImagesList(refsetNidMemberNidForImagesList);
	}

	private static void getRefsetNidMemberNidForRels(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<Integer> refsetNidMemberNidForRelsList = new ArrayList<Integer>(
				eConcept.getRefsetUuidMemberUuidForRels().size());
		for (UUID uuid : eConcept.getRefsetUuidMemberUuidForRels()) {
			refsetNidMemberNidForRelsList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setRefsetNidMemberNidForRelsList(refsetNidMemberNidForRelsList);
	}

	private static void setRefsetNidMemberNidForDescriptions(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<Integer> refsetNidMemberNidForDescriptionsList = new ArrayList<Integer>(
				eConcept.getRefsetUuidMemberUuidForDescriptions().size());
		for (UUID uuid : eConcept.getRefsetUuidMemberUuidForDescriptions()) {
			refsetNidMemberNidForDescriptionsList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setRefsetNidMemberNidForDescriptionsList(refsetNidMemberNidForDescriptionsList);
	}

	private static void setRefsetNidMemberNidForConceptFromEConcept(
			EConcept eConcept, Concept c) throws IOException {
		ArrayList<Integer> refsetNidMemberNidForConceptList = new ArrayList<Integer>(
				eConcept.getRefsetUuidMemberUuidForConcept().size());
		for (UUID uuid : eConcept.getRefsetUuidMemberUuidForConcept()) {
			refsetNidMemberNidForConceptList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setRefsetNidMemberNidForConceptList(refsetNidMemberNidForConceptList);
	}

	private static void setDestRelNidTypeNidFromEConcept(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<Integer> destRelOriginNidTypeNidList = new ArrayList<Integer>(
				eConcept.getDestRelUuidTypeUuids().size());
		for (UUID uuid : eConcept.getDestRelUuidTypeUuids()) {
			destRelOriginNidTypeNidList.add(Bdb.uuidToNid(uuid));
		}
		c.data.setDestRelNidTypeNidList(destRelOriginNidTypeNidList);
	}

	private static void setRefsetMembersFromEConcept(EConcept eConcept,
			Concept c) throws IOException {
		for (ERefsetMember<?> eRefsetMember : eConcept.getRefsetMembers()) {
			RefsetMember<?, ?> refsetMember = RefsetMemberFactory.create(
					eRefsetMember, c);
			c.data.add(refsetMember);
		}
	}

	private static void setImagesFromEConcept(EConcept eConcept, Concept c)
			throws IOException {
		for (EImage eImage : eConcept.getImages()) {
			Image img = new Image(eImage, c);
			c.data.add(img);
		}
	}

	private static void setRelationshipsFromEConcept(EConcept eConcept,
			Concept c) throws IOException {
		for (ERelationship eRel : eConcept.getRelationships()) {
			Relationship rel = new Relationship(eRel, c);
			c.data.add(rel);
		}
	}

	private static void setDescriptionsFromEConcept(EConcept eConcept, Concept c)
			throws IOException {
		for (EDescription eDesc : eConcept.getDescriptions()) {
			Description desc = new Description(eDesc, c);
			c.data.add(desc);
		}
	}

	private static void setAttributesFromEConcept(Concept c,
			EConceptAttributes eAttr) throws IOException {
		assert eAttr != null;
		ConceptAttributes attr = new ConceptAttributes(eAttr, c);
		c.data.set(attr);
		if (eAttr.getRevisionList() != null) {
			for (I_ConceptualizeExternally eav : eAttr.getRevisionList()) {
				attr.addRevision(new ConceptAttributesRevision(eav, attr));
			}
		}
	}


	private static Concept populateFromEConcept(EConcept eConcept, Concept c)
			throws IOException {
		if (eConcept.getConceptAttributes() != null) {
			setAttributesFromEConcept(c, eConcept.getConceptAttributes());
		}
		
		if (eConcept.getDescriptions() != null) {
			setDescriptionsFromEConcept(eConcept, c);
		}
		
		if (eConcept.getRelationships() != null) {
			setRelationshipsFromEConcept(eConcept, c);
		}
		
		if (eConcept.getImages() != null) {
			setImagesFromEConcept(eConcept, c);
		}
		
		if (eConcept.getRefsetMembers() != null) {
			setRefsetMembersFromEConcept(eConcept, c);
		}

		if (eConcept.getDestRelUuidTypeUuids() != null) {
			setDestRelNidTypeNidFromEConcept(eConcept, c);
		}
		
		if (eConcept.getRefsetUuidMemberUuidForConcept() != null) {
			setRefsetNidMemberNidForConceptFromEConcept(eConcept, c);
		}
		
		if (eConcept.getRefsetUuidMemberUuidForDescriptions() != null) {
			setRefsetNidMemberNidForDescriptions(eConcept, c);
		}
		
		if (eConcept.getRefsetUuidMemberUuidForRels() != null) {
			getRefsetNidMemberNidForRels(eConcept, c);
		}

		if (eConcept.getRefsetUuidMemberUuidForImages() != null) {
			getRefsetNidMemberNidForImages(eConcept, c);
		}

		if (eConcept.getRefsetUuidMemberUuidForRefsetMembers() != null) {
			getRefsetNidMemberNidForRefsetMembers(eConcept, c);
		}
		
		return c;
	}

	public static Concept get(int nid) throws IOException {
		assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
		Concept c = concepts.get(nid);
		if (c == null) {
			Concept newC = new Concept(nid);
			c = concepts.putIfAbsent(nid, newC);
			if (c == null) {
				c = newC;
			}
		}
		return c;
	}

	public static Concept get(int nid, byte[] roBytes, byte[] mutableBytes)
			throws IOException {
		assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
		Concept c = concepts.get(nid);
		if (c == null) {
			Concept newC = new Concept(nid, roBytes, mutableBytes);
			c = concepts.putIfAbsent(nid, newC);
			if (c == null) {
				c = newC;
			}
		}
		return c;
	}

	private int nid;
	private I_ManageConceptData data;
	private static int fsDescNid = Integer.MIN_VALUE;
	private static int fsXmlDescNid = Integer.MIN_VALUE;

	private Concept(int nid) throws IOException {
		super();
		assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
		this.nid = nid;
		switch (refType) {
		case SOFT:
		case WEAK:
			data = new ConceptDataSimpleReference(this);
			break;
			
		case STRONG:
			throw new UnsupportedOperationException();
		default:
			throw new UnsupportedOperationException(
					"Can't handle reference type: " + refType);
		}
		if (Bdb.watchList.containsKey(nid)) {
			AceLog.getAppLog().info(
					"$$$$$$$$$$$$$$ Constructing concept: " + nid
							+ " $$$$$$$$$$$$$$");
		}
	}

	/**
	 * For use in testing/test cases only.
	 * 
	 * @param nid
	 * @param editable
	 * @param roBytes
	 * @param mutableBytes
	 * @throws IOException
	 */
	protected Concept(int nid, byte[] roBytes, byte[] mutableBytes)
			throws IOException {
		this.nid = nid;
		data = new ConceptDataSimpleReference(this, roBytes, mutableBytes);
		if (Bdb.watchList.containsKey(nid)) {
			AceLog.getAppLog().info(
					"############  Constructing concept: " + nid
							+ " ############");
		}
	}

	public int getNid() {
		return nid;
	}

	public ComponentList<Description> getDescriptions() throws IOException {
		if (isCanceled()) {
			return new ComponentList<Description>(new ArrayList<Description>());
		}
		return data.getDescriptions();
	}

	public ComponentList<Relationship> getSourceRels() throws IOException {
		if (isCanceled()) {
			return new ComponentList<Relationship>(new ArrayList<Relationship>());
		}
		return data.getSourceRels();
	}

	public ComponentList<Relationship> getNativeSourceRels() throws IOException {
		if (isCanceled()) {
			return new ComponentList<Relationship>(new ArrayList<Relationship>());
		}
		return data.getSourceRels();
	}

	@Override
	public void abort() throws IOException {
		// TODO...
	}

	@Override
	public void commit(int version, Set<TimePathId> values) throws IOException {
		try {
			if (ReadWriteDataVersion.get(nid) == data.getReadWriteDataVersion()) {

			} else {

			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (Concept.class.isAssignableFrom(obj.getClass())) {
			Concept another = (Concept) obj;
			return nid == another.nid;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid });
	}

	public List<UUID> getUids() throws IOException {
		if (getConceptAttributes() != null) {
			return getConceptAttributes().getUUIDs();
		}
		return new ArrayList<UUID>();
	}

	public UUID getPrimUuid() throws IOException {
		if (getConceptAttributes() != null) {
			return getConceptAttributes().getPrimUuid();
		}
		return null;
	}

	public List<UUID> getUidsForComponent(int componentNid) throws IOException {
		if (getComponent(componentNid) != null) {
			return getComponent(componentNid).getUUIDs();
		}
		AceLog.getAppLog().alertAndLogException(
				new Exception("Null component: " + componentNid
						+ " for concept: " + this.toLongString()));
		return new ArrayList<UUID>();
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positionSet)
			throws IOException {
		return getConceptAttributeTuples(allowedStatus, positionSet, true);
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		List<ConceptAttributes.Version> returnTuples = new ArrayList<ConceptAttributes.Version>();
		ConceptAttributes attr = getConceptAttributes();
		if (attr != null) {
			attr.addTuples(allowedStatus, positionSet, returnTuples,
					addUncommitted, returnConflictResolvedLatestState);
		}
		return returnTuples;
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			boolean addUncommitted) throws IOException {
		List<ConceptAttributes.Version> returnTuples = new ArrayList<ConceptAttributes.Version>();
		ConceptAttributes cattr = getConceptAttributes();
		if (cattr != null) {
		    cattr.addTuples(allowedStatus, positionSet,
                returnTuples, addUncommitted);
		}
		return returnTuples;
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getConceptAttributeTuples(config.getAllowedStatus(), config
				.getViewPositionSetReadOnly(), true,
				returnConflictResolvedLatestState);
	}

	public ConceptAttributes getConceptAttributes() throws IOException {
		return data.getConceptAttributes();
	}

	public ArrayList<ConceptAttributes> getConceptAttributesList()
			throws IOException {
		ArrayList<ConceptAttributes> returnList = new ArrayList<ConceptAttributes>(
				1);
		returnList.add(getConceptAttributes());
		return returnList;
	}

	public int getConceptId() {
		return nid;
	}

	public I_DescriptionTuple getDescTuple(I_IntList typePrefOrder,
			I_IntList langPrefOrder, I_IntSet allowedStatus,
			PositionSetReadOnly positionSet, LANGUAGE_SORT_PREF sortPref)
			throws IOException {
		I_IntSet typeSet = new IntSet();
		for (int nid : typePrefOrder.getListArray()) {
			typeSet.add(nid);
		}
		switch (sortPref) {
		case LANG_B4_TYPE:
			return getLangPreferredDesc(getDescriptionTuples(allowedStatus,
					typeSet, positionSet, true), typePrefOrder, langPrefOrder,
					allowedStatus, positionSet, typeSet);
		case TYPE_B4_LANG:
			return getTypePreferredDesc(getDescriptionTuples(allowedStatus,
					typeSet, positionSet, true), typePrefOrder, langPrefOrder,
					allowedStatus, positionSet, typeSet);
		default:
			throw new IOException("Can't handle sort type: " + sortPref);
		}
	}

	private I_DescriptionTuple getLangPreferredDesc(
			Collection<I_DescriptionTuple> descriptions,
			I_IntList typePrefOrder, I_IntList langPrefOrder,
			I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			I_IntSet typeSet) throws IOException, ToIoException {
		if (descriptions.size() > 0) {
			if (descriptions.size() > 1) {
				List<I_DescriptionTuple> matchedList = new ArrayList<I_DescriptionTuple>();
				if (langPrefOrder != null
						&& langPrefOrder.getListValues() != null) {
					for (int langId : langPrefOrder.getListValues()) {
						for (I_DescriptionTuple d : descriptions) {
							try {
								int tupleLangId = ArchitectonicAuxiliary
										.getLanguageConcept(d.getLang())
										.localize().getNid();
								if (tupleLangId == langId) {
									matchedList.add(d);
									if (matchedList.size() == 2) {
										break;
									}
								}
							} catch (TerminologyException e) {
								throw new ToIoException(e);
							}
						}
						if (matchedList.size() > 0) {
							if (matchedList.size() == 1) {
								return matchedList.get(0);
							}
							return getTypePreferredDesc(matchedList,
									typePrefOrder, langPrefOrder,
									allowedStatus, positionSet, typeSet);
						}
					}
				}
				return descriptions.iterator().next();
			} else {
				return descriptions.iterator().next();
			}
		}
		return null;
	}

	private I_DescriptionTuple getTypePreferredDesc(
			Collection<I_DescriptionTuple> descriptions,
			I_IntList typePrefOrder, I_IntList langPrefOrder,
			I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			I_IntSet typeSet) throws IOException, ToIoException {
		if (descriptions.size() > 0) {
			if (descriptions.size() > 1) {
				List<I_DescriptionTuple> matchedList = new ArrayList<I_DescriptionTuple>();
				for (int typeId : typePrefOrder.getListValues()) {
					for (I_DescriptionTuple d : descriptions) {
						if (d.getTypeId() == typeId) {
							matchedList.add(d);
							if (matchedList.size() == 2) {
								break;
							}
						}
					}
					if (matchedList.size() > 0) {
						if (matchedList.size() == 1) {
							return matchedList.get(0);
						}
						return getLangPreferredDesc(matchedList, typePrefOrder,
								langPrefOrder, allowedStatus, positionSet,
								typeSet);
					}
				}
				return descriptions.iterator().next();
			} else {
				return descriptions.iterator().next();
			}
		}
		return null;
	}

	@Override
	public Description.Version getDescTuple(I_IntList descTypePreferenceList,
			I_ConfigAceFrame config) throws IOException {
		return (Version) getDescTuple(descTypePreferenceList, config
				.getLanguagePreferenceList(), config.getAllowedStatus(), config
				.getViewPositionSetReadOnly(), config.getLanguageSortPref());
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions) throws IOException {
		return getDescriptionTuples(allowedStatus, allowedTypes, positions,
				true);
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions,
			boolean returnConflictResolvedLatestState) throws IOException {
		List<I_DescriptionTuple> returnDescriptions = new ArrayList<I_DescriptionTuple>();
		for (Description desc : getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positions,
					returnDescriptions, returnConflictResolvedLatestState);
		}
		return returnDescriptions;
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getDescriptionTuples(config.getAllowedStatus(), config
				.getDescTypes(), config.getViewPositionSetReadOnly(),
				returnConflictResolvedLatestState);
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getDestRelOrigins(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(), addUncommitted,
				returnConflictResolvedLatestState);
	}

	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getSourceRelTuples(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(), addUncommitted,
				returnConflictResolvedLatestState);
	}

	public List<Relationship> getDestRels() throws IOException {
		if (isCanceled()) {
			return new ArrayList<Relationship>();
		}
		return data.getDestRels();
	}

	public List<RefsetMember<?, ?>> getExtensions() throws IOException {
		if (isCanceled()) {
			return new ArrayList<RefsetMember<?,?>>();
		}

		return data.getRefsetMembers();
	}

	public List<I_ImageTuple> getImageTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getImageTuples(config.getAllowedStatus(), null, config
				.getViewPositionSetReadOnly(),
				returnConflictResolvedLatestState);
	}

	public List<Image> getImages() throws IOException {
		return data.getImages();
	}

	public Image getImage(int nid) throws IOException {
		if (isCanceled()) {
			return null;
		}
		for (Image i: data.getImages()) {
			if (i.getNid() == nid) {
				return i;
			}
		}
		return null;
	}

	public String getInitialText() throws IOException {
		if (isCanceled()) {
			return "canceled";
		}

		try {
			if ((AceConfig.config != null)
					&& (AceConfig.config.aceFrames.get(0) != null)) {
				PositionMapper mapper = 
					Bdb.getSapDb().getMapper(
							AceConfig.config.aceFrames.get(0).
								getViewPositionSet().iterator().next());
				if (mapper.isSetup()) {
					I_DescriptionTuple tuple = this.getDescTuple(
							AceConfig.config.aceFrames.get(0)
									.getShortLabelDescPreferenceList(),
							AceConfig.config.getAceFrames().get(0));
					if (tuple != null) {
						return tuple.getText();
					}
				}
			}
			return getText();
		} catch (IndexOutOfBoundsException e) {
			try {
				return getText();
			} catch (IndexOutOfBoundsException e2) {
				return nid + " has no desc";
			}
		}
	}

	private String getText() {
		try {
			if (getDescriptions().size() > 0) {
				return getDescriptions().get(0).getFirstTuple().getText();
			}
		} catch (IOException ex) {
			AceLog.getAppLog().nonModalAlertAndLogException(ex);
		}

		List<I_DescriptionVersioned> localDesc = getUncommittedDescriptions();
		if (localDesc.size() == 0) {
			try {
				if (fsDescNid == Integer.MIN_VALUE) {
					fsDescNid = Terms
							.get()
							.uuidToNative(
									ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE
											.getUids());
					fsDescNid = Terms
							.get()
							.uuidToNative(
									ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
											.getUids());
				}
				if (getDescriptions().size() > 0) {
					I_DescriptionVersioned desc = getDescriptions().get(0);
					for (I_DescriptionVersioned d : getDescriptions()) {
						for (I_DescriptionPart part : d.getMutableParts()) {
							if ((part.getTypeId() == fsDescNid)
									|| (part.getTypeId() == fsXmlDescNid)) {
								return part.getText();
							}
						}
					}
					return desc.getMutableParts().get(0).getText();
				} else {
					int sequence = nid + Integer.MIN_VALUE;
					String errString = nid + " (" + sequence + ") "
							+ " has no descriptions " + getUids();
					getDescriptions();
					return errString;
				}

			} catch (Exception ex) {
				AceLog.getAppLog().nonModalAlertAndLogException(ex);
			}
		}
		I_DescriptionVersioned tdv = localDesc.get(0);
		List<? extends I_DescriptionPart> versions = tdv.getMutableParts();
		I_DescriptionPart first = versions.get(0);
		return first.getText();
	}

	@Deprecated
	public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config)
			throws IOException {
		I_IntSet isATypes = config.getDestRelTypes();
		I_RepresentIdSet possibleKindOfConcepts = Bdb.getConceptDb().getEmptyIdSet();
		Iterator<? extends Integer> relNidTypeNidItr = this.getData()
				.getDestRelNidTypeNidList().iterator();
		while (relNidTypeNidItr.hasNext()) {
			int relNid = relNidTypeNidItr.next();
			int typeNid = relNidTypeNidItr.next();
			if (isATypes.contains(typeNid)) {
				possibleKindOfConcepts.setMember(Bdb.getNidCNidMap().getCNid(relNid));
				Concept origin = Bdb.getConceptForComponent(relNid);
				origin.addPossibleKindOfConcepts(possibleKindOfConcepts, isATypes);
			}
		}
		return possibleKindOfConcepts;
	}

	private void addPossibleKindOfConcepts(I_RepresentIdSet possibleKindOfConcepts, I_IntSet isATypes)
			throws IOException {
		Iterator<? extends Integer> relNidTypeNidItr = this.getData()
				.getDestRelNidTypeNidList().iterator();
		while (relNidTypeNidItr.hasNext()) {
			int relNid = relNidTypeNidItr.next();
			int typeNid = relNidTypeNidItr.next();
			if (isATypes.contains(typeNid)) {
				int destNid = Bdb.getNidCNidMap().getCNid(relNid);
				if (!possibleKindOfConcepts.isMember(destNid)) {
					possibleKindOfConcepts.setMember(Bdb.getNidCNidMap().getCNid(relNid));
					Concept origin = Bdb.getConceptForComponent(relNid);
					origin.addPossibleKindOfConcepts(possibleKindOfConcepts, isATypes);
				}
			}
		}
	}

	public Set<Integer> getPossibleDestRelsOfTypes(I_IntSet relTypes)
			throws IOException {
		Set<Integer> possibleRelNids = new HashSet<Integer>();
		List<? extends Integer> relNidTypeNidList = data.getDestRelNidTypeNidList();
		Iterator<? extends Integer> relNidTypeNidItr = relNidTypeNidList.iterator();
		while (relNidTypeNidItr.hasNext()) {
			int relNid = relNidTypeNidItr.next();
			int typeNid = relNidTypeNidItr.next();
			if (relTypes.contains(typeNid)) {
				possibleRelNids.add(relNid);
			}
		}
		return possibleRelNids;
	}

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getSourceRelTargets(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(), addUncommitted,
				returnConflictResolvedLatestState);
	}

	public List<? extends I_RelTuple> getSourceRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		throw new UnsupportedOperationException(
				"Use a method that does not require getting the 'active' config");
	}

	public ConceptAttributes getUncommittedConceptAttributes() {
		return null;
	}

	public List<I_DescriptionVersioned> getUncommittedDescriptions() {
		return Collections.unmodifiableList(new ArrayList<I_DescriptionVersioned>());
	}

	public List<I_Identify> getUncommittedIdVersioned() {
		return Collections.unmodifiableList(new ArrayList<I_Identify>());
	}

	public I_IntSet getUncommittedIds() {
		return new IntSet();
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		return Collections.unmodifiableList(new ArrayList<I_ImageVersioned>());
	}

	public List<I_RelVersioned> getUncommittedSourceRels() {
		return Collections.unmodifiableList(new ArrayList<I_RelVersioned>());
	}

	public UniversalAceBean getUniversalAceBean() throws IOException,
			TerminologyException {
		UniversalAceBean uab = new UniversalAceBean();

		uab.setIdentifier(getIdentifier().getUniversalId());

		uab.setConceptAttributes(getConceptAttributes().getUniversal());

		for (I_DescriptionVersioned desc : getDescriptions()) {
			uab.getDescriptions().add(desc.getUniversal());
		}

		for (I_RelVersioned rel : getSourceRels()) {
			uab.getSourceRels().add(rel.getUniversal());
		}

		for (I_ImageVersioned image : getImages()) {
			uab.getImages().add(image.getUniversal());
		}
		return uab;
	}

	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted)
			throws IOException {
		I_IntSet destRelTypes = aceConfig.getDestRelTypes();
		List<? extends Integer> relNidTypeNid = data.getDestRelNidTypeNidList();
		IntList possibleChildRels = new ArrayIntList();
		int i = 0;
		while (i < relNidTypeNid.size()) {
			int relNid = relNidTypeNid.get(i++);
			int typeNid = relNidTypeNid.get(i++);
			if (destRelTypes.contains(typeNid)) {
				possibleChildRels.add(relNid);
			}
		}
		if (possibleChildRels.size() == 0
				&& aceConfig.getSourceRelTypes().getSetValues().length == 0) {
			return true;
		}
		IntIterator relNids = possibleChildRels.iterator();
		while (relNids.hasNext()) {
			int relNid = relNids.next();
			Relationship r = Bdb.getConceptForComponent(relNid).getSourceRel(
					relNid);
			if (r != null) {
				List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
				r.addTuples(aceConfig.getAllowedStatus(), destRelTypes, aceConfig
						.getViewPositionSetReadOnly(), currentVersions, true);
				if (currentVersions.size() > 0) {
					return false;
				}
			}
		}

		I_IntSet srcRelTypes = aceConfig.getSourceRelTypes();
		for (Relationship r : getSourceRels()) {
			List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
			r.addTuples(aceConfig.getAllowedStatus(), srcRelTypes, aceConfig
					.getViewPositionSetReadOnly(), currentVersions, true);
			if (currentVersions.size() > 0) {
				return false;
			}
		}
		return true;
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		boolean promotedAnything = false;

		if (getConceptAttributes().promote(viewPosition, pomotionPaths,
				allowedStatus)) {
			promotedAnything = true;
		}

		for (I_DescriptionVersioned dv : getDescriptions()) {
			if (dv.promote(viewPosition, pomotionPaths, allowedStatus)) {
				promotedAnything = true;
			}
		}

		for (I_RelVersioned rv : getSourceRels()) {
			if (rv.promote(viewPosition, pomotionPaths, allowedStatus)) {
				promotedAnything = true;
			}
		}

		for (I_ImageVersioned img : getImages()) {
			if (img.promote(viewPosition, pomotionPaths, allowedStatus)) {
				promotedAnything = true;
			}
		}
		return promotedAnything;
	}

	public Relationship getRelationship(int relNid) throws IOException {
		for (Relationship r : getNativeSourceRels()) {
			if (r.getNid() == relNid) {
				return r;
			}
		}
		return null;
	}

	@Override
	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		Set<Concept> returnValues = new HashSet<Concept>();
		for (I_RelTuple rel : getDestRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted)) {
			returnValues.add(Bdb.getConceptDb().getConcept(rel.getC1Id()));
		}
		return returnValues;
	}

	@Override
	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		Set<Concept> returnValues = new HashSet<Concept>();
		for (I_RelTuple rel : getDestRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted)) {
			returnValues.add(Bdb.getConceptDb().getConcept(rel.getC1Id()));
		}
		return returnValues;
	}

	@Override
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		return getDestRelTuples(allowedStatus, allowedTypes, positions,
				addUncommitted, false);
	}

	@Override
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException {
		try {
			List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
			SetModifiedWhenChangedList relNidTypeNidlist = data.getDestRelNidTypeNidList();
			List<NidPair> invalidPairs = new ArrayList<NidPair>();
			int i = 0;
			while (i < relNidTypeNidlist.size()) {
				int relNid = relNidTypeNidlist.get(i++);
				int typeNid = relNidTypeNidlist.get(i++);
				if (allowedTypes.contains(typeNid)) {
					Concept relSource = Bdb.getConceptForComponent(relNid);
					Relationship r = relSource.getRelationship(relNid);
					if (r != null) {
						r.addTuples(allowedStatus, allowedTypes, positions,
								returnRels, addUncommitted,
								returnConflictResolvedLatestState);
					} else {
						invalidPairs.add(new NidPair(relNid, typeNid));
					}
				}
			}
			
			if (invalidPairs.size() > 0) {
				synchronized (relNidTypeNidlist) {
					for (NidPair pair: invalidPairs) {
						int index = relNidTypeNidlist.indexOf(pair.getNid1());
						if (index >= 0) {
							relNidTypeNidlist.forget(index, index + 1);
							Terms.get().addUncommittedNoChecks(this);
						}
					}
				}
			}
			return returnRels;
		} catch (TerminologyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions)
			throws IOException {
		List<I_ImageTuple> returnTuples = new ArrayList<I_ImageTuple>();
		for (I_ImageVersioned img : getImages()) {
			img.addTuples(allowedStatus, allowedTypes, positions, returnTuples);
		}
		return returnTuples;
	}

	@Override
	public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {

		List<I_ImageTuple> returnTuples = getImageTuples(allowedStatus,
				allowedTypes, positions);
		return Terms.get().getActiveAceFrameConfig()
				.getConflictResolutionStrategy().resolveTuples(returnTuples);
	}

	@Override
	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
		for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted)) {
			returnValues.add(Concept.get(rel.getC2Id()));
		}
		return returnValues;
	}

	@Override
	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
		for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted, returnConflictResolvedLatestState)) {
			returnValues.add(Concept.get(rel.getC2Id()));
		}
		return returnValues;
	}

	@Override
	public List<? extends I_RelTuple> getSourceRelTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (Relationship rel : getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, returnRels,
					addUncommitted);
		}
		return returnRels;
	}

	@Override
	public List<? extends I_RelTuple> getSourceRelTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, returnRels,
					addUncommitted, returnConflictResolvedLatestState);
		}
		return returnRels;
	}

	@Override
	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		for (I_Position p : positions) {
			KindOfSpec kindOfSpec = new KindOfSpec(p, allowedStatus,
					allowedTypes, getNid());
			if (KindOfComputer.isKindOf((Concept) child, kindOfSpec)) {
				return true;
			}
		}
		return false;
	}

	public boolean isParentOf(I_GetConceptData child, boolean addUncommitted)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		return isParentOf(child, config.getAllowedStatus(), config
				.getDestRelTypes(), config.getViewPositionSetReadOnly(), true);
	}

	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			boolean addUncommitted) throws IOException, TerminologyException {
		if (child == this) {
			return true;
		}
		return isParentOf(child, addUncommitted);
	}

	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		if (child == this) {
			return true;
		}
		return isParentOf(child, allowedStatus, allowedTypes, positions, true);
	}

	@Override
	public Object getDenotation(int authorityNid) throws IOException,
			TerminologyException {
		for (I_IdVersion part : getIdentifier().getIdVersions()) {
			if (part.getAuthorityNid() == authorityNid) {
				return part.getDenotation();
			}
		}
		return null;
	}

	@Override
	public I_Identify getIdentifier() throws IOException {
		return getConceptAttributes();
	}

	public I_ManageConceptData getData() {
		return data;
	}

	public Collection<Integer> getAllNids() throws IOException {
		return data.getAllNids();
	}

	/**
	 * Test method to check to see if two concepts are equal in all respects.
	 * 
	 * @param another
	 * @return either a zero length String, or a String containing a description
	 *         of the validation failures.
	 * @throws IOException
	 */
	public String validate(Concept another) throws IOException {
		assert another != null;
		StringBuffer buf = new StringBuffer();
		String validationResult = null;

		// Compare nids
		if (this.nid != another.nid) {
			buf.append("\tConcept.nid not equal: \n" + "\t\tthis.nid = "
					+ this.nid + "\n" + "\t\tanother.nid = " + another.nid
					+ "\n");
		}

		// Compare Attributes
		ConceptAttributes attributes = getConceptAttributes();
		assert attributes != null : "validating: " + nid;
		ConceptAttributes anotherAttributes = another.getConceptAttributes();
		assert anotherAttributes != null : "validating: " + nid;
		validationResult = attributes.validate(anotherAttributes);
		if (validationResult.length() != 0) {
			buf.append(validationResult);
		}

		// Compare Descriptions
		List<Description> descriptionList = this.getDescriptions();
		assert descriptionList != null : "validating: " + nid;
		List<Description> anotherDescriptionList = another.getDescriptions();
		assert anotherDescriptionList != null : "validating: " + nid;
		for (int i = 0; i < descriptionList.size(); i++) {
			// make sure there are elements in both arrays to compare
			if (anotherDescriptionList.size() > i) {
				Description thisDescription = descriptionList.get(i);
				Description anotherDescription = anotherDescriptionList.get(i);
				validationResult = thisDescription.validate(anotherDescription);
				if (validationResult.length() != 0) {
					buf.append("\tConcept.Descriptions[" + i
							+ "] not equal: \n");
					buf.append(validationResult);
				}
			} else {
				buf.append("\tConcept.Descriptions[" + i + "] not equal: \n");
				buf
						.append("\t\tThere is no corresponding Description in another to compare it to.\n");
			}
		}

		// Compare Relationships
		List<Relationship> relationshipList = this.getSourceRels();
		assert relationshipList != null : "validating: " + nid;
		List<Relationship> anotherRelationshipList = another.getSourceRels();
		assert anotherRelationshipList != null : "validating: " + nid;
		for (int i = 0; i < relationshipList.size(); i++) {
			// make sure there are elements in both arrays to compare
			if (anotherRelationshipList.size() > i) {
				Relationship thisRelationship = relationshipList.get(i);
				Relationship anotherRelationship = anotherRelationshipList
						.get(i);
				validationResult = thisRelationship
						.validate(anotherRelationship);
				if (validationResult.length() != 0) {
					buf.append("\tConcept.Relationships[" + i
							+ "] not equal: \n");
					buf.append(validationResult);
				}
			} else {
				buf.append("\tConcept.Relationships[" + i + "] not equal: \n");
				buf
						.append("\t\tThere is no corresponding Relationship in another to compare it to.\n");
			}
		}

		// Compare images
		List<Image> imagesList = this.getImages();
		assert imagesList != null : "validating: " + nid;
		List<Image> anotherImagesList = another.getImages();
		assert anotherImagesList != null : "validating: " + nid;
		for (int i = 0; i < imagesList.size(); i++) {
			// make sure there are elements in both arrays to compare
			if (anotherImagesList.size() > i) {
				Image thisImage = imagesList.get(i);
				Image anotherImage = anotherImagesList.get(i);
				validationResult = thisImage.validate(anotherImage);
				if (validationResult.length() != 0) {
					buf.append("\tConcept.Images[" + i + "] not equal: \n");
					buf.append(validationResult);
				}
			} else {
				buf.append("\tConcept.Images[" + i + "] not equal: \n");
				buf
						.append("\t\tThere is no corresponding Image in another to compare it to.\n");
			}
		}

		// Compare Refset Members
		List<RefsetMember<?, ?>> refsetMembersList = this.getExtensions();
		assert refsetMembersList != null : "validating: " + nid;
		List<RefsetMember<?, ?>> anotherRefsetMembersList = another
				.getExtensions();
		assert anotherRefsetMembersList != null : "validating: " + nid;
		for (int i = 0; i < refsetMembersList.size(); i++) {
			// make sure there are elements in both arrays to compare
			if (anotherRefsetMembersList.size() > i) {
				RefsetMember<?, ?> thisRefsetMember = refsetMembersList.get(i);
				RefsetMember<?, ?> anotherRefsetMember = anotherRefsetMembersList
						.get(i);
				validationResult = thisRefsetMember
						.validate(anotherRefsetMember);
				if (validationResult.length() != 0) {
					buf.append("\tConcept.RefsetMember[" + i
							+ "] not equal: \n");
					buf.append(validationResult);
				}
			} else {
				buf.append("\tConcept.RefsetMember[" + i + "] not equal: \n");
				buf
						.append("\t\tThere is no corresponding RefsetMember in another to compare it to.\n");
			}
		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			if (!isCanceled()) {
				return getInitialText();
			}
			return "canceled concept";
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
			return ex.toString();
		}
	}

	/**
	 * Returns a longer - more complete - string representation of the object.
	 * 
	 * @return
	 */
	public String toLongString() {
		StringBuffer buff = new StringBuffer();
		try {
			buff.append("\nConcept: \"");
			buff.append(getInitialText());
			buff.append("\" nid: ");
			buff.append(nid);
			buff.append("\n  data version: ");
			buff.append(getDataVersion());
			buff.append("\n write version: ");
			buff.append(getWriteVersion());
			buff.append("\n uncommitted: ");
			buff.append(isUncommitted());
			buff.append("\n unwritten: ");
			buff.append(isUnwritten());
			buff.append("\n attributes: ");
			buff.append(getConceptAttributes());
			buff.append("\n descriptions: ");
			formatList(buff, getDescriptions());
			buff.append("\n srcRels: ");
			formatList(buff, getSourceRels());
			buff.append("\n images: ");
			formatList(buff, getImages());
			buff.append("\n refset members: ");
			formatList(buff, getExtensions());
			buff.append("\n destRel/type: ");
			doubleNidFormatter(buff, data.getDestRelNidTypeNidList());
			buff.append("\n refset/member for concept: ");
			refsetMemberNidFormatter(buff, data
					.getRefsetNidMemberNidForConceptList());
			buff.append("\n refset/member for desc: ");
			refsetMemberNidFormatter(buff, data
					.getRefsetNidMemberNidForDescriptionsList());
			buff.append("\n refset/member for rels: ");
			refsetMemberNidFormatter(buff, data
					.getRefsetNidMemberNidForRelsList());
			buff.append("\n refset/member for image: ");
			refsetMemberNidFormatter(buff, data
					.getRefsetNidMemberNidForImagesList());
			buff.append("\n refset/member for members: ");
			refsetMemberNidFormatter(buff, data
					.getRefsetNidMemberNidForRefsetMembersList());
			buff.append("\n desc nids: ");
			buff.append(data.getDescNids());
			buff.append("\n src rel nids: ");
			buff.append(data.getSrcRelNids());
			buff.append("\n member nids: ");
			buff.append(data.getMemberNids());
			buff.append("\n image nids: ");
			buff.append(data.getImageNids());
			buff.append("\n");
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return buff.toString();
	}

	private long getWriteVersion() {
		return data.getLastWrite();
	}

	private void refsetMemberNidFormatter(StringBuffer buff,
			List<? extends Integer> refsetNidMemberNidList) throws IOException {
		if (refsetNidMemberNidList.size() == 0) {
			buff.append("[]");
		} else {
			Iterator<? extends Integer> refsetMemberItr = refsetNidMemberNidList
					.iterator();
			buff.append("[\n");
			while (refsetMemberItr.hasNext()) {
				int refsetNid = refsetMemberItr.next();
				int memberNid = refsetMemberItr.next();
				Concept refsetConcept = Bdb.getConceptForComponent(refsetNid);
				buff.append("     ");
				buff.append(refsetNid);
				buff.append(": ");
				buff.append(refsetConcept);
				buff.append(" ");
				buff.append(memberNid);
				buff.append("\n");
			}
			buff.append("]");
		}
	}

	private void doubleNidFormatter(StringBuffer buff,
			List<? extends Integer> doubleNidList) throws IOException {
		if (doubleNidList.size() == 0) {
			buff.append("[]");
		} else {
			Iterator<? extends Integer> doubleNidItr = doubleNidList.iterator();
			buff.append("[\n");
			while (doubleNidItr.hasNext()) {
				int relNid = doubleNidItr.next();
				int typeNid = doubleNidItr.next();
				Concept relConcept = Bdb.getConceptForComponent(relNid);
				Concept typeConcept = Bdb.getConceptForComponent(typeNid);
				buff.append("     ");
				buff.append(relNid);
				buff.append(": ");
				buff.append(relConcept);
				buff.append(" ");
				buff.append(typeNid);
				buff.append(": ");
				buff.append(typeConcept);
				buff.append("\n");
			}
			buff.append("]");
		}
	}

	private void formatList(StringBuffer buff, List<?> list) {
		if (list != null && list.size() > 0) {
			buff.append("[\n");
			for (Object obj : list) {
				buff.append("   ");
				buff.append(obj);
				buff.append(",\n");
			}
			buff.append("]");
		} else {
			buff.append("[]");
		}
	}

	public List<RefsetMember<?, ?>> getConceptExtensions() throws IOException {
		List<RefsetMember<?, ?>> returnValues = new ArrayList<RefsetMember<?, ?>>();
		Iterator<? extends Integer> itr = data.getRefsetNidMemberNidForConceptList()
				.iterator();
		while (itr.hasNext()) {
			int refsetNid = itr.next();
			int memberNid = itr.next();
			Concept c = Bdb.getConceptDb().getConcept(refsetNid);
			RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
			returnValues.add(member);
		}
		return returnValues;
	}

	public List<RefsetMember<?, ?>> getConceptExtensions(int specifiedRefsetNid)
			throws IOException {
		List<RefsetMember<?, ?>> returnValues = new ArrayList<RefsetMember<?, ?>>();
		Iterator<? extends Integer> itr = data.getRefsetNidMemberNidForConceptList()
				.iterator();
		while (itr.hasNext()) {
			int refsetNid = itr.next();
			int memberNid = itr.next();
			if (specifiedRefsetNid == refsetNid) {
				Concept c = Concept.get(refsetNid);
				RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
				try {
					assert member != null : " null member retrieving "
							+ memberNid + " from: " + c.toLongString()
							+ " references: "
							+ Bdb.getConceptDb().findReferences(memberNid);
				} catch (Exception e) {
					throw new IOException(e);
				}
				returnValues.add(member);
			}
		}
		return returnValues;
	}

	public ConceptComponent<?, ?> getComponent(int nid) throws IOException {
		return data.getComponent(nid);
	}

	public List<RefsetMember<?, ?>> getExtensionsForComponent(int nid)
			throws IOException {
		return data.getExtensionsForComponent(nid);
	}

	public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
		return data.getRefsetMember(memberNid);
	}

	public Relationship getDestRel(int relNid) throws IOException {
		return Bdb.getConceptForComponent(relNid).getRelationship(relNid);
	}

	public Relationship getSourceRel(int relNid) throws IOException {
		return getRelationship(relNid);
	}

	public boolean isUncommitted() {
		return data.isUncommitted();
	}

	public boolean isUnwritten() {
		return data.isUnwritten();
	}

	public long getDataVersion() {
		return data.getLastChange();
	}

	/**
	 * This method is for creating temporary concepts for unit testing only...
	 * 
	 * @param eConcept
	 * @return
	 * @throws IOException
	 */
	public static Concept getTempConcept(EConcept eConcept) throws IOException {
		int conceptNid = Bdb.uuidToNid(eConcept.getConceptAttributes()
				.getPrimordialComponentUuid());
		assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";
		return populateFromEConcept(eConcept, new Concept(conceptNid));
	}

	public void setConceptAttributes(ConceptAttributes attributes)
			throws IOException {
		data.set(attributes);
	}

	public Description getDescription(int nid) throws IOException {
		if (isCanceled()) {
			return null;
		}
		for (Description d : getDescriptions()) {
			if (d.getNid() == nid) {
				return d;
			}
		}
		throw new IOException("No description: " + nid + " found in\n"
				+ toLongString());
	}

	public void modified() {
		data.modified();
	}

	public void setLastWrite(long version) {
		data.setLastWrite(version);
	}
	
    public List<Integer> getConceptMemberNidsForRefset(I_IntSet refsetNidToMatch) 
    		throws IOException {
    	return processMemberNidsForRefset(refsetNidToMatch,
    			data.getRefsetNidMemberNidForConceptList());
    }

	private List<Integer> processMemberNidsForRefset(I_IntSet refsetNids,
			List<? extends Integer> refsetNidMemberNidList) {
		Iterator<? extends Integer> refsetNidMemberNidIterator = 
			refsetNidMemberNidList.iterator();
    	List<Integer> memberNids = new ArrayList<Integer>();
    	while (refsetNidMemberNidIterator.hasNext()) {
    		int refsetNid = refsetNidMemberNidIterator.next();
    		int memberNid = refsetNidMemberNidIterator.next();
    		if (refsetNids.contains(refsetNid)) {
    			memberNids.add(memberNid);
    		}
    	}
    	return memberNids;
	}
    public List<Integer> getDescriptionMemberNidsForRefset(I_IntSet refsetNids) 
    		throws IOException {
    	return processMemberNidsForRefset(refsetNids,
    			data.getRefsetNidMemberNidForDescriptionsList());
    }
    public List<Integer> getSrcRelMemberNidsForRefset(I_IntSet refsetNids) 
    		throws IOException {
    	return processMemberNidsForRefset(refsetNids,
    			data.getRefsetNidMemberNidForRelsList());
    }
    public List<Integer> getImageMemberNidsForRefset(I_IntSet refsetNids) 
    		throws IOException {
    	return processMemberNidsForRefset(refsetNids,
    			data.getRefsetNidMemberNidForImagesList());
    }
    public List<Integer> getRefsetMemberNidsForRefset(I_IntSet refsetNids) 
    		throws IOException {
    	return processMemberNidsForRefset(refsetNids,
    			data.getRefsetNidMemberNidForRefsetMembersList());
    }

	public ComponentList<RefsetMember<?, ?>> getRefsetMembers()
			throws IOException {
		return data.getRefsetMembers();
	}

	public boolean isCanceled() throws IOException {
		if (getConceptAttributes() == null || 
				getConceptAttributes().getTime() == Long.MIN_VALUE) {
			return true;
		}
		return false;
	}

	
	protected void finalize() throws Throwable {
        if (isUnwritten()) {
            try {
            	synchronized (concepts) {
            		if (isUnwritten()) {
                    	concepts.remove(nid);
                        BdbCommitManager.writeImmediate(this);
            		}
				}
            }
            finally {
                super.finalize();
            }
        }
    }
	
	public static void flush() throws Exception {
    	synchronized (concepts) {
    		for (Reference<Concept> cRef: concepts.values()) {
    			Concept c = cRef.get();
    			if (c != null && c.isUnwritten()) {
    	          BdbCommitManager.writeImmediate(c);
    			}
    		}
		}
	}


	  public final Set<I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config)
	      throws IOException {
	    return ConflictHelper.getCommonDescTuples(this, config);
	  }

	  public final Set<I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
	      throws IOException {
	    return ConflictHelper.getCommonRelTuples(this, config);
	  }

	  public final Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(
	      I_ConfigAceFrame config) throws IOException {
	    return ConflictHelper.getCommonConceptAttributeTuples(this,
	        config);
	  }
}
