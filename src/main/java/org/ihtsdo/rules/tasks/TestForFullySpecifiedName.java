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
package org.ihtsdo.rules.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lucene.SearchResult;

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

            ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
            List<? extends I_DescriptionTuple> descriptionTupleList =
                    getDescriptionTupleList(concept, getFrameConfig());
            for (I_DescriptionTuple desc : descriptionTupleList) {
                descriptions.add(desc.getDescVersioned());
            }
            return testDescriptions(concept, descriptions, forCommit);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private List<? extends I_DescriptionTuple> getDescriptionTupleList(final I_GetConceptData concept,
            final I_ConfigAceFrame activeProfile) throws IOException {

        PositionSetReadOnly allPositions = null;
        I_IntSet allTypes = null;
        return concept.getDescriptionTuples(activeProfile.getAllowedStatus(), allTypes, allPositions, getFrameConfig()
            .getPrecedence(), getFrameConfig().getConflictResolutionStrategy());
    }

    private List<AlertToDataConstraintFailure> testDescriptions(I_GetConceptData concept,
            ArrayList<I_DescriptionVersioned> descriptions, boolean forCommit) throws Exception {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
        I_GetConceptData fsn_type =
                getConceptSafe(Terms.get(), ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        if (fsn_type == null)
            return alertList;
        I_GetConceptData snomedRoot = getConceptSafe(Terms.get(), SNOMED.Concept.ROOT.getUids());
        if (snomedRoot == null)
            return alertList;
        if (!snomedRoot.isParentOfOrEqualTo(concept, getFrameConfig().getAllowedStatus(), getFrameConfig()
            .getDestRelTypes(), getFrameConfig().getViewPositionSetReadOnly(), getFrameConfig().getPrecedence(),
            getFrameConfig().getConflictResolutionStrategy())) {
            return alertList;
        }
        I_IntSet actives = getActiveStatus(Terms.get());
        HashMap<String, ArrayList<I_DescriptionVersioned>> langs =
                new HashMap<String, ArrayList<I_DescriptionVersioned>>();
        for (I_DescriptionVersioned<?> desc : descriptions) {
            for (I_DescriptionPart part : desc.getMutableParts()) {
                if (!actives.contains(part.getStatusNid()))
                    continue;
                if (part.getTypeNid() == fsn_type.getConceptNid()) {
                    if (part.getText().matches(".*\\(\\?+\\).*") && part.getTime() == Long.MAX_VALUE) {
                        alertList.add(new AlertToDataConstraintFailure(
                            (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                      : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                            "<html>Unedited semantic tag: " + part.getText(), concept));
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
                    if (part.getTime() == Long.MAX_VALUE && !part.getText().equals("New Fully Specified Description")) {
                        String filteredDescription = part.getText();
                        // remove all non-alphanumeric characters and replace with a space - this is to stop these
                        // characters causing issues with the lucene search
                        // filteredDescription = filteredDescription.replaceAll("[^a-zA-Z0-9]", " ");
                        // new removal using native lucene escaping 
                        filteredDescription = QueryParser.escape(filteredDescription);
                        SearchResult result = Terms.get().doLuceneSearch(filteredDescription);
                        search: for (int i = 0; i < result.topDocs.totalHits; i++) {
                            Document doc = result.searcher.doc(i);
                            int cnid = Integer.parseInt(doc.get("cnid"));
                            int dnid = Integer.parseInt(doc.get("dnid"));
                            if (cnid == concept.getConceptNid())
                                continue;
                            try {
                                I_DescriptionVersioned<?> potential_fsn = Terms.get().getDescription(dnid, cnid);
                                if (potential_fsn != null) {
                                    for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
                                        if (actives.contains(part_search.getStatusNid())
                                            && part_search.getTypeNid() == fsn_type.getConceptNid()
                                            && part_search.getText().equals(part.getText())
                                            && part_search.getLang().equals(part.getLang())) {
                                            alertList.add(new AlertToDataConstraintFailure(
                                                (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                                          : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                                                "<html>FSN already used: " + part.getText(), concept));
                                            break search;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                AceLog.getAppLog().alertAndLogException(e);
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
