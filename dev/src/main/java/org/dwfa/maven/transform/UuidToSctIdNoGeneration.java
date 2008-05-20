package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public abstract class UuidToSctIdNoGeneration extends UuidToSctIdWithGeneration implements I_ReadAndTransform {

   public String transform(String input) throws Exception {
      UUID id = UUID.fromString(input);
      TYPE type = getType();

      // find the existing SCT ID in the mapping
      Long returnValue = super.getMap().get(type).get(id);

      // if it isn't found, use a dummy value as we aren't generating new IDs
      if (returnValue == null) {
          returnValue = new Long(-1);
      }
      return setLastTransform(returnValue.toString());
   }
}
