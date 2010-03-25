/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.maven.flatfile.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public abstract class UuidToSctIdWithGeneration extends AbstractTransform implements I_ReadAndTransform {

   private File sourceDirectory = null;
   static UuidSnomedMapHandler map;

   public void setupImpl(Transform transformer) throws IOException {
      // Nothing to setup
      File buildDirectory = transformer.getBuildDirectory();
      //[INFO]  buildDirectory: /au-ct/ace/amt-release/target
      File idGeneratedDir = new File(new File(buildDirectory, "generated-resources"), "sct-uuid-maps");

      if (sourceDirectory==null) {
          sourceDirectory = transformer.getSourceDirectory();
      }

      initMap(idGeneratedDir, sourceDirectory);
   }

   private static synchronized void initMap(File idGeneratedDir, File sourceDirectory) throws IOException {
 	  if (map == null) {
		  map = new UuidSnomedMapHandler(idGeneratedDir, sourceDirectory);
	  }
   }

   public String transform(String input) throws Exception {
      UUID id = UUID.fromString(input);
      TYPE type = getType();
      return setLastTransform(Long.toString(map.getWithGeneration(id, type)));
   }

   public Long getMappedId(UUID id, TYPE type) {
	   return map.getWithoutGeneration(id, type);
   }

   public void cleanup(Transform transformer) throws Exception {
      if (map != null) {
         map.writeMaps();
      }
   }

   protected abstract TYPE getType();

}
