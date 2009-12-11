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
package org.dwfa.ace.task.cs;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.logging.Logger;

public class ChangeSetXmlEncoder {

    private String outputSuffix = ".xml";

    public void createXmlCopy(Logger logger, File changeset)
            throws IOException, FileNotFoundException, ClassNotFoundException {
        ObjectInputStream ois =
                new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(changeset)));

        XMLEncoder encoder = getEncoder(changeset);

        try {

            logger.info("Starting to process change set " + changeset);

            Object obj = ois.readObject();
            encoder.writeObject(obj);

            Long time = ois.readLong();
            int i = 1;
            long timestamp;
            while (time != Long.MAX_VALUE) {
                obj = ois.readObject();
                encoder.writeObject(time);
                encoder.writeObject(obj);

                if (i % 100 == 0) {
                    encoder.flush();
                    timestamp = System.currentTimeMillis();
                    obj = ois.readObject();
                    logger.info("Object " + i++ + " processed, in "
                        + (System.currentTimeMillis() - timestamp) + "ms");
                }

                time = ois.readLong();
            }
        } catch (EOFException ex) {
            ois.close();
            logger.info("End of change set " + changeset);
        }
        encoder.close();
    }

    private XMLEncoder getEncoder(File changeset) throws FileNotFoundException {
        XMLEncoder encoder =
                new XMLEncoder(new BufferedOutputStream(new FileOutputStream(
                    new File(changeset.getParent(), changeset.getName()
                        + outputSuffix))));

        encoder.setPersistenceDelegate(UUID.class,
            new java.beans.PersistenceDelegate() {
                protected Expression instantiate(Object oldInstance, Encoder out) {
                    return new Expression(oldInstance, oldInstance.getClass(),
                        "fromString", new Object[] { oldInstance.toString() });
                }
            });
        return encoder;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void setOutputSuffix(String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }
}
