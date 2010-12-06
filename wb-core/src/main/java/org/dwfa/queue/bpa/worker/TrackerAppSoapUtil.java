package org.dwfa.queue.bpa.worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.types.SoapFilter;
import com.collabnet.ce.soap50.types.SoapSortKey;
import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapList;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapRow;
import com.collabnet.ce.soap50.webservices.cemain.ICollabNetSoap;
import com.collabnet.ce.soap50.webservices.cemain.TrackerFieldSoapDO;
import com.collabnet.ce.soap50.webservices.filestorage.IFileStorageAppSoap;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDetailSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ITrackerAppSoap;
import com.collabnet.ce.soap50.webservices.tracker.TrackerSoapDO;

/**
 * This utility class operates on Trackers and the Artifact elements within the
 * Tracker.
 * <p>
 * 
 */
public class TrackerAppSoapUtil {

    /* the TeamForge tracker interface */
    private ITrackerAppSoap m_trackerSoap;

    /* the session id returned from a previous call to login() */
    private String m_sessionId;

    private String MAP_FILENAME = "FieldMap.bin";

    private String m_serverUrl;

    /**
     * The Class JODataSource.
     */
    public class ProcDataSource implements DataSource {

        /** The object. */
        private Object object;

        /*
         * (non-Javadoc)
         * 
         * @see javax.activation.DataSource#getContentType()
         */
        @Override
        public String getContentType() {
            return "application/octet-stream";
            // return "multipart/*";
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.activation.DataSource#getName()
         */
        @Override
        public String getName() {
            return "java.object";
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.activation.DataSource#getOutputStream()
         */
        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Unsupported function");
        }

        /**
         * Instantiates a new jO data source.
         * 
         * @param object the object
         * 
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public ProcDataSource(Object object) throws IOException {
            this.object = object;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.activation.DataSource#getInputStream()
         */
        @Override
        public InputStream getInputStream() throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(output);
            objectStream.writeObject(object);
            objectStream.flush();

            ByteArrayInputStream result = new ByteArrayInputStream(output.toByteArray());

            return result;
        }

    }

    /**
     * a simple constructor built around the URL of the TeamForge server
     * <p>
     * 
     * @param serverUrl The fully qualified URL of the TeamForge server
     *            instance
     * @param sessionId A session identifier returned from a prior call
     *            to login()
     */
    public TrackerAppSoapUtil(String serverUrl, String sessionId) {

        m_sessionId = sessionId;
        m_serverUrl = serverUrl;

        m_trackerSoap = (ITrackerAppSoap) ClientSoapStubFactory.getSoapStub(ITrackerAppSoap.class, serverUrl);
    }

    /**
     * sets the priority data element associated with the soap row passed
     * in on the command line.
     * <p>
     * 
     * @param asr The artifact retrieved by a previous call to getArtifact()
     * @throws RemoteException if anything goes wrong within the TeamForge
     *             calls, wrap a remote exception around it and
     *             re-throw
     */
    public void setArtifactData(ArtifactSoapDO asd, String comment) throws RemoteException {
        try {
            ArtifactSoapDO asdtmp = m_trackerSoap.getArtifactData(m_sessionId, asd.getId());
            setActiveASD(asdtmp, asd);
            m_trackerSoap.setArtifactData(m_sessionId, asdtmp, comment, null, null, null);
        } catch (RemoteException e) {
            throw new RemoteException("unable to set artifact " + asd.getId(), e);
        }
    }

    public void setArtifactData(ArtifactSoapDO asd, String comment, String attachFileName, String attachMimeType,
            String attachFileId) throws RemoteException {
        try {
            ArtifactSoapDO asdtmp = m_trackerSoap.getArtifactData(m_sessionId, asd.getId());
            setActiveASD(asdtmp, asd);
            m_trackerSoap.setArtifactData(m_sessionId, asdtmp, comment, attachFileName, attachMimeType, attachFileId);
        } catch (RemoteException e) {
            throw new RemoteException("unable to set artifact with attachment " + asd.getId(), e);
        }
    }

    private void setActiveASD(ArtifactSoapDO asdAct, ArtifactSoapDO asdCust) {

        asdAct.setCategory(asdCust.getCategory());
        asdAct.setDescription(asdCust.getDescription());
        asdAct.setFlexFields(asdCust.getFlexFields());
        asdAct.setPriority(asdCust.getPriority());
        asdAct.setStatus(asdCust.getStatus());
        asdAct.setTitle(asdCust.getTitle());
        asdAct.setGroup(asdCust.getGroup());
        asdAct.setAssignedTo(asdCust.getAssignedTo());

    }

    public ArtifactDetailSoapRow[] getArtifactDetailList(String trackerId, String[] selectedColumns,
            SoapFilter[] filters, SoapSortKey[] sortKeys, int startIndex, int maxRows, boolean exceptionIfExpiredCache,
            boolean forceNewQuery) throws java.rmi.RemoteException {
        ArtifactDetailSoapRow[] adrl =
                m_trackerSoap.getArtifactDetailList(m_sessionId, trackerId, selectedColumns, filters, sortKeys,
                    startIndex, maxRows, exceptionIfExpiredCache, forceNewQuery).getDataRows();

        return adrl;
    }

