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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary.LANG_CODE;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class PromptSearchReplaceCriteria extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private static int objDataVersion = -1;

    /**
	 *
	 */

    private String searchStringPropName = ProcessAttachmentKeys.FIND_TEXT.getAttachmentKey();
    private String replaceStringPropName = ProcessAttachmentKeys.REPLACE_TEXT.getAttachmentKey();
    private String caseSensitivePropName = ProcessAttachmentKeys.CASE_SENSITIVITY.getAttachmentKey();
    private String searchAllPropName = ProcessAttachmentKeys.SEARCH_ALL.getAttachmentKey();
    private String searchFsnPropName = ProcessAttachmentKeys.SEARCH_FSN.getAttachmentKey();
    private String searchPftPropName = ProcessAttachmentKeys.SEARCH_PT.getAttachmentKey();
    private String searchSynonymPropName = ProcessAttachmentKeys.SEARCH_SYNONYM.getAttachmentKey();
    private String retireAsStatusPropName = ProcessAttachmentKeys.RETIRE_AS_STATUS.getAttachmentKey();
    private String languageCodePropName = ProcessAttachmentKeys.LANGUAGE_CODE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(searchStringPropName);
        out.writeObject(replaceStringPropName);
        out.writeObject(caseSensitivePropName);
        out.writeObject(searchAllPropName);
        out.writeObject(searchFsnPropName);
        out.writeObject(searchPftPropName);
        out.writeObject(searchSynonymPropName);
        out.writeObject(retireAsStatusPropName);
        out.writeObject(languageCodePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            searchStringPropName = (String) in.readObject();
            replaceStringPropName = (String) in.readObject();
            caseSensitivePropName = (String) in.readObject();
            searchAllPropName = (String) in.readObject();
            searchFsnPropName = (String) in.readObject();
            searchPftPropName = (String) in.readObject();
            searchSynonymPropName = (String) in.readObject();
            retireAsStatusPropName = ProcessAttachmentKeys.RETIRE_AS_STATUS.getAttachmentKey();
            languageCodePropName = (String) in.readObject();
        } else if (objDataVersion == 2) {
            searchStringPropName = (String) in.readObject();
            replaceStringPropName = (String) in.readObject();
            caseSensitivePropName = (String) in.readObject();
            searchAllPropName = (String) in.readObject();
            searchFsnPropName = (String) in.readObject();
            searchPftPropName = (String) in.readObject();
            searchSynonymPropName = (String) in.readObject();
            retireAsStatusPropName = (String) in.readObject();
            languageCodePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // Nothing to do... I think
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work arg1) throws TaskFailedException {

        SearchReplaceDialog dialog = new SearchReplaceDialog();
        dialog.pack();
        dialog.setTitle("Search and Replace");
        dialog.setResizable(false);
        dialog.setModal(true);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Calculate the frame location
        int x = (screenSize.width - dialog.getWidth()) / 2;
        int y = (screenSize.height - dialog.getHeight()) / 2;

        // Set the new frame location
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.isCancelled()) {
            dialog.dispose();
            dialog = null;

            return Condition.ITEM_CANCELED;
        }

        // Get the values from the dialog
        String searchString = dialog.getSearchString();
        String replaceString = dialog.getReplaceString();
        LANG_CODE selectedLanguageCode = dialog.getLanguageCode();
        boolean caseSensitive = dialog.isCaseSensitive();
        boolean searchAll = dialog.isAll();
        boolean searchFsn = dialog.isFullySpecifiedName();
        boolean searchPft = dialog.isPreferredTerm();
        boolean searchSynonym = dialog.isSynonym();
        int retireAsStatus = dialog.getRetireAsStatus();

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
            process.setProperty(retireAsStatusPropName, retireAsStatus);
            process.setProperty(languageCodePropName, selectedLanguageCode);
            

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

    public String getRetireAsStatusPropName() {
        return retireAsStatusPropName;
    }

    public void setRetireAsStatusPropName(String retireAsStatusPropName) {
        this.retireAsStatusPropName = retireAsStatusPropName;
    }
    
    public String getLanguageCodePropName() {
		return languageCodePropName;
	}

	public void setLanguageCodePropName(String languageCodePropName) {
		this.languageCodePropName = languageCodePropName;
	}

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }
}
