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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.queue.SelectAll;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.translation.ui.SpecialInboxPanel;

/**
 * The Class OpenTranslationForSelectedConcept.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class OpenSpecialInboxToLT extends AbstractTask {
	

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
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new open translation for selected concept.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public OpenSpecialInboxToLT() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			AceFrameConfig config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

			worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);

			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(
					config);

			boolean isReleaseManager = permissionApi
			.checkPermissionForProject(
					config.getDbConfig().getUserConcept(),
					Terms.get()
					.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY
							.localize().getNid()),
							Terms.get()
							.getConcept(ArchitectonicAuxiliary.Concept.RELEASE_AUTHORITY_ROLE
									.localize().getNid()));

			if (isReleaseManager){
				AceFrame ace=config.getAceFrame();
				JTabbedPane tp=ace.getCdePanel().getLeftTabs();
				if (tp!=null){
					int tabCount=tp.getTabCount();
					for (int i=0;i<tabCount;i++){
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.ARCHIVAL_ITEMS_LEFT_MENU)){
							tp.setSelectedIndex(i);
							tp.revalidate();
							tp.repaint();
							return Condition.CONTINUE;
						}
					}
					SpecialInboxPanel uiPanel = new SpecialInboxPanel(worker, "david relm.archival",new SelectAll());

					tp.addTab(TranslationHelperPanel.ARCHIVAL_ITEMS_LEFT_MENU, uiPanel);
					tp.setSelectedIndex(tabCount);
					tp.revalidate();
					tp.repaint();
				}
			}
			else{

				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
						"Release authority role is required for view this option.", "",
						JOptionPane.ERROR_MESSAGE);
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
	
}