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
package org.ihtsdo.rules;

import java.util.HashSet;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.rules.context.RulesContextHelper;

/**
 * The Class UpdateAllPackagesTask.
 */
public class UpdateAllPackagesTask extends SwingWorker<Object, Object> {
	
	/** The config. */
	I_ConfigAceFrame config;

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Boolean doInBackground() throws Exception {
		config.setStatusMessage("Updating knowledge packages...");
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		I_ShowActivity activity =
			Terms.get().newActivityPanel(true, config, 
					"<html>Updating knowledge packages...", true);
		activities.add(activity);
		activity.setValue(0);
		activity.setIndeterminate(true);
		activity.setProgressInfoLower("Updating knowledge packages...");
		long startTime = System.currentTimeMillis();
		
		RulesContextHelper ctxHelper = new RulesContextHelper(config);
		ctxHelper.clearCache();
		
		for (I_GetConceptData loopContext : ctxHelper.getAllContexts()) {
			ctxHelper.getKnowledgeBaseForContext(loopContext, config, true);
		}
		
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;
		String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
		String result = "Done";
		activity.setProgressInfoUpper("<html>Knowledge packages updated...");
		activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; " + result);
		try {
			activity.complete();
		} catch (ComputationCanceled e) {
			// Nothing to do
		}
		config.setStatusMessage("");
		return null;
	}

	/**
	 * Instantiates a new update all packages task.
	 *
	 * @param config the config
	 */
	public UpdateAllPackagesTask(I_ConfigAceFrame config) {
		super();
		this.config = config;
	}

}
