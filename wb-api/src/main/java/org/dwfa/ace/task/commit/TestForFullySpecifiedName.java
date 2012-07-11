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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
/*
 * See TestForPreferredTermValue for comments first.
 */
public class TestForFullySpecifiedName extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private ViewCoordinate vc;

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
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit) throws TaskFailedException {
        try {
            vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, concept.getNid());
            Collection<? extends DescriptionVersionBI> descsActive = cv.getDescriptionsActive();
            ArrayList<DescriptionVersionBI> descriptions = new ArrayList<DescriptionVersionBI>();
            for (DescriptionVersionBI desc : descsActive) {
                descriptions.add(desc);
            }
            
            // only test of concepts have been added to the description. 
            if (descriptions.size() > 0) {
                return testDescriptions(cv, descsActive, forCommit);
            }
            return new ArrayList<AlertToDataConstraintFailure>();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private List<AlertToDataConstraintFailure> testDescriptions(ConceptVersionBI cv,
            Collection<? extends DescriptionVersionBI> descsActive, boolean forCommit) throws Exception {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
        I_IntSet actives = getActiveStatus(Terms.get());
        HashMap<String, ArrayList<DescriptionVersionBI>> langs = new HashMap<String, ArrayList<DescriptionVersionBI>>();
        
        NidSet fsnSet = new NidSet();
        fsnSet.add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
        fsnSet.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getConceptNid());
        fsnSet.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid());
        fsnSet.add(Ts.get().getNidForUuids(UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")));
        
        for (DescriptionVersionBI<?> desc : descsActive) {
                if (fsnSet.contains(desc.getTypeNid())) {
                    if (desc.getText().matches(".*\\(\\?+\\).*") && desc.getTime() == Long.MAX_VALUE) {
                        alertList.add(new AlertToDataConstraintFailure(
                            (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                      : AlertToDataConstraintFailure.ALERT_TYPE.WARNING), "<html>Unedited semantic tag: "
                                + desc.getText(), cv));
                    }
                    String lang = desc.getLang();
                    if (langs.get(lang) != null) {
                        for (DescriptionVersionBI d : langs.get(lang)) {
                            if (d.getNid() != desc.getNid()) {
                                alertList.add(new AlertToDataConstraintFailure(
                                    (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                              : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                                    "<html>More than one FSN for " + lang, cv));
                            }
                        }
                        langs.get(lang).add(desc);
                    } else {
                        ArrayList<DescriptionVersionBI> dl = new ArrayList<DescriptionVersionBI>();
                        dl.add(desc);
                        langs.put(lang, dl);
                    }
                    /*
                     * If desc.getTime() == Long.MAX_VALUE, it means the description is
                     * uncommitted. For any component you can use the isUncommitted() method
                     * instead.
                     */
                    if (desc.isUncommitted() && !desc.getText().equals("New Fully Specified Description")) {
                        String filteredDescription = desc.getText();
                        // new removal using native lucene escaping
                        filteredDescription = QueryParser.escape(filteredDescription);
                        SearchResult result = Terms.get().doLuceneSearch(filteredDescription);
                        search: for (int i = 0; i < result.topDocs.totalHits; i++) {
                            Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                            int cnid = Integer.parseInt(doc.get("cnid"));
                            int dnid = Integer.parseInt(doc.get("dnid"));
                            if (cnid == cv.getConceptNid())
                                continue;
                            try {
                                /*
                                 * Using dnid since we want the description not the concept the
                                 * description is on.
                                 */
                                DescriptionVersionBI potential_fsn = (DescriptionVersionBI) Ts.get().getComponentVersion(vc, dnid);
                                if (potential_fsn != null) {
                                        if (actives.contains(potential_fsn.getStatusNid())
                                            && fsnSet.contains(potential_fsn.getTypeNid())
                                            && potential_fsn.getText().equals(desc.getText())
                                            && potential_fsn.getLang().equals(desc.getLang())) {
                                            alertList.add(new AlertToDataConstraintFailure(
                                                (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                                          : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                                                "<html>FSN already used: " + desc.getText(), cv));
                                            break search;
                                        }
                                }
                            } catch (Exception e) {
                                AceLog.getAppLog().alertAndLogException(e);
                            }
                        }
                    }
                }
            
        }
        if (langs.get("en") == null)
            alertList.add(new AlertToDataConstraintFailure((forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                                                     : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                "<html>No FSN for en", cv));
        return alertList;
    }
}
