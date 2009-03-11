package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.util.id.Type3UuidFactory;

public class SnomedType3Uuid extends AbstractTransform implements I_ReadAndTransform {

   public void setupImpl(Transform transformer) {
   }

   public String transform(String input) throws Exception {
      UUID snomedUuid = Type3UuidFactory.fromSNOMED(input);

      return setLastTransform(snomedUuid.toString());
   }


}
