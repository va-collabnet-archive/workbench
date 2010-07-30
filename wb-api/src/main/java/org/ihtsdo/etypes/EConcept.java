package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.TkConcept;
import org.ihtsdo.tk.concept.component.TkComponent;
import org.ihtsdo.tk.concept.component.description.TkDescription;
import org.ihtsdo.tk.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.concept.component.media.TkMedia;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.concept.component.relationship.TkRelationship;

public class EConcept extends  TkConcept implements I_AmChangeSetObject {
    public static final long serialVersionUID = 1;
    
    protected static Collection<? extends I_ExtendByRef> getRefsetMembers(int nid) throws TerminologyException, IOException {
        return Terms.get().getRefsetExtensionMembers(nid);
    }


    public static TkRefsetAbstractMember<?> convertRefsetMember(I_ExtendByRef m) throws TerminologyException, IOException {
        REFSET_TYPES type = REFSET_TYPES.nidToType(m.getTypeId());
        if (type != null) {
            switch (type) {
            case CID:
                return new ERefsetCidMember(m);
            case CID_CID:
                return new ERefsetCidCidMember(m);
            case CID_CID_CID:
                return new ERefsetCidCidCidMember(m);
            case CID_CID_STR:
                return new ERefsetCidCidStrMember(m);
            case INT:
                return new ERefsetIntMember(m);
            case MEMBER:
                return new ERefsetMemberMember(m);
            case STR:
                return new ERefsetStrMember(m);
            case CID_INT:
                return new ERefsetCidIntMember(m);
            default:
                throw new UnsupportedOperationException("Cannot handle: " + type);
            }
        } else {
            AceLog.getAppLog().severe("Can't handle refset type: " + m);
        }
        return null;
    }


    public static void convertId(I_Identify id, TkComponent<?> component) throws TerminologyException, IOException {
        boolean primordialWritten = false;
        int partCount = id.getMutableIdParts().size() - 1;
        if (partCount > 0) {
        	component.additionalIds = new ArrayList<TkIdentifier>(partCount);
            for (I_IdPart idp : id.getMutableIdParts()) {
                Object denotation = idp.getDenotation();
                switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                case LONG:
                	component.additionalIds.add(new EIdentifierLong(idp));
                    break;
                case STRING:
                	component.additionalIds.add(new EIdentifierString(idp));
                    break;
                case UUID:
                    if (primordialWritten) {
                    	component.additionalIds.add(new EIdentifierUuid(idp));
                    } else {
                    	component.primordialUuid = (UUID) idp.getDenotation();
                        primordialWritten = true;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
                }

            }
        } else {
        	component.primordialUuid = (UUID) id.getUUIDs().get(0);
        }
    }


    /**
     * CID = Component IDentifier
     * 
     * @author kec
     * 
     */
    public enum REFSET_TYPES {
        MEMBER(1, RefsetAuxiliary.Concept.MEMBER_TYPE, I_ExtendByRefPart.class), 
        CID(2, RefsetAuxiliary.Concept.CONCEPT_EXTENSION, I_ExtendByRefPartCid.class), 
        CID_CID(3, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION, I_ExtendByRefPartCidCid.class), 
        CID_CID_CID(4, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION, I_ExtendByRefPartCidCidCid.class), 
        CID_CID_STR(5, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION, I_ExtendByRefPartCidCidString.class), 
        STR(6, RefsetAuxiliary.Concept.STRING_EXTENSION, I_ExtendByRefPartStr.class), 
        INT(7, RefsetAuxiliary.Concept.INT_EXTENSION, I_ExtendByRefPartInt.class), 
        CID_INT(8, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION, I_ExtendByRefPartCidInt.class), 
        BOOLEAN(9, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION, I_ExtendByRefPartBoolean.class), 
        CID_STR(10, RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION, I_ExtendByRefPartStr.class), 
        CID_FLOAT(11, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION, null), //TODO add interface for refset
        CID_LONG(12, RefsetAuxiliary.Concept.CID_LONG_EXTENSION, I_ExtendByRefPartCidLong.class), 
        LONG(13, RefsetAuxiliary.Concept.LONG_EXTENSION, null); //TODO add interface for refset

