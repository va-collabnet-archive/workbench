package org.ihtsdo.document;

import java.io.File;

import javax.swing.SwingWorker;

public class IndexDictionaryWorker extends SwingWorker<String, String> {
	
	private boolean overwrite;
	private File dictionaryTextFile;

	public IndexDictionaryWorker(boolean overwrite, File dictionaryTextFile) {
		this.overwrite = overwrite;
		this.dictionaryTextFile = dictionaryTextFile;
	}
	@Override
	protected String doInBackground() throws Exception {
		DocumentManager.indexDictionaryFromTextFile(overwrite, dictionaryTextFile);
		return null;
	}

}
