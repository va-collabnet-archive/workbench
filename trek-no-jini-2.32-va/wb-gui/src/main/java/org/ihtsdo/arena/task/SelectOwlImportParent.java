/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.ContradictionException;

/**
 * Sets the parent concept for the owl file import.
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class SelectOwlImportParent extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    I_ConfigAceFrame config;
    JPanel panel;
    JPanel workflowPanel;
    protected transient boolean done = false;
    String parentConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    String inputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    UUID parentConceptUuid;
    TermComponentLabel tl;
    Condition returnCondition;
    private transient Exception ex;
    File f;
    JLabel importFileName;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(parentConceptPropName);
        out.writeObject(inputFilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            parentConceptPropName = (String) in.readObject();
            inputFilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException, ContradictionException {
        ex = null;
        config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
        DoSwing swinger = new DoSwing(process);
        swinger.execute();
        synchronized (this) {
            this.waitTillDone(worker.getLogger());
        }
        try {
            if(returnCondition.equals(Condition.CONTINUE)){
                process.setProperty(parentConceptPropName, parentConceptUuid);
                process.setProperty(inputFilePropName, f.getAbsolutePath());
            }
        } catch (IntrospectionException ex) {
            throw new TaskFailedException(ex);
        } catch (IllegalAccessException ex) {
            throw new TaskFailedException(ex);
        } catch (InvocationTargetException ex) {
            throw new TaskFailedException(ex);
        }

        panel.setVisible(false);
        workflowPanel.setVisible(false);

        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        done = false;
        return returnCondition;
    }

    private class DoSwing extends SwingWorker<Boolean, Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            makePanel();
            return true;
        }

        @Override
        protected void done() {
            panel.setVisible(true);
            workflowPanel.setVisible(true);
        }
    }

    private void makePanel() throws TerminologyException, IOException {
        panel = config.getWorkflowPanel();
        // Clear components
        Component[] comp = panel.getComponents();
        for (int i = 0; i < comp.length; i++) {
            panel.remove(comp[i]);
        }
        panel.setVisible(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH;

        // Add the header
        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        gbcTop.weightx = 1.0;
        gbcTop.weighty = 0;
        gbcTop.gridwidth = 2;
        gbcTop.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Please select the import parameters."), gbcTop);

        // Add the processing buttons
        gbcTop.weightx = 0.0;
        gbcTop.gridx = 2;
        gbcTop.gridwidth = 1;
        setUpButtons(panel, gbcTop);
        
        Dimension wfPanelDim = new Dimension(panel.getWidth(), panel.getHeight() * 2);
        config.setWorkflowDetailSheetDimensions(wfPanelDim);
        workflowPanel = config.getWorkflowDetailsSheet();
        workflowPanel.setLayout(new GridBagLayout());
        Component[] components = workflowPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            workflowPanel.remove(components[i]);
        }
        GridBagConstraints gbcBottom = new GridBagConstraints();
        gbcBottom.fill = GridBagConstraints.BOTH;

        // Add the instructions
        gbcBottom.gridx = 0;
        gbcBottom.gridy = 0;
        gbcBottom.weightx = 1.0;
        gbcBottom.weighty = 0;
        gbcBottom.gridwidth = 2;
        gbcBottom.anchor = GridBagConstraints.EAST;

        //add the import file
        gbcBottom.gridy++;
        gbcBottom.gridx = 0;
        gbcBottom.weightx = 0;
        gbcBottom.gridwidth = 1;
        workflowPanel.add(new JLabel("Import file:"), gbcBottom);
        gbcBottom.gridx = 1;
        importFileName = new JLabel("<html><font color='red'>no file selected");
        workflowPanel.add(importFileName, gbcBottom);

        gbcBottom.gridx = 0;
        gbcBottom.weightx = 0;
        gbcBottom.gridx = 2;
        JButton selectButton = new JButton("select file");
        selectButton.addActionListener(new FileSelectActionListener());
        workflowPanel.add(selectButton, gbcBottom);

        //add concept list
        gbcBottom.gridx = 0;
        gbcBottom.weightx = 0;
        gbcBottom.gridy++;
        workflowPanel.add(new JLabel("Import parent concept:"), gbcBottom);

        gbcBottom.gridy++;
        gbcBottom.gridwidth = 10;
        workflowPanel.add(new JSeparator(), gbcBottom);

        gbcBottom.gridwidth = 2;
        gbcBottom.gridy++;
        gbcBottom.weightx = 1.0;
        tl = new TermComponentLabel();
        tl.addTermChangeListener(new ChangeListener());
        workflowPanel.add(tl, gbcBottom);
        gbcBottom.weighty = 0.0;
    }

    protected class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            I_GetConceptData newValue = (I_GetConceptData) e.getNewValue();
            parentConceptUuid = newValue.getPrimUuid();
        }
    }

    protected void setUpButtons(final JPanel panel, GridBagConstraints c) {
        c.gridx++;
        panel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        JButton continueButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));
        continueButton.setToolTipText("continue");
        panel.add(continueButton, c);
        continueButton.addActionListener(new ContinueActionListener());
        c.gridx++;

        JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));
        cancelButton.setToolTipText("cancel");
        panel.add(cancelButton, c);
        cancelButton.addActionListener(new StopActionListener());
        c.gridx++;
        panel.add(new JLabel("     "), c);
        panel.validate();
        Container cont = panel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        continueButton.requestFocusInWindow();
        panel.repaint();
    }

    private class ContinueActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (f == null) {
                JOptionPane.showMessageDialog(null, "Please import file concept before conintuing.", "Select Import File", JOptionPane.ERROR_MESSAGE);
            } 
            if (parentConceptUuid == null) {
                JOptionPane.showMessageDialog(null, "Please select parent concept before conintuing.", "Select Parent Concept", JOptionPane.ERROR_MESSAGE);
            } 
            if(f != null && parentConceptUuid != null){
                returnCondition = Condition.CONTINUE;
                done = true;
                SelectOwlImportParent.this.notifyTaskDone();
            }
        }
    }

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            panel.setVisible(false);
            workflowPanel.setVisible(false);
            done = true;
            SelectOwlImportParent.this.notifyTaskDone();
        }
    }

    private class FileSelectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FileDialog dialog = new FileDialog(new Frame(), "Select import file");
            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".owl");
                }
            });
            dialog.setVisible(true);
            f = new File(dialog.getDirectory(), dialog.getFile());
            importFileName.setText(f.getName());
        }
    }

    protected void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                ex = e;
            }
        }
    }

    protected void notifyTaskDone() {
        synchronized (SelectOwlImportParent.this) {
            SelectOwlImportParent.this.notifyAll();
        }
    }

    public boolean isDone() {
        return this.done;
    }

    protected static String getContinueImage() {
        return "/16x16/plain/navigate_right.png";
    }

    protected static String getCancelImage() {
        return "/16x16/plain/navigate_cross.png";
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getParentConceptPropName() {
        return parentConceptPropName;
    }

    public void setParentConceptPropName(String parentConceptPropName) {
        this.parentConceptPropName = parentConceptPropName;
    }

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }
}
