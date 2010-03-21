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

import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@InputSuffix(".cmrscs")
public class CmrscsXmlEncoder extends ChangeSetTransformer {

    private final Long END_OF_CHANGESET_MARKER = Long.MAX_VALUE;

    private final Object END_OF_REFSET_MARKER = new UUID(0, 0);

    @Override
    public void transform(Logger logger, File changeset) throws IOException, FileNotFoundException,
            ClassNotFoundException {

        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(changeset)));

        XMLEncoder encoder = getEncoder(changeset);

        try {

            logger.info("Starting to process change set " + changeset);

            int i = 1;
            long timestamp = System.currentTimeMillis();
            Long time = dis.readLong();

            while (time != END_OF_CHANGESET_MARKER) {

                Object pathUuid = readUuid(dis);
                Object refsetUuid = readUuid(dis);

                encoder.writeObject(time);
                encoder.writeObject(pathUuid);
                encoder.writeObject(refsetUuid);

                Object memberUuid = readUuid(dis);

                while (!END_OF_REFSET_MARKER.equals(memberUuid)) {

                    Object componentUuid = readUuid(dis);
                    Object statusUuid = readUuid(dis);
                    Object conceptValueUuid = readUuid(dis);

                    encoder.writeObject(memberUuid);
                    encoder.writeObject(componentUuid);
                    encoder.writeObject(statusUuid);
                    encoder.writeObject(conceptValueUuid);

                    if (i++ % 100 == 0) {
                        encoder.flush();
                        long duration = (System.currentTimeMillis() - timestamp);
                        timestamp = System.currentTimeMillis();
                        logger.info("Object " + i + " processed, in " + duration + "ms");
                    }

                    memberUuid = readUuid(dis);
                }

                time = dis.readLong();
            }
        } catch (EOFException ex) {
            dis.close();
            logger.info("End of member refset change set " + changeset);
        }
        encoder.close();

    }

    private UUID readUuid(DataInputStream dis) throws IOException {
        return new UUID(dis.readLong(), dis.readLong());
    }

}
