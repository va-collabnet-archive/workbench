/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation;

import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;

/**
 * The Class SimilarityResultsToCptPanel.
 */
public class SimilarityResultsToCptPanel extends SimilarityResultsPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new similarity results to cpt panel.
	 * 
	 * @param query the query
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 * @param results the results
	 * @param config the config
	 */
	public SimilarityResultsToCptPanel(String query, String sourceLangCode,
			String targetLangCode, List<SimilarityMatchedItem> results,
			I_ConfigAceFrame config) {
		super(query, sourceLangCode, targetLangCode, results, config);
		// TODO Auto-generated constructor stub
	}

    /**
     * Update.
     * 
     * @param query the query
     */
    public void update(String query){
    	queryField.setText(query);
		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, 
				(String) sourceLang.getSelectedItem(), (String) targetLang.getSelectedItem(), config);
		while (tableModel.getRowCount()>0){
			tableModel.removeRow(0);
			}
		for (SimilarityMatchedItem item : results) {
			tableModel.addRow(new String[] {item.getSourceText(),item.getTargetText(),String.valueOf(item.getScore())});
		}
//		System.out.println("model count=" + tableModel.getRowCount());
//		System.out.println("table count=" + table.getRowCount());
		
		table.revalidate();
    	table.repaint();
    }

}
