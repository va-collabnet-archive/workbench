/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.ace.workflow.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.swing.JOptionPane;
import org.dwfa.ace.api.I_ConfigAceFrame;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Exports the members of the workflow history refset to the specified file, in
 * a tab delimited format of: workflowUUID, conceptUUID, modeler, action, stated,
 * time, effective time.
 * @author AKF
 *
 */
@BeanList(specs = {
    @Spec(directory = "tasks/workflow", type = BeanType.TASK_BEAN)})
public class ExportWorkflowHistory extends AbstractTask {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private String outputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private I_ConfigAceFrame config;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(outputFilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            outputFilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            String outputFileName = (String) process.getProperty(outputFilePropName);
            String fileNameNoTxt = outputFileName.replaceAll(".txt", "");
            File outputFile = new File(fileNameNoTxt + ".txt");
            BufferedWriter exportFileWriter = new BufferedWriter(new FileWriter(outputFile, true));
            WorkflowProcessesor wfprocessor = new WorkflowProcessesor();
            Ts.get().iterateConceptDataInParallel(wfprocessor);
            ConcurrentSkipListSet<String> wfHxText = wfprocessor.getWfHxText();
            for(String wfLine : wfHxText){
                exportFileWriter.append(wfLine);
            }
            exportFileWriter.close();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Workflow History Refset export is complete.");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    private class WorkflowProcessesor implements ProcessUnfetchedConceptDataBI {

        private NidBitSetBI nidSet;
        private ConcurrentSkipListSet<String> wfHxText = new ConcurrentSkipListSet<String>();
        private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public WorkflowProcessesor() throws IOException {
            this.nidSet = Ts.get().getAllConceptNids();
        }

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            I_GetConceptData concept = (I_GetConceptData) conceptFetcher.fetch();
            TreeSet<WorkflowHistoryJavaBean> allWorkflowHistory = WorkflowHelper.getAllWorkflowHistory(concept);
            for (WorkflowHistoryJavaBean wfBean : allWorkflowHistory) {
                String tab = "\t";
                StringBuilder sb = new StringBuilder();
                sb.append(wfBean.getWorkflowId().toString());
                sb.append(tab);
                sb.append(wfBean.getConcept().toString());
                sb.append(tab);
                sb.append(Ts.get().getConceptVersion(config.getViewCoordinate(), wfBean.getModeler()).getDescriptionPreferred().getText());
                sb.append(tab);
                sb.append(Ts.get().getConceptVersion(config.getViewCoordinate(), wfBean.getAction()).getDescriptionFullySpecified().getText());
                sb.append(tab);
                sb.append(Ts.get().getConceptVersion(config.getViewCoordinate(), wfBean.getState()).getDescriptionFullySpecified().getText());
                sb.append(tab);
                sb.append(wfBean.getFullySpecifiedName());
                sb.append(tab);
                try{
                    sb.append(formatter.format(new Date(wfBean.getWorkflowTime())));
                    sb.append(tab);
                    sb.append(formatter.format(new Date(wfBean.getEffectiveTime())));
                }catch(Exception e){
                    Date wfTime = new Date(wfBean.getWorkflowTime());
                    sb.append(formatter.format(wfTime));
                    sb.append(tab);
                    Date effectiveTime = new Date(wfBean.getEffectiveTime());
                    sb.append(formatter.format(effectiveTime));
                }
                sb.append(System.getProperty("line.separator"));
                wfHxText.add(sb.toString());
            }
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return nidSet;
        }

        @Override
        public boolean continueWork() {
            return true;
        }

        ConcurrentSkipListSet<String> getWfHxText() {
            return wfHxText;
        }
    }
    
    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }
}
