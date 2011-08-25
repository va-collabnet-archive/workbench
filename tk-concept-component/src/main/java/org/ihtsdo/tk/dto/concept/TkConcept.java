package org.ihtsdo.tk.dto.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TkConcept {
   public static final String PADDING          = "     ";
   public static final int    dataVersion      = 7;
   public static final long   serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public boolean                         annotationStyleRefex = false;
   public TkConceptAttributes             conceptAttributes;
   public List<TkDescription>             descriptions;
   public List<TkMedia>                   media;
   public UUID                            primordialUuid;
   public List<TkRefsetAbstractMember<?>> refsetMembers;
   public List<TkRelationship>            relationships;

   //~--- constructors --------------------------------------------------------

   public TkConcept() {
      super();
   }

   public TkConcept(DataInput in) throws IOException, ClassNotFoundException {
      super();
      readExternal(in);
   }

   //~--- methods -------------------------------------------------------------

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
    * Returns a hash code for this <code>EConcept</code>.
    *
    * @return a hash code value for this <tt>EConcept</tt>.
    */
   @Override
   public int hashCode() {
      return this.conceptAttributes.primordialUuid.hashCode();
   }

   public final void readExternal(DataInput in) throws IOException, ClassNotFoundException {
      int readDataVersion = in.readInt();

      if (readDataVersion > dataVersion) {
         throw new IOException("Unsupported dataVersion: " + readDataVersion);
      }

      if (readDataVersion == 1) {
         conceptAttributes = new TkConceptAttributes(in, readDataVersion);
         primordialUuid    = conceptAttributes.primordialUuid;
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
         refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(refsetMemberCount);

         for (int i = 0; i < refsetMemberCount; i++) {
            TK_REFSET_TYPE type = TK_REFSET_TYPE.readType(in);

            switch (type) {
            case CID :
               refsetMembers.add(new TkRefsetCidMember(in, readDataVersion));

               break;

            case CID_CID :
               refsetMembers.add(new TkRefsetCidCidMember(in, readDataVersion));

               break;

            case MEMBER :
               refsetMembers.add(new TkRefsetMember(in, readDataVersion));

               break;

            case CID_CID_CID :
               refsetMembers.add(new TkRefsetCidCidCidMember(in, readDataVersion));

               break;

            case CID_CID_STR :
               refsetMembers.add(new TkRefsetCidCidStrMember(in, readDataVersion));

               break;

            case INT :
               refsetMembers.add(new TkRefsetIntMember(in, readDataVersion));

               break;

            case STR :
               refsetMembers.add(new TkRefsetStrMember(in, readDataVersion));

               break;

            case CID_INT :
               refsetMembers.add(new TkRefsetCidIntMember(in, readDataVersion));

               break;

            case BOOLEAN :
               refsetMembers.add(new TkRefsetBooleanMember(in, readDataVersion));

               break;

            case CID_FLOAT :
               refsetMembers.add(new TkRefsetCidFloatMember(in, readDataVersion));

               break;

            case CID_LONG :
               refsetMembers.add(new TkRefsetCidLongMember(in, readDataVersion));

               break;

            case CID_STR :
               refsetMembers.add(new TkRefsetCidStrMember(in, readDataVersion));

               break;

            case LONG :
               refsetMembers.add(new TkRefsetLongMember(in, readDataVersion));

               break;

            default :
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
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName());
      buff.append(": \n   primordial UUID: ");
      buff.append(TkRevision.informAboutUuid(this.primordialUuid));
      buff.append("\n   ConceptAttributes: \n");
      buff.append(PADDING);

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
         for (TkRefsetAbstractMember<?> r : this.refsetMembers) {
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

         for (TkRefsetAbstractMember<?> r : refsetMembers) {
            r.getType().writeType(out);
            r.writeExternal(out);
         }
      }

      out.writeBoolean(annotationStyleRefex);
   }

   //~--- get methods ---------------------------------------------------------

   public TkConceptAttributes getConceptAttributes() {
      return conceptAttributes;
   }

   public List<TkDescription> getDescriptions() {
      return descriptions;
   }

   public List<TkMedia> getImages() {
      return media;
   }

   public UUID getPrimordialUuid() {
      return primordialUuid;
   }

   public List<TkRefsetAbstractMember<?>> getRefsetMembers() {
      return refsetMembers;
   }

   public List<TkRelationship> getRelationships() {
      return relationships;
   }

   public boolean isAnnotationStyleRefex() {
      return annotationStyleRefex;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAnnotationStyleRefex(boolean annotationStyleRefex) {
      this.annotationStyleRefex = annotationStyleRefex;
   }

   public void setConceptAttributes(TkConceptAttributes conceptAttributes) {
      this.conceptAttributes = conceptAttributes;
   }

   public void setDescriptions(List<TkDescription> descriptions) {
      this.descriptions = descriptions;
   }

   public void setImages(List<TkMedia> images) {
      this.media = images;
   }

   public void setPrimordialUuid(UUID primordialUuid) {
      this.primordialUuid = primordialUuid;
   }

   public void setRefsetMembers(List<TkRefsetAbstractMember<?>> refsetMembers) {
      this.refsetMembers = refsetMembers;
   }

   public void setRelationships(List<TkRelationship> relationships) {
      this.relationships = relationships;
   }
}
