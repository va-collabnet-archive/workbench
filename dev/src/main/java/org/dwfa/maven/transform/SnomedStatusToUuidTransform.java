package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.tapi.I_ConceptualizeUniversally;

/**
 * Creates a type 5 uuid for a path given a fully specified name. 
 * 
 * @author kec
 *
 */
public class SnomedStatusToUuidTransform extends AbstractTransform implements I_ReadAndTransform {

   public void setupImpl(Transform transformer) {
   }

   public String transform(String input) throws Exception {
      I_ConceptualizeUniversally status = ArchitectonicAuxiliary.getStatusFromId(Integer.parseInt(input));
      UUID statusUuid = status.getUids().iterator().next();
      return setLastTransform(statusUuid.toString());
   }

}
