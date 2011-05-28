package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.thread.NamedThreadFactory;

public class BpAction extends AbstractAction implements Runnable {

    private static final ThreadGroup bpActionThreadGroup = new ThreadGroup("BpAction threads");
    private static ExecutorService executorService = Executors.newCachedThreadPool(
            new NamedThreadFactory(bpActionThreadGroup, "BpAction executor service"));
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private URL processURL;
    private I_ConfigAceFrame frameConfig;
    private I_HostConceptPlugins host;
    private ScrollablePanel wizardPanel;

    public BpAction(String processUrlStr, I_ConfigAceFrame frameConfig,
            I_HostConceptPlugins host, ScrollablePanel wizardPanel)
            throws MalformedURLException, IOException, ClassNotFoundException {
        this(new URL(processUrlStr), frameConfig, host, wizardPanel);
    }

    public BpAction(URL processUrl, I_ConfigAceFrame frameConfig,
            I_HostConceptPlugins host, ScrollablePanel wizardPanel)
            throws MalformedURLException, IOException, ClassNotFoundException {
        super();
        processURL = processUrl;
        this.frameConfig = frameConfig;
        this.host = host;
        this.wizardPanel = wizardPanel;
        I_EncodeBusinessProcess process = getProcess();

        putValue(NAME, process.getName());
        putValue(SHORT_DESCRIPTION, process.getSubject());
        try {
            String longDesc = process.getProcessDocumentation();
            if (longDesc != null) {
                putValue(LONG_DESCRIPTION, longDesc);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        try {
            byte[] iconBytes = (byte[]) process.readAttachement("menu_icon");
            if (iconBytes != null) {
                ImageIcon icon = new ImageIcon(iconBytes);
                putValue(SMALL_ICON, icon);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        try {
            byte[] iconBytes = (byte[]) process.readAttachement("button_icon");
            if (iconBytes != null) {
                ImageIcon icon = new ImageIcon(iconBytes);
                putValue(LARGE_ICON_KEY, icon);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public BpAction(String processUrlStr, I_ConfigAceFrame frameConfig,
            I_HostConceptPlugins host)
            throws MalformedURLException, IOException, ClassNotFoundException {
        this(new URL(processUrlStr), frameConfig, host);
    }

    public BpAction(URL processUrl, I_ConfigAceFrame frameConfig,
            I_HostConceptPlugins host)
            throws MalformedURLException, IOException, ClassNotFoundException {
        super();
        processURL = processUrl;
        this.frameConfig = frameConfig;
        this.host = host;
        I_EncodeBusinessProcess process = getProcess();

        putValue(NAME, process.getName());
        putValue(SHORT_DESCRIPTION, process.getSubject());
        try {
            String longDesc = process.getProcessDocumentation();
            if (longDesc != null) {
                putValue(LONG_DESCRIPTION, longDesc);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        try {
            byte[] iconBytes = (byte[]) process.readAttachement("menu_icon");
            if (iconBytes != null) {
                ImageIcon icon = new ImageIcon(iconBytes);
                putValue(SMALL_ICON, icon);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        try {
            byte[] iconBytes = (byte[]) process.readAttachement("button_icon");
            if (iconBytes != null) {
                ImageIcon icon = new ImageIcon(iconBytes);
                putValue(LARGE_ICON_KEY, icon);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    private I_EncodeBusinessProcess getProcess() throws FileNotFoundException,
            IOException, ClassNotFoundException {
        InputStream inStream;
        if (processURL.getProtocol().toLowerCase().startsWith("file")) {
            inStream = new FileInputStream(processURL.getFile());
        } else {
            inStream = processURL.openStream();
        }
        BufferedInputStream bis = new BufferedInputStream(inStream);
        ObjectInputStream ois = new ObjectInputStream(bis);

        I_EncodeBusinessProcess bp = (I_EncodeBusinessProcess) ois.readObject();
        return bp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        executorService.submit(this);
    }

    @Override
    public void run() {
        try {
            I_EncodeBusinessProcess process = getProcess();
            I_Work worker = frameConfig.getWorker().getTransactionIndependentClone();
            worker.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(),
                    host.getTermComponent());
            worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(),
                    host);
            worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
                    frameConfig);
            worker.writeAttachment(WorkerAttachmentKeys.WIZARD_PANEL.name(),
                    wizardPanel);
            process.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(),
                    host.getTermComponent());
            process.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(),
                    host);
            process.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
                    frameConfig);
            worker.execute(process);
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } 
    }
}
