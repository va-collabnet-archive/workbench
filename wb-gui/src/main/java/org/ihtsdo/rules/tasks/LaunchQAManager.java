package org.ihtsdo.rules.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import javax.swing.JTabbedPane;

import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.qa.gui.QAManager;

@BeanList(specs = 
{ @Spec(directory = "tasks/qa tasks", type = BeanType.TASK_BEAN)})
public class LaunchQAManager extends AbstractTask {
	

	private QAManager uiPanel;
	
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion != 1) {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	public LaunchQAManager() throws MalformedURLException {
		super();
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {

			AceFrameConfig config = (AceFrameConfig) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

			AceFrame ace=config.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				boolean bPanelExists=false;
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(QAManager.QA_MANAGER)){
						tp.setSelectedIndex(i);
						bPanelExists=true;
						break;
					}
				}
				if (!bPanelExists){
					uiPanel = new QAManager();
					tp.addTab(QAManager.QA_MANAGER, uiPanel);
					tp.setSelectedIndex(tabCount);
				}
				tp.revalidate();
				tp.repaint();
			}
			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {  };
	}

	
}