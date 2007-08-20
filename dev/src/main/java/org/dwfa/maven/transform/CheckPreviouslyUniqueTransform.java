package org.dwfa.maven.transform;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.maven.Transform;

public class CheckPreviouslyUniqueTransform extends AbstractTransform {
   
   static Set<String> dups;
   boolean duplicatesFound = false;

   @Override
   public void setupImpl(Transform transformer) throws IOException {
      if (dups == null) {
         dups = new HashSet<String>();
      }
   }

   public String transform(String input) throws Exception {
      if (dups.contains(input)) {
         return  setLastTransform("Duplicate: " + input);
      } else {
         return  setLastTransform(input);
      }
   }

   @Override
   public void cleanup(Transform transformer) throws Exception {
      super.cleanup(transformer);
      if (duplicatesFound) {
         transformer.getLog().info(this.getName() + " FOUND DUPLICATES. *** Please view the output file for details.");
      } else {
         transformer.getLog().info(this.getName() + " found no duplicates.");
      }
   }

}
