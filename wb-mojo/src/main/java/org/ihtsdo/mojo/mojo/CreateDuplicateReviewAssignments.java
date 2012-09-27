/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.mojo.mojo;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.assignment.LaunchBatchGenAssignmentProcess;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.ace.task.queue.OpenQueuesInFolder;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.tasks.log.LogMessageOnWorkerLog;
import org.dwfa.bpa.tasks.util.Complete;
import org.dwfa.jini.JiniManager;
import org.dwfa.jini.TransactionParticipantAggregator;
import org.dwfa.tapi.TerminologyException;

import com.sun.jini.mahalo.LocalTransactionManager;

/**
 * Automated process to perform the creation of the review assignments required.
 * 
 * @goal duplicate-review-assignments
 * 
 */
public class CreateDuplicateReviewAssignments extends AbstractMojo {

    /**
     * Location of the queue file to use.
     * 
     * @parameter expression="${project.build.directory}\\..\\src\\main\\profiles\\users\\tore.dev\\inbox\\queue.config"
     * @required
     */
    private File queueConfigFile;

    /**
     * Name of file to read uuids from
     * 
     * @parameter expression="${project.build.directory}\\default.txt"
     * @required
     * 
     */
    private File dupUuidFile;

    /**
     * Name of workflow manager, as found in ace address book
     * 
     * @parameter expression="tore.dev"
     * @required
     * 
     */
    private String workFlowManager;

    // /**
    // * Name of business process to assign
    // *
    // * @parameter expression="dupReviewAssignment.bp"
    // * @required
    // *
    // */
    // private String businessProcessToAssign;

    /**
     * File path of the business process to assign to the workflow manager
     * 
     * @parameter expression="${project.build.directory}\\..\\src\\main\\bp\\assignment generators\\assignmentGenFromUuidList_InProperty.bp"
     * @required
     * 
     */
    private String wfAssignmentProcessFile;

    /**
     * File path of the business process to assign to the TAs
     * 
     * @parameter expression="${project.build.directory}\\..\\src\\main\\bp\\TA assignment processes\\dupReviewAssignment.bp"
     * @required
     * 
     */
    private String taAssignmentProcessFile;

    private String separator = System.getProperty("file.separator");
    private int listListSize = 250;
    private String inboxPath = null;

