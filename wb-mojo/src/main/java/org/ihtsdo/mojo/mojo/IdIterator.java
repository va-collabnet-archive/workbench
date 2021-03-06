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
package org.ihtsdo.mojo.mojo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class IdIterator implements I_ProcessIds {

    private BufferedWriter output = null;
    private int count = 0;
    private I_TermFactory termFactory = LocalVersionedTerminology.get();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private final BufferedWriter mapfile;
    private Collection<UUID> snomedIdUuids;

    public IdIterator(BufferedWriter output, BufferedWriter mapfile) throws IOException {
        System.out.println("IdIterator - blah");
        this.mapfile = mapfile;
        this.output = output;
        snomedIdUuids = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids();
    }

    public void processId(I_Identify idv) throws Exception {

        for (UUID uuid : idv.getUUIDs()) {
            for (I_IdPart idvPart : idv.getMutableIdParts()) {

                Date date = new Date(ThinVersionHelper.convert(idvPart.getVersion()));

                if (snomedSource(idvPart)) {
                    mapfile.write(uuid.toString());
                    mapfile.newLine();
                    mapfile.write(idvPart.getDenotation().toString());
                    mapfile.newLine();
                }

                if (termFactory.hasConcept(idvPart.getAuthorityNid())) {
                    UUID sourceUuid = getFirstUuid(idvPart.getAuthorityNid());
                    UUID statusUuid = getFirstUuid(idvPart.getStatusId());
                    UUID pathUuid = getFirstUuid(idvPart.getPathId());
                    output.write(uuid.toString() + "\t" + sourceUuid + "\t" + idvPart.getDenotation() + "\t" + statusUuid
                        + "\t" + dateFormat.format(date) + "\t" + pathUuid);

                    output.newLine();
                } else {
                    System.out.println("WARNING: id " + idv.getUUIDs() + " has source id " + idvPart.getDenotation()
                        + " but the source " + idvPart.getAuthorityNid() + " does not map to a source concept - skipping");
                    try {
                        System.out.println("the uuids do map to a concept - " + termFactory.getConcept(idv.getUUIDs()));
                    } catch (Exception e) {
                        System.out.println("the uuids do not map to a concept");
                        e.printStackTrace();
                    }
                }
            }
        }

        if (++count % 1000 == 0) {
            System.out.println("processed id number " + count);
        }

    }

    private boolean snomedSource(I_IdPart idvPart) throws TerminologyException, IOException {
        if (termFactory.hasConcept(idvPart.getAuthorityNid())) {
            for (UUID uuid : termFactory.getUids(idvPart.getAuthorityNid())) {
                if (snomedIdUuids.contains(uuid)) {
                    return true;
                }
            }
        } else {
            System.out.println("no concept for source, id was " + idvPart.getDenotation());
        }
        return false;
    }

    private UUID getFirstUuid(int nid) throws TerminologyException, IOException {
        return termFactory.getUids(nid).iterator().next();
    }
}
