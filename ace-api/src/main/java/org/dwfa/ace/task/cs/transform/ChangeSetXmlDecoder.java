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
package org.dwfa.ace.task.cs.transform;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

/**
 * Converts XML change sets back to binary
 * 
 * @author Dion McMurtrie
 * 
 */
@InputSuffix(".xml")
public class ChangeSetXmlDecoder extends ChangeSetTransformer {

    protected String outputSuffix = ".jcs";

    public void transform(Logger logger, File changeset) throws IOException, FileNotFoundException,
            ClassNotFoundException {

        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(changeset)));

        File outputFile = new File(changeset.getParent(), changeset.getName() + outputSuffix);

        ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
            outputFile)));

        int longCount = 0;
        int objectCount = 0;

        try {
            logger.info("Starting to process change set " + changeset);

            // the XMLDecoder lets you know you are at the end of the file when
            // an ArrayIndexOutOfBoundsException is thrown
            while (true) {
                Object obj = decoder.readObject();
                if (obj instanceof Long) {
                    logger.info("Writing Long value " + obj + " count " + ++longCount);
                    outputStream.writeLong((Long) obj);
                } else {
                    logger.info("Writing Object value " + obj + " count " + ++objectCount);
                    outputStream.writeObject(obj);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.info("End of file");
            decoder.close();
            outputStream.close();
        }

        logger.info("Change set " + changeset + " complete. " + objectCount + " objects read/written, " + longCount
            + " Long values read/written, " + (longCount + objectCount) + " total");
    }

}
