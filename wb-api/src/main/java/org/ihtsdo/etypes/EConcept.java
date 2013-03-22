package org.ihtsdo.etypes;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageVersioned;
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
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfBytearrayMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

public class EConcept extends TkConcept implements I_AmChangeSetObject {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public EConcept() {
      super();
   }

   public EConcept(DataInput in) throws IOException, ClassNotFoundException {
      super(in);
   }

   /**
    * @TODO remove componentRefsetMap added to get around bug in current database implementation!
    * @param c
    * @throws IOException
    * @throws TerminologyException
    */
   public EConcept(I_GetConceptData c) throws IOException, TerminologyException {
      annotationStyleRefex = c.isAnnotationStyleRefex();
      annotationIndexStyleRefex = c.isAnnotationIndex();
      conceptAttributes = new EConceptAttributes(c.getConAttrs());
      primordialUuid = conceptAttributes.primordialUuid;
      EConcept.convertId(c.getIdentifier(), conceptAttributes);
      relationships = new ArrayList<TkRelationship>(c.getSourceRels().size());

      for (I_RelVersioned rel : c.getSourceRels()) {
         relationships.add(new ERelationship(rel));
      }

      descriptions = new ArrayList<TkDescription>(c.getDescs().size());

      for (I_DescriptionVersioned desc : c.getDescs()) {
         descriptions.add(new EDescription(desc));
      }

      media = new ArrayList<TkMedia>(c.getImages().size());

      for (I_ImageVersioned img : c.getImages()) {
         EImage eImage = new EImage(img);

         if (eImage.getTime() == Long.MIN_VALUE) {
            eImage.setTime(this.conceptAttributes.getTime());

            // Fixup for a data issue.
         }

         media.add(eImage);
      }

      if (!c.isAnnotationStyleRefex()) {
         Collection<? extends I_ExtendByRef> members = getRefsetMembers(c.getNid());

         if (members != null) {
            refsetMembers = new ArrayList<TkRefexAbstractMember<?>>(members.size());

            for (I_ExtendByRef m : members) {
               TkRefexAbstractMember<?> member = convertRefsetMember(m);

               if (member != null) {
                  refsetMembers.add(member);
               } else {
                  AceLog.getAppLog().severe("Could not convert refset member: " + m + "\nfrom refset: " + c);
               }
            }
         }
      }
   }

   public EConcept(I_ConceptualizeLocally cNoHx, I_StoreLocalFixedTerminology mts)
           throws IOException, TerminologyException {
      UUID currentUuid = ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid();
      UUID pathUuid    = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
      UUID authorUuid    = ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid();
      UUID moduleUuid    = ArchitectonicAuxiliary.Concept.AUXILIARY_MODULE.getPrimoridalUid();
      long time        = System.currentTimeMillis();

      primordialUuid                   = cNoHx.getUids().iterator().next();
      conceptAttributes                = new EConceptAttributes();
      conceptAttributes.defined        = false;
      conceptAttributes.primordialUuid = primordialUuid;
      conceptAttributes.statusUuid     = currentUuid;
      conceptAttributes.authorUuid = authorUuid;
      conceptAttributes.setPathUuid(pathUuid);
      conceptAttributes.setTime(time);
      conceptAttributes.moduleUuid = moduleUuid;

      if (cNoHx.getDescriptions() == null) {
         AceLog.getAppLog().warning("cNoHx has null descriptions: " + cNoHx);
      } else {
         descriptions = new ArrayList<TkDescription>(cNoHx.getDescriptions().size());

         for (I_DescribeConceptLocally descNoHx : cNoHx.getDescriptions()) {
            assert descNoHx != null;
            assert descNoHx.getUids() != null : descNoHx;
            assert descNoHx.getUids().iterator() != null : descNoHx;

            EDescription desc = new EDescription();

            desc.primordialUuid = descNoHx.getUids().iterator().next();
            desc.statusUuid     = currentUuid;
            desc.authorUuid = authorUuid;
            desc.moduleUuid = moduleUuid;
            desc.setPathUuid(pathUuid);
            desc.setTime(time);
            desc.conceptUuid            = conceptAttributes.primordialUuid;
            desc.initialCaseSignificant = descNoHx.isInitialCapSig();
            desc.lang                   = descNoHx.getLangCode();
            desc.text                   = descNoHx.getText();
            desc.typeUuid               = descNoHx.getDescType().getUids().iterator().next();
            descriptions.add(desc);
         }
      }

      if (cNoHx.getSourceRels() == null) {
         AceLog.getAppLog().warning("cNoHx has null rels: " + cNoHx);
      } else {
         relationships = new ArrayList<TkRelationship>(cNoHx.getSourceRels().size());

         for (I_RelateConceptsLocally relNoHx : cNoHx.getSourceRels()) {
            ERelationship rel = new ERelationship();

            rel.primordialUuid = relNoHx.getUids().iterator().next();
            rel.statusUuid     = currentUuid;
            rel.authorUuid = authorUuid;
            rel.moduleUuid = moduleUuid;
            rel.setPathUuid(pathUuid);
            rel.setTime(time);
            rel.c1Uuid             = conceptAttributes.primordialUuid;
            rel.c2Uuid             = relNoHx.getC2().getUids().iterator().next();
            rel.characteristicUuid = relNoHx.getCharacteristic().getUids().iterator().next();
            rel.refinabilityUuid   = relNoHx.getRefinability().getUids().iterator().next();
            rel.relGroup           = relNoHx.getRelGrp();
            rel.typeUuid           = relNoHx.getRelType().getUids().iterator().next();
            relationships.add(rel);
         }
      }
   }

