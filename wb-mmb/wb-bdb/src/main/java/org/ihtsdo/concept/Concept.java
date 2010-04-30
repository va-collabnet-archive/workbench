package org.ihtsdo.concept;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import jsr166y.ConcurrentReferenceHashMap;

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
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PRECEDENCE;
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
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.ReferenceType;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.lucene.LuceneManager;

public class Concept implements I_Transact, I_GetConceptData {

	public static ReferenceType refType = ReferenceType.WEAK;
	
    public static ConcurrentReferenceHashMap<Integer, Concept> conceptsCRHM = 
        new ConcurrentReferenceHashMap<Integer, Concept>(
                ConcurrentReferenceHashMap.ReferenceType.STRONG, 
                ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static ConcurrentReferenceHashMap<Integer, Object> componentsCRHM = 
        new ConcurrentReferenceHashMap<Integer, Object>(
                ConcurrentReferenceHashMap.ReferenceType.STRONG, 
                ConcurrentReferenceHashMap.ReferenceType.WEAK);

    
	public static Concept mergeAndWrite(EConcept eConcept) throws IOException {
		int conceptNid = Bdb.uuidToNid(eConcept.getPrimordialUuid());
		assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";
		Concept c = get(conceptNid);
		mergeWithEConcept(eConcept, c);
		BdbCommitManager.addUncommittedNoChecks(c);
		return c;
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
					if (currentDNids.contains(dNid) && d != null) {
						d.merge(new Description(ed, c));
					} else {
						c.getDescriptions().add(new Description(ed, c));
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
					if (currentSrcRelNids.contains(rNid) && r != null) {
						r.merge(new Relationship(er, c));
					} else {
						c.getSourceRels().add(new Relationship(er, c));
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
				for (EImage eImg: eConcept.getImages()) {
					int iNid = Bdb.uuidToNid(eImg.primordialUuid);
					Image img = c.getImage(iNid);
					if (currentImageNids.contains(iNid) && img != null) {
						img.merge(new Image(eImg, c));
					} else {
						c.getImages().add(new Image(eImg, c));
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
					if (currentMemberNids.contains(rNid) && r != null) {
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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
				ArrayList<NidPair> nidList = mergeNidLists(c, 
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

	private static ArrayList<NidPair> mergeNidLists(Concept c,
			List<UUID> uuidPairList, List<NidPair> nidPairList)
			throws IOException {
		HashSet<NidPair> pairSet = new HashSet<NidPair>(
				c.getData().getRefsetNidMemberNidForRefsetMembersList().size());
		for (NidPair pair: nidPairList) {
			pairSet.add(pair);
		}

		Iterator<UUID> uuidIterator = uuidPairList.iterator();
		while (uuidIterator.hasNext()) {
			pairSet.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
					Bdb.uuidToNid(uuidIterator.next())));
		}
		return new ArrayList<NidPair>(pairSet);
	}

	private static void getRefsetNidMemberNidForRefsetMembers(
			EConcept eConcept, Concept c) throws IOException {
		ArrayList<NidPair> refsetNidMemberNidForRefsetMembersList = new ArrayList<NidPair>(
				eConcept.getRefsetUuidMemberUuidForRefsetMembers().size());
        Iterator<UUID> uuidIterator = eConcept.getRefsetUuidMemberUuidForRefsetMembers().iterator();
        while (uuidIterator.hasNext()) {
            refsetNidMemberNidForRefsetMembersList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
        }
		c.data.setRefsetNidMemberNidForRefsetMembersList(refsetNidMemberNidForRefsetMembersList);
	}

	private static void getRefsetNidMemberNidForImages(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<NidPair> refsetNidMemberNidForImagesList = new ArrayList<NidPair>(
				eConcept.getRefsetUuidMemberUuidForImages().size());
        Iterator<UUID> uuidIterator = eConcept.getRefsetUuidMemberUuidForImages().iterator();
        while (uuidIterator.hasNext()) {
            refsetNidMemberNidForImagesList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
        }
		c.data.setRefsetNidMemberNidForImagesList(refsetNidMemberNidForImagesList);
	}

	private static void getRefsetNidMemberNidForRels(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<NidPair> refsetNidMemberNidForRelsList = new ArrayList<NidPair>(
				eConcept.getRefsetUuidMemberUuidForRels().size());
        Iterator<UUID> uuidIterator = eConcept.getRefsetUuidMemberUuidForImages().iterator();
        while (uuidIterator.hasNext()) {
            refsetNidMemberNidForRelsList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
        }
		c.data.setRefsetNidMemberNidForRelsList(refsetNidMemberNidForRelsList);
	}

	private static void setRefsetNidMemberNidForDescriptions(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<NidPair> refsetNidMemberNidForDescriptionsList = new ArrayList<NidPair>(
				eConcept.getRefsetUuidMemberUuidForDescriptions().size());
        Iterator<UUID> uuidIterator = eConcept.getRefsetUuidMemberUuidForDescriptions().iterator();
        while (uuidIterator.hasNext()) {
            refsetNidMemberNidForDescriptionsList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
        }
		c.data.setRefsetNidMemberNidForDescriptionsList(refsetNidMemberNidForDescriptionsList);
	}

	private static void setRefsetNidMemberNidForConceptFromEConcept(
			EConcept eConcept, Concept c) throws IOException {
		ArrayList<NidPair> refsetNidMemberNidForConceptList = new ArrayList<NidPair>(
				eConcept.getRefsetUuidMemberUuidForConcept().size());
        Iterator<UUID> uuidIterator = eConcept.getRefsetUuidMemberUuidForConcept().iterator();
        while (uuidIterator.hasNext()) {
            refsetNidMemberNidForConceptList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
        }
		c.data.setRefsetNidMemberNidForConceptList(refsetNidMemberNidForConceptList);
	}

	private static void setDestRelNidTypeNidFromEConcept(EConcept eConcept,
			Concept c) throws IOException {
		ArrayList<NidPair> destRelOriginNidTypeNidList = new ArrayList<NidPair>(
				eConcept.getDestRelUuidTypeUuids().size());
        Iterator<UUID> uuidIterator = eConcept.getDestRelUuidTypeUuids().iterator();
        while (uuidIterator.hasNext()) {
            destRelOriginNidTypeNidList.add(new NidPair(Bdb.uuidToNid(uuidIterator.next()), 
                    Bdb.uuidToNid(uuidIterator.next())));
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
		Concept c = conceptsCRHM.get(nid);
		if (c == null) {
			Concept newC = new Concept(nid);
			c = conceptsCRHM.putIfAbsent(nid, newC);
			if (c == null) {
				c = newC;
			}
		}
		conceptsCRHM.put(nid, c);
		return c;
	}
	
	public static Concept getIfInMap(int nid) {
	    return conceptsCRHM.get(nid);
	}

	public static Concept get(int nid, byte[] roBytes, byte[] mutableBytes)
			throws IOException {
		assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
		Concept c = conceptsCRHM.get(nid);
		if (c == null) {
			Concept newC = new Concept(nid, roBytes, mutableBytes);
			c = conceptsCRHM.putIfAbsent(nid, newC);
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
	
	public void resetNidData() {
		data.resetNidData();
	}

	public int getNid() {
		return nid;
	}

	public Collection<Description> getDescriptions() throws IOException {
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

	public List<I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positionSet, 
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException, TerminologyException {
		List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
		ConceptAttributes attr = getConceptAttributes();
		if (attr != null) {
			attr.addTuples(allowedStatus, positionSet, returnTuples,
			    precedencePolicy, contradictionManager);
		}
		return returnTuples;
	}

	public List<I_ConceptAttributeTuple> getConceptAttributeTuples( 
        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException,
			TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getConceptAttributeTuples(config.getAllowedStatus(), config
				.getViewPositionSetReadOnly(), precedencePolicy, contradictionManager);
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
			PositionSetReadOnly positionSet, LANGUAGE_SORT_PREF sortPref, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException {
		I_IntSet typeSet = new IntSet();
		for (int nid : typePrefOrder.getListArray()) {
			typeSet.add(nid);
		}
		switch (sortPref) {
		case LANG_B4_TYPE:
			return getLangPreferredDesc(getDescriptionTuples(allowedStatus,
					typeSet, positionSet, precedencePolicy, contradictionManager), typePrefOrder, langPrefOrder,
					allowedStatus, positionSet, typeSet);
		case TYPE_B4_LANG:
			return getTypePreferredDesc(getDescriptionTuples(allowedStatus,
					typeSet, positionSet, precedencePolicy, contradictionManager), typePrefOrder, langPrefOrder,
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
				.getViewPositionSetReadOnly(), config.getLanguageSortPref(),
				config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException {
		List<I_DescriptionTuple> returnDescriptions = new ArrayList<I_DescriptionTuple>();
		for (Description desc : getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positions,
					returnDescriptions, precedencePolicy, contradictionManager);
		}
		return returnDescriptions;
	}

	public List<I_DescriptionTuple> getDescriptionTuples() throws IOException,
			TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getDescriptionTuples(config.getAllowedStatus(), config
				.getDescTypes(), config.getViewPositionSetReadOnly(), 
	            config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedTypes)
			throws IOException, TerminologyException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getDestRelOrigins(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(),
	            config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes, 
        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getSourceRelTuples(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(), 
				precedencePolicy, contradictionManager);
	}

	public Collection<Relationship> getDestRels() throws IOException {
		if (isCanceled()) {
			return new ArrayList<Relationship>();
		}
		return data.getDestRels();
	}

	public Collection<RefsetMember<?,?>> getExtensions() throws IOException {
		if (isCanceled()) {
			return new ArrayList<RefsetMember<?,?>>();
		}

		return data.getRefsetMembers();
	}

	public List<I_ImageTuple> getImageTuples() throws IOException,
			TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getImageTuples(config.getAllowedStatus(), null, config
				.getViewPositionSetReadOnly(), 
	            config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public Collection<Image> getImages() throws IOException {
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
				return getDescriptions().iterator().next().getFirstTuple().getText();
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
					I_DescriptionVersioned desc = getDescriptions().iterator().next();
					for (I_DescriptionVersioned d : getDescriptions()) {
						for (I_DescriptionPart part : d.getMutableParts()) {
							if ((part.getTypeId() == fsDescNid)
									|| (part.getTypeId() == fsXmlDescNid)) {
								return part.getText();
							}
						}
					}
					return desc.getFirstTuple().getText();
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

    public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException {
        I_IntSet isATypes = config.getDestRelTypes();
        I_RepresentIdSet possibleKindOfConcepts = Bdb.getConceptDb().getEmptyIdSet();
        possibleKindOfConcepts.setMember(getNid());
        for (NidPair pair : this.getData().getDestRelNidTypeNidList()) {
            int relNid = pair.getNid1();
            int typeNid = pair.getNid2();
            if (isATypes.contains(typeNid)) {
                possibleKindOfConcepts.setMember(Bdb.getNidCNidMap().getCNid(relNid));
                Concept origin = Bdb.getConceptForComponent(relNid);
                origin.addPossibleKindOfConcepts(possibleKindOfConcepts, isATypes);
            }
        }
        return possibleKindOfConcepts;
    }

    public I_RepresentIdSet getPossibleChildOfConcepts(I_ConfigAceFrame config) throws IOException {
        I_IntSet isATypes = config.getDestRelTypes();
        I_RepresentIdSet possibleChildOfConcepts = Bdb.getConceptDb().getEmptyIdSet();
        for (NidPair pair : this.getData().getDestRelNidTypeNidList()) {
            int relNid = pair.getNid1();
            int typeNid = pair.getNid2();
            if (isATypes.contains(typeNid)) {
                possibleChildOfConcepts.setMember(Bdb.getNidCNidMap().getCNid(relNid));
            }
        }
        return possibleChildOfConcepts;
    }

	private void addPossibleKindOfConcepts(I_RepresentIdSet possibleKindOfConcepts, I_IntSet isATypes)
			throws IOException {
        possibleKindOfConcepts.setMember(getNid());
        for (NidPair pair: this.getData().getDestRelNidTypeNidList()) {
            int relNid = pair.getNid1();
            int typeNid = pair.getNid2();
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
        for (NidPair pair: this.getData().getDestRelNidTypeNidList()) {
            int relNid = pair.getNid1();
            int typeNid = pair.getNid2();
			if (relTypes.contains(typeNid)) {
				possibleRelNids.add(relNid);
			}
		}
		return possibleRelNids;
	}

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes, 
        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		return getSourceRelTargets(config.getAllowedStatus(), allowedTypes,
				config.getViewPositionSetReadOnly(), precedencePolicy, contradictionManager);
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
		
        I_IntSet srcRelTypes = aceConfig.getSourceRelTypes();
        if (srcRelTypes.size() > 0) {
            for (Relationship r : getSourceRels()) {
                List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
                r.addTuples(aceConfig.getAllowedStatus(), srcRelTypes, aceConfig
                        .getViewPositionSetReadOnly(), currentVersions, 
                        aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                if (currentVersions.size() > 0) {
                    return false;
                }
            }
        }
	    return data.isLeafByDestRels(aceConfig);
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus, PRECEDENCE precedence)
			throws IOException, TerminologyException {
		boolean promotedAnything = false;

		if (getConceptAttributes().promote(viewPosition, pomotionPaths,
				allowedStatus, precedence)) {
			promotedAnything = true;
		}

		for (I_DescriptionVersioned dv : getDescriptions()) {
			if (dv.promote(viewPosition, pomotionPaths, allowedStatus, precedence)) {
				promotedAnything = true;
			}
		}

		for (I_RelVersioned rv : getSourceRels()) {
			if (rv.promote(viewPosition, pomotionPaths, allowedStatus, precedence)) {
				promotedAnything = true;
			}
		}

		for (I_ImageVersioned img : getImages()) {
			if (img.promote(viewPosition, pomotionPaths, allowedStatus, precedence)) {
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
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException {
		Set<Concept> returnValues = new HashSet<Concept>();
		for (I_RelTuple rel : getDestRelTuples(allowedStatus, allowedTypes,
				positions, precedencePolicy, contradictionManager)) {
			returnValues.add(Bdb.getConceptDb().getConcept(rel.getC1Id()));
		}
		return returnValues;
	}

	@Override
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions, 
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
        SetModifiedWhenChangedList relNidTypeNidlist = data.getDestRelNidTypeNidList();
        List<NidPair> invalidPairs = new ArrayList<NidPair>();
        for (NidPair pair: relNidTypeNidlist) {
        	int relNid = pair.getNid1();
        	int typeNid = pair.getNid2();
        	if (allowedTypes.contains(typeNid)) {
        		Concept relSource = Bdb.getConceptForComponent(relNid);
        		Relationship r = relSource.getRelationship(relNid);
        		if (r != null) {
        			r.addTuples(allowedStatus, allowedTypes, positions,
        					returnRels, precedencePolicy, contradictionManager);
        		} else {
        			invalidPairs.add(new NidPair(relNid, typeNid));
        		}
        	}
        }
        
        if (invalidPairs.size() > 0) {
        	synchronized (relNidTypeNidlist) {
        		for (NidPair pair: invalidPairs) {
        			if (relNidTypeNidlist.forget(pair)) {
        				Terms.get().addUncommittedNoChecks(this);
        			}
        		}
        	}
        }
        return returnRels;
	}

	@Override
	public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions, 
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException {
		List<I_ImageTuple> returnTuples = new ArrayList<I_ImageTuple>();
		for (I_ImageVersioned img : getImages()) {
			img.addTuples(allowedStatus, allowedTypes, positions, returnTuples,
			    precedencePolicy, contradictionManager);
		}
		return returnTuples;
	}

	@Override
	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions, 
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
		Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
		for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes,
				positions, precedencePolicy, contradictionManager)) {
			returnValues.add(Concept.get(rel.getC2Id()));
		}
		return returnValues;
	}

	@Override
	public List<? extends I_RelTuple> getSourceRelTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, 
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException,
			TerminologyException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, returnRels,
			    precedencePolicy, contradictionManager);
		}
		return returnRels;
	}

	@Override
	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
		for (I_Position p : positions) {
			KindOfSpec kindOfSpec = new KindOfSpec(p, allowedStatus,
					allowedTypes, getNid(), precedencePolicy, contradictionManager);
			if (KindOfComputer.isKindOf((Concept) child, kindOfSpec)) {
				return true;
			}
		}
		return false;
	}

	public boolean isParentOf(I_GetConceptData child)
			throws IOException, TerminologyException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		return isParentOf(child, config.getAllowedStatus(), config
				.getDestRelTypes(), config.getViewPositionSetReadOnly(), 
				config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public boolean isParentOfOrEqualTo(I_GetConceptData child) throws IOException, TerminologyException {
		if (child == this) {
			return true;
		}
		return isParentOf(child);
	}

	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws IOException, TerminologyException {
		if (child == this) {
			return true;
		}
		return isParentOf(child, allowedStatus, allowedTypes, positions, 
		        precedencePolicy, contradictionManager);
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
			formatCollection(buff, getDescriptions());
			buff.append("\n srcRels: ");
			formatCollection(buff, getSourceRels());
			buff.append("\n images: ");
			formatCollection(buff, getImages());
			buff.append("\n refset members: ");
			formatCollection(buff, getExtensions());
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
			List<? extends NidPair> refsetNidMemberNidList) throws IOException {
		if (refsetNidMemberNidList.size() == 0) {
			buff.append("[]");
		} else {
			buff.append("[\n");
			for (NidPair pair: refsetNidMemberNidList) {
				int refsetNid = pair.getNid1();
				int memberNid = pair.getNid2();
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
			List<? extends NidPair> doubleNidList) throws IOException {
		if (doubleNidList.size() == 0) {
			buff.append("[]");
		} else {
			buff.append("[\n");
			for (NidPair pair: doubleNidList) {
				Concept relConcept = Bdb.getConceptForComponent(pair.getNid1());
				Concept typeConcept = Bdb.getConceptForComponent(pair.getNid2());
				buff.append("     ");
				buff.append(pair.getNid1());
				buff.append(": ");
				buff.append(relConcept);
				buff.append(" ");
				buff.append(pair.getNid2());
				buff.append(": ");
				buff.append(typeConcept);
				buff.append("\n");
			}
			buff.append("]");
		}
	}

	private void formatCollection(StringBuffer buff, Collection<?> list) {
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
		for (NidPair pair: data.getRefsetNidMemberNidForConceptList()) {
			int refsetNid = pair.getNid1();
			int memberNid = pair.getNid2();
			Concept c = Bdb.getConceptDb().getConcept(refsetNid);
			RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
			returnValues.add(member);
		}
		return returnValues;
	}

	public List<RefsetMember<?, ?>> getConceptExtensions(int specifiedRefsetNid)
			throws IOException {
		List<RefsetMember<?, ?>> returnValues = new ArrayList<RefsetMember<?, ?>>();
        for (NidPair pair: data.getRefsetNidMemberNidForConceptList()) {
            int refsetNid = pair.getNid1();
            int memberNid = pair.getNid2();
			if (specifiedRefsetNid == refsetNid) {
				Concept c = Concept.get(refsetNid);
				RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
				assert member != null: "\n\nMissing concept refset exetension " + 
				    memberNid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(memberNid) +
				    " in concept: \n-------------------------\n\n" + 
				    this.toLongString() + 
				    "\n\nIn refset\n\n**********************************\n\n" +
				    Concept.get(refsetNid).toLongString() +
				    "\n-------------------------\n\n";
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
	    assert attributes.nid != 0;
	    nid = attributes.nid;
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
			List<? extends NidPair> refsetNidMemberNidList) {
    	List<Integer> memberNids = new ArrayList<Integer>();
    	for (NidPair pair: refsetNidMemberNidList) {
    		int refsetNid = pair.getNid1();
    		int memberNid = pair.getNid2();
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
	
	public final Set<I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config)
	      throws IOException {
	    return ConflictHelper.getCommonDescTuples(this, config);
	  }

	  public final Set<I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
	      throws IOException, TerminologyException {
	    return ConflictHelper.getCommonRelTuples(this, config);
	  }

	  public final Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(
	      I_ConfigAceFrame config) throws IOException, TerminologyException {
	    return ConflictHelper.getCommonConceptAttributeTuples(this,
	        config);
	  }
}
