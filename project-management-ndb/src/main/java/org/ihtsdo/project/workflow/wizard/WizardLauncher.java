package org.ihtsdo.project.workflow.wizard;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.wizard.I_fastWizard;
import org.ihtsdo.wizard.I_notifyPanelChange;
import org.ihtsdo.wizard.I_wizardResult;
import org.ihtsdo.wizard.WizardFrame;

public class WizardLauncher {

	public void launchWfWizard(List<WfUser> users){
		Result resultWizard = new Result();
		Notifier notifier = new Notifier(users);
		int panel=0;
		I_fastWizard[] panels = new I_fastWizard[3];
		SimpleTextCollector stc=new SimpleTextCollector();
		stc.setLabel("Worklist Name");
		stc.setKey("WORKLIST_NAME");
		panels[0]=stc;
		WorkflowDefinitionSelection wds = new WorkflowDefinitionSelection();
		wds.setKey("WDS");
		panels[1]=wds;
		DataGridCollectorFromList uc=new DataGridCollectorFromList();
		panels[2]=uc;
		WizardFrame ww=new WizardFrame(panels, resultWizard,notifier);
		ww.setModalityType(ModalityType.APPLICATION_MODAL);
		ww.setSize(1000,600);
		Toolkit tk = Toolkit.getDefaultToolkit();
	    Dimension screenSize = tk.getScreenSize();
	    int screenHeight = screenSize.height;
	    int screenWidth = screenSize.width;
	    ww.setLocation(screenWidth / 4, screenHeight / 4);
		ww.setPanel(0);
		ww.setVisible(true);
		

	}

	public void launchWfWizard(){
		Result resultWizard = new Result();
		
		List<WfUser>users=new ArrayList<WfUser>();
		WfUser user1=new WfUser();
		user1.setUsername("John");
		WfUser user2=new WfUser();
		user2.setUsername("Sammy");
		WfUser user3=new WfUser();
		user3.setUsername("Linda");
		
		users.add(user1);
		users.add(user2);
		users.add(user3);
		
		Notifier notifier = new Notifier(users);
		I_fastWizard[] panels = new I_fastWizard[3];
		SimpleTextCollector stc=new SimpleTextCollector();
		stc.setLabel("Worklist Name");
		stc.setKey("WORKLIST_NAME");
		panels[0]=stc;
		WorkflowDefinitionSelection wds = new WorkflowDefinitionSelection();
		wds.setKey("WDS");
		panels[1]=wds;
		DataGridCollectorFromList uc=new DataGridCollectorFromList();
		panels[2]=uc;
		WizardFrame ww=new WizardFrame(panels, resultWizard,notifier);
		ww.setSize(500,300);
		ww.setPanel(0);

		ww.setVisible(true);

	}
	public HashMap<String, Object> result;
	class Result implements I_wizardResult{

		@Override
		public void setResultMap(HashMap<String, Object> resultMap) {
			result=resultMap;

		}

	}
	class Notifier implements I_notifyPanelChange{

		private List<WfUser> users;
		public Notifier(List<WfUser> users){
			this.users=users;
		}
		@Override
		public void notifyThis(WizardFrame wizardFrame, int index,HashMap<String,Object> mapCollector) {
			if (index==2){
				File wdff=(File)mapCollector.get("WDS");
				WorkflowDefinition wd=WorkflowDefinitionManager.readWfDefinition(wdff.getName());
				List<WfRole> roles = wd.getRoles();
				I_fastWizard[] panels = wizardFrame.getPanels();
				int panelnr=0;
				I_fastWizard[] newPanels = new I_fastWizard[3];
				newPanels[panelnr]=panels[0];
				panelnr++;
				newPanels[panelnr]=panels[1];
				panelnr++;
					DataGridCollectorFromList uc=new DataGridCollectorFromList(roles,users);
					uc.setLabel("Set Users for Roles:");
					uc.setKey("roles");
					newPanels[panelnr++]=uc;
				wizardFrame.setPanels(newPanels);
			}

		}

	}
	public HashMap<String, Object> getResult() {
		return result;
	}

}
