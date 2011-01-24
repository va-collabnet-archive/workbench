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
package org.ihtsdo.translation.tasks;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.SimilarityResultsPanel;

/**
 * The Class SearchSimilarityToSignpost.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class SearchSimilarityToSignpost extends AbstractTask {
	
	/** The query. */
	private String query;

	/** The source lang code. */
	private String sourceLangCode;
	
	/** The target lang code. */
	private String targetLangCode;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(query);
		out.writeObject(sourceLangCode);
		out.writeObject(targetLangCode);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			query = (String) in.readObject();
			sourceLangCode = (String) in.readObject();
			targetLangCode = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new search similarity to signpost.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public SearchSimilarityToSignpost() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			String message = "<html><body>Similarity results:<br>";
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			
			if (query == null) {
				query = config.getHierarchySelection().toString();
			}
			
			if (sourceLangCode == null) {
				sourceLangCode = "en";
			}
			
			if (targetLangCode == null) {
				targetLangCode = "es";
			}
			
			List<SimilarityMatchedItem> matches = LanguageUtil.getSimilarityResults(query, sourceLangCode, targetLangCode, config);
			TranslationHelperPanel thp=PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp=thp.getTabbedPanel();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.SIMILARITY_TAB_NAME)){
						tp.setSelectedIndex(i);
						thp.showTabbedPanel();
						return Condition.CONTINUE;
					}
				}
				JPanel panel=new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(new SimilarityResultsPanel(query, sourceLangCode, targetLangCode, matches, config), BorderLayout.CENTER);
				
				tp.addTab(TranslationHelperPanel.SIMILARITY_TAB_NAME, panel);
				tp.setSelectedIndex(tp.getTabCount()-1);
				thp.showTabbedPanel();
			}
			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}
	
	/**
	 * Gets the query.
	 * 
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 * 
	 * @param query the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Gets the source lang code.
	 * 
	 * @return the source lang code
	 */
	public String getSourceLangCode() {
		return sourceLangCode;
	}

	/**
	 * Sets the source lang code.
	 * 
	 * @param sourceLangCode the new source lang code
	 */
	public void setSourceLangCode(String sourceLangCode) {
		this.sourceLangCode = sourceLangCode;
	}

	/**
	 * Gets the target lang code.
	 * 
	 * @return the target lang code
	 */
	public String getTargetLangCode() {
		return targetLangCode;
	}

	/**
	 * Sets the target lang code.
	 * 
	 * @param targetLangCode the new target lang code
	 */
	public void setTargetLangCode(String targetLangCode) {
		this.targetLangCode = targetLangCode;
	}
	
	

}