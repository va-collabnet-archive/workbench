package org.dwfa.maven.transform;

import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class UuidToSctConIdNoGeneration extends UuidToSctIdNoGeneration {

   @Override
   protected TYPE getType() {
      return TYPE.CONCEPT;
   }
}
