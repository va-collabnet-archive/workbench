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
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefexUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.concept.component.refex.type_arrayofbytearray.TkRefexArrayOfBytearrayMember;

public abstract class TkComponent<V extends TkRevision> extends TkRevision {
   private static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public List<TkIdentifier>              additionalIds;
   public List<TkRefexAbstractMember<?>> annotations;
   public UUID                            primordialUuid;
   public List<V>                         revisions;

   //~--- constructors --------------------------------------------------------

   public TkComponent() {
      super();
   }

   public TkComponent(ComponentVersionBI componentVersion) throws IOException {
      super(componentVersion);

      Collection<? extends IdBI> anotherAdditionalIds = componentVersion.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds = new ArrayList<TkIdentifier>(anotherAdditionalIds.size());
         nextId:
         for (IdBI id : anotherAdditionalIds) {
            this.additionalIds.add((TkIdentifier) TkIdentifier.convertId(id));
         }
      }

      Collection<? extends RefexChronicleBI<?>> anotherAnnotations = componentVersion.getAnnotations();

      processAnnotations(anotherAnnotations);
      this.primordialUuid = componentVersion.getPrimUuid();
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
         this.annotations = new ArrayList<TkRefexAbstractMember<?>>(another.annotations.size());

         for (TkRefexAbstractMember<?> r : another.annotations) {
            this.annotations.add((TkRefexAbstractMember<?>) r.makeConversion(conversionMap, offset, mapAll));
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

   public TkComponent(ComponentVersionBI componentVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
                      long offset, boolean mapAll, ViewCoordinate viewCoordinate)
           throws IOException, ContradictionException {
      super(componentVersion, conversionMap, offset, mapAll);

      Collection<? extends IdBI> anotherAdditionalIds = componentVersion.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds = new ArrayList<TkIdentifier>(anotherAdditionalIds.size());
         nextId:
         for (IdBI id : anotherAdditionalIds) {
            for (int nid : id.getAllNidsForId()) {
               if (excludedNids.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                  continue nextId;
               } else if (Ts.get().getComponent(nid).getVersions(viewCoordinate).isEmpty()) {
                   continue nextId;
               }
            }

            this.additionalIds.add((TkIdentifier) TkIdentifier.convertId(id).makeConversion(conversionMap,
                    offset, mapAll));
         }
      }

      Collection<? extends RefexChronicleBI<?>> anotherAnnotations = componentVersion.getAnnotations();

      processAnnotations(anotherAnnotations, viewCoordinate, excludedNids, conversionMap);
      this.primordialUuid = conversionMap.get(componentVersion.getPrimUuid());
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
         this.annotations = new ArrayList<TkRefexAbstractMember<?>>(annotations.size());

         for (RefexChronicleBI<?> r : annotations) {
            this.annotations.add(TkConcept.convertRefex(r));
         }
      }
   }

   private void processAnnotations(Collection<? extends RefexChronicleBI<?>> annotations, ViewCoordinate viewCoordinate,
                                   NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap)
           throws IOException, ContradictionException {
      if ((annotations != null) &&!annotations.isEmpty()) {
         this.annotations = new ArrayList<TkRefexAbstractMember<?>>(annotations.size());

         for (RefexChronicleBI<?> r : annotations) {
            nextVersion:
            for (RefexVersionBI v : r.getVersions(viewCoordinate)) {
               for (int vNid : v.getAllNidsForVersion()) {
                  if (excludedNids.isMember(vNid) || (Ts.get().getComponent(vNid) == null)) {
                     continue nextVersion;
                  } else if (Ts.get().getComponent(vNid).getVersions(viewCoordinate).isEmpty()) {
                      continue nextVersion;
                  }
               }

               this.annotations.add(v.getTkRefsetMemberActiveOnly(viewCoordinate, excludedNids, conversionMap));
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
         annotations = new ArrayList<TkRefexAbstractMember<?>>(annotationCount);

         for (int i = 0; i < annotationCount; i++) {
            TK_REFEX_TYPE type = TK_REFEX_TYPE.readType(in);

            switch (type) {
            case CID :
               annotations.add(new TkRefexUuidMember(in, dataVersion));

               break;

            case CID_CID :
               annotations.add(new TkRefexUuidUuidMember(in, dataVersion));

               break;

            case MEMBER :
               annotations.add(new TkRefexMember(in, dataVersion));

               break;

            case CID_CID_CID :
               annotations.add(new TkRefexUuidUuidUuidMember(in, dataVersion));

               break;

            case CID_CID_STR :
               annotations.add(new TkRefexUuidUuidStringMember(in, dataVersion));

               break;

            case INT :
               annotations.add(new TkRefexIntMember(in, dataVersion));

               break;

            case STR :
               annotations.add(new TkRefsetStrMember(in, dataVersion));

               break;

            case CID_INT :
               annotations.add(new TkRefexUuidIntMember(in, dataVersion));

               break;

            case BOOLEAN :
               annotations.add(new TkRefexBooleanMember(in, dataVersion));

               break;

            case CID_FLOAT :
               annotations.add(new TkRefexUuidFloatMember(in, dataVersion));

               break;

            case CID_LONG :
               annotations.add(new TkRefexUuidLongMember(in, dataVersion));

               break;

            case CID_STR :
               annotations.add(new TkRefexUuidStringMember(in, dataVersion));

               break;

            case LONG :
               annotations.add(new TkRefexLongMember(in, dataVersion));

               break;
                
            case ARRAY_BYTEARRAY:
                annotations.add(new TkRefexArrayOfBytearrayMember(in, dataVersion));
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

      if (this instanceof TkRefexAbstractMember) {
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

         for (TkRefexAbstractMember m : this.annotations) {
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

         for (TkRefexAbstractMember<?> r : annotations) {
            r.getType().writeType(out);
            r.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public List<TkIdentifier> getAdditionalIdComponents() {
      return additionalIds;
   }

   public List<TkRefexAbstractMember<?>> getAnnotations() {
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

   public void setAnnotations(List<TkRefexAbstractMember<?>> annotations) {
      this.annotations = annotations;
   }

   public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
      this.primordialUuid = primordialComponentUuid;
   }

   public void setRevisions(List<V> revisions) {
      this.revisions = revisions;
   }
}
