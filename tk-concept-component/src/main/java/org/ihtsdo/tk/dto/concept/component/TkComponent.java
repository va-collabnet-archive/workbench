package org.ihtsdo.tk.dto.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;
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

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.concept.component.refset.array.bytearray.TkRefsetArrayOfBytearrayMember;

public abstract class TkComponent<V extends TkRevision> extends TkRevision {
   private static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public List<TkIdentifier>              additionalIds;
   public List<TkRefsetAbstractMember<?>> annotations;
   public UUID                            primordialUuid;
   public List<V>                         revisions;

   //~--- constructors --------------------------------------------------------

   public TkComponent() {
      super();
   }

   public TkComponent(ComponentVersionBI another) throws IOException {
      super(another);

      Collection<? extends IdBI> anotherAdditionalIds = another.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds = new ArrayList<TkIdentifier>(anotherAdditionalIds.size());
         nextId:
         for (IdBI id : anotherAdditionalIds) {
            this.additionalIds.add((TkIdentifier) TkIdentifier.convertId(id));
         }
      }

      Collection<? extends RefexChronicleBI<?>> anotherAnnotations = another.getAnnotations();

      processAnnotations(anotherAnnotations);
      this.primordialUuid = another.getPrimUuid();
   }

   public TkComponent(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkComponent(TkComponent<V> another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (another.additionalIds != null) {
         this.additionalIds = new ArrayList<TkIdentifier>(another.additionalIds.size());

         for (TkIdentifier id : another.additionalIds) {
            this.additionalIds.add((TkIdentifier) id.makeConversion(conversionMap, offset, mapAll));
         }
      }

      if (another.annotations != null) {
         this.annotations = new ArrayList<TkRefsetAbstractMember<?>>(another.annotations.size());

         for (TkRefsetAbstractMember<?> r : another.annotations) {
            this.annotations.add((TkRefsetAbstractMember<?>) r.makeConversion(conversionMap, offset, mapAll));
         }
      }

      this.primordialUuid = conversionMap.get(another.primordialUuid);

      if (another.revisions != null) {
         this.revisions = new ArrayList<V>(another.revisions.size());

         for (V r : another.revisions) {
            this.revisions.add((V) r.makeConversion(conversionMap, offset, mapAll));
         }
      }
   }

   public TkComponent(ComponentVersionBI another, NidBitSetBI exclusions, Map<UUID, UUID> conversionMap,
                      long offset, boolean mapAll, ViewCoordinate vc)
           throws IOException, ContradictionException {
      super(another, conversionMap, offset, mapAll);

      Collection<? extends IdBI> anotherAdditionalIds = another.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds = new ArrayList<TkIdentifier>(anotherAdditionalIds.size());
         nextId:
         for (IdBI id : anotherAdditionalIds) {
            for (int nid : id.getAllNidsForId()) {
               if (exclusions.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                  continue nextId;
               } else if (Ts.get().getComponent(nid).getVersions(vc).isEmpty()) {
                   continue nextId;
               }
            }

            this.additionalIds.add((TkIdentifier) TkIdentifier.convertId(id).makeConversion(conversionMap,
                    offset, mapAll));
         }
      }

      Collection<? extends RefexChronicleBI<?>> anotherAnnotations = another.getAnnotations();

      processAnnotations(anotherAnnotations, vc, exclusions, conversionMap);
      this.primordialUuid = conversionMap.get(another.getPrimUuid());
   }

   //~--- methods -------------------------------------------------------------


   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EComponent</tt> object, and contains the same values, field by field,
    * as this <tt>EComponent</tt>.
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

      if (TkComponent.class.isAssignableFrom(obj.getClass())) {
         TkComponent<?> another = (TkComponent<?>) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare primordialComponentUuid
         if (!this.primordialUuid.equals(another.primordialUuid)) {
            return false;
         }

         // Compare additionalIdComponents
         if (this.additionalIds == null) {
            if (another.additionalIds == null) {             // Equal!
            } else if (another.additionalIds.isEmpty()) {    // Equal!
            } else {
               return false;
            }
         } else if (!this.additionalIds.equals(another.additionalIds)) {
            return false;
         }

         // Compare extraVersions
         if (this.revisions == null) {
            if (another.revisions == null) {                 // Equal!
            } else if (another.revisions.isEmpty()) {        // Equal!
            } else {
               return false;
            }
         } else if (!this.revisions.equals(another.revisions)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EComponent</code>.
    *
    * @return a hash code value for this <tt>EComponent</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { getPrimordialComponentUuid().hashCode(), statusUuid.hashCode(),
                                         pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
   }

   private void processAnnotations(Collection<? extends RefexChronicleBI<?>> annotations) throws IOException {
      if ((annotations != null) &&!annotations.isEmpty()) {
         this.annotations = new ArrayList<TkRefsetAbstractMember<?>>(annotations.size());

         for (RefexChronicleBI<?> r : annotations) {
            this.annotations.add(TkConcept.convertRefex(r));
         }
      }
   }

   private void processAnnotations(Collection<? extends RefexChronicleBI<?>> annotations, ViewCoordinate vc,
                                   NidBitSetBI exclusions, Map<UUID, UUID> conversionMap)
           throws IOException, ContradictionException {
      if ((annotations != null) &&!annotations.isEmpty()) {
         this.annotations = new ArrayList<TkRefsetAbstractMember<?>>(annotations.size());

         for (RefexChronicleBI<?> r : annotations) {
            nextVersion:
            for (RefexVersionBI v : r.getVersions(vc)) {
               for (int vNid : v.getAllNidsForVersion()) {
                  if (exclusions.isMember(vNid) || (Ts.get().getComponent(vNid) == null)) {
                     continue nextVersion;
                  } else if (Ts.get().getComponent(vNid).getVersions(vc).isEmpty()) {
                      continue nextVersion;
                  }
               }

               this.annotations.add(v.getTkRefsetMemberActiveOnly(vc, exclusions, conversionMap));
            }
         }
      }
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      primordialUuid = new UUID(in.readLong(), in.readLong());

      short idVersionCount = in.readShort();

      assert idVersionCount < 500 : "idVersionCount is: " + idVersionCount;

      if (idVersionCount > 0) {
         additionalIds = new ArrayList<TkIdentifier>(idVersionCount);

         for (int i = 0; i < idVersionCount; i++) {
            switch (IDENTIFIER_PART_TYPES.readType(in)) {
            case LONG :
               additionalIds.add(new TkIdentifierLong(in, dataVersion));

               break;

            case STRING :
               additionalIds.add(new TkIdentifierString(in, dataVersion));

               break;

            case UUID :
               additionalIds.add(new TkIdentifierUuid(in, dataVersion));

               break;

            default :
               throw new UnsupportedOperationException();
            }
         }
      }

      short annotationCount = in.readShort();

      assert annotationCount < 5000 : "annotation count is: " + annotationCount;

      if (annotationCount > 0) {
         annotations = new ArrayList<TkRefsetAbstractMember<?>>(annotationCount);

         for (int i = 0; i < annotationCount; i++) {
            TK_REFSET_TYPE type = TK_REFSET_TYPE.readType(in);

            switch (type) {
            case CID :
               annotations.add(new TkRefsetCidMember(in, dataVersion));

               break;

            case CID_CID :
               annotations.add(new TkRefsetCidCidMember(in, dataVersion));

               break;

            case MEMBER :
               annotations.add(new TkRefsetMember(in, dataVersion));

               break;

            case CID_CID_CID :
               annotations.add(new TkRefsetCidCidCidMember(in, dataVersion));

               break;

            case CID_CID_STR :
               annotations.add(new TkRefsetCidCidStrMember(in, dataVersion));

               break;

            case INT :
               annotations.add(new TkRefsetIntMember(in, dataVersion));

               break;

            case STR :
               annotations.add(new TkRefsetStrMember(in, dataVersion));

               break;

            case CID_INT :
               annotations.add(new TkRefsetCidIntMember(in, dataVersion));

               break;

            case BOOLEAN :
               annotations.add(new TkRefsetBooleanMember(in, dataVersion));

               break;

            case CID_FLOAT :
               annotations.add(new TkRefsetCidFloatMember(in, dataVersion));

               break;

            case CID_LONG :
               annotations.add(new TkRefsetCidLongMember(in, dataVersion));

               break;

            case CID_STR :
               annotations.add(new TkRefsetCidStrMember(in, dataVersion));

               break;

            case LONG :
               annotations.add(new TkRefsetLongMember(in, dataVersion));

               break;
                
            case ARRAY_BYTEARRAY:
                annotations.add(new TkRefsetArrayOfBytearrayMember(in, dataVersion));
                break;
            default :
               throw new UnsupportedOperationException("Can't handle refset type: " + type);
            }
         }
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      int depth = 1;

      if (this instanceof TkRefsetAbstractMember) {
         depth = 2;
      }

      StringBuilder buff = new StringBuilder();

      buff.append(" primordial:");
      buff.append(this.primordialUuid);
      buff.append(" xtraIds:");
      buff.append(this.additionalIds);
      buff.append(super.toString());

      if ((annotations != null) && (annotations.size() > 0)) {
         buff.append("\n" + TkConcept.PADDING);

         for (int i = 0; i < depth; i++) {
            buff.append(TkConcept.PADDING);
         }

         buff.append("annotations:\n");

         for (TkRefsetAbstractMember m : this.annotations) {
            buff.append(TkConcept.PADDING);
            buff.append(TkConcept.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(TkConcept.PADDING);
            }

            buff.append(m);
            buff.append("\n");
         }
      }

      if ((revisions != null) && (revisions.size() > 0)) {
         buff.append("\n" + TkConcept.PADDING + "revisions:\n");

         for (TkRevision r : this.revisions) {
            buff.append(TkConcept.PADDING);
            buff.append(TkConcept.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(TkConcept.PADDING);
            }

            buff.append(r);
            buff.append("\n");
         }
      }

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(primordialUuid.getMostSignificantBits());
      out.writeLong(primordialUuid.getLeastSignificantBits());

      if (additionalIds == null) {
         out.writeShort(0);
      } else {
         assert additionalIds.size() < 500 : "additionalIds is: " + additionalIds.size();
         out.writeShort(additionalIds.size());

         for (TkIdentifier idv : additionalIds) {
            idv.getIdType().writeType(out);
            idv.writeExternal(out);
         }
      }

      if (annotations == null) {
         out.writeShort(0);
      } else {
         assert annotations.size() < 500 : "annotation count is: " + annotations.size();
         out.writeShort(annotations.size());

         for (TkRefsetAbstractMember<?> r : annotations) {
            r.getType().writeType(out);
            r.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public List<TkIdentifier> getAdditionalIdComponents() {
      return additionalIds;
   }

   public List<TkRefsetAbstractMember<?>> getAnnotations() {
      return annotations;
   }

   public List<TkIdentifier> getEIdentifiers() {
      List<TkIdentifier> ids;

      if (additionalIds != null) {
         ids = new ArrayList<TkIdentifier>(additionalIds.size() + 1);
         ids.addAll(additionalIds);
      } else {
         ids = new ArrayList<TkIdentifier>(1);
      }

      ids.add(new TkIdentifierUuid(this.primordialUuid));

      return ids;
   }

   public int getIdComponentCount() {
      if (additionalIds == null) {
         return 1;
      }

      return additionalIds.size() + 1;
   }

   public UUID getPrimordialComponentUuid() {
      return primordialUuid;
   }

   public abstract List<? extends TkRevision> getRevisionList();

   public List<V> getRevisions() {
      return revisions;
   }

   public List<UUID> getUuids() {
      List<UUID> uuids = new ArrayList<UUID>();

      uuids.add(primordialUuid);

      if (additionalIds != null) {
         for (TkIdentifier idv : additionalIds) {
            if (TkIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
               uuids.add((UUID) idv.getDenotation());
            }
         }
      }

      return uuids;
   }

   public int getVersionCount() {
      List<? extends TkRevision> extraVersions = getRevisionList();

      if (extraVersions == null) {
         return 1;
      }

      return extraVersions.size() + 1;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAdditionalIdComponents(List<TkIdentifier> additionalIdComponents) {
      this.additionalIds = additionalIdComponents;
   }

   public void setAnnotations(List<TkRefsetAbstractMember<?>> annotations) {
      this.annotations = annotations;
   }

   public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
      this.primordialUuid = primordialComponentUuid;
   }

   public void setRevisions(List<V> revisions) {
      this.revisions = revisions;
   }
}
