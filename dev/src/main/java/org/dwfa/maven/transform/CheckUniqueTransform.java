package org.dwfa.maven.transform;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.maven.Transform;

public class CheckUniqueTransform extends AbstractTransform {
   
   Set<String> keys = new HashSet<String>();
   boolean duplicates = false;

   @Override
   public void setupImpl(Transform transformer) throws IOException {
      
   }

   public String transform(String input) throws Exception {
      if (keys.contains(input)) {
         duplicates = true;
         return "Duplicate: " + input;
      } else {
         keys.add(input);
         return input;
      }
   }

   @Override
   public void cleanup(Transform transformer) throws Exception {
      super.cleanup(transformer);
      if (duplicates) {
         transformer.getLog().info(this.getName() + " FOUND DUPLICATES. *** Please view the output file for details.");
      } else {
         transformer.getLog().info(this.getName() + " found no duplicates.");
      }
   }

}