   //~--- enums ---------------------------------------------------------------

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
      CID_CID_CID(4, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION,
              I_ExtendByRefPartCidCidCid.class), 
      CID_CID_STR(5, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION, 
              I_ExtendByRefPartCidCidString.class), 
      STR(6, RefsetAuxiliary.Concept.STRING_EXTENSION, I_ExtendByRefPartStr.class), 
      INT(7, RefsetAuxiliary.Concept.INT_EXTENSION, I_ExtendByRefPartInt.class), 
      CID_INT(8, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION, I_ExtendByRefPartCidInt.class), 
      BOOLEAN(9, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION, I_ExtendByRefPartBoolean.class), 
      CID_STR(10, RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION, I_ExtendByRefPartStr.class), 
      CID_FLOAT(11, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION, I_ExtendByRefPartCidFloat.class), 
      CID_LONG(12, RefsetAuxiliary.Concept.CID_LONG_EXTENSION, I_ExtendByRefPartCidLong.class), 
      LONG(13, RefsetAuxiliary.Concept.LONG_EXTENSION, I_ExtendByRefPartLong.class),
      ARRAY_OF_BYTEARRAY(14, RefsetAuxiliary.Concept.ARRAY_OF_BYTEARRAY_EXTENSION, 
              RefexArrayOfBytearrayVersionBI.class);

      private static Map<Integer, REFSET_TYPES> nidTypeMap;

      //~--- fields -----------------------------------------------------------

      private int                                externalizedToken;
      private Class<? extends RefexVersionBI> partClass;
      private RefsetAuxiliary.Concept            typeConcept;
      private int                                typeNid;

      //~--- constructors -----------------------------------------------------

      REFSET_TYPES(int externalizedToken, RefsetAuxiliary.Concept typeConcept,
                   Class<? extends RefexVersionBI> partClass) {
         this.externalizedToken = externalizedToken;
         this.typeConcept       = typeConcept;
         this.partClass         = partClass;
      }

