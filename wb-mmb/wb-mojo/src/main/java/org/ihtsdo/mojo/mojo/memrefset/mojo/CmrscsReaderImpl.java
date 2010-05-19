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
package org.ihtsdo.mojo.mojo.memrefset.mojo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.mojo.mojo.memrefset.mojo.builder.ChangeSetBuilder;
import org.ihtsdo.mojo.mojo.memrefset.mojo.builder.CmrscsResultBuilder;
import org.ihtsdo.mojo.mojo.memrefset.mojo.builder.NullChangeSetBuilder;

public final class CmrscsReaderImpl implements CmrscsReader {

    private final long END_OF_CHANGESET_MARKER = Long.MAX_VALUE;

    private final Object END_OF_REFSET_MARKER = new UUID(0, 0);

    public CmrscsResult read(final String fileName) {
        DataInputStream in = open(fileName);
        CmrscsResult result = readCmrscsFile(in);
        close(in);
        return result;
    }

    private DataInputStream open(final String fileName) {
        try {
            return new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
        } catch (FileNotFoundException e) {
            throw new CmrscsReaderException(e);
        }
    }

    private CmrscsResult readCmrscsFile(final DataInputStream in) {
        CmrscsResultBuilder builder = new CmrscsResultBuilder();
        ChangeSetBuilder changeSetBuilder = new NullChangeSetBuilder();

        try {
            Long time = in.readLong();

            while (time != END_OF_CHANGESET_MARKER) {
                changeSetBuilder = builder.openChangeSet().withTime(time).withPathUUID(readUuid(in)).withRefsetUUID(
                    readUuid(in));
                UUID memberUuid = readUuid(in);

                while (!END_OF_REFSET_MARKER.equals(memberUuid)) {

                    changeSetBuilder.openRefset()
                        .withComponentUUID(readUuid(in))
                        .withMemberUUID(memberUuid)
                        .withStatusUUID(readUuid(in))
                        .withConceptUUID(readUuid(in))
                        .closeRefset();

                    memberUuid = readUuid(in);
                }

                changeSetBuilder.closeChangeSet();
                changeSetBuilder = new NullChangeSetBuilder();
                time = in.readLong();
            }

            return builder.build();
        } catch (IOException e) {
            changeSetBuilder.closeChangeSet();
            return builder.build();
        }
    }

    private void close(final DataInputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            // do nothing.
        }
    }

    private UUID readUuid(final DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    // public static void main(String[] args) {
    // CmrscsReader reader = new CmrscsReaderImpl();
    // CmrscsResult result =
    // reader.read("/home/sanjiv/Projects/Nehta/au-ct/branches/dev-1.0/e5822c48-5386-4111-8a33-b5a15d5c4727.20081110T000512.cmrscs");
    // XStream xstream = new XStream();
    // xstream.alias("CmrscsResult", CmrscsResultImpl.class);
    // xstream.alias("ChangeSet", ChangeSet.class);
    // xstream.alias("RefSet", RefSet.class);
    // new TextFileWriterImpl(new
    // FileUtilImpl()).write("/home/sanjiv/Projects/Nehta/au-ct/branches/dev-1.0/testing567/e5822c48-5386-4111-8a33-b5a15d5c4727.20081110T000512.xml",
    // xstream.toXML(result));
    // }
}
