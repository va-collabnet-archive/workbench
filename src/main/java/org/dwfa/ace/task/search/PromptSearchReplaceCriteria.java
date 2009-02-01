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
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class PromptSearchReplaceCriteria extends AbstractTask {

	private static final long serialVersionUID = 1L;
	
	private static final int dataVersion = 1;

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

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(searchStringPropName);
		out.writeObject(replaceStringPropName);
		out.writeObject(caseSensitivePropName);
		out.writeObject(searchAllPropName);
		out.writeObject(searchFsnPropName);
		out.writeObject(searchPftPropName);
		out.writeObject(searchSynonymPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			searchStringPropName = (String) in.readObject();
			replaceStringPropName = (String) in.readObject();
			caseSensitivePropName = (String) in.readObject();
			searchAllPropName = (String) in.readObject();
			searchFsnPropName = (String) in.readObject();
			searchPftPropName = (String) in.readObject();
			searchSynonymPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

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
        String searchString = dialog.getSearchString();
        String replaceString = dialog.getReplaceString();
        boolean caseSensitive = dialog.isCaseSensitive();
        boolean searchAll = dialog.isAll();
        boolean searchFsn = dialog.isFullySpecifiedName();
        boolean searchPft = dialog.isPreferredTerm();
        boolean searchSynonym = dialog.isSynonym();
        
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
