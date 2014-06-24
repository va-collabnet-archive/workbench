/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.dto;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.dto.concept.TkConcept;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.UUID;


/**
 * The Class DtoToText contains methods for converting a changeset or eConcept
 * file to text.
 *
 */
public class DtoToText {

    /**
     * Converts the given
     * <code>changeSetFile</code> to text.
     *
     * @param changeSetFile the file to convert
     * @param changeSet set to <code>true</code> to indicate the file is for a
     * changeset and not an eCocnept
     * @param append if <code>true</code>, then bytes will be written to the end
     * of the file rather than the beginning
     * @throws IOException signals that an I/O exception has occurred
     * @throws FileNotFoundException if a specified file was not found
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private static void convert(File changeSetFile, boolean changeSet, boolean append)
            throws IOException, FileNotFoundException, ClassNotFoundException {
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

    /**
     * Converts a change set file to text.
     *
     * @param changeSetFile the change set file to convert
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static void convertChangeSet(File changeSetFile) throws IOException, ClassNotFoundException {
        convert(changeSetFile, true, false);
    }

    /**
     * Convert an eConcept file to text.
     *
     * @param changeSetFile the change set file
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static void convertDto(File changeSetFile) throws IOException, ClassNotFoundException {
        convert(changeSetFile, false, false);
    }

    /**
     * Searches the given changeset or eConcept file for the specified
     * <code>conceptUuids</code>.
     *
     * @param changeSetFile the file to convert
     * @param conceptUuids the concept uuids to find
     * @param append if <code>true</code>, then bytes will be written to the end
     * of the file rather than the beginning
     * @throws IOException signals that an I/O exception has occurred
     * @throws FileNotFoundException if a specified file was not found
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private static void search(File changeSetFile, Collection<UUID> conceptUuids, boolean append)
            throws IOException, FileNotFoundException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(changeSetFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dataStream = new DataInputStream(bis);
        File textFile = new File(changeSetFile.getParentFile(),
                changeSetFile.getName() + "-search.txt");
        FileWriter textOut = new FileWriter(textFile, append);

        try {
            int count = 0;

            while (dataStream.available() > 0) {
                TkConcept eConcept = new TkConcept(dataStream);

                if (conceptUuids.contains(eConcept.getPrimordialUuid())) {
                    textOut.append("\n*******************************\n");
                    textOut.append(eConcept.toString());
                }

                count++;
            }
        } catch (EOFException ex) {
            // Nothing to do...
        } finally {
            dataStream.close();
            textOut.close();
        }
    }

    /**
     * Searches the eConcept file for the given <code>conceptUuids</code>.
     *
     * @param file the eConcept file to search
     * @param conceptUuids the concept uuids to find
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static void searchForDto(File file, Collection<UUID> conceptUuids)
            throws IOException, ClassNotFoundException {
        search(file, conceptUuids, false);
    }
}
