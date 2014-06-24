package org.ihtsdo.translation.tasks.commit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.task.GetInforForUnassignedWorkDC;
import org.ihtsdo.translation.DefaultProjectForLanguage;
import org.ihtsdo.translation.LanguageUtil;

public class TranslationProjectAndWlstSelect implements ActionListener {
	private Timer t;
	private HashMap<I_GetConceptData, HashMap<Integer, List<TranslationProject>>> conceptToAdd;

	public TranslationProjectAndWlstSelect(
			HashMap<I_GetConceptData, HashMap<Integer, List<TranslationProject>>> conceptToAdd) {

		this.conceptToAdd=conceptToAdd;
		t = new Timer(1000, this);
		t.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try{
			t.stop();
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
//			Map<String,String[]> res=new HashMap<String,String[]>();

			DefaultProjectForLanguage defProjectsBox = LanguageUtil.readDefaultProjects();
			Map<String, String[]> defProjects=null;
			if (defProjectsBox==null){
				defProjects=new HashMap<String, String[]>();
				defProjectsBox=new DefaultProjectForLanguage(defProjects);
			}else{
				defProjects = defProjectsBox.getDefaultProjects();
			}
			for (I_GetConceptData concept1: conceptToAdd.keySet()){
				HashMap<Integer, List<TranslationProject>> reqProjForConcept = conceptToAdd.get(concept1);
				for (Integer refId:reqProjForConcept.keySet()){
					String refUuid=Terms.get().nativeToUuid(refId).toString();
					String projId=null;
					String wklId=null;
					WorkList worklist=null;
					if (!defProjects.containsKey(refUuid)){
						GetInforForUnassignedWorkDC getInfo=new GetInforForUnassignedWorkDC(config, refId,reqProjForConcept.get(refId));

						int action = JOptionPane.showOptionDialog(null, getInfo, "Setting default worklist for language", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

						TranslationProject project =(TranslationProject) getInfo.getSelectedProject();
						worklist=getInfo.getSelectedWorkList();

						 
						if (project!=null){
							projId=project.getUids().iterator().next().toString();
						}
						if (worklist!=null){
							wklId=worklist.getUids().iterator().next().toString();
						}
						if (projId!=null && wklId!=null){
							String[] projWorklist=new String[2];
							projWorklist[0]=projId;
							projWorklist[1]=wklId;

							defProjects.put(refUuid,projWorklist);
							TerminologyProjectDAO.addConceptAsNacWorklistMember(worklist, concept1, config);
						}
					}else{
						wklId= ((String[])defProjects.get(refUuid))[1];
						worklist=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(UUID.fromString(wklId)), config);
						TerminologyProjectDAO.addConceptAsNacWorklistMember(worklist, concept1, config);
					
					//					I_Work worker=config.getWorker().getTransactionIndependentClone() ;
					//					if (worker.isExecuting()) {
					//						worker = worker.getTransactionIndependentClone();
					//					}
					//
					//					//			Stack<I_EncodeBusinessProcess> stack =  worker.getProcessStack();
					//					//			Stack<I_EncodeBusinessProcess> stackNP = new Stack<I_EncodeBusinessProcess>();
					//					//			worker.setProcessStack(stackNP);
					//					I_EncodeBusinessProcess process = null;
					//					FileInputStream fis = new FileInputStream("sampleProcesses/setTranslProjectForConcept.bp");
					//					BufferedInputStream bis = new BufferedInputStream(fis);
					//					ObjectInputStream ois = new ObjectInputStream(bis);
					//					final BusinessProcess bp = (BusinessProcess) ois.readObject();
					//					ois.close();
					//					config.setStatusMessage("Executing: " + bp.getName());
					//
					//					bp.writeAttachment("ConceptSelected", concept1.getNid());
					//					bp.writeAttachment("LangRefsetSelected", refId);
					//					process=bp;
					//					I_ConfigAceFrame profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
					//					worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);
					//					process.execute(worker);
					//
					//					worker.commitTransactionIfActive();
					//					String translProj=(String)bp.readAttachement(ProcessAttachmentKeys.TERMINOLOGY_PROJECT.getAttachmentKey());
					//					String worklist=(String)bp.readAttachement("NacWorklistSelected");
					//			worker.setProcessStack(stack);

					//save default
					}
				}
			}

			defProjectsBox.setDefaultProjects(defProjects) ;
			LanguageUtil.writeDefaultProjects(defProjectsBox);
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.removeActionListener(this);
	}

}
