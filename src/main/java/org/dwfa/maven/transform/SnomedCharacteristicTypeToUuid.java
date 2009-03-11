package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.tapi.I_ConceptualizeUniversally;

public class SnomedCharacteristicTypeToUuid extends AbstractTransform implements I_ReadAndTransform {

   public void setupImpl(Transform transformer) {
   }

   public String transform(String input) throws Exception {
      I_ConceptualizeUniversally characteristic = ArchitectonicAuxiliary.getSnomedCharacteristicType(Integer.parseInt(input));
      UUID uid = characteristic.getUids().iterator().next();
      return setLastTransform(uid.toString());
   }

}