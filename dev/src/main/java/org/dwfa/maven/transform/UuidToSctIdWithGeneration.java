package org.dwfa.maven.transform;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public abstract class UuidToSctIdWithGeneration extends AbstractTransform implements I_ReadAndTransform {
   
   

   private static UuidSnomedMap map;
   private File writableMapFile;
   
   public void setupImpl(Transform transformer) throws IOException {
      // Nothing to setup
      File buildDirectory = transformer.getBuildDirectory();
      transformer.getLog().info(" buildDirectory: " + buildDirectory.getAbsolutePath());
      //[INFO]  buildDirectory: /au-ct/ace/amt-release/target
      
      File sourceDirectory = transformer.getSourceDirectory();
      transformer.getLog().info(" sourceDirectory: " + sourceDirectory.getAbsolutePath());
      //[INFO]  sourceDirectory: /au-ct/ace/amt-release/src/main/java
      
      if (map == null) {
         File idSourceDir = new File(sourceDirectory.getParentFile(), "sct-uuid-maps");
         File[] rwMapFiles = idSourceDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
               return f.getName().endsWith("-sct-map-rw.txt");
            }
            
         });
         if (rwMapFiles == null || rwMapFiles.length != 1) {
            throw new IOException("RW mapping file not found. There must be one--and only one--file of format [namespace]-[project]-sct-map-rw.txt in the directory " + idSourceDir.getAbsolutePath());
         }
         writableMapFile = rwMapFiles[0];
         String[] nameParts = writableMapFile.getName().split("-");
         NAMESPACE namespace = NAMESPACE.valueOf(nameParts[0]);
         PROJECT project = PROJECT.valueOf(nameParts[1]);
         map = UuidSnomedMap.read(writableMapFile, namespace, project);

         for (File fixedMapFile: idSourceDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
               return f.getName().endsWith("sct-map.txt");
            }
            
         })) {
            UuidSnomedMap fixedMap = UuidSnomedMap.read(fixedMapFile);
            map.addFixedMap(fixedMap);
         }
      }
   
   
   
   }


   public String transform(String input) throws Exception {
      UUID id = UUID.fromString(input);
      return setLastTransform(Long.toString(map.getWithGeneration(id, getType())));
   }
   
   public void cleanup(Transform transformer) throws Exception {
      System.out.println("Writing map to file: " + writableMapFile);
      if (writableMapFile != null) {
         map.write(writableMapFile);
         map = null;
      }
   }

   protected abstract TYPE getType();

}
