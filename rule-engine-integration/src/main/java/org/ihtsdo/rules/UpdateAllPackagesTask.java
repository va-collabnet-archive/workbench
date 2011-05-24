package org.ihtsdo.rules;

import java.util.HashSet;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;

public class UpdateAllPackagesTask extends SwingWorker<Object, Object> {
	
	I_ConfigAceFrame config;

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
		
		RulesDeploymentPackageReferenceHelper pkgHelper = new RulesDeploymentPackageReferenceHelper(config);
		for ( RulesDeploymentPackageReference loopPkg : pkgHelper.getAllRulesDeploymentPackages()) {
			loopPkg.updateKnowledgeBase();
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
			e.printStackTrace();
		}
		config.setStatusMessage("");
		return null;
	}

	public UpdateAllPackagesTask(I_ConfigAceFrame config) {
		super();
		this.config = config;
	}

}
