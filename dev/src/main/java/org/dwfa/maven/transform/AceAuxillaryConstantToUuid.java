package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class AceAuxillaryConstantToUuid extends AbstractTransform implements
      I_ReadAndTransform {

   String param;
   UUID uuid;
   
   public void setupImpl(Transform transformer) {
      ArchitectonicAuxiliary.Concept concept = Enum.valueOf(ArchitectonicAuxiliary.Concept.class, param);
      uuid = concept.getUids().iterator().next();
   }

   public String transform(String input) throws Exception {
      return setLastTransform(uuid.toString());
   }
}