        private int externalizedToken;
        private int typeNid;
        private RefsetAuxiliary.Concept typeConcept;
        private static Map<Integer, REFSET_TYPES> nidTypeMap;
        private Class<? extends I_ExtendByRefPart> partClass;
        
        REFSET_TYPES(int externalizedToken, RefsetAuxiliary.Concept typeConcept, 
        		Class<? extends I_ExtendByRefPart> partClass) {
            this.externalizedToken = externalizedToken;
            this.typeConcept = typeConcept;
            this.partClass = partClass;
        }
        
        public static REFSET_TYPES classToType(Class<? extends I_ExtendByRefPart> partType) {
        	if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(partType)) {
        		return CID_CID_CID;
        	} else if (I_ExtendByRefPartCidCidString.class.isAssignableFrom(partType)) {
        		return CID_CID_STR;
        	} else if (I_ExtendByRefPartCidLong.class.isAssignableFrom(partType)) {
        		return CID_LONG;
        	} else if (I_ExtendByRefPartCidLong.class.isAssignableFrom(partType)) {
        		return CID_LONG;
        	} else if (I_ExtendByRefPartCidCid.class.isAssignableFrom(partType)) {
        		return CID_CID;
        	} else if (I_ExtendByRefPartCidInt.class.isAssignableFrom(partType)) {
        		return CID_INT;
        	} else if (I_ExtendByRefPartCidString.class.isAssignableFrom(partType)) {
        		return CID_STR;
        	} else if (I_ExtendByRefPartCidFloat.class.isAssignableFrom(partType)) {
        		return CID_FLOAT;
        	} else if (I_ExtendByRefPartBoolean.class.isAssignableFrom(partType)) {
        		return BOOLEAN;
        	} else if (I_ExtendByRefPartCid.class.isAssignableFrom(partType)) {
        		return CID;
        	} else if (I_ExtendByRefPartInt.class.isAssignableFrom(partType)) {
        		return INT;
        	} else if (I_ExtendByRefPartLong.class.isAssignableFrom(partType)) {
        		return LONG;
        	} else if (I_ExtendByRefPartStr.class.isAssignableFrom(partType)) {
        		return STR;
        	} 
        	throw new UnsupportedOperationException("Unsupported refset type: " + partType);
        }

        public static REFSET_TYPES nidToType(int nid) throws IOException {
            setupNids();
            if (nidTypeMap.containsKey(nid)) {
                return nidTypeMap.get(nid);
            } else {
            	if (Terms.get().hasConcept(nid)) {
                	I_GetConceptData typeConcept;
					try {
						typeConcept = Terms.get().getConcept(nid);
					} catch (TerminologyException e) {
						throw new IOException(e);
					}
                	throw new IOException("Unknown refset type: " + nid + 
                			" concept: " + typeConcept.getInitialText());
            	} else {
                	throw new IOException("Unknown refset type: " + nid);
            	}
            }
        }

