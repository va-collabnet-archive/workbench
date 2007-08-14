package org.dwfa.vodb.bind;

import java.util.List;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.vodb.types.ThinExtPartBoolean;
import org.dwfa.vodb.types.ThinExtPartConcept;
import org.dwfa.vodb.types.ThinExtPartInteger;
import org.dwfa.vodb.types.ThinExtPartLanguage;
import org.dwfa.vodb.types.ThinExtPartScopedLanguage;
import org.dwfa.vodb.types.ThinExtVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinExtBinder extends TupleBinding {
   public static enum EXT_TYPE {
      BOOLEAN(1, "boolean"), CONCEPT(2, "concept"), INTEGER(3, "integer"), LANGUAGE(4, "language"), 
      SCOPED_LANGUAGE(5, "scoped language");

      private int id;
      private String interfaceName;

      private EXT_TYPE(int id, String interfaceName) {
         this.id = id;
         this.interfaceName = interfaceName;
      }

      public int getId() {
         return id;
      }

      public static EXT_TYPE fromId(int id) {
         switch (id) {
         case 1:
            return BOOLEAN;
         case 2:
            return CONCEPT;
         case 3:
            return INTEGER;
         case 4:
            return LANGUAGE;
         case 5:
            return SCOPED_LANGUAGE;

         default:
            throw new RuntimeException("Can't convert to EXT_TYPE: " + id);
         }
      }

      public String getInterfaceName() {
         return interfaceName;
      }
   }

   private boolean fixedOnly;

   public ThinExtBinder() {
      super();
      this.fixedOnly = false;
   }

   public ThinExtBinder(boolean fixedOnly) {
      super();
      this.fixedOnly = fixedOnly;
   }

   public ThinExtVersioned entryToObject(TupleInput ti) {
      int refsetId = ti.readInt();
      int memberId = ti.readInt();
      int componentId = ti.readInt();
      EXT_TYPE type = EXT_TYPE.fromId(ti.readInt());
      int typeId;
      try {
         switch (type) {
         case BOOLEAN:
            typeId = RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.localize().getNid();
            break;
         case CONCEPT:
            typeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
            break;
         case INTEGER:
            typeId = RefsetAuxiliary.Concept.INT_EXTENSION.localize().getNid();
            break;
         case LANGUAGE:
            typeId = RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.localize().getNid();
            break;
         case SCOPED_LANGUAGE:
            typeId = RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.localize().getNid();
            break;

         default:
            throw new RuntimeException("Can't convert to type: " + type);
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      int partCount = ti.readInt();
      ThinExtVersioned versioned = new ThinExtVersioned(refsetId, memberId, componentId, typeId, partCount);
      if (fixedOnly) {
         return versioned;
      }
      switch (type) {
      case BOOLEAN:
         for (int x = 0; x < partCount; x++) {
            ThinExtPartBoolean part = new ThinExtPartBoolean();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setValue(ti.readBoolean());
            versioned.addVersion(part);
         }
         break;
      case CONCEPT:
         for (int x = 0; x < partCount; x++) {
            ThinExtPartConcept part = new ThinExtPartConcept();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setConceptId(ti.readInt());
            versioned.addVersion(part);
         }
         break;
      case INTEGER:
         for (int x = 0; x < partCount; x++) {
            ThinExtPartInteger part = new ThinExtPartInteger();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setValue(ti.readInt());
            versioned.addVersion(part);
         }
         break;
      case LANGUAGE:
         for (int x = 0; x < partCount; x++) {
            ThinExtPartLanguage part = new ThinExtPartLanguage();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setAcceptabilityId(ti.readInt());
            part.setCorrectnessId(ti.readInt());
            part.setDegreeOfSynonymyId(ti.readInt());
            versioned.addVersion(part);
         }
         break;
      case SCOPED_LANGUAGE:
         for (int x = 0; x < partCount; x++) {
            ThinExtPartScopedLanguage part = new ThinExtPartScopedLanguage();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setAcceptabilityId(ti.readInt());
            part.setCorrectnessId(ti.readInt());
            part.setDegreeOfSynonymyId(ti.readInt());
            part.setScopeId(ti.readInt());
            part.setTagId(ti.readInt());
            part.setPriority(ti.readInt());
            versioned.addVersion(part);
         }
         break;
      default:
         throw new RuntimeException("Can't handle type: " + type);
      }
      return versioned;
   }

   /*
    * private int refsetId; private int memberId; private int componentId;
    * private int typeId;
    */
   @SuppressWarnings("unchecked")
   public void objectToEntry(Object obj, TupleOutput to) {
      ThinExtVersioned versioned = (ThinExtVersioned) obj;
      to.writeInt(versioned.getRefsetId());
      to.writeInt(versioned.getMemberId());
      to.writeInt(versioned.getComponentId());
      EXT_TYPE extType = EXT_TYPE.BOOLEAN;
      if (versioned.getVersions() == null || versioned.getVersions().size() == 0) {
         try {
            if (versioned.getTypeId() == RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.localize().getNid()) {
               extType = EXT_TYPE.BOOLEAN;
            } else if (versioned.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) {
               extType = EXT_TYPE.CONCEPT;
            } else if (versioned.getTypeId() == RefsetAuxiliary.Concept.INT_EXTENSION.localize().getNid()) {
               extType = EXT_TYPE.INTEGER;
            } else if (versioned.getTypeId() == RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.localize().getNid()) {
               extType = EXT_TYPE.LANGUAGE;
            } else if (versioned.getTypeId() == RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.localize().getNid()) {
               extType = EXT_TYPE.LANGUAGE;
            }
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      } else {
         Object firstPart = versioned.getVersions().get(0);
         if (ThinExtPartBoolean.class.isAssignableFrom(firstPart.getClass())) {
            extType = EXT_TYPE.BOOLEAN;
         } else if (ThinExtPartConcept.class.isAssignableFrom(firstPart.getClass())) {
            extType = EXT_TYPE.CONCEPT;
         } else if (ThinExtPartInteger.class.isAssignableFrom(firstPart.getClass())) {
            extType = EXT_TYPE.INTEGER;
         } else if (ThinExtPartLanguage.class.isAssignableFrom(firstPart.getClass())) {
            extType = EXT_TYPE.LANGUAGE;
         } else if (ThinExtPartScopedLanguage.class.isAssignableFrom(firstPart.getClass())) {
            extType = EXT_TYPE.SCOPED_LANGUAGE;
         }
      }
      to.writeInt(extType.getId());

      to.writeInt(versioned.getVersions().size());
      switch (extType) {
      case BOOLEAN:
         List<ThinExtPartBoolean> booleanParts = (List<ThinExtPartBoolean>) versioned.getVersions();
         for (ThinExtPartBoolean part : booleanParts) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeBoolean(part.getValue());
         }
         break;
      case CONCEPT:
         List<ThinExtPartConcept> conceptParts = (List<ThinExtPartConcept>) versioned.getVersions();
         for (ThinExtPartConcept part : conceptParts) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getConceptId());
         }
         break;
      case INTEGER:
         List<ThinExtPartInteger> intParts = (List<ThinExtPartInteger>) versioned.getVersions();
         for (ThinExtPartInteger part : intParts) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getValue());
         }
         break;
      case LANGUAGE:
         List<ThinExtPartLanguage> langParts = (List<ThinExtPartLanguage>) versioned.getVersions();
         for (ThinExtPartLanguage part : langParts) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getAcceptabilityId());
            to.writeInt(part.getCorrectnessId());
            to.writeInt(part.getDegreeOfSynonymyId());
         }
         break;
      case SCOPED_LANGUAGE:
         List<ThinExtPartScopedLanguage> scopedLangParts = (List<ThinExtPartScopedLanguage>) versioned.getVersions();
         for (ThinExtPartScopedLanguage part : scopedLangParts) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getAcceptabilityId());
            to.writeInt(part.getCorrectnessId());
            to.writeInt(part.getDegreeOfSynonymyId());
            to.writeInt(part.getScopeId());
            to.writeInt(part.getTagId());
            to.writeInt(part.getPriority());
         }
         break;
      default:
         throw new RuntimeException("Can't handle type: " + extType);
      }
   }

}
