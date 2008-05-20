package org.dwfa.maven.transform;

import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class UuidToSctDescIdNoGeneration extends UuidToSctIdNoGeneration {

   @Override
   protected TYPE getType() {
      return TYPE.DESCRIPTION;
   }

}