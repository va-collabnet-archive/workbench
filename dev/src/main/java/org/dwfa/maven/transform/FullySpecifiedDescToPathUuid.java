package org.dwfa.maven.transform;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.dwfa.maven.Transform;
import org.dwfa.util.id.Type5UuidFactory;

public class FullySpecifiedDescToPathUuid extends AbstractTransform {
   
   String pathFsName;
   
   UUID pathUUID;
   public String transform(String input) throws Exception {
      return setLastTransform(pathUUID.toString());
   }
   public void setupImpl(Transform transformer) {
      try {
         pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsName);
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }

   }
}
