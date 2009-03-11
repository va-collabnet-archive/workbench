package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.tapi.I_ConceptualizeUniversally;

public class SnomedDescTypeToUuid extends AbstractTransform implements I_ReadAndTransform {

   public void setupImpl(Transform transformer) {
   }

   public String transform(String input) throws Exception {
      I_ConceptualizeUniversally descType = ArchitectonicAuxiliary.getSnomedDescriptionType(Integer.parseInt(input));
      UUID uid = descType.getUids().iterator().next();
      return setLastTransform(uid.toString());
   }

}
