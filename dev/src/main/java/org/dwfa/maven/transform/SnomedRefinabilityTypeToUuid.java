package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.tapi.I_ConceptualizeUniversally;

public class SnomedRefinabilityTypeToUuid extends AbstractTransform implements I_ReadAndTransform {

   public void setupImpl(Transform transformer) {
    }

   public String transform(String input) throws Exception {
      I_ConceptualizeUniversally refinability = ArchitectonicAuxiliary.getSnomedRefinabilityType(Integer.parseInt(input));
      UUID uid = refinability.getUids().iterator().next();
      return setLastTransform(uid.toString());
   }

}