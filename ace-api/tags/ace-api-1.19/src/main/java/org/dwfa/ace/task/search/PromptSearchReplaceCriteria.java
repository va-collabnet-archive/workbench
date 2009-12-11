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
package org.dwfa.ace.task.search;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/listview", type = BeanType.TASK_BEAN) })
public class PromptSearchReplaceCriteria extends AbstractTask {

	/**
	 * 
	 */
	
//	private I_TermFactory termFactory;
//	private I_ConfigAceFrame config;
	
	// Variables from the UI
	private String searchString = "";
	private String replaceString = "";
	private boolean caseSensitive = false;
	private boolean searchAll = true;
	private boolean searchFsn = true;
	private boolean searchPft = true;
	private boolean searchSynonym = true;
	
	private String searchStringPropName = ProcessAttachmentKeys.FIND_TEXT.getAttachmentKey();
	private String replaceStringPropName = ProcessAttachmentKeys.REPLACE_TEXT.getAttachmentKey();
	private String caseSensitivePropName = ProcessAttachmentKeys.CASE_SENSITIVITY.getAttachmentKey();
	private String searchAllPropName = ProcessAttachmentKeys.SEARCH_ALL.getAttachmentKey();
	private String searchFsnPropName = ProcessAttachmentKeys.SEARCH_FSN.getAttachmentKey();
	private String searchPftPropName = ProcessAttachmentKeys.SEARCH_PT.getAttachmentKey();
	private String searchSynonymPropName = ProcessAttachmentKeys.SEARCH_SYNONYM.getAttachmentKey();

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
			throws TaskFailedException {
		// Nothing to do... I think
	}
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work arg1)
			throws TaskFailedException {
	
        SearchReplaceDialog dialog = new SearchReplaceDialog();
        dialog.pack();
        dialog.setTitle("Search and Replace");
        dialog.setResizable(false);
        dialog.setModal(true);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        //Calculate the frame location
        int x = (screenSize.width - dialog.getWidth()) / 2;  
        int y = (screenSize.height - dialog.getHeight()) / 2;

        //Set the new frame location
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.isCancelled()) {
            dialog.dispose();
            dialog = null;

            return Condition.ITEM_CANCELED;
        }

        // Get the values from the dialog
        searchString = dialog.getSearchString();
        replaceString = dialog.getReplaceString();
        caseSensitive = dialog.isCaseSensitive();
        searchAll = dialog.isAll();
        searchFsn = dialog.isFullySpecifiedName();
        searchPft = dialog.isPreferredTerm();
        searchSynonym = dialog.isSynonym();
        
        dialog.dispose();
        dialog = null;

        try {
        	
        	// Set the values from the dialog as properties for this process
			process.setProperty(searchStringPropName, searchString);
			process.setProperty(replaceStringPropName, replaceString);
			process.setProperty(caseSensitivePropName, caseSensitive);
			process.setProperty(searchAllPropName, searchAll);
			process.setProperty(searchFsnPropName, searchFsn);
			process.setProperty(searchPftPropName, searchPft);
			process.setProperty(searchSynonymPropName, searchSynonym);
		} catch (IntrospectionException e) {
			throw new TaskFailedException("Can't bind variables from Find and Replace dialog: " + e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException("Can't bind variables from Find and Replace dialog: " + e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException("Can't bind variables from Find and Replace dialog: " + e);
		}
        
		return Condition.ITEM_COMPLETE;
	}
	public String getSearchStringPropName() {
		return searchStringPropName;
	}
	public void setSearchStringPropName(String searchStringPropName) {
		this.searchStringPropName = searchStringPropName;
	}
	public String getReplaceStringPropName() {
		return replaceStringPropName;
	}
	public void setReplaceStringPropName(String replaceStringPropName) {
		this.replaceStringPropName = replaceStringPropName;
	}
	public String getCaseSensitivePropName() {
		return caseSensitivePropName;
	}
	public void setCaseSensitivePropName(String caseSensitivePropName) {
		this.caseSensitivePropName = caseSensitivePropName;
	}
	public String getSearchAllPropName() {
		return searchAllPropName;
	}
	public void setSearchAllPropName(String searchAllPropName) {
		this.searchAllPropName = searchAllPropName;
	}
	public String getSearchFsnPropName() {
		return searchFsnPropName;
	}
	public void setSearchFsnPropName(String searchFsnPropName) {
		this.searchFsnPropName = searchFsnPropName;
	}
	public String getSearchPftPropName() {
		return searchPftPropName;
	}
	public void setSearchPftPropName(String searchPftPropName) {
		this.searchPftPropName = searchPftPropName;
	}
	public String getSearchSynonymPropName() {
		return searchSynonymPropName;
	}
	public void setSearchSynonymPropName(String searchSynonymPropName) {
		this.searchSynonymPropName = searchSynonymPropName;
	}
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}
	public int[] getDataContainerIds() {
		return new int[] {};
	}
}
