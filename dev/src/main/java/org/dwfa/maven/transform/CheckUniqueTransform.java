package org.dwfa.maven.transform;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.maven.Transform;

public class CheckUniqueTransform extends AbstractTransform {
   
   Set<String> keys = new HashSet<String>();
   boolean duplicatesFound = false;
   static Set<String> dups;

   @Override
   public void setupImpl(Transform transformer) throws IOException {
      if (dups == null) {
         dups = new HashSet<String>();
      }
   }

   public String transform(String input) throws Exception {
      if (keys.contains(input) || dups.contains(input)) {
         duplicatesFound = true;
         dups.add(input);
         return  setLastTransform("Duplicate: " + input);
      } else {
         keys.add(input);
         return  setLastTransform(input);
      }
   }

   @Override
   public void cleanup(Transform transformer) throws Exception {
      super.cleanup(transformer);
      if (duplicatesFound) {
         transformer.getLog().info(this.getName() + " FOUND DUPLICATES. *** Please view the output file for details.");
         transformer.getLog().info(this.getName() + " Dups: " + dups);
      } else {
         transformer.getLog().info(this.getName() + " found no duplicates.");
      }
   }

}
