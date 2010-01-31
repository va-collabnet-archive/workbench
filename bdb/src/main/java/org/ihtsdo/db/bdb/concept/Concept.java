package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
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
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.Description.Version;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.ERefset;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.I_ConceptualizeExternally;

public class Concept implements I_Transact, I_GetConceptData {

	private static class TransactionHandler implements I_Transact {

		@Override
		public void abort() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void commit(int version, Set<TimePathId> values)
				throws IOException {
			// TODO Auto-generated method stub

		}

	}

	public static Concept get(EConcept eConcept) throws IOException {
		int conceptNid = Bdb.uuidToNid(
				eConcept.getConceptAttributes().getPrimordialComponentUuid());
		assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";
		Concept c = get(conceptNid, true);
		EConceptAttributes eAttr = eConcept.getConceptAttributes();
		
		ConceptAttributes attr = new ConceptAttributes(eAttr, c);
		c.data.set(attr);
		if (eAttr.getExtraVersionsList() != null) {
			for (I_ConceptualizeExternally eav: eAttr.getExtraVersionsList()) {
				attr.addVersion(new ConceptAttributesRevision(eav, attr));
			}
		}
		if (eConcept.getDescriptions() != null) {
			for (EDescription eDesc: eConcept.getDescriptions()) {
				Description desc = new Description(eDesc, c);
				c.data.add(desc);
			}
		}
		if (eConcept.getRelationships() != null) {
			for (ERelationship eRel: eConcept.getRelationships()) {
				Relationship rel = new Relationship(eRel, c);
				c.data.add(rel);
			}
		}
		if (eConcept.getImages() != null) {
			for (EImage eImage: eConcept.getImages()) {
				Image img = new Image(eImage, c);
				c.data.add(img);
			}
		}
		if (eConcept.getRefsetMembers() != null) {
			for (ERefset<?> eRefsetMember: eConcept.getRefsetMembers()) {
				RefsetMember<?,?> refsetMember = 
					RefsetMemberFactory.create(eRefsetMember, c);
				c.data.add(refsetMember);
			}
		}
		
		if (eConcept.getDestRelUuidTypeUuids() != null) {
			ArrayIntList destRelOriginNidTypeNidList = 
				new ArrayIntList(eConcept.getDestRelUuidTypeUuids().size());
			for (UUID uuid: eConcept.getDestRelUuidTypeUuids()) {
				destRelOriginNidTypeNidList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setDestRelNidTypeNidList(destRelOriginNidTypeNidList);
		}
		if (eConcept.getRefsetUuidMemberUuidForConcept() != null) {
			ArrayIntList refsetNidMemberNidForConceptList = 
				new ArrayIntList(eConcept.getRefsetUuidMemberUuidForConcept().size());
			for (UUID uuid: eConcept.getRefsetUuidMemberUuidForConcept()) {
				refsetNidMemberNidForConceptList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setRefsetNidMemberNidForConceptList(refsetNidMemberNidForConceptList);
		}
		if (eConcept.getRefsetUuidMemberUuidForDescriptions() != null) {
			ArrayIntList refsetNidMemberNidForDescriptionsList = 
				new ArrayIntList(eConcept.getRefsetUuidMemberUuidForDescriptions().size());
			for (UUID uuid: eConcept.getRefsetUuidMemberUuidForDescriptions()) {
				refsetNidMemberNidForDescriptionsList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setRefsetNidMemberNidForDescriptionsList(refsetNidMemberNidForDescriptionsList);
		}
		if (eConcept.getRefsetUuidMemberUuidForRels() != null) {
			ArrayIntList refsetNidMemberNidForRelsList = 
				new ArrayIntList(eConcept.getRefsetUuidMemberUuidForRels().size());
			for (UUID uuid: eConcept.getRefsetUuidMemberUuidForRels()) {
				refsetNidMemberNidForRelsList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setRefsetNidMemberNidForRelsList(refsetNidMemberNidForRelsList);
		}

		if (eConcept.getRefsetUuidMemberUuidForImages() != null) {
			ArrayIntList refsetNidMemberNidForImagesList = 
				new ArrayIntList(eConcept.getRefsetUuidMemberUuidForImages().size());
			for (UUID uuid: eConcept.getRefsetUuidMemberUuidForImages()) {
				refsetNidMemberNidForImagesList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setRefsetNidMemberNidForImagesList(refsetNidMemberNidForImagesList);
		}

		if (eConcept.getRefsetUuidMemberUuidForRefsetMembers() != null) {
			ArrayIntList refsetNidMemberNidForRefsetMembersList = 
				new ArrayIntList(eConcept.getRefsetUuidMemberUuidForRefsetMembers().size());
			for (UUID uuid: eConcept.getRefsetUuidMemberUuidForRefsetMembers()) {
				refsetNidMemberNidForRefsetMembersList.add(Bdb.uuidToNid(uuid));
			}
			c.data.setRefsetNidMemberNidForRefsetMembersList(refsetNidMemberNidForRefsetMembersList);
		}

		return c;
	}	
	
	public static Concept get(int nid, boolean editable) throws IOException {
		return new Concept(nid, editable);
	}

	public static Concept get(int nid, boolean editable,
			byte[] roBytes, byte[] mutableBytes) throws IOException {
		return new Concept(nid, editable, roBytes, mutableBytes);
	}

	private static I_Transact transactionHandler = new TransactionHandler();

	private int nid;
	private boolean editable;
	private I_ManageConceptData data;
    private int fsDescNid = Integer.MIN_VALUE;
    private int fsXmlDescNid = Integer.MIN_VALUE;

	protected Concept(int nid, boolean editable) throws IOException {
		super();
		this.nid = nid;
		this.editable = editable;
		data = new ConceptDataSoftReference(this);
	}

	public Concept(int nid, boolean editable, byte[] roBytes,
			byte[] mutableBytes) throws IOException {
		this.nid = nid;
		this.editable = editable;
		data = new ConceptDataSoftReference(this, roBytes, mutableBytes);
	}

	public int getNid() {
		return nid;
	}

	public List<Description> getDescriptions() throws IOException {
		return data.getDescriptions();
	}

	public List<Relationship> getSourceRels() throws IOException {
		return data.getSourceRels();
	}
	public List<Relationship> getNativeSourceRels() throws IOException {
		return data.getSourceRels();
	}

	public boolean isEditable() {
		return editable;
	}

	@Override
	public void abort() throws IOException {
		editable = false;
	}

	@Override
	public void commit(int version, Set<TimePathId> values) throws IOException {
		if (editable) {
			try {
				if (ReadWriteDataVersion.get(nid) == data
						.getReadWriteDataVersion()) {

				} else {

				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
		} else {
			throw new UnsupportedOperationException(
					"Concept is not editable. nid: " + nid);
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
		return getConceptAttributes().getUUIDs();
	}

	public List<UUID> getUidsForComponent(int componentNid) throws IOException {
		return getComponent(componentNid).getUUIDs();
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
        I_ConceptAttributeVersioned attr = getConceptAttributes();
        if (attr != null) {
            getConceptAttributes().addTuples(allowedStatus, positionSet, returnTuples, addUncommitted,
                returnConflictResolvedLatestState);
        }
        return returnTuples;
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			boolean addUncommitted) throws IOException {
        List<ConceptAttributes.Version> returnTuples = new ArrayList<ConceptAttributes.Version>();
        getConceptAttributes().addTuples(allowedStatus, positionSet, returnTuples, addUncommitted);
        return returnTuples;
	}

	public List<ConceptAttributes.Version> getConceptAttributeTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getConceptAttributeTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), true,
            returnConflictResolvedLatestState);
	}

	public ConceptAttributes getConceptAttributes() throws IOException {
		return data.getConceptAttributes();
	}
	public ArrayList<ConceptAttributes> getConceptAttributesList() throws IOException {
		ArrayList<ConceptAttributes> returnList = new ArrayList<ConceptAttributes>(1);
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
		    return getLangPreferredDesc(getDescriptionTuples(allowedStatus, typeSet, positionSet, true),
		        typePrefOrder, langPrefOrder, allowedStatus, positionSet, typeSet);
		case TYPE_B4_LANG:
		    return getTypePreferredDesc(getDescriptionTuples(allowedStatus, typeSet, positionSet, true),
		        typePrefOrder, langPrefOrder, allowedStatus, positionSet, typeSet);
		default:
		    throw new IOException("Can't handle sort type: " + sortPref);
		}
	}

    private I_DescriptionTuple getLangPreferredDesc(Collection<I_DescriptionTuple> descriptions,
            I_IntList typePrefOrder, I_IntList langPrefOrder, I_IntSet allowedStatus, PositionSetReadOnly positionSet,
            I_IntSet typeSet) throws IOException, ToIoException {
        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                List<I_DescriptionTuple> matchedList = new ArrayList<I_DescriptionTuple>();
                if (langPrefOrder != null && langPrefOrder.getListValues() != null) {
                    for (int langId : langPrefOrder.getListValues()) {
                        for (I_DescriptionTuple d : descriptions) {
                            try {
                                int tupleLangId = ArchitectonicAuxiliary.getLanguageConcept(d.getLang())
                                    .localize()
                                    .getNid();
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
                            return getTypePreferredDesc(matchedList, typePrefOrder, langPrefOrder, allowedStatus,
                                positionSet, typeSet);
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

    private I_DescriptionTuple getTypePreferredDesc(Collection<I_DescriptionTuple> descriptions,
            I_IntList typePrefOrder, I_IntList langPrefOrder, I_IntSet allowedStatus, PositionSetReadOnly positionSet,
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
                        return getLangPreferredDesc(matchedList, typePrefOrder, langPrefOrder, allowedStatus,
                            positionSet, typeSet);
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
        return (Version) getDescTuple(descTypePreferenceList, config.getLanguagePreferenceList(), config.getAllowedStatus(),
                config.getViewPositionSetReadOnly(), config.getLanguageSortPref());
	}

	public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions)
			throws IOException {
        return getDescriptionTuples(allowedStatus, allowedTypes, positions, true);
	}

	public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean returnConflictResolvedLatestState) throws IOException {
        List<I_DescriptionTuple> returnDescriptions = new ArrayList<I_DescriptionTuple>();
        for (Description desc : getDescriptions()) {
            desc.addTuples(allowedStatus, allowedTypes, positions, returnDescriptions,
                returnConflictResolvedLatestState);
        }
        return returnDescriptions;
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getDescriptionTuples(config.getAllowedStatus(), 
        		config.getDescTypes(), 
        		config.getViewPositionSetReadOnly(),
            returnConflictResolvedLatestState);
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), addUncommitted,
            returnConflictResolvedLatestState);
	}

	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getSourceRelTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), addUncommitted,
            returnConflictResolvedLatestState);
	}

	public List<Relationship> getDestRels() throws IOException {
		return data.getDestRels();
	}

	public List<RefsetMember<?, ?>> getExtensions() throws IOException {
		return data.getRefsetMembers();
	}

	public List<I_ImageTuple> getImageTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getImageTuples(config.getAllowedStatus(), null, config.getViewPositionSetReadOnly(),
            returnConflictResolvedLatestState);
	}

	public List<Image> getImages() throws IOException {
		return data.getImages();
	}

	public String getInitialText() throws IOException {
        try {
            if ((AceConfig.config != null) && (AceConfig.config.aceFrames.get(0) != null)) {
                I_DescriptionTuple tuple = this.getDescTuple(AceConfig.config.aceFrames.get(0)
                    .getShortLabelDescPreferenceList(), AceConfig.config.getAceFrames().get(0));
                if (tuple != null) {
                    return tuple.getText();
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
                    fsDescNid = Terms.get().uuidToNative(
                        ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids());
                    fsDescNid = Terms.get().uuidToNative(
                        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
                }
                if (getDescriptions().size() > 0) {
                    I_DescriptionVersioned desc = getDescriptions().get(0);
                    for (I_DescriptionVersioned d : getDescriptions()) {
                        for (I_DescriptionPart part : d.getMutableParts()) {
                            if ((part.getTypeId() == fsDescNid) || (part.getTypeId() == fsXmlDescNid)) {
                                return part.getText();
                            }
                        }
                    }
                    return desc.getMutableParts().get(0).getText();
                } else {
                    StringBuffer errorBuffer = new StringBuffer();
                    errorBuffer.append("No descriptions for concept. uuids: "
                        + AceConfig.getVodb().getUids(nid).toString() + " nid: "
                        + AceConfig.getVodb().uuidToNative(getUids()));

                    int sequence = nid + Integer.MIN_VALUE;
                    String errString = nid + " (" + sequence + ") " + " has no descriptions " + getUids();
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

	public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getSourceRelTargets(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(),
            addUncommitted, returnConflictResolvedLatestState);
	}

	public List<? extends I_RelTuple> getSourceRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		List<I_RelTuple> returnTuples = new ArrayList<I_RelTuple>();
		for (Relationship r: getNativeSourceRels()) {
			r.addTuples(allowedTypes, returnTuples, addUncommitted, 
					returnConflictResolvedLatestState);
		}
		return returnTuples;
	}

	public ConceptAttributes getUncommittedConceptAttributes() {
		return null;
	}

	public List<I_DescriptionVersioned> getUncommittedDescriptions() {
		return new ArrayList<I_DescriptionVersioned>();
	}

	public List<I_Identify> getUncommittedIdVersioned() {
		return new ArrayList<I_Identify>();
	}

	public I_IntSet getUncommittedIds() {
		return new IntSet();
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		return new ArrayList<I_ImageVersioned>();
	}

	public List<I_RelVersioned> getUncommittedSourceRels() {
		return new ArrayList<I_RelVersioned>();
	}

	public UniversalAceBean getUniversalAceBean() throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted)
			throws IOException {
		I_IntSet destRelTypes = aceConfig.getDestRelTypes();
		IntList relNidTypeNid = data.getDestRelNidTypeNidList();
		IntList possibleChildRels = new ArrayIntList();
		int i = 0;
		while (i < relNidTypeNid.size()) {
			int relNid = relNidTypeNid.get(i++);
			int typeNid = relNidTypeNid.get(i++);
			if (destRelTypes.contains(typeNid)) {
				possibleChildRels.add(relNid);
			}
		}
		if (possibleChildRels.size() == 0 && aceConfig.getSourceRelTypes().getSetValues().length == 0) {
			return true;
		}
		IntIterator relNids = possibleChildRels.iterator();
		while (relNids.hasNext()) {
			int relNid = relNids.next();
			Relationship r = Bdb.getConceptForComponent(relNid).getSourceRel(relNid);
			List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
			try {
				r.addTuples(destRelTypes, currentVersions, addUncommitted, false);
			} catch (TerminologyException e) {
				throw new IOException(e);
			}
			if (currentVersions.size() > 0) {
				return false;
			}
		}
		
		I_IntSet srcRelTypes = aceConfig.getSourceRelTypes();
		for (Relationship r: getSourceRels()) {
			List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
			try {
				r.addTuples(srcRelTypes, currentVersions, addUncommitted, false);
			} catch (TerminologyException e) {
				throw new IOException(e);
			}
			if (currentVersions.size() > 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isParentOf(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public boolean isParentOfOrEqualTo(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Relationship getRelationship(int relNid) throws IOException {
		for (Relationship r: getNativeSourceRels()) {
			if (r.getNid() == relNid) {
				return r;
			}
		}
		throw new IOException("no such relid: " + relNid + " in concept: " + nid);
	}



	@Override
	public Set<Concept> getDestRelOrigins(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
        Set<Concept> returnValues = new HashSet<Concept>();
        for (I_RelTuple rel : getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted)) {
            returnValues.add(Bdb.getConceptDb().getConcept(rel.getC1Id()));
        }
        return returnValues;
	}


	@Override
	public Set<Concept> getDestRelOrigins(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        return getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), addUncommitted,
            returnConflictResolvedLatestState);
	}


	@Override
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		return getDestRelTuples(allowedStatus,
				allowedTypes, positions,
				addUncommitted, false);
	}


	@Override
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException {
		try {
			List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
			IntList relNidTypeNidlist = data.getDestRelNidTypeNidList();
			int i = 0;
			while (i < relNidTypeNidlist.size()) {
				int relNid = relNidTypeNidlist.get(i++);
				int typeNid = relNidTypeNidlist.get(i++);
				if (allowedTypes.contains(typeNid)) {
					Concept relSource = Bdb.getConceptForComponent(relNid);
					Relationship r = relSource.getRelationship(relNid);
					r.addTuples(allowedStatus, allowedTypes, positions, 
							returnRels, addUncommitted, returnConflictResolvedLatestState);
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

        List<I_ImageTuple> returnTuples = getImageTuples(allowedStatus, allowedTypes, positions);
        return Terms.get()
            .getActiveAceFrameConfig()
            .getConflictResolutionStrategy()
            .resolveTuples(returnTuples);
	}

	@Override
	public Set<I_GetConceptData> getSourceRelTargets(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
        Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
        for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted)) {
            returnValues.add(ConceptBean.get(rel.getC2Id()));
        }
        return returnValues;
	}


	@Override
	public Set<I_GetConceptData> getSourceRelTargets(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
        Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
        for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted,
            returnConflictResolvedLatestState)) {
            returnValues.add(ConceptBean.get(rel.getC2Id()));
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
            rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
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
            rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted,
                returnConflictResolvedLatestState);
        }
        return returnRels;
	}


	@Override
	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOf(I_GetConceptData child, boolean addUncommitted)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			boolean addUncommitted) throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public Object getDenotation(int authorityNid) throws IOException,
			TerminologyException {
		for (I_IdVersion part: getIdentifier().getIdVersions()) {
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

	public int[] getAllNids() throws IOException {
		return data.getAllNids();
	}

	/**
	 * Test method to check to see if two concepts are equal in all respects. 
	 * @param another
	 * @return either a zero length String, or a String containing a description of the
	 * validation failures. 
	 * @throws IOException 
	 */
	public String validate(Concept another) throws IOException {
		assert another != null;
		StringBuffer buff = new StringBuffer();
		if (nid != another.nid) {
			buff.append("Nids not equal: " + nid + " " + another.nid + "\n");
		}
		ConceptAttributes attributes = getConceptAttributes();
		assert attributes != null: "validating: " + nid;
		ConceptAttributes anotherAttributes = another.getConceptAttributes();
		assert anotherAttributes != null: "validating: " + nid;
		if (attributes.equals(anotherAttributes) == false) {
			buff.append("Concept attributes are not equal:\n" + 
					getConceptAttributes() + "\n" + 
					another.getConceptAttributes() + "\n");
		}
		
		return buff.toString();
	}	
	
	public String toString() {
        try {
            return getInitialText();
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
            return ex.toString();
        }
	}

	public String toLongString() {
		StringBuffer buff = new StringBuffer();
		try {
			buff.append("\nConcept: \n attributes: ");
			buff.append(getConceptAttributes());
			buff.append("\n descriptions: ");
			buff.append(getDescriptions());
			buff.append("\n srcRels: ");
			buff.append(getSourceRels());
			buff.append("\n images: ");
			buff.append(getImages());
			buff.append("\n refset members: ");
			buff.append(getExtensions());
			buff.append("\n dest rel nid, type nid: ");
			buff.append(data.getDestRelNidTypeNidList());
			buff.append("\n refset/member for concept: ");
			buff.append(data.getRefsetNidMemberNidForConceptList());
			buff.append("\n refset/member for desc: ");
			buff.append(data.getRefsetNidMemberNidForDescriptionsList());
			buff.append("\n refset/member for rels: ");
			buff.append(data.getRefsetNidMemberNidForRelsList());
			buff.append("\n refset/member for image: ");
			buff.append(data.getRefsetNidMemberNidForImagesList());
			buff.append("\n refset/member for members: ");
			buff.append(data.getRefsetNidMemberNidForRefsetMembersList());
			buff.append("\n desc nids: ");
			buff.append(data.getDescNids());
			buff.append("\n src rel nids: ");
			buff.append(data.getSrcRelNids());
			buff.append("\n image nids: ");
			buff.append(data.getImageNids());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return buff.toString();
	}

	public List<RefsetMember<?, ?>> getConceptExtensions() throws IOException {
		List<RefsetMember<?, ?>> returnValues = new ArrayList<RefsetMember<?,?>>();
		IntIterator itr = data.getRefsetNidMemberNidForConceptList().iterator();
		while (itr.hasNext()) {
			int refsetNid = itr.next();
			int memberNid = itr.next();
			Concept c = Bdb.getConceptDb().getConcept(refsetNid);
			RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
			returnValues.add(member);
		}
		return returnValues;
	}
	public List<RefsetMember<?, ?>> getConceptExtensions(int specifiedRefsetNid) throws IOException {
		List<RefsetMember<?, ?>> returnValues = new ArrayList<RefsetMember<?,?>>();
		IntIterator itr = data.getRefsetNidMemberNidForConceptList().iterator();
		while (itr.hasNext()) {
			int refsetNid = itr.next();
			int memberNid = itr.next();
			if (specifiedRefsetNid == refsetNid) {
				Concept c = Bdb.getConceptDb().getConcept(refsetNid);
				RefsetMember<?, ?> member = c.getRefsetMember(memberNid);
				returnValues.add(member);
			}
		}
		return returnValues;
	}
	public ConceptComponent<?, ?> getComponent(int nid) throws IOException {
		return data.getComponent(nid);
	}

	public List<RefsetMember<?, ?>> getExtensionsForComponent(int nid) throws IOException {
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
}
