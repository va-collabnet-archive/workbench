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
package org.ihtsdo.translation.tasks.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
/*
 * See TestForPreferredTermValue for comments first.
 */
public class TestForHomograph extends AbstractConceptTest {

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


		for (DescriptionVersionBI<?> desc : descsActive) {
			if (desc.isUncommitted()) {
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
							if (potential_fsn.getStatusNid() == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()
									&& potential_fsn.getText().equals(desc.getText())
									&& potential_fsn.getLang().equals(desc.getLang())) {
								alertList.add(new AlertToDataConstraintFailure(
										AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
												"<html>Homograph detected: " + desc.getText(), cv));
								break search;
							}
						}
					} catch (Exception e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}
			}
		}

		return alertList;
	}
}