    public ArtifactSoapRow[] findArtifacts(String queryString, String projectId, boolean searchAttachments)
            throws java.rmi.RemoteException {

        ArtifactSoapRow[] asl =
                m_trackerSoap.findArtifacts(m_sessionId, queryString, projectId, searchAttachments).getDataRows();

        return asl;
    }

    public String uploadAttachment(I_EncodeBusinessProcess process) throws IOException {
        ProcDataSource ds = new ProcDataSource(process);
        DataHandler dh = new DataHandler(ds);
        IFileStorageAppSoap fileStorage =
                (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class, m_serverUrl);
        String ObjId = fileStorage.uploadFile(m_sessionId, dh);
        return ObjId;
    }

    public String uploadAttachment(DataSource ds) throws IOException {
        DataHandler dh = new DataHandler(ds);
        IFileStorageAppSoap fileStorage =
                (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class, m_serverUrl);
        String ObjId = fileStorage.uploadFile(m_sessionId, dh);
        return ObjId;
    }

    public String sendFieldMap(HashMap map) throws IOException {
        // DataHandler dh = new DataHandler(new
        // org.ihtsdo.issue.util.JODataSource(map));
        // IFileStorageAppSoap fileStorage = (IFileStorageAppSoap)
        // ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class,m_serverUrl);
        // String ObjId=fileStorage.uploadFile(m_sessionId, dh);
        // return ObjId;
        return null;
    }

    public void setArtifactFieldMap(String artifactId, String mapFileId) throws RemoteException {
        try {
            /*
             * re-retrieve the artifact. This is good practice to minimize
             * errors resulting from artifacts that have been modified while
             * we hold them inside this jvm
             */
            ArtifactSoapDO ado = m_trackerSoap.getArtifactData(m_sessionId, artifactId);

            m_trackerSoap.setArtifactData(m_sessionId, ado, "field map setting", MAP_FILENAME,
                "application/octet-stream", mapFileId);
        } catch (RemoteException e) {
            StringBuilder sb = new StringBuilder("unable to set artifact field map ");
            System.out.println(sb.toString());

            // if(AceLog.getAppLog().isLoggable(Level.FINE))
            // AceLog.getAppLog().info((new
            // StringBuilder()).append("unable to set artifact field map"
            // ).append(artifactId).append(e.getMessage()).toString());
            // throw new RemoteException("unable to set artifact field map" +
            // artifactId, e);
        }
    }

    public DataHandler downloadAttachment(String artifactId, String fName, String rawFileId) throws IOException {

        IFileStorageAppSoap fileStorage =
                (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class, m_serverUrl);

        DataHandler dh = fileStorage.downloadFileDirect(m_sessionId, artifactId, rawFileId);

        return dh;
    }

    public File downloadAttachmentFile(String artifactId, String fName, String rawFileId, File toFolder)
            throws IOException {

        IFileStorageAppSoap fileStorage =
                (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class, m_serverUrl);

        DataHandler dh = fileStorage.downloadFileDirect(m_sessionId, artifactId, rawFileId);

        File file = new File(toFolder.getAbsolutePath() + File.separator + fName);
        FileOutputStream outputStream = new FileOutputStream(file);
        dh.writeTo(outputStream);

        return file;
    }

    // Get the field map of attachments of the artifact
    public Object[] getFieldMap(String artifactId) throws IOException, ClassNotFoundException {
        Object[] retObj = null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        // Now, retrieve the attachments
        IFileStorageAppSoap fileStorage =
                (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class, m_serverUrl);

        AttachmentSoapRow thisAttachment = getFieldMapAttachment(artifactId);
        if (thisAttachment != null) {
            retObj = new Object[2];
            DataHandler dh = fileStorage.downloadFileDirect(m_sessionId, artifactId, thisAttachment.getRawFileId());
            // Read the contents
            ObjectInputStream ois = new ObjectInputStream(dh.getInputStream());
            map = (HashMap<String, Object>) ois.readObject();
            retObj[0] = thisAttachment.getAttachmentId();
            retObj[1] = map;
        }
        return retObj;
    }

    public AttachmentSoapRow getFieldMapAttachment(String artifactId) throws IOException, ClassNotFoundException {
        ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
        AttachmentSoapList attachmentSoapList = sfSoap.listAttachments(m_sessionId, artifactId);

        // Now, retrieve the field map attachment
        AttachmentSoapRow[] attachmentRows = attachmentSoapList.getDataRows();
        AttachmentSoapRow Att = null;
        for (int i = 0; i < attachmentRows.length; i++) {
            Att = attachmentRows[i];
            if (Att.getFileName().equals(MAP_FILENAME)) {
                break;
            }
        }
        return Att;
    }

