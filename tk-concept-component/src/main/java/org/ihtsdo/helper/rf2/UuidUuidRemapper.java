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
package org.ihtsdo.helper.rf2;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Campbell
 */
public class UuidUuidRemapper {

    public UUID[] uuidComputedArrayLookupByComputed;
    public UUID[] uuidDeclaredArrayLookupByComputed;
    public UUID[] uuidComputedArrayLookupByDeclared;
    public UUID[] uuidDeclaredArrayLookupByDeclared;

    public UuidUuidRemapper(String filePathName)
            throws IOException {
        ArrayList<UuidUuidRecord> idList = new ArrayList<>();
        ObjectInputStream ois;
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePathName)));
        try {
            Object obj;
            while ((obj = ois.readObject()) != null) {
                if (obj instanceof UuidUuidRecord) {
                    idList.add((UuidUuidRecord) obj);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UuidUuidRemapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EOFException ex) {
            // getLog().info(" relationship count = " + count + " @EOF\r\n");
            ois.close();
        }
        setupArrays(idList);
    }

    public UuidUuidRemapper(ArrayList<UuidUuidRecord> idList) {
        setupArrays(idList);
    }
    
    public void setupReverseLookup() {
        ArrayList<UuidUuidRecord> idList = new ArrayList<>();
        for (int i = 0; i < uuidComputedArrayLookupByComputed.length; i++) {
            idList.add(new UuidUuidRecord(uuidComputedArrayLookupByComputed[i], 
                    uuidDeclaredArrayLookupByComputed[i]));
        }
        
        Comparator<UuidUuidRecord> byDeclaredComparator = new Comparator<UuidUuidRecord>() {

            @Override
            public int compare(UuidUuidRecord o1, UuidUuidRecord o2) {
                int more = 1;
                int less = -1;
                if (o1.uuidDeclared.compareTo(o2.uuidDeclared) < 0) {
                    return less; // instance less than received
                } else if (o1.uuidDeclared.compareTo(o2.uuidDeclared) > 0) {
                    return more; // instance greater than received
                } else {
                    if (o1.uuidComputed.compareTo(o2.uuidComputed) < 0) {
                        return less; // instance less than received
                    } else if (o1.uuidComputed.compareTo(o2.uuidComputed) > 0) {
                        return more; // instance greater than received
                    } else {
                        return 0; // instance == received
                    }
                }
            }
        };
        Collections.sort(idList, byDeclaredComparator);
    
        int countDuplicates = 0;
        int countPairUuidChanged = 0;
        StringBuilder sb = new StringBuilder();
        // check for duplicates
        for (int i = 0; i < idList.size() - 1; i++) {
            if (idList.get(i).uuidComputed.compareTo(idList.get(i + 1).uuidComputed) == 0) {
                countDuplicates++;
                boolean isNotChanged = true;
                if (idList.get(i).uuidDeclared.compareTo(idList.get(i + 1).uuidDeclared) == 0) {
                    countPairUuidChanged++;
                    isNotChanged = false;
                }
                if (countDuplicates < 200) {
                    if (isNotChanged) {
                        sb.append("\r\n::: reverse lookup :: AMBIGUOUS REVERSE PRIMORDIAL UUID ====\r\n");
                    } else {
                        sb.append("\r\n::: reverse lookup :: AMBIGUOUS PRIMORDIAL UUID\r\n");
                    }
                    sb.append(idList.get(i).uuidComputed);
                    sb.append("\t");
                    sb.append(idList.get(i).uuidDeclared);
                    sb.append("\r\n");
                }
            }
        }
        sb.append("\r\n::: reverse lookup :: countDuplicates = ");
        sb.append(countDuplicates);
        sb.append("\r\n::: reverse lookup :: countPairUuidChanged = ");
        sb.append(countPairUuidChanged);
        sb.append("\r\n");
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.info(sb.toString());
        if (countDuplicates > 0) {
            throw new UnsupportedOperationException(" reverse lookup :: duplicate uuids not supported");
        }
        this.uuidComputedArrayLookupByDeclared = new UUID[idList.size()];
        this.uuidDeclaredArrayLookupByDeclared = new UUID[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            UuidUuidRecord uuidUuidRecord = idList.get(i);
            this.uuidComputedArrayLookupByDeclared[i] = uuidUuidRecord.uuidComputed;
            this.uuidDeclaredArrayLookupByDeclared[i] = uuidUuidRecord.uuidDeclared;
        }
    }

    private void setupArrays(ArrayList<UuidUuidRecord> idList) {
        int countDuplicates = 0;
        int countPairUuidChanged = 0;
        StringBuilder sb = new StringBuilder();
        Collections.sort(idList); // required for binarySearch
        for (int i = 0; i < idList.size() - 1; i++) {
            if (idList.get(i).uuidComputed.compareTo(idList.get(i + 1).uuidComputed) == 0) {
                countDuplicates++;
                boolean isNotChanged = true;
                if (idList.get(i).uuidDeclared.compareTo(idList.get(i + 1).uuidDeclared) == 0) {
                    countPairUuidChanged++;
                    isNotChanged = false;
                }
                if (countDuplicates < 200) {
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
        sb.append("\r\n::: countDuplicates = ");
        sb.append(countDuplicates);
        sb.append("\r\n::: countPairUuidChanged = ");
        sb.append(countPairUuidChanged);
        sb.append("\r\n");
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.info(sb.toString());
        if (countDuplicates > 0) {
            throw new UnsupportedOperationException("duplicate uuids not supported");
        }
        this.uuidComputedArrayLookupByComputed = new UUID[idList.size()];
        this.uuidDeclaredArrayLookupByComputed = new UUID[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            UuidUuidRecord uuidUuidRecord = idList.get(i);
            this.uuidComputedArrayLookupByComputed[i] = uuidUuidRecord.uuidComputed;
            this.uuidDeclaredArrayLookupByComputed[i] = uuidUuidRecord.uuidDeclared;
        }
    }

    public UUID getUuid(String uuidString) {
        return getUuid(UUID.fromString(uuidString));
    }

    public UUID getUuid(UUID cUuid) {
        int idx = Arrays.binarySearch(uuidComputedArrayLookupByComputed, cUuid);
        if (idx >= 0) {
            return uuidDeclaredArrayLookupByComputed[idx];
        } else {
            return null;
        }
    }
    
    public UUID getComputedUuid(UUID cUuid) {
        int idx = Arrays.binarySearch(uuidDeclaredArrayLookupByDeclared, cUuid);
        if (idx >= 0) {
            return uuidComputedArrayLookupByDeclared[idx];
        } else {
            return null;
        }
    }
}
