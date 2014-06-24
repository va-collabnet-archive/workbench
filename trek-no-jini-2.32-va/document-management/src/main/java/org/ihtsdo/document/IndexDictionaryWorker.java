/*
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
package org.ihtsdo.document;

import java.io.File;

import javax.swing.SwingWorker;

/**
 * The Class IndexDictionaryWorker.
 */
public class IndexDictionaryWorker extends SwingWorker<String, String> {
	
	/** The overwrite. */
	private boolean overwrite;
	
	/** The dictionary text file. */
	private File dictionaryTextFile;

	/**
	 * Instantiates a new index dictionary worker.
	 *
	 * @param overwrite the overwrite
	 * @param dictionaryTextFile the dictionary text file
	 */
	public IndexDictionaryWorker(boolean overwrite, File dictionaryTextFile) {
		this.overwrite = overwrite;
		this.dictionaryTextFile = dictionaryTextFile;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected String doInBackground() throws Exception {
		DocumentManager.indexDictionaryFromTextFile(overwrite, dictionaryTextFile);
		return null;
	}

}
