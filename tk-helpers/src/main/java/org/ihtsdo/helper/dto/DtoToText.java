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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.dto.concept.TkConcept;

/**
 *
 * @author kec
 */
public class DtoToText {

    public static void convertChangeSet(File changeSetFile) throws IOException, ClassNotFoundException {
        convert(changeSetFile, true, false);
    }

    public static void convertDto(File changeSetFile) throws IOException, ClassNotFoundException {
        convert(changeSetFile, false, false);
    }

    private static void convert(File changeSetFile, boolean changeSet, boolean append) throws IOException, FileNotFoundException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(changeSetFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dataStream = new DataInputStream(bis);
        File textFile = new File(changeSetFile.getParentFile(),
                changeSetFile.getName() + ".txt");
        FileWriter textOut = new FileWriter(textFile, append);
        try {
            int count = 0;
            while (dataStream.available() > 0) {
                if (changeSet) {
                    long nextCommit = dataStream.readLong();
                    textOut.append("\n*******************************\n");
                    textOut.append(Integer.toString(count));
                    textOut.append(": ");
                    textOut.append(TimeHelper.formatDateForFile(nextCommit));
                }
                TkConcept eConcept = new TkConcept(dataStream);
                textOut.append("\n*******************************\n");
                textOut.append(eConcept.toString());
                count++;
            }
        } catch (EOFException ex) {
            // Nothing to do...
        } finally {
            dataStream.close();
            textOut.close();
        }
    }
}