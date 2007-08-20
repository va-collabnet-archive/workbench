package org.dwfa.maven.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.maven.Transform;

public class CheckUniqueTransform extends AbstractTransform {
   
   static File dupFile = new File("target/dups.txt");
   Set<String> keys = new HashSet<String>();
   boolean duplicatesFound = false;
   static Set<String> dups;

   @SuppressWarnings("unchecked")
   @Override
   public void setupImpl(Transform transformer) throws IOException, ClassNotFoundException {
      if (dups == null) {
         if (dupFile.exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dupFile));
            dups = (Set<String>) ois.readObject();
            ois.close();
         } else {
            dups = new HashSet<String>();
         }
      }
   }

   public String transform(String input) throws Exception {
      if (keys.contains(input) || dups.contains(input)) {
         duplicatesFound = true;
         dups.add(input);
         return setLastTransform("Duplicate: " + input);
      } else {
         keys.add(input);
         return setLastTransform(input);
      }
   }

   @Override
   public void cleanup(Transform transformer) throws Exception {
      super.cleanup(transformer);
      if (duplicatesFound) {
         transformer.getLog().info(this.getName() + " FOUND DUPLICATES. *** Please view the output file for details.");
         transformer.getLog().info(this.getName() + " Dups: " + dups);
         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dupFile));
         oos.writeObject(dups);
         oos.close();
      } else {
         transformer.getLog().info(this.getName() + " found no duplicates.");
      }
   }

}