    public void delFieldMap(String artifactId, String fieldMapId) throws IOException, ClassNotFoundException {
        ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
        sfSoap.deleteAttachment(m_sessionId, artifactId, fieldMapId);
        return;
    }

    /**
     * returns a type-safe list built from the artifacts within the trackerId,
     * given as an argument
     * <p>
     * 
     * @param trackerId the containing tracker the contents of which we're
     *            going to read and return
     *            <p>
     * @throws RemoteException if any errors occur in a TeamForge API, we wrap
     *             this in an exception and throw it on up the call
     *             stack
     *             <p>
     * @return a list containing all artifacts within the trackerId given on
     *         the command line
     */

    public List<ArtifactSoapRow> getArtifactList(String trackerId) throws RemoteException {
        return getArtifactList(trackerId, null);
    }

    public List<ArtifactSoapRow> getArtifactList(String trackerId, SoapFilter[] soapFilters) throws RemoteException {
        ArtifactSoapList asl = null;
        try {
            /* return a list of artifacts from the tracker argument */
            asl = m_trackerSoap.getArtifactList(m_sessionId, trackerId, soapFilters);
        } catch (RemoteException e) {
            throw new RemoteException("unable to retrieve artifacts", e);
        }

        List<ArtifactSoapRow> artifacts = new ArrayList<ArtifactSoapRow>();

        if (asl != null) {
            /* put our artifacts in a type-safe list */
            ArtifactSoapRow[] artfRows = asl.getDataRows();
            artifacts.addAll(Arrays.asList(artfRows));
        }

        return artifacts;
    }

    public TrackerFieldSoapDO[] getFields(String trackerId) throws RemoteException {
        return m_trackerSoap.getFields(m_sessionId, trackerId);
    }

    public TrackerSoapDO createTracker(String projectId, String trackerName, String trackerTitle,
            String trackerDescription) throws RemoteException {
        TrackerSoapDO tsd;
        try {
            tsd = m_trackerSoap.createTracker(m_sessionId, projectId, trackerName, trackerTitle, trackerDescription);
        } catch (RemoteException e) {
            // if(AceLog.getAppLog().isLoggable(Level.FINE))
            // AceLog.getAppLog().info((new
            // StringBuilder()).append("unable to create tracker "
            // ).append(e.getMessage()).toString());
            throw new RemoteException("unable to create tracker", e);
        }
        return tsd;
    }

    public ArtifactSoapDO createArtifact(String trackerId, String artTitle, String artDescription, String group,
            String category, String status, String customer, int priority, int estimatedHours, String assignedUsername,
            String releasedId, SoapFieldValues flexFields, String attachmentFileName, String attachmentMimeType,
            String attachmentFileId) throws RemoteException {
        ArtifactSoapDO asd;
        try {
            asd =
                    m_trackerSoap.createArtifact(m_sessionId, trackerId, artTitle, artDescription, group, category,
                        status, customer, priority, estimatedHours, assignedUsername, releasedId, flexFields,
                        attachmentFileName, attachmentMimeType, attachmentFileId);
        } catch (RemoteException e) {
            // if(AceLog.getAppLog().isLoggable(Level.FINE))
            // AceLog.getAppLog().info((new
            // StringBuilder()).append("unable to create artifact "
            // ).append(e.getMessage()).toString());

            throw new RemoteException("unable to create artifact", e);
        }
        return asd;
    }

    public ArtifactSoapDO getArtifactData(String sessionId, String issueExternalId) throws RemoteException {

        ArtifactSoapDO asd = m_trackerSoap.getArtifactData(sessionId, issueExternalId);
        return asd;
    }

    public void deleteArtifact(String sessionId, String issueExternalId) throws RemoteException {

        m_trackerSoap.deleteArtifact(sessionId, issueExternalId);
        return;
    }

    public AttachmentSoapList listAttachments(String artifactId) throws RemoteException {
        ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
        AttachmentSoapList attachmentSoapList = sfSoap.listAttachments(m_sessionId, artifactId);

        return attachmentSoapList;
    }

    public void deleteAttachment(String artifactId, String attachmentId) throws RemoteException {
        ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
        sfSoap.deleteAttachment(m_sessionId, artifactId, attachmentId);
    }

    public void createArtifactDependency(String originId, String targetId, String description) throws RemoteException {
        m_trackerSoap.createArtifactDependency(m_sessionId, originId, targetId, description);
    }

    public ArtifactDependencySoapRow[] getChildDependencyRows(String artfId) throws RemoteException {
        ArtifactDependencySoapList adsl = m_trackerSoap.getChildDependencyList(m_sessionId, artfId);
        return adsl.getDataRows();
    }

    public ArtifactDependencySoapRow[] getParentDependencyRows(String artfId) throws RemoteException {
        ArtifactDependencySoapList adsl = m_trackerSoap.getParentDependencyList(m_sessionId, artfId);
        return adsl.getDataRows();
    }
}