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
import org.dwfa.tapi.TerminologyException;

public class EConcept implements I_AmChangeSetObject {
    public static final long serialVersionUID = 1;

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
                        type.typeNid = EComponent.uuidToNid(type.typeConcept.getUids());
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

    protected static final int dataVersion = 3;
    protected EConceptAttributes conceptAttributes;
    protected List<EDescription> descriptions;
    protected List<ERelationship> relationships;
    protected List<EImage> images;
    protected List<ERefsetMember<?>> refsetMembers;
    protected List<UUID> destRelUuidTypeUuids;
    protected List<UUID> refsetUuidMemberUuidForConcept;
    protected List<UUID> refsetUuidMemberUuidForDescriptions;
    protected List<UUID> refsetUuidMemberUuidForRels;
    protected List<UUID> refsetUuidMemberUuidForImages;
    protected List<UUID> refsetUuidMemberUuidForRefsetMembers;
    protected UUID primordialUuid;
    
    public EConcept(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int readDataVersion = in.readInt();
        if (readDataVersion > dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion);
        }
        if (readDataVersion == 1) {
            conceptAttributes = new EConceptAttributes(in, readDataVersion);
            primordialUuid = conceptAttributes.primordialUuid;
        } else {
        	primordialUuid = new UUID(in.readLong(), in.readLong());
        	int attributeCount = in.readByte();
        	if (attributeCount == 1) {
                conceptAttributes = new EConceptAttributes(in, readDataVersion);
        	}
        }
        int descCount = in.readInt();
        if (descCount > 0) {
            descriptions = new ArrayList<EDescription>(descCount);
            for (int i = 0; i < descCount; i++) {
                descriptions.add(new EDescription(in, readDataVersion));
            }
        }
        int relCount = in.readInt();
        if (relCount > 0) {
            relationships = new ArrayList<ERelationship>(relCount);
            for (int i = 0; i < relCount; i++) {
                relationships.add(new ERelationship(in, readDataVersion));
            }
        }
        int imgCount = in.readInt();
        if (imgCount > 0) {
            images = new ArrayList<EImage>(imgCount);
            for (int i = 0; i < imgCount; i++) {
                images.add(new EImage(in, readDataVersion));
            }
        }
        int refsetMemberCount = in.readInt();
        if (refsetMemberCount > 0) {
            refsetMembers = new ArrayList<ERefsetMember<?>>(refsetMemberCount);
            for (int i = 0; i < refsetMemberCount; i++) {
                REFSET_TYPES type = REFSET_TYPES.readType(in);
                switch (type) {
                case CID:
                    refsetMembers.add(new ERefsetCidMember(in, readDataVersion));
                    break;
                case CID_CID:
                    refsetMembers.add(new ERefsetCidCidMember(in, readDataVersion));
                    break;
                case MEMBER:
                    refsetMembers.add(new ERefsetMemberMember(in, readDataVersion));
                    break;
                case CID_CID_CID:
                    refsetMembers.add(new ERefsetCidCidCidMember(in, readDataVersion));
                    break;
                case CID_CID_STR:
                    refsetMembers.add(new ERefsetCidCidStrMember(in, readDataVersion));
                    break;
                case INT:
                    refsetMembers.add(new ERefsetIntMember(in, readDataVersion));
                    break;
                case STR:
                    refsetMembers.add(new ERefsetStrMember(in, readDataVersion));
                    break;
                case CID_INT:
                    refsetMembers.add(new ERefsetCidIntMember(in, readDataVersion));
                    break;
                case BOOLEAN:
                    refsetMembers.add(new ERefsetBooleanMember(in, readDataVersion));
                    break;
                case CID_FLOAT:
                    refsetMembers.add(new ERefsetCidFloatMember(in, readDataVersion));
                    break;
                case CID_LONG:
                    refsetMembers.add(new ERefsetCidLongMember(in, readDataVersion));
                    break;
                case CID_STR:
                    refsetMembers.add(new ERefsetCidStrMember(in, readDataVersion));
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }
        int destRelNidTypeNidsCount = in.readInt();
        if (destRelNidTypeNidsCount > 0) {
        	destRelUuidTypeUuids = new ArrayList<UUID>(
        			destRelNidTypeNidsCount);
        	for (int i = 0; i < destRelNidTypeNidsCount; i++) {
        		destRelUuidTypeUuids.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForConceptCount = in.readInt();
        if (refsetUuidMemberUuidForConceptCount > 0) {
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(
        			refsetUuidMemberUuidForConceptCount);
        	for (int i = 0; i < refsetUuidMemberUuidForConceptCount; i++) {
        		refsetUuidMemberUuidForConcept.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForDescsCount = in.readInt();
        if (refsetUuidMemberUuidForDescsCount > 0) {
        	refsetUuidMemberUuidForDescriptions = new ArrayList<UUID>(
        			refsetUuidMemberUuidForDescsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForDescsCount; i++) {
        		refsetUuidMemberUuidForDescriptions.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForRelsCount = in.readInt();
        if (refsetUuidMemberUuidForRelsCount > 0) {
        	refsetUuidMemberUuidForRels = new ArrayList<UUID>(
        			refsetUuidMemberUuidForRelsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForRelsCount; i++) {
        		refsetUuidMemberUuidForRels.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForImagesCount = in.readInt();
        if (refsetUuidMemberUuidForImagesCount > 0) {
        	refsetUuidMemberUuidForImages = new ArrayList<UUID>(
        			refsetUuidMemberUuidForImagesCount);
        	for (int i = 0; i < refsetUuidMemberUuidForImagesCount; i++) {
        		refsetUuidMemberUuidForImages.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForRefsetMembersCount = in.readInt();
        if (refsetUuidMemberUuidForRefsetMembersCount > 0) {
        	refsetUuidMemberUuidForRefsetMembers = new ArrayList<UUID>(
        			refsetUuidMemberUuidForRefsetMembersCount);
        	for (int i = 0; i < refsetUuidMemberUuidForRefsetMembersCount; i++) {
        		refsetUuidMemberUuidForRefsetMembers.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(dataVersion);
        if (primordialUuid == null) {
        	primordialUuid = conceptAttributes.primordialUuid;
        }
    	out.writeLong(primordialUuid.getMostSignificantBits());
    	out.writeLong(primordialUuid.getLeastSignificantBits());
    	if (conceptAttributes == null) {
    		out.writeByte(0);
    	} else {
    		out.writeByte(1);
            conceptAttributes.writeExternal(out);
    	}
        if (descriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(descriptions.size());
            for (EDescription d : descriptions) {
                d.writeExternal(out);
            }
        }
        if (relationships == null) {
            out.writeInt(0);
        } else {
            out.writeInt(relationships.size());
            for (ERelationship r : relationships) {
                r.writeExternal(out);
            }
        }
        if (images == null) {
            out.writeInt(0);
        } else {
            out.writeInt(images.size());
            for (EImage img : images) {
                img.writeExternal(out);
            }
        }
        if (refsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembers.size());
            for (ERefsetMember<?> r : refsetMembers) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }
        if (destRelUuidTypeUuids == null) {
            out.writeInt(0);
        } else {
            out.writeInt(destRelUuidTypeUuids.size());
            assert destRelUuidTypeUuids.size() % 2 == 0: 
            	"Illegal size: " + destRelUuidTypeUuids.size();
            for (UUID uuid : destRelUuidTypeUuids) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForConcept == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForConcept.size());
            assert refsetUuidMemberUuidForConcept.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForConcept.size();
            for (UUID uuid : refsetUuidMemberUuidForConcept) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForDescriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForDescriptions.size());
            assert refsetUuidMemberUuidForDescriptions.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForDescriptions.size();
            for (UUID uuid : refsetUuidMemberUuidForDescriptions) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForRels == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForRels.size());
            assert refsetUuidMemberUuidForRels.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForRels.size();
            for (UUID uuid : refsetUuidMemberUuidForRels) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForImages == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForImages.size());
            assert refsetUuidMemberUuidForImages.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForImages.size();
            for (UUID uuid : refsetUuidMemberUuidForImages) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForRefsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForRefsetMembers.size());
            assert refsetUuidMemberUuidForRefsetMembers.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForRefsetMembers.size();
            for (UUID uuid : refsetUuidMemberUuidForRefsetMembers) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
    }

    public List<EDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<EDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public List<ERelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<ERelationship> relationships) {
        this.relationships = relationships;
    }

    public List<ERefsetMember<?>> getRefsetMembers() {
        return refsetMembers;
    }

    public void setRefsetMembers(List<ERefsetMember<?>> refsetMembers) {
        this.refsetMembers = refsetMembers;
    }
    
    public EConceptAttributes getConceptAttributes() {
        return conceptAttributes;
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
        conceptAttributes.convert(c.getIdentifier());
        relationships = new ArrayList<ERelationship>(c.getSourceRels().size());
        for (I_RelVersioned rel : c.getSourceRels()) {
            relationships.add(new ERelationship(rel));
        }
        descriptions = new ArrayList<EDescription>(c.getDescriptions().size());
        for (I_DescriptionVersioned desc : c.getDescriptions()) {
            descriptions.add(new EDescription(desc));
        }
        images = new ArrayList<EImage>(c.getImages().size());
        for (I_ImageVersioned img : c.getImages()) {
            EImage eImage = new EImage(img);
            if (eImage.time == Long.MIN_VALUE) {
                eImage.time = this.conceptAttributes.time;
                // Fixup for a data issue.
            }
            images.add(eImage);
        }
        Collection<? extends I_ExtendByRef> members = EComponent.getRefsetMembers(c.getNid());
        if (members != null) {
            refsetMembers = new ArrayList<ERefsetMember<?>>(members.size());
            for (I_ExtendByRef m : members) {
                ERefsetMember<?> member = ERefsetMember.convert(m);
                if (member != null) {
                    refsetMembers.add(member);
                } else {
                    AceLog.getAppLog().severe("Could not convert refset member: " + m + "\nfrom refset: " + c);
                }
            }
        }

        Collection<? extends I_ExtendByRef> conceptMembers = componentRefsetMap.get(c.getNid());
        if (conceptMembers != null) {
        	ArrayList<ERefsetMember<?>> refsetMemberForComponent = new ArrayList<ERefsetMember<?>>(conceptMembers.size());
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(refsetMemberForComponent.size() * 2);
        	for (I_ExtendByRef m : conceptMembers) {
        		assert m.getComponentId() == c.getNid() : 
        			"getRefsetMembersForComponent query error: componentId: " + m.getComponentId() + 
        			" conceptId: " + c.getNid();
                UUID refsetUuid = ERevision.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForConcept.add(refsetUuid);
                UUID memberUuid = ERevision.nidToUuid(m.getNid());
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
                UUID refsetUuid = ERevision.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForDescriptions.add(refsetUuid);
                UUID memberUuid = ERevision.nidToUuid(m.getNid());
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
                UUID refsetUuid = ERevision.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForRels.add(refsetUuid);
                UUID memberUuid = ERevision.nidToUuid(m.getNid());
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
        		UUID refsetUuid = ERevision.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForImages.add(refsetUuid);
                UUID memberUuid = ERevision.nidToUuid(m.getNid());
                refsetUuidMemberUuidForImages.add(memberUuid);
            }
        }
        
        destRelUuidTypeUuids = new ArrayList<UUID>();
        for (I_RelVersioned r: c.getDestRels()) {
            UUID relUuid = ERevision.nidToUuid(r.getNid());
            HashSet<UUID> typesAdded = new HashSet<UUID>();
            for (I_RelPart p: r.getMutableParts()) {
                UUID typeUuid = ERevision.nidToUuid(p.getTypeId());
                if (!typesAdded.contains(typeUuid)) {
                    destRelUuidTypeUuids.add(relUuid);            	
                    destRelUuidTypeUuids.add(typeUuid);
                    typesAdded.add(typeUuid);
                }
            }
        }
    }

    public List<EImage> getImages() {
        return images;
    }

    public void setImages(List<EImage> images) {
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
