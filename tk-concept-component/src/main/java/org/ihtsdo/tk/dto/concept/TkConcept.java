/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.dto.concept;

//~--- non-JDK imports --------------------------------------------------------
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfBytearrayMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefexUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Class TkConcept contains methods for importing/exporting a concept.
 * TkConcept was originally named eConcept which stood for "external concept".
 * When the classes where moved out of ace-api to the tk-concept-component, they
 * where renamed to be TK classes (toolkit) classes, but they still implement
 * the eConcept format. <p> The eConcept format is a "data" format, not an
 * "object-serialization" format. The Java object serialization format was found
 * to be to slow and memory intensive for the import and export of these
 * concepts.<p> For the TK concepts class, each component consists of a set of
 * revisions. These revisions each represent a version.
 */
public class TkConcept {

    /**
     * The padding to use in the string representation of this TK Concept.
     */
    public static final String PADDING = "     ";
    /**
     * The dataVersion of this class. Increment if adding additional
     * functionality.
     */
    public static final int dataVersion = 9;
    /**
     * The Constant serialVersionUID, used to prevent the class from computing its
     * own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The boolean value indicating if the concept is an annotation style refex.
     */
    public boolean annotationStyleRefex = false;
    /**
     * The boolean value indicating if the concept is an indexed annotation
     * style refex.
     */
    public boolean annotationIndexStyleRefex = false;
    /**
     * The concept attributes of this concept.
     */
    public TkConceptAttributes conceptAttributes;
    /**
     * The descriptions on this concept.
     */
    public List<TkDescription> descriptions;
    /**
     * The media associated with this concept.
     */
    public List<TkMedia> media;
    /**
     * The primordial uuid of this concept.
     */
    public UUID primordialUuid;
    /**
     * The refset members associated with this concept.
     */
    public List<TkRefexAbstractMember<?>> refsetMembers;
    /**
     * The relationships on this concept.
     */
    public List<TkRelationship> relationships;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Concept.
     */
    public TkConcept() {
        super();
    }

