/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.helper.dto;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.TkConcept;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class DtoExtract {
   public static boolean extract(File changeSetFile, Collection<UUID> cUuids, File extractFile)
           throws IOException, ClassNotFoundException {
      boolean             foundConcept  = false;
      FileInputStream     fis           = new FileInputStream(changeSetFile);
      BufferedInputStream bis           = new BufferedInputStream(fis);
      DataInputStream     dataInStream  = new DataInputStream(bis);
      DataOutputStream    dataOutStream = null;

      try {
         int count = 0;

         while (dataInStream.available() > 0) {
            long      nextCommit = dataInStream.readLong();
            TkConcept eConcept   = new TkConcept(dataInStream);

            if (cUuids.contains(eConcept.getPrimordialUuid())) {
               if (dataOutStream == null) {
                  FileOutputStream     fos = new FileOutputStream(extractFile);
                  BufferedOutputStream bos = new BufferedOutputStream(fos);

                  dataOutStream = new DataOutputStream(bos);
               }

               dataOutStream.writeLong(nextCommit);
               eConcept.writeExternal(dataOutStream);
               foundConcept = true;
            }

            count++;
         }
      } catch (EOFException ex) {

         // Nothing to do...
      } finally {
         dataInStream.close();

         if (dataOutStream != null) {
            dataOutStream.close();
         }
      }

      return foundConcept;
   }
}
