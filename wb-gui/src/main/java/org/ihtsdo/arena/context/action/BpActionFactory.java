package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;

public class BpActionFactory {
	
	I_ConfigAceFrame frameConfig;
	I_HostConceptPlugins host;
	
	public BpActionFactory(I_ConfigAceFrame frameConfig, I_HostConceptPlugins host) {
		super();
		this.frameConfig = frameConfig;
		this.host = host;
	}


	public Action make(String processUrlStr) {
		try {
			return new BpAction(processUrlStr, frameConfig, host);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLog(null,
					Level.SEVERE,
					"processing: " + processUrlStr, e);
			AceLog.getAppLog().alertAndLogException(e);
		} 
		
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// nothing to do...
			}
		};
	}
	public Action make(File processfile) {
		try {
			return new BpAction(processfile.toURI().toURL(), frameConfig, host);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLog(null,
					Level.SEVERE,
					"processing: " + processfile, e);
			AceLog.getAppLog().alertAndLogException(e);
		} 
		
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// nothing to do...
			}
		};
	}
	
	public Action makeDisabled(File processfile) {
		try {
			BpAction act = new BpAction(processfile.toURI().toURL(), frameConfig, host);
			act.setEnabled(false);
			return act;
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLog(null,
					Level.SEVERE,
					"processing: " + processfile, e);
			AceLog.getAppLog().alertAndLogException(e);
		} 

		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// nothing to do...
			}
		};
	}
	
	public Collection<Action> getProcessActions(File menuDir) {
		Collection<Action> actions = new ArrayList<Action>();
		if (menuDir.listFiles() != null) {
			for (File f : menuDir.listFiles()) {
				if (f.isDirectory()) {
					actions.addAll(getProcessActions(f));
				} else if (f.getName().toLowerCase().endsWith(".bp")) {
					try {
						actions.add(make(f.toURI().toURL().toExternalForm()));
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLog(null, Level.SEVERE,
								"processing: " + f, e);
					}
				}
			}
		}
		return actions;
	}
	
	public Collection<Action> fromDir(String dirStr)  {
		return getProcessActions(new File(dirStr));
	}

}
