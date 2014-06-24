/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.log.AceLog;

/**
 *
 * @author Marc Campbell
 */
public class Sct2_UuidUuidRemapper {

    private UUID uuidComputedArray[];
    private UUID uuidDeclaredArray[];

    public Sct2_UuidUuidRemapper(String filePathName)
            throws IOException {
        ArrayList<Sct2_UuidUuidRecord> idList = new ArrayList<>();
        ObjectInputStream ois;
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePathName)));
        try {
            Object obj;
            while ((obj = ois.readObject()) != null) {
                if (obj instanceof Sct2_UuidUuidRecord) {
                    idList.add((Sct2_UuidUuidRecord) obj);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Sct2_UuidUuidRemapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EOFException ex) {
            // getLog().info(" relationship count = " + count + " @EOF\r\n");
            ois.close();
        }
        setupArrays(idList);
    }

    public Sct2_UuidUuidRemapper(ArrayList<Sct2_UuidUuidRecord> idList) {
        setupArrays(idList);
    }

    private void setupArrays(ArrayList<Sct2_UuidUuidRecord> idList) {
        int countSctDuplicates = 0;
        int countSctPairUuidChanged = 0;
        StringBuilder sb = new StringBuilder();
        Collections.sort(idList); // required for binarySearch
        for (int i = 0; i < idList.size() - 1; i++) {
            if (idList.get(i).uuidComputed.compareTo(idList.get(i + 1).uuidComputed) == 0) {
                countSctDuplicates++;
                boolean isNotChanged = true;
                if (idList.get(i).uuidDeclared.compareTo(idList.get(i + 1).uuidDeclared) == 0) {
                    countSctPairUuidChanged++;
                    isNotChanged = false;
                }
                if (countSctDuplicates < 200) {
                    if (isNotChanged) {
                        sb.append("\r\nAMBIGUOUS PRIMORDIAL UUID ====\r\n");
                    } else {
                        sb.append("\r\nAMBIGUOUS PRIMORDIAL UUID\r\n");
                    }
                    sb.append(idList.get(i).uuidComputed);
                    sb.append("\t");
                    sb.append(idList.get(i).uuidDeclared);
                    sb.append("\r\n");
                }
            }
        }
        sb.append("\r\n::: countSctDuplicates = ");
        sb.append(countSctDuplicates);
        sb.append("\r\n::: countSctPairUuidChanged = ");
        sb.append(countSctPairUuidChanged);
        sb.append("\r\n");
        AceLog.getAppLog().log(Level.INFO, sb.toString());
        if (countSctDuplicates > 0) {
            throw new UnsupportedOperationException("duplicate uuids not supported");
        }
        this.uuidComputedArray = new UUID[idList.size()];
        this.uuidDeclaredArray = new UUID[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            Sct2_UuidUuidRecord sct2_UuidUuidRecord = idList.get(i);
            this.uuidComputedArray[i] = sct2_UuidUuidRecord.uuidComputed;
            this.uuidDeclaredArray[i] = sct2_UuidUuidRecord.uuidDeclared;
        }
    }

    public UUID getUuid(String uuidString) {
        return getUuid(UUID.fromString(uuidString));
    }

    public UUID getUuid(UUID cUuid) {
        int idx = Arrays.binarySearch(uuidComputedArray, cUuid);
        if (idx >= 0) {
            return uuidDeclaredArray[idx];
        } else {
            return null;
        }
    }
}