      //~--- methods ----------------------------------------------------------

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
         } else if (RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(partType)) {
            return ARRAY_OF_BYTEARRAY;
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

               throw new IOException("Unknown refset type: " + nid + " concept: "
                                     + typeConcept.getInitialText());
            } else {
               throw new IOException("Unknown refset type: " + nid);
            }
         }
      }

      public static REFSET_TYPES readType(DataInput input) throws IOException {
          byte type = input.readByte();
         switch (type) {
         case 1 :
            return MEMBER;

         case 2 :
            return CID;

         case 3 :
            return CID_CID;

         case 4 :
            return CID_CID_CID;

         case 5 :
            return CID_CID_STR;

         case 6 :
            return STR;

         case 7 :
            return INT;

         case 8 :
            return CID_INT;

         case 9 :
            return BOOLEAN;

         case 10 :
            return CID_STR;

         case 11 :
            return CID_FLOAT;

         case 12 :
            return CID_LONG;

         case 13 :
            return LONG;
         case 14: 
             return ARRAY_OF_BYTEARRAY;
         }

         throw new UnsupportedOperationException("Can't handle refset type: " + type);
      }

      public static void resetNids() {
          nidTypeMap = null;
      }
      public static void setupNids() {
         if (nidTypeMap == null) {
            HashMap<Integer, REFSET_TYPES> temp = new HashMap<Integer, REFSET_TYPES>();

            for (REFSET_TYPES type : REFSET_TYPES.values()) {
               try {
                  type.typeNid = Terms.get().uuidToNative(type.typeConcept.getUids());
                  temp.put(type.typeNid, type);
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }

            nidTypeMap = temp;
         }
      }

      public void writeType(DataOutput output) throws IOException {
         output.writeByte(externalizedToken);
      }

      //~--- get methods ------------------------------------------------------

      public Class<? extends RefexVersionBI> getPartClass() {
         return partClass;
      }

      public int getTypeNid() {
         setupNids();

         return typeNid;
      }
   }

   //~--- methods -------------------------------------------------------------

   public static void convertId(I_Identify id, TkComponent<?> component) throws IOException {
      boolean primordialWritten = false;
      int     partCount         = id.getMutableIdParts().size() - 1;

      if (partCount > 0) {
         component.additionalIds = new ArrayList<TkIdentifier>(partCount);

         for (I_IdPart idp : id.getMutableIdParts()) {
            Object denotation = idp.getDenotation();

            switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
            case LONG :
               component.additionalIds.add(new EIdentifierLong(idp));

               break;

            case STRING :
               component.additionalIds.add(new EIdentifierString(idp));

               break;

            case UUID :
               if (primordialWritten) {
                  component.additionalIds.add(new EIdentifierUuid(idp));
               } else {
                  component.primordialUuid = (UUID) idp.getDenotation();
                  primordialWritten        = true;
               }

               break;

            default :
               throw new UnsupportedOperationException();
            }
         }
      } else {
         component.primordialUuid = (UUID) id.getUUIDs().get(0);
      }
   }

   public static TkRefexAbstractMember<?> convertRefsetMember(I_ExtendByRef m)
           throws TerminologyException, IOException {
      REFSET_TYPES type = REFSET_TYPES.nidToType(m.getTypeNid());

      if (type != null) {
         switch (type) {
         case CID :
            return new ERefsetCidMember(m);

         case CID_CID :
            return new ERefsetCidCidMember(m);

         case CID_CID_CID :
            return new ERefsetCidCidCidMember(m);

         case CID_CID_STR :
            return new ERefsetCidCidStrMember(m);

         case INT :
            return new ERefsetIntMember(m);

         case MEMBER :
            return new ERefsetMemberMember(m);

         case STR :
            return new ERefsetStrMember(m);

         case CID_INT :
            return new ERefsetCidIntMember(m);

         case CID_LONG :
            return new ERefsetCidLongMember(m);

         case LONG :
            return new ERefsetLongMember(m);

         case BOOLEAN :
            return new ERefsetBooleanMember(m);

         case CID_STR :
            return new ERefsetCidStrMember(m);
             
         case ARRAY_OF_BYTEARRAY:
             return new TkRefexArrayOfBytearrayMember((RefexVersionBI) 
                     ((RefexChronicleBI) m).getPrimordialVersion());

         default :
            throw new UnsupportedOperationException("Cannot handle: " + type);
         }
      } else {
         AceLog.getAppLog().severe("Can't handle refset type: " + m);
      }

      return null;
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
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      return super.equals(obj);
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

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append("\n   primordial UUID: ");
      buff.append(this.primordialUuid);
      buff.append("\n   annotation: ");
      buff.append(this.annotationStyleRefex);
      buff.append("\n   annotation index: ");
      buff.append(this.annotationIndexStyleRefex);
      buff.append("\n   ConceptAttributes: \n\t");
      buff.append(this.conceptAttributes);
      buff.append("\n   Descriptions: \n\t");
      buff.append(this.descriptions);
      buff.append("\n   Relationships: \n\t");
      buff.append(this.relationships);
      buff.append("\n   RefsetMembers: \n\t");
      buff.append(this.refsetMembers);
      buff.append("\n   Media: \n\t");
      buff.append(this.media);

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<TkMedia> getImages() {
      return media;
   }

   @Override
   public UUID getPrimordialUuid() {
      return primordialUuid;
   }

   protected static Collection<? extends I_ExtendByRef> getRefsetMembers(int nid)
           throws TerminologyException, IOException {
      return Terms.get().getRefsetExtensionMembers(nid);
   }

   ;

   //~--- set methods ---------------------------------------------------------

   public void setConceptAttributes(EConceptAttributes conceptAttributes) {
      this.conceptAttributes = conceptAttributes;
   }

   @Override
   public void setImages(List<TkMedia> images) {
      this.media = images;
   }

   @Override
   public void setPrimordialUuid(UUID primordialUuid) {
      this.primordialUuid = primordialUuid;
   }
}
