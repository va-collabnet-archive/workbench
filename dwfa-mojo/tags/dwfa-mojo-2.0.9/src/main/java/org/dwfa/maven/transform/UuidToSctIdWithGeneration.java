/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.maven.transform;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public abstract class UuidToSctIdWithGeneration extends AbstractTransform implements I_ReadAndTransform {
   
   
   private static Map<TYPE, UuidSnomedMap> mapMap;
   private static Map<TYPE, File> fileMap;
   private File sourceDirectory = null;
   
   public void setupImpl(Transform transformer) throws IOException {
      // Nothing to setup
      File buildDirectory = transformer.getBuildDirectory();
      //[INFO]  buildDirectory: /au-ct/ace/amt-release/target
      File idGeneratedDir = new File(new File(buildDirectory, "generated-resources"), "sct-uuid-maps");
      
      if (sourceDirectory==null) {
    	  sourceDirectory = transformer.getSourceDirectory();
      }
       //[INFO]  sourceDirectory: /au-ct/ace/amt-release/src/main/java
      if (mapMap == null) {
         mapMap = new HashMap<TYPE, UuidSnomedMap>();
         fileMap = new HashMap<TYPE, File>();
         File idSourceDir = new File(sourceDirectory.getParentFile(), "sct-uuid-maps");
         for (final TYPE type: TYPE.values()) {
            File[] rwMapFileArray = idSourceDir.listFiles(new FileFilter() {
               public boolean accept(File f) {
                  return f.getName().endsWith(type + "-sct-map-rw.txt");
               }
               
            });
            if (rwMapFileArray == null || rwMapFileArray.length != 1) {
               throw new IOException("RW mapping file not found. There must be one--and only one--file of format [namespace]-[project]-" + 
                     type + "-sct-map-rw.txt in the directory " + idSourceDir.getAbsolutePath());
            }
            fileMap.put(type, rwMapFileArray[0]);
             String[] nameParts = fileMap.get(type).getName().split("-");
            NAMESPACE namespace = NAMESPACE.valueOf(nameParts[0]);
            PROJECT project = PROJECT.valueOf(nameParts[1]);
            mapMap.put(type, UuidSnomedMap.read(fileMap.get(type), namespace, project));
         }
         for (File fixedMapFile: idSourceDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
               return f.getName().endsWith("sct-map.txt");
            }
            
         })) {
            UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
            for (TYPE type: TYPE.values()) {
               mapMap.get(type).addFixedMap(fixedMap);
            }
         }
         if (idGeneratedDir.listFiles() != null) {
            for (File fixedMapFile: idGeneratedDir.listFiles(new FileFilter() {
               public boolean accept(File f) {
                  return f.getName().endsWith("sct-map.txt");
               }
               
            })) {
               UuidSnomedMap fixedMap = UuidSnomedMap.read(fixedMapFile);
               for (TYPE type: TYPE.values()) {
                  mapMap.get(type).addFixedMap(fixedMap);
               }
            }
         }
      }
   }


   public String transform(String input) throws Exception {
      UUID id = UUID.fromString(input);
      TYPE type = getType();
      return setLastTransform(Long.toString(mapMap.get(type).getWithGeneration(id, type)));
   }
   
   public void cleanup(Transform transformer) throws Exception {
      if (mapMap != null) {
         for (TYPE type: TYPE.values()) {
            mapMap.get(type).write(fileMap.get(type));
         }      
         mapMap = null;
         fileMap = null;
      }
   }

   protected abstract TYPE getType();

}