    /**
     * Instantiates a new TK Concept based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkConcept(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    /**
     * Instantiates a new TK Concept based on the specified
     * <code>conceptChronicle</code>.
     *
     * @param conceptChronicle the concept chronicle specifying how to construct
     * the new TK Concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkConcept(ConceptChronicleBI conceptChronicle) throws IOException {
        annotationStyleRefex = conceptChronicle.isAnnotationStyleRefex();
        annotationIndexStyleRefex = conceptChronicle.isAnnotationIndex();
        conceptAttributes = new TkConceptAttributes(conceptChronicle.getConceptAttributes());
        primordialUuid = conceptAttributes.primordialUuid;
        relationships = new ArrayList<TkRelationship>(conceptChronicle.getRelationshipsOutgoing().size());

        for (RelationshipChronicleBI rel : conceptChronicle.getRelationshipsOutgoing()) {
            relationships.add(new TkRelationship(rel));
        }

        descriptions = new ArrayList<TkDescription>(conceptChronicle.getDescriptions().size());

        for (DescriptionChronicleBI desc : conceptChronicle.getDescriptions()) {
            descriptions.add(new TkDescription(desc));
        }

        media = new ArrayList<TkMedia>(conceptChronicle.getMedia().size());

        for (MediaChronicleBI mediaChronicle : conceptChronicle.getMedia()) {
            TkMedia tkMedia = new TkMedia(mediaChronicle);
            media.add(tkMedia);
        }

        if (!conceptChronicle.isAnnotationStyleRefex()) {
            Collection<? extends RefexChronicleBI> members = conceptChronicle.getRefsetMembers();

            if (members != null) {
                refsetMembers = new ArrayList<TkRefexAbstractMember<?>>(members.size());

                for (RefexChronicleBI m : members) {
                    TkRefexAbstractMember<?> member = convertRefex(m);

                    if (member != null) {
                        refsetMembers.add(member);
                    } else {
                        throw new IOException("Could not convert refset member: " + m + "\nfrom refset: " + conceptChronicle);
                    }
                }
            }
        }
    }

    /**
     * Converts refex from a RefexVersionBI object, using native identifiers, to
     * a tk refex object, using uuids.
     *
     * @param refexChronicle the refex chronicle to convert
     * @return an abstract tk refex member
     * @throws IOException signals that an I/O exception has occurred
     */
    public static TkRefexAbstractMember<?> convertRefex(RefexChronicleBI<?> refexChronicle) throws IOException {
        if (refexChronicle.getPrimordialVersion() instanceof RefexNidNidNidVersionBI) {
            return new TkRefexUuidUuidUuidMember((RefexNidNidNidVersionBI) refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidNidStringVersionBI) {
            return new TkRefexUuidUuidStringMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidNidVersionBI) {
            return new TkRefexUuidUuidMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidFloatVersionBI) {
            return new TkRefexUuidFloatMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidIntVersionBI) {
            return new TkRefexUuidIntMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidLongVersionBI) {
            return new TkRefexUuidLongMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidStringVersionBI) {
            return new TkRefexUuidStringMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexNidVersionBI) {
            return new TkRefexUuidMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexIntVersionBI) {
            return new TkRefexIntMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexStringVersionBI) {
            return new TkRefsetStrMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexLongVersionBI) {
            return new TkRefexLongMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexBooleanVersionBI) {
            return new TkRefexBooleanMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexArrayOfBytearrayVersionBI) {
            return new TkRefexArrayOfBytearrayMember(refexChronicle);
        } else if (refexChronicle.getPrimordialVersion() instanceof RefexMemberVersionBI) {
            return new TkRefexMember(refexChronicle);
        } else {
            throw new UnsupportedOperationException("Cannot handle: " + refexChronicle);
        }
    }

    /**
     * Instantiates a new TK Concept based on
     * <code>another</code> TK Concept and allows for uuid conversion.
     *
     * @param another the TK Concept specifying how to construct this TK Concept
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept based on the conversion map
     */
    public TkConcept(TkConcept another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super();
        this.annotationStyleRefex = another.annotationStyleRefex;
        this.annotationIndexStyleRefex = another.annotationIndexStyleRefex;

        if (another.conceptAttributes != null) {
            this.conceptAttributes = another.conceptAttributes.makeConversion(conversionMap, offset, mapAll);
        }

        if (another.descriptions != null) {
            this.descriptions = new ArrayList<TkDescription>(another.descriptions.size());

            for (TkDescription d : another.descriptions) {
                this.descriptions.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }

        if (another.media != null) {
            this.media = new ArrayList<TkMedia>(another.media.size());

            for (TkMedia d : another.media) {
                this.media.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }

        this.primordialUuid = conversionMap.get(another.primordialUuid);

        if (another.refsetMembers != null) {
            this.refsetMembers = new ArrayList<TkRefexAbstractMember<?>>(another.refsetMembers.size());

            for (TkRefexAbstractMember<?> d : another.refsetMembers) {
                this.refsetMembers.add((TkRefexAbstractMember<?>) d.makeConversion(conversionMap, offset,
                        mapAll));
            }
        }

        if (another.relationships != null) {
            this.relationships = new ArrayList<TkRelationship>(another.relationships.size());

            for (TkRelationship d : another.relationships) {
                this.relationships.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }
    }

    /**
     * Instantiates a new TK Concept based on the given
     * <code>conceptVersion</code> and allows for uuid conversion. Uses one view
     * coordinate to find versions of all components. Can exclude components
     * based on their nid.
     *
     * @param conceptVersion the concept version specifying how to construct
     * this TK Concept
     * @param excludedNids the nids in the specified concept version to exclude
     * from this TK Concept
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept based on the conversion map
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
     */
    public TkConcept(ConceptVersionBI conceptVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        this(conceptVersion, excludedNids, conversionMap,
                offset, mapAll, viewCoordinate, viewCoordinate, viewCoordinate);
    }

    /**
     * Instantiates a new TK Concept based on the given
     * <code>conceptVersion</code> and allows for uuid conversion. Can specify
     * view coordinates for the concept, descriptions, and relationships
     *
     * @param conceptVersion the concept version specifying how to construct
     * this TK Concept
     * @param excludedNids the excluded nids
     * @param conversionMap the nids in the specified concept version to exclude
     * from this TK Concept
     * @param offset the offset to be applied to the time associated with this
     * TK Concept
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept based on the conversion map
     * @param conceptVc the view coordinate specifying which version of the
     * concept attributes to use
     * @param descVc the view coordinate specifying which version of the
     * descriptions to use
     * @param relVc the view coordinate specifying which version of the
     * relationships to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
     */
    public TkConcept(ConceptVersionBI conceptVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll,
            ViewCoordinate conceptVc,
            ViewCoordinate descVc,
            ViewCoordinate relVc)
            throws IOException, ContradictionException {
        super();
        this.primordialUuid = conversionMap.get(conceptVersion.getPrimUuid());
        this.annotationStyleRefex = conceptVersion.isAnnotationStyleRefex();
        this.annotationIndexStyleRefex = conceptVersion.isAnnotationIndex();
        this.conceptAttributes = new TkConceptAttributes(conceptVersion.getConceptAttributesActive(), excludedNids,
                conversionMap, offset, mapAll, conceptVc);

        Collection<? extends DescriptionVersionBI> activeDescriptions =
                conceptVersion.getChronicle().getVersion(descVc).getDescriptionsActive();

        if (activeDescriptions != null) {
            this.descriptions = new ArrayList<TkDescription>(activeDescriptions.size());
            nextDescription:
            for (DescriptionVersionBI d : activeDescriptions) {
                for (int nid : d.getAllNidsForVersion()) {
                    if (excludedNids.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextDescription;
                    } else if (Ts.get().getComponent(nid).getVersions(descVc).isEmpty()) {
                        continue nextDescription;
                    }
                }

                this.descriptions.add(new TkDescription(d, excludedNids, conversionMap, offset, mapAll, descVc));
            }
        }

        Collection<? extends MediaVersionBI> activeMedia = conceptVersion.getMediaActive();

        if (activeMedia != null) {
            this.media = new ArrayList<TkMedia>(activeMedia.size());
            nextMedia:
            for (MediaVersionBI m : activeMedia) {
                for (int nid : m.getAllNidsForVersion()) {
                    if (excludedNids.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextMedia;
                    } else if (Ts.get().getComponent(nid).getVersions(conceptVc).isEmpty()) {
                        continue nextMedia;
                    }
                }

                this.media.add(new TkMedia(m, excludedNids, conversionMap, offset, mapAll, conceptVc));
            }
        }

        Collection<? extends RefexVersionBI<?>> activeRefsetMembers = conceptVersion.getRefsetMembersActive();

        if (activeRefsetMembers != null) {
            this.refsetMembers = new ArrayList<TkRefexAbstractMember<?>>(activeRefsetMembers.size());
            nextRefsetMember:
            for (RefexVersionBI rxm : activeRefsetMembers) {
                for (int nid : rxm.getAllNidsForVersion()) {
                    if (excludedNids.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextRefsetMember;
                    } else if (Ts.get().getComponent(nid).getVersions(conceptVc).isEmpty()) {
                        continue nextRefsetMember;
                    }
                }

                this.refsetMembers.add(rxm.getTkRefsetMemberActiveOnly(conceptVc, excludedNids, conversionMap));
            }
        }

        Collection<? extends RelationshipVersionBI> rels = conceptVersion.getChronicle().getVersion(relVc).getRelationshipsOutgoingActive();

        if (rels != null) {
            this.relationships = new ArrayList<TkRelationship>(rels.size());
            nextRel:
            for (RelationshipVersionBI rel : rels) {
                int destNid = rel.getTargetNid();
                for (int nid : rel.getAllNidsForVersion()) {
                    if (excludedNids.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextRel;
                    } else if (Ts.get().getComponent(nid).getVersions(relVc).isEmpty()) {
                        if (nid == destNid) {
                            if (Ts.get().getComponent(destNid).getVersions(conceptVc).isEmpty()) {
                                continue nextRel;
                            }
                        } else {
                            continue nextRel;
                        }
                    }
                }
                this.relationships.add(new TkRelationship(rel, excludedNids, conversionMap, offset, mapAll, relVc));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a <tt>EConcept</tt>
     * object, and contains the same values, field by field, as this
     * <tt>EConcept</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful <code>true</code> if the objects
     * are the same; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkConcept.class.isAssignableFrom(obj.getClass())) {
            TkConcept another = (TkConcept) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare ConceptAttributes
            if (this.conceptAttributes == null) {
                if (this.conceptAttributes != another.conceptAttributes) {
                    return false;
                }
            } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
                return false;
            }

            // Compare Descriptions
            if (this.descriptions == null) {
                if (another.descriptions == null) {              // Equal!
                } else if (another.descriptions.isEmpty()) {     // Equal!
                } else {
                    return false;
                }
            } else if (!this.descriptions.equals(another.descriptions)) {
                return false;
            }

            // Compare Relationships
            if (this.relationships == null) {
                if (another.relationships == null) {             // Equal!
                } else if (another.relationships.isEmpty()) {    // Equal!
                } else {
                    return false;
                }
            } else if (!this.relationships.equals(another.relationships)) {
                return false;
            }

            // Compare Images
            if (this.media == null) {
                if (another.media == null) {                     // Equal!
                } else if (another.media.isEmpty()) {            // Equal!
                } else {
                    return false;
                }
            } else if (!this.media.equals(another.media)) {
                return false;
            }

            // Compare Refset Members
            if (this.refsetMembers == null) {
                if (another.refsetMembers == null) {             // Equal!
                } else if (another.refsetMembers.isEmpty()) {    // Equal!
                } else {
                    return false;
                }
            } else if (!this.refsetMembers.equals(another.refsetMembers)) {
                return false;
            }

            // If none of the previous comparisons fail, the objects must be equal
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EConcept</code>.
     *
     * @return a hash code value for this <tt>EConcept</tt>.
     */
    @Override
    public int hashCode() {
        return this.conceptAttributes.primordialUuid.hashCode();
    }

    /**
     * Reads a TK Concept from an external source.
     *
     * @param in the data input specifying the TK Concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public final void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int readDataVersion = in.readInt();

        if (readDataVersion > dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion);
        }

        if (readDataVersion == 1) {
            conceptAttributes = new TkConceptAttributes(in, readDataVersion);
            primordialUuid = conceptAttributes.primordialUuid;
        } else {
            primordialUuid = new UUID(in.readLong(), in.readLong());

            int attributeCount = in.readByte();

            if (attributeCount == 1) {
                conceptAttributes = new TkConceptAttributes(in, readDataVersion);
            }
        }

        int descCount = in.readInt();

        if (descCount > 0) {
            descriptions = new ArrayList<TkDescription>(descCount);

            for (int i = 0; i < descCount; i++) {
                descriptions.add(new TkDescription(in, readDataVersion));
            }
        }

        int relCount = in.readInt();

        if (relCount > 0) {
            relationships = new ArrayList<TkRelationship>(relCount);

            for (int i = 0; i < relCount; i++) {
                relationships.add(new TkRelationship(in, readDataVersion));
            }
        }

        int imgCount = in.readInt();

        if (imgCount > 0) {
            media = new ArrayList<TkMedia>(imgCount);

            for (int i = 0; i < imgCount; i++) {
                media.add(new TkMedia(in, readDataVersion));
            }
        }

        int refsetMemberCount = in.readInt();

        if (refsetMemberCount > 0) {
            refsetMembers = new ArrayList<TkRefexAbstractMember<?>>(refsetMemberCount);

            for (int i = 0; i < refsetMemberCount; i++) {
                TK_REFEX_TYPE type = TK_REFEX_TYPE.readType(in);

                switch (type) {
                    case CID:
                        refsetMembers.add(new TkRefexUuidMember(in, readDataVersion));

                        break;

                    case CID_CID:
                        refsetMembers.add(new TkRefexUuidUuidMember(in, readDataVersion));

                        break;

                    case MEMBER:
                        refsetMembers.add(new TkRefexMember(in, readDataVersion));

                        break;

                    case CID_CID_CID:
                        refsetMembers.add(new TkRefexUuidUuidUuidMember(in, readDataVersion));

                        break;

                    case CID_CID_STR:
                        refsetMembers.add(new TkRefexUuidUuidStringMember(in, readDataVersion));

                        break;

                    case INT:
                        refsetMembers.add(new TkRefexIntMember(in, readDataVersion));

                        break;

                    case STR:
                        refsetMembers.add(new TkRefsetStrMember(in, readDataVersion));

                        break;
                        
                    case STR_STR:
                        refsetMembers.add(new TkRefsetStrStrMember(in, readDataVersion));

                        break;

                    case CID_INT:
                        refsetMembers.add(new TkRefexUuidIntMember(in, readDataVersion));

                        break;

                    case BOOLEAN:
                        refsetMembers.add(new TkRefexBooleanMember(in, readDataVersion));

                        break;

                    case CID_FLOAT:
                        refsetMembers.add(new TkRefexUuidFloatMember(in, readDataVersion));

                        break;

                    case CID_LONG:
                        refsetMembers.add(new TkRefexUuidLongMember(in, readDataVersion));

                        break;

                    case CID_STR:
                        refsetMembers.add(new TkRefexUuidStringMember(in, readDataVersion));

                        break;

                    case LONG:
                        refsetMembers.add(new TkRefexLongMember(in, readDataVersion));

                        break;
                    case ARRAY_BYTEARRAY:
                        refsetMembers.add(new TkRefexArrayOfBytearrayMember(in, dataVersion));
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }

        if (readDataVersion < 4) {
            in.readInt();    // destRelNidTypeNidsCount
            in.readInt();    // refsetUuidMemberUuidForConceptCount
            in.readInt();    // refsetUuidMemberUuidForDescsCount
            in.readInt();    // refsetUuidMemberUuidForRelsCount
            in.readInt();    // refsetUuidMemberUuidForImagesCount
            in.readInt();    // refsetUuidMemberUuidForRefsetMembersCount
        }

        if (readDataVersion >= 5) {
            annotationStyleRefex = in.readBoolean();
        } else {
            annotationStyleRefex = false;
        }

        if (readDataVersion >= 9) {
            annotationIndexStyleRefex = in.readBoolean();
        } else {
            annotationIndexStyleRefex = false;
        }
    }

    /**
     * Returns a string representation of this TK Concept object.
     *
     * @return a string representation of this TK Concept object and all of its
     * components
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName());
        buff.append(": \n   primordial UUID: ");
        buff.append(TkRevision.informAboutUuid(this.primordialUuid));
        buff.append("\n   ConceptAttributes: \n");
        buff.append(PADDING);
        buff.append(": \n   annotation refex: ");
        buff.append(this.annotationStyleRefex);
        buff.append(": \n   indexed annotation: ");
        buff.append(this.annotationIndexStyleRefex);
        if (this.conceptAttributes == null) {
            buff.append(PADDING + "none\n");
        } else {
            buff.append(this.conceptAttributes);
            buff.append("\n");
        }

        buff.append("\n   Descriptions: \n");

        if (this.descriptions == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkDescription d : this.descriptions) {
                buff.append(PADDING);
                buff.append(d);
                buff.append("\n");
            }
        }

        buff.append("\n   Relationships: \n");

        if (this.relationships == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkRelationship r : this.relationships) {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            }
        }

        buff.append("\n   RefsetMembers: \n");

        if (this.refsetMembers == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkRefexAbstractMember<?> r : this.refsetMembers) {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            }
        }

        buff.append("\n   Media: \n");

        if (this.media == null) {
            buff.append(PADDING + "none");
        } else {
            for (TkMedia m : this.media) {
                buff.append(PADDING);
                buff.append(m);
                buff.append("\n");
            }
        }

        return buff.toString();
    }

    /**
     * Writes this TK Concept to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
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

            for (TkDescription d : descriptions) {
                d.writeExternal(out);
            }
        }

        if (relationships == null) {
            out.writeInt(0);
        } else {
            out.writeInt(relationships.size());

            for (TkRelationship r : relationships) {
                r.writeExternal(out);
            }
        }

        if (media == null) {
            out.writeInt(0);
        } else {
            out.writeInt(media.size());

            for (TkMedia img : media) {
                img.writeExternal(out);
            }
        }

        if (refsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembers.size());

            for (TkRefexAbstractMember<?> r : refsetMembers) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }

        out.writeBoolean(annotationStyleRefex);
        out.writeBoolean(annotationIndexStyleRefex);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept attributes associated with this TK Concept.
     *
     * @return the TK Concept attributes associated with this TK Concept
     */
    public TkConceptAttributes getConceptAttributes() {
        return conceptAttributes;
    }

    /**
     * Gets the descriptions associated with this TK Concept.
     *
     * @return the TK descriptions associated with this TK Concept
     */
    public List<TkDescription> getDescriptions() {
        return descriptions;
    }

    /**
     * Gets the media associated with this TK Concept.
     *
     * @return the TK media associated with this TK Concept
     */
    public List<TkMedia> getImages() {
        return media;
    }

    /**
     * Gets the primordial uuid of this TK Concept.
     *
     * @return the primordial uuid
     */
    public UUID getPrimordialUuid() {
        return primordialUuid;
    }

    /**
     * Gets the refset members associated with this TK Concept. These are the
     * refset members, not annotations.
     *
     * @return the TK refset members associated with this TK Concept
     */
    public List<TkRefexAbstractMember<?>> getRefsetMembers() {
        return refsetMembers;
    }

    /**
     * Gets the relationships associated with this TK Concept.
     *
     * @return the relationships
     */
    public List<TkRelationship> getRelationships() {
        return relationships;
    }

    /**
     * Checks if this TK Concept is an annotation style refex.
     *
     * @return <code>true</code>, if this TK Concept is an annotation style
     * refex
     */
    public boolean isAnnotationStyleRefex() {
        return annotationStyleRefex;
    }

    /**
     * Checks if this TK Concept is an indexed annotation style refex.
     *
     * @return <code>true</code>, if this TK Concept is an indexed annotation
     * style refex
     */
    public boolean isAnnotationIndexStyleRefex() {
        return annotationIndexStyleRefex;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Marks this TK Concept as an annotation style refex.
     *
     * @param annotationStyleRefex set to <code>true</code> to mark as
     * annotation style refex
     */
    public void setAnnotationStyleRefex(boolean annotationStyleRefex) {
        this.annotationStyleRefex = annotationStyleRefex;
    }

    /**
     * Marks this TK Concept as an indexed annotation style refex.
     *
     *
     * @param annotationIndexStyleRefex set to <code>true</code> to mark as
     * indexed annotation style refex
     */
    public void setAnnotationIndexStyleRefex(boolean annotationIndexStyleRefex) {
        this.annotationIndexStyleRefex = annotationIndexStyleRefex;
    }

    /**
     * Sets the concept attributes associated with this TK Concept.
     *
     * @param conceptAttributes the TK Concept attributes for this TK Concept
     */
    public void setConceptAttributes(TkConceptAttributes conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    /**
     * Sets the descriptions associated with this TK Concept.
     *
     * @param descriptions the TK descriptions for this TK Concept
     */
    public void setDescriptions(List<TkDescription> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Sets the media associated with this TK Concept.
     *
     * @param images the TK media for this TK Concept
     */
    public void setImages(List<TkMedia> images) {
        this.media = images;
    }

    /**
     * Sets the primordial uuid of this TK Concept.
     *
     * @param primordialUuid the primordial uuid
     */
    public void setPrimordialUuid(UUID primordialUuid) {
        this.primordialUuid = primordialUuid;
    }

    /**
     * Sets the refset members associated with this TK Concept. This is refset
     * members, not annotations.
     *
     * @param refsetMembers the TK refset members for this TK Concept
     */
    public void setRefsetMembers(List<TkRefexAbstractMember<?>> refsetMembers) {
        this.refsetMembers = refsetMembers;
    }

    /**
     * Sets the relationships on this TK Concept.
     *
     * @param relationships the TK relationships for this TK Concept
     */
    public void setRelationships(List<TkRelationship> relationships) {
        this.relationships = relationships;
    }

    public boolean hasSnomedIsa() {
        if (this.relationships != null) {
            for (TkRelationship tkr : this.relationships) {
                for (UUID isaUuid : Snomed.IS_A.getUuids()) {
                    if (tkr.typeUuid.compareTo(isaUuid) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
