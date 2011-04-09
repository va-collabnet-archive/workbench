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
package org.ihtsdo.batch;

import java.util.List;

public interface Z_I_MakeBatchActionCriterionPanel {

	int searchPanelId = 1; // :!!!: see DescSearchResultsTablePopupListener.java
	int workflowHistorySearchPanelId = 2; // :!!!: see DescSearchResultsTablePopupListener.java

    public void layoutCriterion();

    public List<Z_BatchActionCriterionPanel> getCriterionPanels();

    public Z_BatchActionCriterionPanel makeCriterionPanel() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException;
}
