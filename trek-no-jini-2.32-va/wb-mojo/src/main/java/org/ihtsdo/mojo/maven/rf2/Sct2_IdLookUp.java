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
public class Sct2_IdLookUp {

    private long sctIdArray[];
    private long uuidMsbArray[];
    private long uuidLsbArray[];

    public Sct2_IdLookUp(String filePathName)
            throws IOException {
        ArrayList<Sct2_IdCompact> idList = new ArrayList<>();
        ObjectInputStream ois;
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePathName)));
        try {
            Object obj;
            while ((obj = ois.readObject()) != null) {
                if (obj instanceof Sct2_IdCompact) {
                    idList.add((Sct2_IdCompact) obj);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Sct2_IdLookUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EOFException ex) {
            // getLog().info(" relationship count = " + count + " @EOF\r\n");
            ois.close();
        }
        setupArrays(idList);
    }

    public Sct2_IdLookUp(ArrayList<Sct2_IdCompact> idList) {
        setupArrays(idList);
    }

    private void setupArrays(ArrayList<Sct2_IdCompact> idList) {
        int countSctDuplicates = 0;
        int countSctPairUuidChanged = 0;
        StringBuilder sb = new StringBuilder();
        Collections.sort(idList); // required for binarySearch
        for (int i = 0; i < idList.size() - 1; i++) {
            if (idList.get(i).sctIdL == idList.get(i + 1).sctIdL) {
                countSctDuplicates++;
                boolean isNotChanged = true;
                if (idList.get(i).uuidMsbL != idList.get(i + 1).uuidMsbL
                        || idList.get(i).uuidLsbL != idList.get(i + 1).uuidLsbL) {
                    countSctPairUuidChanged++;
                    isNotChanged = false;
                }
                if (countSctDuplicates < 200) {
                    if (isNotChanged) {
                        sb.append("\r\nAMBIGUOUS PRIMORDIAL UUID ====\r\n");
                    } else {
                        sb.append("\r\nAMBIGUOUS PRIMORDIAL UUID\r\n");
                    }
                    sb.append(idList.get(i).sctIdL);
                    sb.append("\r\n");
                    UUID uuid1 = new UUID(idList.get(i).uuidMsbL,
                            idList.get(i).uuidLsbL);
                    UUID uuid2 = new UUID(idList.get(i + 1).uuidMsbL,
                            idList.get(i + 1).uuidLsbL);
                    sb.append(uuid1.toString());
                    sb.append("\r\n");
                    sb.append(uuid2.toString());
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
            throw new UnsupportedOperationException("duplicate sctids not supported");
        }
        this.sctIdArray = new long[idList.size()];
        this.uuidMsbArray = new long[idList.size()];
        this.uuidLsbArray = new long[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            Sct2_IdCompact sct2_IdCompact = idList.get(i);
            this.sctIdArray[i] = sct2_IdCompact.sctIdL;
            this.uuidMsbArray[i] = sct2_IdCompact.uuidMsbL;
            this.uuidLsbArray[i] = sct2_IdCompact.uuidLsbL;
        }
    }

    public UUID getUuid(String sctIdString) {
        return getUuid(Long.parseLong(sctIdString));
    }

    public UUID getUuid(long sctId) {
        int idx = Arrays.binarySearch(sctIdArray, sctId);
        if (idx >= 0) {
            long msb = uuidMsbArray[idx];
            long lsb = uuidLsbArray[idx];
            return new UUID(msb, lsb);
        } else {
            return null;
        }
    }
}
