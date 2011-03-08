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
/*
 * Created on Jun 7, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.transaction.Transaction;

import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;
import org.dwfa.util.LogWithAlerts;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.types.SoapFilter;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapList;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;

/*
 * @author Marc Campbell
 */
public class CollabInboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {
    private I_QueueProcesses queue;

    // WORKER
    private Thread workerThread;
    private boolean sleeping;
    private long sleepTime = 1000 * 60 * 1;
    private Properties props;

    // CollabNet parameters
    private String repoUrlStr;
    private String repoTrackerIdStr;
    private String userNameStr;
    private String userPwdStr;
    private HashMap<String, String> categoryBpMap; 
    private boolean disabled = false;

    // :NYI:!!!: CollabAuthenticator extends Authenticator see MailAuthenticator
    // :NYI:!!!: public class SvnPrompter implements PromptUserPassword3

    /**
     * @param config
     * @param id
     * @param desc
     * @throws Exception
     */
    public CollabInboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector) throws Exception {
        super(config, id, desc);
        props = new Properties();

        // TRACKER CONNECTION PARAMETERS
        // -- REPOSITORY URL "https://csfe.aceworkspace.net"
        repoUrlStr = (String) this.config.getEntry(this.getClass().getName(), "repoUrlStr", String.class);
        props.put("repo.url.str", repoUrlStr);
        // -- TRACKER ID "trackerNNNN"
        repoTrackerIdStr = (String) this.config.getEntry(this.getClass().getName(), "repoTrackerIdStr", String.class);
        props.put("repo.trackerid.str", repoTrackerIdStr);

        // LOGIN SESSION PARAMETERS
        // -- USER NAME
        userNameStr = (String) this.config.getEntry(this.getClass().getName(), "userNameStr", String.class);
        props.put("user.name.str", userNameStr);
        // -- USER PASSWORD
        userPwdStr = (String) this.config.getEntry(this.getClass().getName(), "userPwdStr", String.class);
        props.put("user.pwd.str", userPwdStr);

        categoryBpMap = new HashMap<String, String>(); 
        String tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category01Str", String.class);
        props.put("category.bp.01", tmpStr);
        int splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));

        tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category02Str", String.class);
        props.put("category.bp.02", tmpStr);
        splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));
         
        tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category03Str", String.class);
        props.put("category.bp.03", tmpStr);
        splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));
        
        tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category04Str", String.class);
        props.put("category.bp.04", tmpStr);
        splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));
        
        tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category05Str", String.class);
        props.put("category.bp.05", tmpStr);
        splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));
        
        tmpStr = (String) this.config.getEntry(this.getClass().getName(), "category06Str", String.class);
        props.put("category.bp.06", tmpStr);
        splitIdx = tmpStr.indexOf("/");
        categoryBpMap.put(tmpStr.substring(0, splitIdx), tmpStr.substring(splitIdx + 1));
        
        // :NYI:!!!: ADD Authenticator. PROMPTS FOR PWD IF NOT PRESENT.
        if (!validLoginDetails()) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Invalid CollabNet username or password.", "",
                JOptionPane.ERROR_MESSAGE);
            disabled = true;
            return;
        }

        this.setPluginForInterface(I_GetWorkFromQueue.class, this);
    }

    public boolean validLoginDetails() {
        try {
            CollabNetSoapConnection sfc = new CollabNetSoapConnection(repoUrlStr);
            String sessionId = sfc.login(userNameStr, userPwdStr);
            // CLOSE SESSION
            sfc.logoff(sessionId);
        } catch (RemoteException e) {
            return false;
        }

        return true;
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#queueContentsChanged()
     */
    public void queueContentsChanged() {
        if (this.sleeping) {
            this.workerThread.interrupt();
        }
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
     */
    public void start(I_QueueProcesses queue) {
        this.queue = queue;
        this.workerThread = new Thread(this, "Worker " + this.getWorkerDesc());
        this.workerThread.start();
    }

    public void sleep() {
        this.sleeping = true;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            // NOTHING TO DO
        }
        this.sleeping = false;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (!disabled) {
        logger.info("RUN: starting CollabInboxQueueWorker.run() ... " + this.getWorkerDesc());

        Transaction t;
        while (true) {
            try {
                // OPEN CONNECTION (URL_STR)
                CollabNetSoapConnection sfc = new CollabNetSoapConnection(repoUrlStr);

                logger.info("RUN: connection object: " + sfc.toString());

                // OPEN SESSION (USER_STR, PWD_STR)
                String sessionId = sfc.login(userNameStr, userPwdStr);
                logger.info("RUN:         sessionId: " + sessionId);

                // TRACKER (URL_STR, sessionId)
                TrackerAppSoapUtil tracker = new TrackerAppSoapUtil(repoUrlStr, sessionId);
                logger.info("RUN:         trackerId: " + repoTrackerIdStr);
                logger.info("RUN:           tracker: " + tracker);

                // SETUP ARTIFACT FILTER
                SoapFilter sfStatusA = new SoapFilter();
                sfStatusA.setName("status");
                sfStatusA.setValue("Master ready to download");
                SoapFilter sfStatusB = new SoapFilter();
                sfStatusB.setName("status");
                sfStatusB.setValue("Detail ready to download");
                SoapFilter sfStatusC = new SoapFilter();
                sfStatusC.setName("status");
                sfStatusC.setValue("Unreviewed ready to download");
                SoapFilter[] sf = new SoapFilter[] {sfStatusA, sfStatusB, sfStatusC};

                // GET FILTERED LIST OF ARTIFACTS
                List<ArtifactSoapRow> arts = tracker.getArtifactList(repoTrackerIdStr, sf);

                // :NYI: list tracker fields

                // LOOP THROUGH TRACKER ARTIFACTS
                String artifactId = null; // :WAS: issueExternalArtfId
                for (ArtifactSoapRow asr : arts) {
                        // only look at the artifacts assigned to this user
                        if (userNameStr.equals(asr.getAssignedToUsername())) {
                    logArtfSoapRow(asr);
                    artifactId = asr.getId();

                    // SETUP DOWNLOAD ATTACHMENT DIRECTORY
                    // File downloadDir = new File("src/test/java");

                    AttachmentSoapList attachList = tracker.listAttachments(artifactId);
                    AttachmentSoapRow[] attachSoapRowArray = attachList.getDataRows();
                    // :!!!:NYI: should only have one attachment
                    // Get DataOjbect to be able to change values
                    ArtifactSoapDO asdo = tracker.getArtifactData(sessionId, artifactId);
                    logArtfSoapDO(asdo);

                    AttachmentSoapRow attachSoapRow = null;
                    for (int i = 0; i < attachSoapRowArray.length; i++) {
                        attachSoapRow = attachSoapRowArray[i];
                        logAttachSoapRow(attachSoapRow);

                        // DOWNLOAD ATTACHMENT
                        String fName = attachSoapRow.getFileName();
                        String fRawId = attachSoapRow.getRawFileId();
                                // String fStoredId =
                                // attachSoapRow.getStoredFileId();
                        String attachmentId = attachSoapRow.getAttachmentId();
                        DataHandler dh = tracker.downloadAttachment(artifactId, fName, fRawId);

                        String fileName = dh.getName().toLowerCase();
                        logger.info("RUN:  attachment file name:  " + fileName);

                        if (fName.endsWith("bp")) {

                            t = this.getActiveTransaction();
                            
                            // :DEBUG:BEGIN:
                            ObjectInputStream ois = new ObjectInputStream(dh.getInputStream());
                            I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                            process.setProperty("A: ID_ARTF", artifactId);

                                    process.setProperty("A: COLLABNET_ARTIFACT_SUBMITTER", asdo.getCreatedBy());

                            this.queue.write(process, t);
                            
                            // DELETE ATTACHMENT
                            tracker.deleteAttachment(artifactId, attachmentId);
                            
                            // SET STATUS
                                    if (asdo.getStatus().equalsIgnoreCase("Unreviewed ready to download")) {
                                        asdo.setStatus("Unreviewed in progress");
                                    } else {
                                asdo.setStatus("Detail in process");
                                    }

                            tracker.setArtifactData(asdo, "Detail downloaded for next step.");
                            
                            this.commitTransactionIfActive();

                        } else if (fName.endsWith("csv")) {
                            // String contentType = dh.getContentType();
                            // String name = dh.getName();
                            
                            File file = new File(fName);
                            FileOutputStream fos = new FileOutputStream(file);
                            dh.writeTo(fos);
                            fos.close();
                            logger.info("RUN:  attachment file saved:  " + file);
                            
                            // ** READ ** each CSV line
                            FileReader     fr = new FileReader(file);
                            BufferedReader br = new BufferedReader(fr);
                            String eachLine = br.readLine();    
                            
                            if (eachLine != null) {
                                String[] csvHeader = parseLine(eachLine);

                                // if (csvHeader.length < 1)

                                // READ IN ALL "VALID" ROWS
                                ArrayList<String[]> fileRows = new ArrayList<String[]>();
                                eachLine = br.readLine();
                                while (eachLine != null) {
                                    String[] row = parseLine(eachLine);
                                    // CHECK IF "VALID"
                                            if (row.length > 1 && row[0].length() > 0 && row.length == csvHeader.length) {
                                        fileRows.add(row);
                                    }
                                    // READ NEXT CSV LINE
                                    eachLine = br.readLine();
                                }
                                br.close();
                                file.delete();

                                int rowCounter = 1;
                                Integer rowTotal = Integer.valueOf(fileRows.size());
                                for (String[] csvRow : fileRows) {

                                    t = this.getActiveTransaction();
                                    // CREATE BP AND SAVE BP
                                            String processName =
                                                    asdo.getTitle() + " (detail#" + Integer.toString(rowCounter) + ")";

                                    String bpStr = categoryBpMap.get(asdo.getCategory());

                                            // I_EncodeBusinessProcess newProcess =
                                            // (I_EncodeBusinessProcess)
                                            // Class.forName("org.kp.bpa.KpetBusinessProcess").newInstance();
                                            I_EncodeBusinessProcess newProcess =
                                                    (I_EncodeBusinessProcess) Class.forName(bpStr).newInstance();
                                    newProcess.setName(processName);

                                    newProcess.setProperty("A: CATEGORY", asdo.getCategory());
                                    newProcess.setProperty("A: CUSTOMER", asdo.getCustomer());

                                    newProcess.setProperty("A: ID_ARTF", "NA");
                                    newProcess.setProperty("A: ID_ARTF_PARENT", asdo.getId());

                                            newProcess.setProperty("A: SEND_STATUS", "Detail ready to download");
                                    newProcess.setProperty("A: SEND_TO_USER", "username");
                                    newProcess.setProperty("A: SEND_COMMENT", "Remotely updated.");
                                    newProcess.setProperty("A: ROW", rowCounter);
                                    newProcess.setProperty("A: ROW_TOTAL", rowTotal);

                                    StringBuilder sb = new StringBuilder();
                                    for (int ci = 0; ci < csvHeader.length; ci++)
                                                sb.append("<html>" + csvHeader[ci] + ": " + csvRow[ci] + "<br>");
                                    newProcess.setProperty("A: MESSAGE", sb.toString());

                                    // Set artifact description.
                                    sb = new StringBuilder();
                                    for (int ci = 0; ci < csvHeader.length; ci++)
                                        sb.append(csvHeader[ci] + ": " + csvRow[ci] + "\r\n");
                                    if (asdo.getDescription().length() > 0)
                                                newProcess.setProperty("A: DESCRIPTION", asdo.getDescription() + "\r\n"
                                                    + sb.toString());
                                    else
                                        newProcess.setProperty("A: DESCRIPTION", sb.toString());

                                    this.queue.write(newProcess, t);

                                    // SET STATUS
                                    asdo.setStatus("Master in process");
                                    tracker.setArtifactData(asdo, "Master downloaded.");

                                    this.commitTransactionIfActive();

                                    rowCounter = rowCounter + 1;
                                }
                            }
                        } // if (fName.endsWith("csv"))

                    }

                    logger.info("RUN:  priority SoapDO  " + asdo.getPriority());
                }
                    }

                // CLOSE SESSION
                sfc.logoff(sessionId);
                logger.info("[INFO] logoff successful for \"" + sessionId + "\"");

            } catch (Throwable ex) {
                this.discardActiveTransaction();
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            logger.info("RUN: sleeping CollabInboxQueueWorker.run() ... " + this.getWorkerDesc());
            this.sleep();
            logger.info("RUN: waking CollabInboxQueueWorker.run() ... " + this.getWorkerDesc());
        }
    }
    }

    /** The default separator to use if none is supplied to the constructor. */
    public static final char CSV_SEPARATOR = ',';

    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char CSV_QUOTE_CHARACTER = '"';

   /**
     * Parses an incoming String and returns an array of elements.
     * 
     * @param nextLine
     *            the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String nextLine) throws IOException {

        if (nextLine == null) {
            return null;
        }

        List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                // continuing a quoted section, re-append newline
                sb.append("\n");
                // nextLine = getNextLine(); // :NYI: will need if any field
                // includes new line
                // if (nextLine == null)
                break;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                char c = nextLine.charAt(i);
                if (c == CSV_QUOTE_CHARACTER) {
                    // this gets complex... the quote may end a quoted block, or
                    // escape another quote.
                    // do a 1-char lookahead:
                    if (inQuotes // we are in quotes, therefore there can be
                        // escaped quotes in here.
                        && nextLine.length() > (i + 1) // there is indeed
                        // another character to
                        // check.
                        && nextLine.charAt(i + 1) == CSV_QUOTE_CHARACTER) { // ..and
                        // that
                        // char.
                        // is
                        // a
                        // quote
                        // also.
                        // we have two quote chars in a row == one quote char,
                        // so consume them both and
                        // put one on the token. we do *not* exit the quoted
                        // text.
                        sb.append(nextLine.charAt(i + 1));
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                        // the tricky case of an embedded quote in the middle:
                        // a,bc"d"ef,g
                        if (i > 2 //not on the begining of the line
                            && nextLine.charAt(i - 1) != this.CSV_SEPARATOR // not
                            // at
                            // the
                            // begining
                            // of
                            // an
                            // escape
                            // sequence
                            && nextLine.length() > (i + 1) && nextLine.charAt(i + 1) != this.CSV_SEPARATOR // not
                        // at
                        // the
                        // end
                        // of
                        // an
                        // escape
                        // sequence
                        ) {
                            sb.append(c);
                        }
                    }
                } else if (c == CSV_SEPARATOR && !inQuotes) {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuffer(); // start work on next token
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return (String[]) tokensOnThisLine.toArray(new String[0]);

    }

    public synchronized Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        throw new UnsupportedOperationException();
    }

    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            Exception {
        throw new UnsupportedOperationException();
    }

    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
        throw new UnsupportedOperationException();
    }

    public I_Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException();
    }

    public void setCurrentWorkspace(I_Workspace workspace) {
        throw new UnsupportedOperationException();
    }

    public Collection<I_Workspace> getWorkspaces() {
        throw new UnsupportedOperationException();
    }

    public Object selectFromList(Object[] list, String title, String instructions) {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir)
            throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }

    public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException,
            ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
        throw new UnsupportedOperationException();
    }

    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
            PrivilegedActionException {
        throw new UnsupportedOperationException();
    }
    
    void logArtfSoapRow(ArtifactSoapRow asr) {
        StringBuilder sb = new StringBuilder();
        // artifactId = "artfNNNN"
        sb.append("\r\n**    (artifact) Id = " + asr.getId());
        sb.append("\r\n**            Title = " + asr.getTitle());
        // priority = {... , 3, ...}
        sb.append("\r\n**         Priority = " + asr.getPriority());
        // Status = "Ready to download"
        sb.append("\r\n**           Status = " + asr.getStatus());
        // StatusClass = "Open"
        sb.append("\r\n**      StatusClass = " + asr.getStatusClass());
        sb.append("\r\n        ActualHours = " + asr.getActualHours());
        sb.append("\r\n      ArtifactGroup = " + asr.getArtifactGroup());
        sb.append("\r\n AssignedToFullname = " + asr.getAssignedToFullname());
        sb.append("\r\n AssignedToUsername = " + asr.getAssignedToUsername());
        sb.append("\r\n           Category = " + asr.getCategory());
        sb.append("\r\n          CloseDate = " + asr.getCloseDate());
        sb.append("\r\n           Customer = " + asr.getCustomer());
        sb.append("\r\n        Description = " + asr.getDescription());
        sb.append("\r\n     EstimatedHours = " + asr.getEstimatedHours());

        // ProjectId = "proj1040"
        sb.append("\r\n          ProjectId : " + asr.getProjectId());
        // ProjectTitle = "IHTSDO Subset Editor"
        sb.append("\r\n       ProjectTitle : " + asr.getProjectTitle());
        // FolderId = "tracker1157"
        sb.append("\r\n           FolderId : " + asr.getFolderId());
        // FolderTitle = "SME Workflow Tracker"
        sb.append("\r\n        FolderTitle : " + asr.getFolderTitle());
        // FolderPathString = "tracker.sme_workflow_tracker"
        // ProjectPathString = "projects.ihtsdo_subset_editor"
        sb.append("\r\n  ProjectPathString : " + asr.getProjectPathString());
        sb.append("\r\n   FolderPathString : " + asr.getFolderPathString());
        sb.append("\r\n   LastModifiedDate = " + asr.getLastModifiedDate());
        // SubmittedByFullname = "FirstName LastName (ABC Inc.)"
        sb.append("\r\nSubmittedByFullname = " + asr.getSubmittedByFullname());
        // SubmittedByUsername = "usrname"
        sb.append("\r\nSubmittedByUsername = " + asr.getSubmittedByUsername());
        // 
        sb.append("\r\n      SubmittedDate = " + asr.getSubmittedDate());
        logger.info(sb.toString());
    }
    
    void logArtfSoapDO(ArtifactSoapDO asdo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n**     (Artifact) Id = " + asdo.getId());
        sb.append("\r\n**             Title = " + asdo.getTitle());
        sb.append("\r\n**          Priority = " + asdo.getPriority());
        sb.append("\r\n**            Status = " + asdo.getStatus());
        sb.append("\r\n**       StatusClass = " + asdo.getStatusClass());
        
        sb.append("\r\n         ActualHours = " + asdo.getActualHours());
        sb.append("\r\n    (Artifact) Group = " + asdo.getGroup());
        sb.append("\r\n          AssignedTo = " + asdo.getAssignedTo());
        sb.append("\r\n            Category = " + asdo.getCategory());
        sb.append("\r\n           CloseDate = " + asdo.getCloseDate());
        sb.append("\r\n            Customer = " + asdo.getCustomer());
        sb.append("\r\n         Description = " + asdo.getDescription());
        sb.append("\r\n      EstimatedHours = " + asdo.getEstimatedHours());
        sb.append("\r\n            FolderId : " + asdo.getFolderId());
        sb.append("\r\n       (Folder) Path : " + asdo.getPath());
        sb.append("\r\n      LastModifiedBy = " + asdo.getLastModifiedBy());
        sb.append("\r\n    LastModifiedDate = " + asdo.getLastModifiedDate());
        sb.append("\r\n   ReportedReleaseId = " + asdo.getReportedReleaseId());
        sb.append("\r\n   ResolvedReleaseId = " + asdo.getResolvedReleaseId());
        sb.append("\r\n  (Submit) CreatedBy = " + asdo.getCreatedBy());
        sb.append("\r\n(Submit) CreatedDate = " + asdo.getCreatedDate());
        sb.append("\r\n             Version = " + asdo.getVersion());
        SoapFieldValues sfv = asdo.getFlexFields();
        String[] sfvNames = sfv.getNames();
        String[] sfvTypes = sfv.getTypes();
        Object[] sfvValues = sfv.getValues();
        sb.append("\r\n          FlexFields = " + sfv);
        
        for (int i=0; i<sfvNames.length; i++) { 
            sb.append("Name/Type/Value :: " + sfvNames[i]);
            sb.append(" / " + sfvTypes[i]);
            sb.append("\r\n / " + sfvValues[i]);
        }
        logger.info(sb.toString());
    }
    
    void logAttachSoapRow(AttachmentSoapRow attachSoapRow) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n// AttachmentId = " + attachSoapRow.getAttachmentId());
        sb.append("\r\n//  DateCreated = " + attachSoapRow.getDateCreated());
        sb.append("\r\n//     FileName = " + attachSoapRow.getFileName());
        sb.append("\r\n//     FileSize = " + attachSoapRow.getFileSize());
        sb.append("\r\n//     Mimetype = " + attachSoapRow.getMimetype());
        sb.append("\r\n//    RawFileId = " + attachSoapRow.getRawFileId());
        sb.append("\r\n// StoredFileId = " + attachSoapRow.getStoredFileId());
        logger.info(sb.toString());
    }

}