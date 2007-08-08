package org.dwfa.maven.transform;

import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class UuidToSctRelIdWithGeneration extends UuidToSctIdWithGeneration {

   @Override
   protected TYPE getType() {
      return TYPE.RELATIONSHIP;
   }

}