    public void execute() throws MojoExecutionException, MojoFailureException {
        JiniManager.setLocalOnly(true);

        inboxPath = "src" + separator + "main" + separator + "profiles" + separator + "users" + separator
            + workFlowManager + separator + "inbox";
        // inboxPath = "target" + separator + "amt-edit-bundle.dir" + separator
        // + "profiles" + separator + "users" + separator + workFlowManager +
        // separator + "inbox";
        try {

            if (dupUuidFile.exists()) {

                TransactionParticipantAggregator tpa = new TransactionParticipantAggregator(
                    new String[] { "src/main/config/transactionAggregator.config" }, null);
                LocalTransactionManager ltm = new LocalTransactionManager(
                    new String[] { "src/main/config/transactionManager.config" }, null);

                I_ConfigAceFrame config = LocalVersionedTerminology.get().getActiveAceFrameConfig();

                // queueDirectory = new File("target/myConfig.config");
                // queueDirectory.getParentFile().mkdirs();
                // FileWriter fw = new FileWriter(queueDirectory);
                // fw.append(" org.ihtsdo.mojo.mojo.MojoWorker {	tranDurLong = new Long(300000); ");
                // fw.append("    serviceDiscovery = new net.jini.lookup.ServiceDiscoveryManager(new net.jini.discovery.LookupDiscovery(groups, this), null, this);");
                // fw.append("    groups = org.dwfa.queue.QueueServer.groups; ");
                // fw.append(" }");
                // fw.close();

                queueConfigFile = new File(inboxPath + separator + "queue.config");

                String[] entries = new String[] { queueConfigFile.getAbsolutePath() };

                Configuration configuration = ConfigurationProvider.getInstance(entries, getClass().getClassLoader());

                Configuration workerConfiguration = ConfigurationProvider.getInstance(
                    new String[] { "myConfig.config" }, getClass().getClassLoader());

                MojoWorker mw = new MojoWorker(workerConfiguration, UUID.randomUUID(), "MoJo worker");

                I_ConfigAceFrame configFrame = NewDefaultProfile.newProfile(workFlowManager, workFlowManager,
                    workFlowManager, workFlowManager, workFlowManager);
                mw.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), configFrame);
                mw.getLogger().setLevel(Level.FINE);

                getLog().info("Generate assignment process for workflow manager");
                mw.execute(createAssignmentProcess(dupUuidFile));

            }// End if
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }// End method execute

    private BusinessProcess createAssignmentProcess(File inputFile) throws PropertyVetoException, IOException,
            TerminologyException, IntrospectionException, IllegalAccessException, InvocationTargetException {
        // Construct the process.
        BusinessProcess assignmentProcess = new BusinessProcess("Assignment process", Condition.CONTINUE, false);
        LogMessageOnWorkerLog log1task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
        log1task.setMessage("-------- Starting assignment process...");

        OpenQueuesInFolder oqf = new OpenQueuesInFolder();
        oqf.setQueueDir(inboxPath);
        assignmentProcess.addTask(oqf);
        assignmentProcess.addBranch(log1task, oqf, Condition.CONTINUE);

        LaunchBatchGenAssignmentProcess genAssign = new LaunchBatchGenAssignmentProcess();
        genAssign.setProcessFileStr(wfAssignmentProcessFile);
        assignmentProcess.addTask(genAssign);
        assignmentProcess.addBranch(oqf, genAssign, Condition.CONTINUE);

        LogMessageOnWorkerLog log2task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
        log2task.setMessage("-------- Finished assignment process....");
        assignmentProcess.addBranch(genAssign, log2task, Condition.CONTINUE);

        assignmentProcess.addBranch(log2task, assignmentProcess.addTask(new Complete()), Condition.CONTINUE);

        List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String uuidLineStr;
        int counter = 0;
        while ((uuidLineStr = br.readLine()) != null) { // while loop begins
            // here
            List<UUID> uuidList = new ArrayList<UUID>();
            for (String uuidStr : uuidLineStr.split("\t")) {
                assignmentProcess.getLogger().info("uuidStrs: " + uuidStr);
                UUID uuid = UUID.fromString(uuidStr);
                uuidList.add(uuid);
                counter++;
            }
            uuidListOfLists.add(uuidList);
        } // End while

        List<List<UUID>> tempListList = uuidListOfLists;
        int sizeOfList = tempListList.size();

        List<Collection<UUID>> uuidListList = null;
        if (sizeOfList > listListSize) {
            uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, listListSize));
        } else {
            uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, sizeOfList));
        }

        if (tempListList.removeAll(uuidListList)) {
            // do nothing
        } else {
            assignmentProcess.getLogger().info("error encountered in removing uuid collection from list");
        }

        /*
         * Set workflow manager
         */
        List<String> addresses = new ArrayList<String>();
        addresses.add(workFlowManager);
        assignmentProcess.writeAttachment("workFlowManager", addresses);

        I_TermFactory termFact = LocalVersionedTerminology.get();
        I_ConfigAceFrame cf = termFact.getActiveAceFrameConfig();
        cf.setUsername(workFlowManager);
        assignmentProcess.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), cf);

        // the getAttachmentKey method prepends an A: to the key, thus providing
        // the information necessary to read and write
        // the attachment as a property... Thus we use the get/setProperty
        // methods. If you want to use
        // the read/writeAttachments methods, call
        // ProcessAttachmentKeys.ASSIGNEE.getName() instead of getAttachment
        // key.

        assignmentProcess.setProperty(ProcessAttachmentKeys.ASSIGNEE.getAttachmentKey(), workFlowManager);
        assignmentProcess.setProperty(ProcessAttachmentKeys.BATCH_UUID_LIST2.getAttachmentKey(), uuidListList);
        assignmentProcess.setProperty(ProcessAttachmentKeys.DESTINATION_ADR.getAttachmentKey(), workFlowManager);
        assignmentProcess.setProperty(ProcessAttachmentKeys.SELECTED_ADDRESSES.getAttachmentKey(), addresses);
        assignmentProcess.setProperty(ProcessAttachmentKeys.TO_ASSIGN_PROCESS.getAttachmentKey(),
            taAssignmentProcessFile);
        assignmentProcess.setProperty(ProcessAttachmentKeys.DETAIL_HTML_DIR.getAttachmentKey(),
            "src/main/bp/instructions");
        assignmentProcess.setProperty(ProcessAttachmentKeys.DETAIL_HTML_FILE.getAttachmentKey(),
            "instructions_dup.html");

        return assignmentProcess;
    }
}// End class
