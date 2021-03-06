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
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
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
    BufferedWriter uuidsWriter;
    protected File additionalUuidsFile;

    public Sct2_IdLookUp(String filePathName)
            throws IOException {
        int indexOf = filePathName.indexOf("target");
        String uuidsFileName = filePathName.substring(0, indexOf);
        uuidsFileName = uuidsFileName + "/target/input-files/generated-arf/additional.txt";
        additionalUuidsFile = new File(uuidsFileName);
        FileOutputStream uuidsOs = new FileOutputStream(additionalUuidsFile);
        uuidsWriter = new BufferedWriter(new OutputStreamWriter(uuidsOs, "UTF8"));
        
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

    public Sct2_IdLookUp(ArrayList<Sct2_IdCompact> idList) throws IOException {
        setupArrays(idList);
    }

    private void setupArrays(ArrayList<Sct2_IdCompact> idList) throws IOException {
        int countSctDuplicates = 0;
        int countSctPairUuidChanged = 0;
        StringBuilder sb = new StringBuilder();
        ArrayList<Sct2_IdCompact> tempIdList = new ArrayList<>(idList);
        Collections.sort(idList); // required for binarySearch
        Collections.sort(tempIdList);
        for (int i = 0; i < tempIdList.size() - 1; i++) {
            if (tempIdList.get(i).sctIdL == tempIdList.get(i + 1).sctIdL) {
                //remove and write to additional ids file
                idList.remove(i);
                Sct2_IdCompact sct = tempIdList.get(i);
                uuidsWriter.write(tempIdList.get(i).toString());
            }
        }
        uuidsWriter.flush();
        uuidsWriter.close();
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
