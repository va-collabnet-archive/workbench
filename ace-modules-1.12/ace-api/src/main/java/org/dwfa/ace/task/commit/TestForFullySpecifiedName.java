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
package org.dwfa.ace.task.commit;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForFullySpecifiedName extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();

            Set<I_Position> allPositions = getPositions(termFactory);

            ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
            List<I_DescriptionTuple> descriptionTupleList = getDescriptionTupleList(concept, activeProfile);
            for (I_DescriptionTuple desc : descriptionTupleList) {
                descriptions.add(desc.getDescVersioned());
            }
            for (I_DescriptionVersioned desc : concept.getUncommittedDescriptions()) {
                descriptions.add(desc);
            }

            return testDescriptions(concept, descriptions, forCommit);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private List<I_DescriptionTuple> getDescriptionTupleList(final I_GetConceptData concept,
            final I_ConfigAceFrame activeProfile) throws IOException {

        Set<I_Position> allPositions = null;
        I_IntSet allTypes = null;
        boolean conflictResolvedLatestState = true;
        return concept.getDescriptionTuples(activeProfile.getAllowedStatus(), allTypes, allPositions,
            conflictResolvedLatestState);
    }

    private List<AlertToDataConstraintFailure> testDescriptions(I_GetConceptData concept,
            ArrayList<I_DescriptionVersioned> descriptions, boolean forCommit) throws Exception {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData fsn_type = getConceptSafe(termFactory,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        if (fsn_type == null)
            return alertList;
        I_IntSet actives = getActiveStatus(termFactory);
        HashMap<String, ArrayList<I_DescriptionVersioned>> langs = new HashMap<String, ArrayList<I_DescriptionVersioned>>();
        for (I_DescriptionVersioned desc : descriptions) {
            for (I_DescriptionPart part : desc.getVersions(true)) {
                if (!actives.contains(part.getStatusId()))
                    continue;
                if (part.getTypeId() == fsn_type.getConceptId()) {
                    if (part.getText().matches(".*\\(\\?+\\).*")) {
                        alertList.add(new AlertToDataConstraintFailure(
                            (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                      : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                            "<html>Unedited semantic tag", concept));
                    }
                    String lang = part.getLang();
                    if (langs.get(lang) != null) {
                        for (I_DescriptionVersioned d : langs.get(lang)) {
                            if (d.getDescId() != desc.getDescId()) {
                                alertList.add(new AlertToDataConstraintFailure(
                                    (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                              : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                                    "<html>More than one FSN for " + lang, concept));
                            }
                        }
                        langs.get(lang).add(desc);
                    } else {
                        ArrayList<I_DescriptionVersioned> dl = new ArrayList<I_DescriptionVersioned>();
                        dl.add(desc);
                        langs.put(lang, dl);
                    }

                    Hits hits = termFactory.doLuceneSearch("\""
                        + part.getText().replace("(", "\\(").replace(")", "\\)") + "\"");
                    // System.out.println("Found " + hits.length());
                    SEARCH: for (int i = 0; i < hits.length(); i++) {
                        // if (i == 10000)
                        // break;
                        Document doc = hits.doc(i);
                        int cnid = Integer.parseInt(doc.get("cnid"));
                        int dnid = Integer.parseInt(doc.get("dnid"));
                        if (cnid == concept.getConceptId())
                            continue;
                        I_DescriptionVersioned potential_fsn = termFactory.getDescription(dnid, cnid);
                        VERSIONS: for (I_DescriptionPart part_search : potential_fsn.getVersions()) {

                            if (part_search.equals(part)) {
                                continue VERSIONS;
                            }

                            if (actives.contains(part_search.getStatusId())
                                && part_search.getTypeId() == fsn_type.getConceptId()
                                && part_search.getText().equals(part.getText())
                                && part_search.getLang().equals(part.getLang())) {
                                alertList.add(new AlertToDataConstraintFailure(
                                    (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                              : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                                    "<html>FSN already used", concept));
                                break SEARCH;
                            }
                        }
                    }
                }
            }
        }
        if (langs.get("en") == null)
            alertList.add(new AlertToDataConstraintFailure(
                (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                          : AlertToDataConstraintFailure.ALERT_TYPE.WARNING), "<html>No FSN for en", concept));
        return alertList;
    }

}
