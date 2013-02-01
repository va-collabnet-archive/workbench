package org.ihtsdo.translation.tasks.commit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;


import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;

public class TranslationTermChangeListener implements PropertyChangeListener {
	private PropertyChangeEvent myEvt;
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		myEvt=arg0;
		SwingUtilities.invokeLater(new Runnable() {

			synchronized
			public void run() {
				I_GetConceptData concept=null;
				I_TermFactory termFactory = Terms.get();
				I_ConfigAceFrame config;
				long acceptableDiffForCommited=3600;
				try {
					config = termFactory.getActiveAceFrameConfig();

					HashMap<Integer, List<TranslationProject>> targetLangProjs = TerminologyProjectDAO.getAllTranslationProjectTargetLanguages(config);
					I_RepresentIdSet idSet=(I_RepresentIdSet)myEvt.getNewValue();
					if (idSet!=null){
						HashMap<I_GetConceptData, HashMap<Integer, List<TranslationProject>>> conceptToAdd = new HashMap<I_GetConceptData, HashMap<Integer, List<TranslationProject>>>();
						NidBitSetItrBI possibleItr = idSet.iterator();
						HashMap<Integer, List<TranslationProject>> reqProjForConcept=null;
						while (possibleItr.next()) {
							concept=Terms.get().getConcept(possibleItr.nid());
							if (conceptToAdd.containsKey(concept)){
								reqProjForConcept=(HashMap<Integer, List<TranslationProject>>) conceptToAdd.get(concept);
							}else{
								reqProjForConcept = new HashMap<Integer, List<TranslationProject>>();
							}
							boolean isInWorkset=false;
							Collection<I_DescriptionVersioned> descs = (Collection<I_DescriptionVersioned>) concept.getDescriptions();
							for (I_DescriptionVersioned desc:descs){
								for (RefexChronicleBI refDesc:desc.getAnnotations()){
									if (reqProjForConcept.containsKey(refDesc.getRefexNid())){
										continue;
									}
									if (targetLangProjs.containsKey(refDesc.getRefexNid())){
										boolean checkConceptInWorkset=false;
										I_DescriptionTuple dTuple=desc.getLastTuple();
										long difSec=Math.abs((System.currentTimeMillis()-dTuple.getTime())/1000);

										if (difSec<=acceptableDiffForCommited){
											checkConceptInWorkset=true;
										}else{
											ComponentVersionBI compVer = refDesc.getVersion(config.getViewCoordinate());
											if (compVer!=null){
												difSec=Math.abs((System.currentTimeMillis()-compVer.getTime())/1000);
												if (difSec<=acceptableDiffForCommited){
													checkConceptInWorkset=true;
												}
											}
										}
										if (!checkConceptInWorkset){
											continue;
										}
										//verify that concept belong to workset with same target language
										HashMap<Integer, List<Integer>> tgtLangWSets = TerminologyProjectDAO.getAllWorksetTargetLanguages(config);

										List<Integer> wSetList = tgtLangWSets.get(refDesc.getRefexNid());
										isInWorkset=false;
										if (wSetList!=null){
											for (RefexChronicleBI refConc:concept.getAnnotations()){
												if (wSetList.contains(refConc.getRefexNid())){
													isInWorkset=true;
													break;
												}
											}
										}
										if (!isInWorkset){
											reqProjForConcept.put(refDesc.getRefexNid(), targetLangProjs.get(refDesc.getRefexNid()));
										}
									}
								}
							}
						}
						if (reqProjForConcept != null && reqProjForConcept.size()>0){
							conceptToAdd.put(concept, reqProjForConcept);
						}

						//				break;
						//			}
						//		}

						if (conceptToAdd.size()>0){
							TranslationProjectAndWlstSelect selector = new TranslationProjectAndWlstSelect(conceptToAdd);
						}
						//					for (I_GetConceptData concept1: conceptToAdd.keySet()){
						//						reqProjForConcept=conceptToAdd.get(concept1);
						//						for (Integer refId:reqProjForConcept.keySet()){
						//							String refUuid=Terms.get().nativeToUuid(refId).toString();
						//							I_Work worker=config.getWorker().getTransactionIndependentClone() ;
						//
						//							//			Stack<I_EncodeBusinessProcess> stack =  worker.getProcessStack();
						//							//			Stack<I_EncodeBusinessProcess> stackNP = new Stack<I_EncodeBusinessProcess>();
						//							//			worker.setProcessStack(stackNP);
						//							I_EncodeBusinessProcess process = null;
						//							FileInputStream fis = new FileInputStream("sampleProcesses/setTranslProjectForConcept.bp");
						//							BufferedInputStream bis = new BufferedInputStream(fis);
						//							ObjectInputStream ois = new ObjectInputStream(bis);
						//							final BusinessProcess bp = (BusinessProcess) ois.readObject();
						//							ois.close();
						//							config.setStatusMessage("Executing: " + bp.getName());
						//
						//							bp.writeAttachment("ConceptSelected", concept1.getNid());
						//							bp.writeAttachment("LangRefsetSelected", refId);
						//							process=bp;
						//							I_ConfigAceFrame profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
						//							worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);
						//							worker.execute(process);
						//
						//							String translProj=(String)bp.readAttachement(ProcessAttachmentKeys.TERMINOLOGY_PROJECT.getAttachmentKey());
						//							String worklist=(String)bp.readAttachement("NacWorklistSelected");
						//							//			worker.setProcessStack(stack);
						//
						//							//save default
						//						}
						//					}
					}
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TaskFailedException e) {
					e.printStackTrace();
				} catch (LoginException e) {
					e.printStackTrace();
				} catch (PrivilegedActionException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (CancellationException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});


	}

}
