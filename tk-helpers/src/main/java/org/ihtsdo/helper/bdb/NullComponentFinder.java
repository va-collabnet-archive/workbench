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
package org.ihtsdo.helper.bdb;

//~--- non-JDK imports --------------------------------------------------------
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author kec
 */
public class NullComponentFinder implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger dots = new AtomicInteger(0);
    ConcurrentSkipListSet<UUID> allPrimUuids = new ConcurrentSkipListSet<UUID>();
    ConcurrentSkipListSet<UUID> dupUuids = new ConcurrentSkipListSet<UUID>();
    File dupsFile = new File("dups.oos");
    private final NidBitSetBI nidset;

    //~--- constant enums ------------------------------------------------------
    private enum PASS {

        PASS_ONE, PASS_TWO
    }

    //~--- constructors --------------------------------------------------------
    public NullComponentFinder() throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
    private void verifyComponent(ComponentChroncileBI component) throws IOException {
        if (component != null) {
        	if (component instanceof ConAttrChronicleBI) {
        		ConAttrChronicleBI attr = (ConAttrChronicleBI) component;
        		for (ConAttrVersionBI loopVersion : attr.getVersions()) {
        			verifyNids(loopVersion.getAllNidsForVersion());
            	}
        	} else if (component instanceof DescriptionChronicleBI) {
        		DescriptionChronicleBI desc = (DescriptionChronicleBI) component;
        		for (DescriptionVersionBI loopVersion : desc.getVersions()) {
        			verifyNids(loopVersion.getAllNidsForVersion());
            	}
        	} else if (component instanceof RelationshipChronicleBI) {
        		RelationshipChronicleBI rel = (RelationshipChronicleBI) component;
        		for (RelationshipVersionBI loopVersion : rel.getVersions()) {
        			verifyNids(loopVersion.getAllNidsForVersion());
            	}
        	} else if (component instanceof MediaChronicleBI) {
        		MediaChronicleBI media = (MediaChronicleBI) component;
        		for (MediaVersionBI loopVersion : media.getVersions()) {
        			verifyNids(loopVersion.getAllNidsForVersion());
            	}
        	}  else if (component instanceof RefexChronicleBI) {
        		RefexChronicleBI refex = (RefexChronicleBI) component;
        		for (Object loopVersion : refex.getVersions()) {
        			RefexVersionBI loopRefversion = (RefexVersionBI) loopVersion;
        			verifyNids(loopRefversion.getAllNidsForVersion());
            	}
        	}

            for (ComponentChroncileBI annotation : component.getAnnotations()) {
                verifyComponent(annotation);
            }
        }
    }
    
    private void verifyNids(Set<Integer> nids) {
    	for (Integer nid : nids) {
    		ComponentChroncileBI<?> component = null;
    		try {
				component = Ts.get().getComponent(nid);
			} catch (IOException e) {
				AceLog.getAppLog().warning(e.getMessage());
			}
			if (component == null) {
				AceLog.getAppLog().warning("No component for Nid: " + nid);
			}
    	}
    	
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    private void processConcept(ConceptChronicleBI concept) throws IOException {

        // add prim uuids to list
        // concept attributtes
        verifyComponent(concept.getConAttrs());

        // descriptions
        for (DescriptionChronicleBI desc : concept.getDescs()) {
            verifyComponent(desc);
        }

        // relationships
        for (RelationshipChronicleBI rel : concept.getRelsOutgoing()) {
            verifyComponent(rel);
        }

        // media
        for (MediaChronicleBI media : concept.getMedia()) {
            verifyComponent(media);
        }

        if (!concept.isAnnotationStyleRefex()) {
            for (RefexChronicleBI refex : concept.getRefsetMembers()) {
                verifyComponent(refex);
            }
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        count.incrementAndGet();

        if (count.get() % 1000 == 0) {
            System.out.print(".");
            System.out.flush();
            dots.incrementAndGet();

            if (dots.get() > 80) {
                dots.set(0);
                System.out.println();
                System.out.print(count.get() + ": ");
            }
        }

        processConcept(fetcher.fetch());
    }

    public void writeDupFile() throws IOException { //TODO: implement for null components
        count.set(0);

        FileOutputStream fos = new FileOutputStream(dupsFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(dupUuids);
        } finally {
            oos.close();
        }
    }

    //~--- get methods ---------------------------------------------------------
    public ConcurrentSkipListSet<UUID> getDupUuids() {
        return dupUuids;
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }
}