		private static void setupNids() {
			if (nidTypeMap == null) {
			    HashMap<Integer, REFSET_TYPES> temp  = new HashMap<Integer, REFSET_TYPES>();
                for (REFSET_TYPES type : REFSET_TYPES.values()) {
                    try {
                        type.typeNid = Terms.get().uuidToNative(type.typeConcept.getUids());
                        temp.put(type.typeNid, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                nidTypeMap = temp;
            }
		}

        public void writeType(DataOutput output) throws IOException {
            output.writeByte(externalizedToken);
        }

        public static REFSET_TYPES readType(DataInput input) throws IOException {
            switch (input.readByte()) {
            case 1:
                return MEMBER;
            case 2:
                return CID;
            case 3:
                return CID_CID;
            case 4:
                return CID_CID_CID;
            case 5:
                return CID_CID_STR;
            case 6:
                return STR;
            case 7:
                return INT;
            case 8:
                return CID_INT;
            case 9:
                return BOOLEAN;
            case 10:
                return CID_STR;
            case 11:
                return CID_FLOAT;
            case 12:
                return CID_LONG;
            }
            throw new UnsupportedOperationException();
        }

        public int getTypeNid() {
            setupNids();
            return typeNid;
        }

		public Class<? extends I_ExtendByRefPart> getPartClass() {
			return partClass;
		}
    };

    
    public EConcept(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
    }


    public EConcept() {
        super();
    }
    
    private static Map<Integer, Set<I_ExtendByRef>> componentRefsetMap;
    
    private void initComponentRefsetMap() throws IOException, TerminologyException {
    	componentRefsetMap = new HashMap<Integer, Set<I_ExtendByRef>>();
    	addMembersToMap(RefsetAuxiliary.Concept.PATH_ORIGIN.localize().getNid());
    	addMembersToMap(RefsetAuxiliary.Concept.REFSET_PATHS.localize().getNid());
    	addMembersToMap(RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS.localize().getNid());
    	addMembersToMap(ArchitectonicAuxiliary.Concept.PATH.localize().getNid());
		System.out.println("component refset map: " + componentRefsetMap);
    }

	private void addMembersToMap(int nid) throws IOException {
		for (I_ExtendByRef member: Terms.get().getRefsetExtensionMembers(nid)) {
			System.out.println("adding to map: " + member);
			Set<I_ExtendByRef> set = componentRefsetMap.get(member.getComponentId());
			if (set == null) {
				set = new HashSet<I_ExtendByRef>();
				componentRefsetMap.put(member.getComponentId(), set);
			}
			set.add(member);
    	}
	}

    public EConcept(I_ConceptualizeLocally cNoHx, 
    		I_StoreLocalFixedTerminology mts) throws IOException, TerminologyException {
    	UUID currentUuid = ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid();
       	UUID pathUuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
       	long time = System.currentTimeMillis();
        conceptAttributes = new EConceptAttributes();
        conceptAttributes.defined = false;
    	conceptAttributes.primordialUuid = cNoHx.getUids().iterator().next();
    	conceptAttributes.statusUuid = currentUuid;
    	conceptAttributes.setPathUuid(pathUuid);
    	conceptAttributes.setTime(time);
    	
        descriptions = new ArrayList<TkDescription>(cNoHx.getDescriptions().size());
    	for (I_DescribeConceptLocally descNoHx: cNoHx.getDescriptions()) {
    		EDescription desc  = new EDescription();
    		desc.primordialUuid = descNoHx.getUids().iterator().next();
    		desc.statusUuid = currentUuid;
    		desc.setPathUuid(pathUuid);
    		desc.setTime(time);

        	desc.conceptUuid = conceptAttributes.primordialUuid;
        	desc.initialCaseSignificant = descNoHx.isInitialCapSig();
        	desc.lang = descNoHx.getLangCode();
        	desc.text = descNoHx.getText();
        	desc.typeUuid = descNoHx.getDescType().getUids().iterator().next();
        	descriptions.add(desc);
    	}
    	
    	relationships = new ArrayList<TkRelationship>(cNoHx.getSourceRels().size());
    	for (I_RelateConceptsLocally relNoHx: cNoHx.getSourceRels()) {
    		ERelationship rel  = new ERelationship();
    		rel.primordialUuid = relNoHx.getUids().iterator().next();
    		rel.statusUuid = currentUuid;
    		rel.setPathUuid(pathUuid);
    		rel.setTime(time);
    		
    		rel.c1Uuid = conceptAttributes.primordialUuid;
    		rel.c2Uuid = relNoHx.getC2().getUids().iterator().next();
    		rel.characteristicUuid = relNoHx.getCharacteristic().getUids().iterator().next();
    		rel.refinabilityUuid = relNoHx.getRefinability().getUids().iterator().next();
    		rel.relGroup = relNoHx.getRelGrp();
    		rel.typeUuid = relNoHx.getRelType().getUids().iterator().next();
    		relationships.add(rel);
    		
    	}
	}

	/**
	 * @TODO remove componentRefsetMap added to get around bug in current database implementation!
	 * @param c
	 * @throws IOException
	 * @throws TerminologyException
	 */
    public EConcept(I_GetConceptData c) throws IOException, TerminologyException {
    	if (componentRefsetMap == null) {
    		initComponentRefsetMap();
    	}
        conceptAttributes = new EConceptAttributes(c.getConceptAttributes());
        EConcept.convertId(c.getIdentifier(), conceptAttributes);
        relationships = new ArrayList<TkRelationship>(c.getSourceRels().size());
        for (I_RelVersioned rel : c.getSourceRels()) {
            relationships.add(new ERelationship(rel));
        }
        descriptions = new ArrayList<TkDescription>(c.getDescriptions().size());
        for (I_DescriptionVersioned desc : c.getDescriptions()) {
            descriptions.add(new EDescription(desc));
        }
        images = new ArrayList<TkMedia>(c.getImages().size());
        for (I_ImageVersioned img : c.getImages()) {
            EImage eImage = new EImage(img);
            if (eImage.getTime() == Long.MIN_VALUE) {
                eImage.setTime(this.conceptAttributes.getTime());
                // Fixup for a data issue.
            }
            images.add(eImage);
        }
        Collection<? extends I_ExtendByRef> members = getRefsetMembers(c.getNid());
        if (members != null) {
            refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(members.size());
            for (I_ExtendByRef m : members) {
                TkRefsetAbstractMember<?> member = convertRefsetMember(m);
                if (member != null) {
                    refsetMembers.add(member);
                } else {
                    AceLog.getAppLog().severe("Could not convert refset member: " + m + "\nfrom refset: " + c);
                }
            }
        }

        Collection<? extends I_ExtendByRef> conceptMembers = componentRefsetMap.get(c.getNid());
        if (conceptMembers != null) {
        	ArrayList<TkRefsetAbstractMember<?>> refsetMemberForComponent = new ArrayList<TkRefsetAbstractMember<?>>(conceptMembers.size());
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(refsetMemberForComponent.size() * 2);
        	for (I_ExtendByRef m : conceptMembers) {
        		assert m.getComponentId() == c.getNid() : 
        			"getRefsetMembersForComponent query error: componentId: " + m.getComponentId() + 
        			" conceptId: " + c.getNid();
                UUID refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForConcept.add(refsetUuid);
                UUID memberUuid = Terms.get().nidToUuid(m.getNid());
                refsetUuidMemberUuidForConcept.add(memberUuid);
            }
        }

        Collection<I_ExtendByRef> descriptionMembers = 
        	new ArrayList<I_ExtendByRef>();
        for (I_DescriptionVersioned desc: c.getDescriptions()) {
        	Collection<? extends I_ExtendByRef> componentMembers = 
        		componentRefsetMap.get(desc.getNid());
        	if (componentMembers != null) {
        		descriptionMembers.addAll(componentMembers);
        	}
        }
        if (descriptionMembers.size() > 0) {
        	refsetUuidMemberUuidForDescriptions = 
        		new ArrayList<UUID>(descriptionMembers.size() * 2);
        	for (I_ExtendByRef m : descriptionMembers) {
        		System.out.println("Found description extension: " + m + " for component: " + this);
                UUID refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForDescriptions.add(refsetUuid);
                UUID memberUuid = Terms.get().nidToUuid(m.getNid());
                refsetUuidMemberUuidForDescriptions.add(memberUuid);
            }
        }

        Collection<I_ExtendByRef> relMembers = 
        	new ArrayList<I_ExtendByRef>();
        for (I_RelVersioned r: c.getSourceRels()) {
        	Collection<? extends I_ExtendByRef> componentMembers = 
        		componentRefsetMap.get(r.getNid());
        	if (componentMembers != null) {
        		relMembers.addAll(componentMembers);
        	}
        }
        if (relMembers.size() > 0) {
        	refsetUuidMemberUuidForRels = new ArrayList<UUID>(relMembers.size() * 2);
        	for (I_ExtendByRef m : relMembers) {
        		System.out.println("Found rel extension: " + m + " for component: " + this);
                UUID refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForRels.add(refsetUuid);
                UUID memberUuid = Terms.get().nidToUuid(m.getNid());
                refsetUuidMemberUuidForRels.add(memberUuid);
            }
        }
        
        Collection<I_ExtendByRef> imageMembers = 
        	new ArrayList<I_ExtendByRef>();
        for (I_ImageVersioned img: c.getImages()) {
        	Collection<? extends I_ExtendByRef> componentMembers = 
        		componentRefsetMap.get(img.getNid());
        	if (componentMembers != null) {
        		imageMembers.addAll(componentMembers);
        	}
        }
        
        if (imageMembers.size() > 0) {
        	refsetUuidMemberUuidForImages = new ArrayList<UUID>(relMembers.size() * 2);
        	for (I_ExtendByRef m : imageMembers) {
        		System.out.println("Found image extension: " + m + " for component: " + this);
        		UUID refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForImages.add(refsetUuid);
                UUID memberUuid = Terms.get().nidToUuid(m.getNid());
                refsetUuidMemberUuidForImages.add(memberUuid);
            }
        }
        
        destRelUuidTypeUuids = new ArrayList<UUID>();
        for (I_RelVersioned r: c.getDestRels()) {
            UUID relUuid = Terms.get().nidToUuid(r.getNid());
            HashSet<UUID> typesAdded = new HashSet<UUID>();
            for (I_RelPart p: r.getMutableParts()) {
                UUID typeUuid = Terms.get().nidToUuid(p.getTypeId());
                if (!typesAdded.contains(typeUuid)) {
                    destRelUuidTypeUuids.add(relUuid);            	
                    destRelUuidTypeUuids.add(typeUuid);
                    typesAdded.add(typeUuid);
                }
            }
        }
    }
    
	public List<TkMedia> getImages() {
        return images;
    }

    public void setImages(List<TkMedia> images) {
        this.images = images;
    }

    public void setConceptAttributes(EConceptAttributes conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append("\n   primordial UUID: ");
        buff.append(this.primordialUuid);
        buff.append("\n   ConceptAttributes: \n\t");
        buff.append(this.conceptAttributes);
        buff.append("\n   Descriptions: \n\t");
        buff.append(this.descriptions);
        buff.append("\n   Relationships: \n\t");
        buff.append(this.relationships);
        buff.append("\n   RefsetMembers: \n\t");
        buff.append(this.refsetMembers);
        buff.append("\n   Images: \n\t");
        buff.append(this.images);
        buff.append("\n   destRelUuidTypeUuids: \n\t");
        buff.append(this.destRelUuidTypeUuids);
        buff.append("\n   refsetUuidMemberUuidForConcept: \n\t");
        buff.append(this.refsetUuidMemberUuidForConcept);
        buff.append("\n   refsetUuidMemberUuidForDescriptions: \n\t");
        buff.append(this.refsetUuidMemberUuidForDescriptions);
        buff.append("\n   refsetUuidMemberUuidForRels: \n\t");
        buff.append(this.refsetUuidMemberUuidForRels);
        buff.append("\n   refsetUuidMemberUuidForImages: \n\t");
        buff.append(this.refsetUuidMemberUuidForImages);
        return buff.toString();
    }

	public List<UUID> getDestRelUuidTypeUuids() {
		return destRelUuidTypeUuids;
	}

	public void setDestRelUuidTypeUuids(List<UUID> destRelOriginUuidTypeUuids) {
		this.destRelUuidTypeUuids = destRelOriginUuidTypeUuids;
	}

	public List<UUID> getRefsetUuidMemberUuidForConcept() {
		return refsetUuidMemberUuidForConcept;
	}

	public void setRefsetUuidMemberUuidForConcept(
			List<UUID> refsetUuidMemberUuidForConcept) {
		this.refsetUuidMemberUuidForConcept = refsetUuidMemberUuidForConcept;
	}

	public List<UUID> getRefsetUuidMemberUuidForDescriptions() {
		return refsetUuidMemberUuidForDescriptions;
	}

	public void setRefsetUuidMemberUuidForDescriptions(
			List<UUID> refsetUuidMemberUuidForDescriptions) {
		this.refsetUuidMemberUuidForDescriptions = refsetUuidMemberUuidForDescriptions;
	}

	public List<UUID> getRefsetUuidMemberUuidForRels() {
		return refsetUuidMemberUuidForRels;
	}

	public void setRefsetUuidMemberUuidForRels(
			List<UUID> refsetUuidMemberUuidForRels) {
		this.refsetUuidMemberUuidForRels = refsetUuidMemberUuidForRels;
	}
	
    /**
     * Returns a hash code for this <code>EConcept</code>.
     * 
     * @return a hash code value for this <tt>EConcept</tt>.
     */
    public int hashCode() {
        return this.conceptAttributes.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EConcept</tt> object, and contains the same values, field by field, 
     * as this <tt>EConcept</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EConcept.class.isAssignableFrom(obj.getClass())) {
            EConcept another = (EConcept) obj;
            
            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare ConceptAttributes
            if (this.conceptAttributes == null) {
                if (this.conceptAttributes != another.conceptAttributes)
                    return false;
            } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
                return false;
            }
            // Compare Descriptions
            if (this.descriptions == null) {
                if (another.descriptions == null) { // Equal!
                } else if (another.descriptions.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.descriptions.equals(another.descriptions)) {
                return false;
            }
            // Compare Relationships
            if (this.relationships == null) {
                if (another.relationships == null) { // Equal!
                } else if (another.relationships.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.relationships.equals(another.relationships)) {
                return false;
            }
            // Compare Images
            if (this.images == null) {
                if (another.images == null) { // Equal!
                } else if (another.images.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.images.equals(another.images)) {
                return false;
            }
            // Compare Refset Members
            if (this.refsetMembers == null) {
                if (another.refsetMembers == null) { // Equal!
                } else if (another.refsetMembers.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetMembers.equals(another.refsetMembers)) {
                return false;
            }
            // Compare destRelUuidTypeUuids
            if (this.destRelUuidTypeUuids == null) {
                if (another.destRelUuidTypeUuids == null) { // Equal!
                } else if (another.destRelUuidTypeUuids.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.destRelUuidTypeUuids.equals(another.destRelUuidTypeUuids)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForConcept
            if (this.refsetUuidMemberUuidForConcept == null) {
                if (another.refsetUuidMemberUuidForConcept == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForConcept.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForConcept.equals(another.refsetUuidMemberUuidForConcept)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForDescriptions
            if (this.refsetUuidMemberUuidForDescriptions == null) {
                if (another.refsetUuidMemberUuidForDescriptions == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForDescriptions.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForDescriptions.equals(another.refsetUuidMemberUuidForDescriptions)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForRels
            if (this.refsetUuidMemberUuidForRels == null) {
                if (another.refsetUuidMemberUuidForRels == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForRels.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForRels.equals(another.refsetUuidMemberUuidForRels)) {
                return false;
            }

            // If none of the previous comparisons fail, the objects must be equal
            return true;
        }
        return false;
    }

	public List<UUID> getRefsetUuidMemberUuidForImages() {
		return refsetUuidMemberUuidForImages;
	}

	public void setRefsetUuidMemberUuidForImages(
			List<UUID> refsetUuidMemberUuidForImages) {
		this.refsetUuidMemberUuidForImages = refsetUuidMemberUuidForImages;
	}

	public List<UUID> getRefsetUuidMemberUuidForRefsetMembers() {
		return refsetUuidMemberUuidForRefsetMembers;
	}

	public void setRefsetUuidMemberUuidForRefsetMembers(
			List<UUID> refsetUuidMemberUuidForRefsetMembers) {
		this.refsetUuidMemberUuidForRefsetMembers = refsetUuidMemberUuidForRefsetMembers;
	}

	public UUID getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(UUID primordialUuid) {
		this.primordialUuid = primordialUuid;
	}
}
