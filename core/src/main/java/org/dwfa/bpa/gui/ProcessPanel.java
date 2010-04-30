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
package org.dwfa.bpa.gui;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.util.io.FileIO;

/**
 * @author kec
 * 
 */
public class ProcessPanel extends JPanel implements PropertyChangeListener {

    private static final String VIEW_MESSAGE = "message";

    private static final String VIEW_TASKS = "tasks";

    private static final String VIEW_HISTORY = "history";

    private static final String VIEW_DOCUMENTATION = "documentation";

    private static Logger logger = Logger.getLogger(ProcessPanel.class.getName());

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private I_EncodeBusinessProcess process;

    private ProcessDiagramPanel processDiagram;

    List<ActionListener> taskAddedListeners = new ArrayList<ActionListener>();

    private JComboBox viewCombo = new JComboBox(new Object[] { VIEW_HISTORY, VIEW_TASKS, VIEW_MESSAGE,
                                                              VIEW_DOCUMENTATION });

    private I_Work worker;

    private JCheckBox viewAttachments;

    private JPopupMenu attachmentPopup;

    private ProcessAttachmentTableModel attachmentTableModel;

    private JTable attachmentTable;

    private ActionListener popupListener = new AttachmentPopupActionListener();

    private JButton addAttachment;

    private JButton addEmptyAttachmentKey;

    private JSplitPane headerProcessSplit;

    private I_HandleDoubleClickInTaskProcess doubleClickHandler;

    private class AddAttachmentActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) getTopLevelAncestor(), "Add Attachment", FileDialog.LOAD);
            f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "beans");
            f.setFilenameFilter(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".bean") || name.endsWith(".xml") || name.toLowerCase().endsWith(".bp")
                        || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")
                        || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".html")
                        || name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".gif");
                }
            });
            f.setVisible(true); // Display dialog and wait for response
            try {
                if (f.getFile() != null) {
                    File processFile = new File(f.getDirectory(), f.getFile());
                    String attachmentName;
                    if (f.getFile().toLowerCase().endsWith(".xml")) {
                        XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(processFile)));
                        Object obj = d.readObject();
                        d.close();
                        attachmentName = f.getFile();
                        attachmentName = attachmentName.substring(0, attachmentName.length() - 4);
                        addAttachment(obj, attachmentName);
                        layoutComponents();
                    } else if (f.getFile().toLowerCase().endsWith(".bp")) {
                        FileInputStream fis = new FileInputStream(processFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        Object obj = ois.readObject();
                        obj = new MarshalledObject(obj);
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Read object: " + obj.getClass().toString());
                        }
                        ois.close();
                        attachmentName = f.getFile();
                        attachmentName = attachmentName.substring(0, attachmentName.length() - 5);
                        addAttachment(obj, attachmentName);
                        layoutComponents();

                    } else if (f.getFile().toLowerCase().endsWith(".bean")) {
                        FileInputStream fis = new FileInputStream(processFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        Object obj = ois.readObject();
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Read object: " + obj.getClass().toString());
                        }
                        ois.close();
                        attachmentName = f.getFile();
                        attachmentName = attachmentName.substring(0, attachmentName.length() - 5);
                        addAttachment(obj, attachmentName);
                        layoutComponents();

                    } else if ((f.getFile().toLowerCase().endsWith(".html"))
                        || (f.getFile().toLowerCase().endsWith(".txt"))) {
                        FileReader fr = new FileReader(processFile);
                        BufferedReader br = new BufferedReader(fr);
                        String fileString = FileIO.readerToString(br);
                        attachmentName = f.getFile();
                        addAttachment(fileString, attachmentName);
                        layoutComponents();
                    } else {
                        // Handle a graphic image...
                        FileInputStream fis = new FileInputStream(processFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        byte[] bytes = new byte[bis.available()];
                        int bytesToRead = bytes.length;
                        int bytesRead = 0;
                        while (bytesToRead != 0) {
                            int read = bis.read(bytes, bytesRead, bytesToRead);
                            bytesRead = bytesRead + read;
                            bytesToRead = bytesToRead - read;
                        }
                        bis.close();
                        attachmentName = f.getFile();
                        addAttachment(bytes, attachmentName);
                        layoutComponents();
                    }
                }
                f.dispose(); // Get rid of the dialog box
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Exception reading object: " + f.getFile(), ex);
            }
        }

        private void addAttachment(Object obj, String name) {
            String attachmentName = name;
            int counter = 1;
            while (process.getAttachmentKeys().contains(attachmentName)) {
                attachmentName = name + " " + counter++;
            }
            process.writeAttachment(attachmentName, obj);
        }

    }

    private class AddEmptyAttachmentActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            String rootName = "rename-key";
            String attachmentName = rootName;
            int counter = 1;
            while (process.getAttachmentKeys().contains(attachmentName)) {
                attachmentName = rootName + " " + counter++;
            }
            process.writeAttachment(attachmentName, null);
        }

    }

    private class AttachmentPopupActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(e.getActionCommand());
            }
            String command = e.getActionCommand().toLowerCase();
            if (command.equals("open...")) {
                if (attachmentTable.getSelectedRow() >= 0) {
                    String key = (String) attachmentTableModel.getValueAt(attachmentTable.getSelectedRow(),
                        ProcessAttachmentTableModel.NAME);

                    Object object = process.readAttachement(key);
                    if (logger.isLoggable(Level.INFO)) {
                        if (object != null) {
                            logger.info("Opening attachment: " + key + " value: " + object.toString());
                        } else {
                            logger.info("Opening attachment: " + key + " value: " + null);
                        }
                    }
                    open(object);
                } else {
                    JOptionPane.showMessageDialog(attachmentTable, "No row is selected...");
                }

            } else if (command.equals("save as...")) {
                saveAs();
            } else if (command.equals("remove")) {
                removeAttachment();

            } else if (command.equals("edit value...")) {
                editValue();
            }

        }

        private void editValue() {
            throw new UnsupportedOperationException();
            /*
             * String key = (String) attachmentTableModel.getValueAt(
             * attachmentTable.getSelectedRow(), AttachmentTableModel.NAME);
             * Object obj = process.readAttachement(key);
             */
        }

        /**
         *
         */
        private void saveAs() {
            try {
                String key = (String) attachmentTableModel.getValueAt(attachmentTable.getSelectedRow(),
                    ProcessAttachmentTableModel.NAME);
                Object obj = process.readAttachement(key);
                // Create a file dialog box to prompt for a new file to display
                FileDialog f = new FileDialog((Frame) getTopLevelAncestor(), "Save Attachment", FileDialog.SAVE);
                f.setDirectory(System.getProperty("user.dir"));

                if (obj instanceof FileContent) {
                    f.setFile(((FileContent) obj).getFilename());
                } else {
                    f.setFile(key + ".bean");
                }

                // f.setFile(key + ".bean");
                f.setVisible(true); // Display dialog and wait for response
                if (f.getFile() != null) {
                    File processBinaryFile = new File(f.getDirectory(), f.getFile());
                    FileOutputStream fos = new FileOutputStream(processBinaryFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    if (obj instanceof FileContent) {
                        FileContent inputFile = (FileContent) obj;
                        byte[] contents = inputFile.getContents();

                        for (int i = 0; i < contents.length; i++) {
                            bos.write(contents[i]);
                        }
                        bos.close();

                    } else {

                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(obj);
                        oos.close();
                    }

                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Wrote to disk:\n" + obj);

                    }
                }
                f.dispose(); // Get rid of the dialog box
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        /**
         *
         */
        private void removeAttachment() {
            String key = (String) attachmentTableModel.getValueAt(attachmentTable.getSelectedRow(), 
                ProcessAttachmentTableModel.NAME);
            process.takeAttachment(key);
            try {
                layoutComponents();
            } catch (PropertyVetoException e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            }
        }

        private void open(Object object) {
            if (object == null) {
                return;
            }
            try {
                if (MarshalledObject.class.isAssignableFrom(object.getClass())) {

                    MarshalledObject marshalledObj = (MarshalledObject) object;
                    this.open(marshalledObj.get());
                } else if (I_EncodeBusinessProcess.class.isAssignableFrom(object.getClass())) {
                    if (doubleClickHandler == null) {
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) object;
                        ProcessPanel panel = new ProcessPanel(process, null, doubleClickHandler);
                        new FrameWithOpenFramesListener("Attached Process: " + process.getName(), "Attachment",
                            new JScrollPane(panel));
                    } else {
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) object;
                        doubleClickHandler.handle(process, worker, ProcessPanel.this.process);
                    }
                } else if (byte[].class.isAssignableFrom(object.getClass())) {
                    byte[] imageBytes = (byte[]) object;
                    new FrameWithOpenFramesListener("Attached Image: " + process.getName(), "Attachment", new JLabel(
                        new ImageIcon(imageBytes)));

                } else if (FileContent.class.isAssignableFrom(object.getClass())) {
                    FileContent contents = (FileContent) object;

                    new FrameWithOpenFramesListener("Attached File: " + contents.getFilename(), "Attachment",
                        new JLabel("To view this file, choose 'Save as...' from the previous menu."));

                } else {
                    new FrameWithOpenFramesListener("Attached Object: " + process.getName(), "Attachment",
                        new JScrollPane(new JLabel(object.toString())));

                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private class AttachmentPopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                attachmentPopup.show(e.getComponent(), e.getX(), e.getY());
                e.consume();
            }
        }
    }

    private class UpdateFieldDocumentListener implements DocumentListener {
        Method setMethod;

        I_DefineTask task;

        JTextArea textField;

        /**
         * @param setMethod
         * @param task
         * @param textField
         */
        public UpdateFieldDocumentListener(Method setMethod, I_DefineTask task, JTextArea textField) {
            super();
            this.setMethod = setMethod;
            this.task = task;
            this.textField = textField;
        }

        public void insertUpdate(DocumentEvent e) {
            try {
                setMethod.invoke(task, new Object[] { textField.getText() });
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            }

        }

        public void removeUpdate(DocumentEvent e) {
            try {
                setMethod.invoke(task, new Object[] { textField.getText() });
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            }
        }

        public void changedUpdate(DocumentEvent e) {
            try {
                setMethod.invoke(task, new Object[] { textField.getText() });
            } catch (Exception e1) {
                logger.log(Level.SEVERE, e1.getMessage(), e1);
            }
        }
    }

    private class UpdatePriorityActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ev) {
            JComboBox cb = (JComboBox) ev.getSource();
            Priority p = (Priority) cb.getSelectedItem();
            process.setPriority(p);
        }

    }

    private class ShowAttachmentListActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                layoutComponents();
            } catch (PropertyVetoException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private class SetPanelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent ev) {
            try {
                layoutComponents();
                ProcessPanel.this.invalidate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param process
     * @param worker
     * @throws PropertyVetoException
     * @throws Exception
     */
    public ProcessPanel(I_EncodeBusinessProcess process, I_Work worker,
            I_HandleDoubleClickInTaskProcess doubleClickHandler) throws PropertyVetoException, Exception {
        super(new GridBagLayout());
        this.doubleClickHandler = doubleClickHandler;
        if (process.getMessageRenderer() != null) {
            viewCombo.setSelectedIndex(2);
        } else {
            viewCombo.setSelectedIndex(1);
        }
        viewCombo.addActionListener(new SetPanelActionListener());

        this.process = process;
        this.worker = worker;
        this.viewAttachments = new JCheckBox("attachments: ");
        this.viewAttachments.addActionListener(new ShowAttachmentListActionListener());
        this.attachmentPopup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Open...");
        menuItem.addActionListener(popupListener);
        this.attachmentPopup.add(menuItem);
        menuItem = new JMenuItem("Save As...");
        menuItem.addActionListener(popupListener);
        this.attachmentPopup.add(menuItem);
        // menuItem = new JMenuItem("Edit Value...");
        // menuItem.addActionListener(popupListener);
        // this.attachmentPopup.add(menuItem);
        menuItem = new JMenuItem("Remove");
        menuItem.addActionListener(popupListener);
        this.attachmentPopup.add(menuItem);
        addEmptyAttachmentKey = new JButton("   Add Key    ");
        addAttachment = new JButton("Add Attachment");
        addEmptyAttachmentKey.addActionListener(new AddEmptyAttachmentActionListener());
        addAttachment.addActionListener(new AddAttachmentActionListener());
        process.addPropertyChangeListener(this);
        layoutComponents();

    }

    /**
     * @throws PropertyVetoException
     * @throws Exception
     */
    private void layoutComponents() throws PropertyVetoException, Exception {
        if (headerProcessSplit == null) {
            this.setLayout(new GridLayout(1, 1));
            headerProcessSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            headerProcessSplit.setOneTouchExpandable(true);
            this.add(headerProcessSplit);
        }
        setTopPanel();
        setBottomPanel();
        this.revalidate();
        this.repaint();

    }

    private void setTopPanel() throws NoSuchMethodException, ClassNotFoundException {
        JPanel topPanel = new JPanel(new GridBagLayout());
        headerProcessSplit.setTopComponent(topPanel);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel("priority: ", JLabel.RIGHT), c);
        c.gridx++;

        JComboBox priorityComboBox = new JComboBox(Priority.values);
        priorityComboBox.setSelectedItem(process.getPriority());
        priorityComboBox.addActionListener(new UpdatePriorityActionListener());
        topPanel.add(priorityComboBox, c);
        c.gridx++;
        topPanel.add(new JLabel("     process id: ", JLabel.RIGHT), c);
        c.gridx++;
        JLabel pidLabel = new JLabel(process.getProcessID().toString());
        c.gridwidth = 2;
        topPanel.add(pidLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        topPanel.add(new JLabel("process name: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        JTextArea textField = new JTextArea(1, 50);
        textField.setLineWrap(false);
        textField.setText(process.getName());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
            new UpdateFieldDocumentListener(process.getClass().getMethod("setName", new Class[] { String.class }),
                process, textField));
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        c.gridwidth = 6;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(textField, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridy++;

        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel("subject: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;

        c.gridx++;
        textField = new JTextArea(1, 50);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(false);
        textField.setText(process.getSubject());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
            new UpdateFieldDocumentListener(process.getClass().getMethod("setSubject", new Class[] { String.class }),
                process, textField));
        c.gridwidth = 6;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(textField, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridy++;
        // ------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel("originator: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        textField = new JTextArea(1, 50);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(false);
        textField.setText(process.getOriginator());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
            new UpdateFieldDocumentListener(
                process.getClass().getMethod("setOriginator", new Class[] { String.class }), process, textField));
        c.gridwidth = 6;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(textField, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridy++;
        // ------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel("destination: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        // c.weightx = 0.5;
        textField = new JTextArea(1, 50);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(false);
        textField.setText(process.getDestination());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
            new UpdateFieldDocumentListener(process.getClass()
                .getMethod("setDestination", new Class[] { String.class }), process, textField));
        c.gridwidth = 6;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(textField, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridy++;

        // --------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        // this.add(new JLabel("attachments: ", JLabel.RIGHT), c);
        topPanel.add(viewAttachments, c);

        c.gridx++;
        topPanel.add(new JLabel(new Integer(process.getAttachmentKeys().size()).toString(), JLabel.LEFT), c);
        c.gridx++;
        c.weightx = 0;
        topPanel.add(new JLabel(), c);
        c.anchor = GridBagConstraints.EAST;

        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        addEmptyAttachmentKey.setVisible(viewAttachments.isSelected());
        topPanel.add(addEmptyAttachmentKey, c);

        c.gridx++;
        addAttachment.setVisible(viewAttachments.isSelected());
        c.anchor = GridBagConstraints.WEST;
        topPanel.add(addAttachment, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy++;
        if (viewAttachments.isSelected()) {
            attachmentTableModel = new ProcessAttachmentTableModel(this.process);
            attachmentTable = new JTable(attachmentTableModel);
            SortClickListener.setupSorter(attachmentTable);
            attachmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            attachmentTableModel.setWidths(attachmentTable);

            // Set up tool tips for column headers.
            attachmentTable.getTableHeader().setToolTipText(
                "Click to specify sorting");
            attachmentTable.addMouseListener(new AttachmentPopupListener());
            JScrollPane scroller = new JScrollPane(attachmentTable);
            // scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scroller.setPreferredSize(new Dimension(500, 100));
            scroller.setMinimumSize(new Dimension(500, 100));
            c.gridx = 0;
            c.gridwidth = 7;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            topPanel.add(scroller, c);
            c.weightx = 0.0;
            c.gridy++;
        }
        // ---------- view line
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel("view: ", JLabel.RIGHT), c);
        c.gridx++;
        topPanel.add(viewCombo, c);

        JLabel label = new JLabel("            current task id: ", JLabel.RIGHT);
        c.gridx++;

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        topPanel.add(label, c);

        TaskIdPanel idLabel = new TaskIdPanel(process.getCurrentTaskId(), process);
        idLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        idLabel.addPropertyChangeListener("id", new SetCurrentTaskId(process));
        this.process.addPropertyChangeListener("currentTaskId", idLabel);
        c.gridx = c.gridx + c.gridwidth;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        topPanel.add(idLabel, c);
        c.gridy++;
    }

    private void setBottomPanel() throws PropertyVetoException, Exception {
        // ------------ Process/History/Message pane...

        String viewString = (String) this.viewCombo.getSelectedItem();
        if (viewString.equals(VIEW_HISTORY)) {
            ExecutionRecordTableModel tableModel = new ExecutionRecordTableModel(process);
            JTable executionTable = new JTable(tableModel);
            SortClickListener.setupSorter(executionTable);

            // Set up tool tips for column headers.
            executionTable.getTableHeader().setToolTipText(
                "Click to specify sorting");

            JScrollPane sp = new JScrollPane(executionTable);
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary.setBorder(BorderFactory.createTitledBorder("Execution History:"));
            headerProcessSplit.setBottomComponent(intermediary);
        } else if (viewString.equals(VIEW_TASKS)) {
            ProcessDiagramPanel processDiagram = new ProcessDiagramPanel(this.process, worker, doubleClickHandler);
            JScrollPane sp = new JScrollPane(processDiagram);
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary.setBorder(BorderFactory.createTitledBorder("Process Diagram:"));
            headerProcessSplit.setBottomComponent(intermediary);
            Iterator<ActionListener> listenerItr = this.taskAddedListeners.iterator();
            while (listenerItr.hasNext()) {
                ActionListener l = listenerItr.next();
                this.processDiagram.addTaskAddedActionListener(l);
            }

        } else if (viewString.equals(VIEW_MESSAGE)) {
            I_RenderMessage renderer = this.process.getMessageRenderer();
            String message;
            if (renderer != null) {
                message = renderer.getMessage();
                if (message == null || message.length() == 0) {
                    message = "<html>" + this.process.getName() + " has a null message.";
                }
            } else {
                message = "<html>" + this.process.getName() + " has no message renderer.";
            }
            JEditorPane messagePane = new JEditorPane("text/html", message);
            messagePane.setEditable(false);
            JScrollPane sp = new JScrollPane(messagePane);
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary.setBorder(BorderFactory.createTitledBorder("Message:"));
            headerProcessSplit.setBottomComponent(intermediary);
        } else if (viewString.equals(VIEW_DOCUMENTATION)) {
            JTextFieldEditor processDocEditor = new JTextFieldEditor(8, 10);
            processDocEditor.setValue(process.getProcessDocumentationSource());
            processDocEditor.addPropertyChangeListener(new PropertyListenerGlue("setProcessDocumentationSource",
                String.class, process));
            JSplitPane intermediary = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            intermediary.setResizeWeight(0.4);
            intermediary.setTopComponent(processDocEditor.getCustomEditor());
            JEditorPane processDocPane = new JEditorPane("text/html", process.getProcessDocumentation()) {
                /**
				 *
				 */
                private static final long serialVersionUID = 1L;

                @Override
                public Dimension getMaximumSize() {
                    Dimension size = super.getMaximumSize();
                    if (getParent() != null) {
                        size.width = getParent().getWidth() - 10;
                    }
                    return size;
                }
            };
            processDocPane.setEditable(false);
            intermediary.setBottomComponent(new JScrollPane(processDocPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
            ProcessDocChangeListener changeListener = new ProcessDocChangeListener(processDocPane);
            process.addPropertyChangeListener(changeListener);
            processDocEditor.addPropertyChangeListener(changeListener);
            intermediary.setBorder(BorderFactory.createTitledBorder("Process documentation:"));
            headerProcessSplit.setBottomComponent(intermediary);
        }
    }

    private class ProcessDocChangeListener implements PropertyChangeListener, ActionListener {

        JEditorPane processDocPane;

        Timer updateTimer;

        public ProcessDocChangeListener(JEditorPane processDocPane) {
            super();
            this.processDocPane = processDocPane;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (updateTimer != null) {
                updateTimer.stop();
            }
            updateTimer = new Timer(1200, this);
            updateTimer.setRepeats(false);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            try {
                processDocPane.setText(process.getProcessDocumentation());
            } catch (Exception ex) {
                ProcessBuilderPanel.getLogWithAlerts().alertAndLogException(ex);
            }
        }

    }

    public class TextfieldKeyAdaptor extends KeyAdapter {
        JTextArea textArea;

        /**
         * @param textArea
         */
        public TextfieldKeyAdaptor(JTextArea textArea) {
            super();
            this.textArea = textArea;
        }

        public void keyPressed(KeyEvent event) {
            // look for tab keys
            if (event.getKeyCode() == KeyEvent.VK_TAB || event.getKeyChar() == '\t') {

                if (event.isShiftDown()) {
                    this.textArea.transferFocusBackward();
                } else {
                    this.textArea.transferFocus();
                }

                event.consume();

            }
        }
    }

    public class SelectAllFocusAdapter extends FocusAdapter {
        JTextArea textArea;

        /**
         * @param textArea
         */
        public SelectAllFocusAdapter(JTextArea textArea) {
            super();
            this.textArea = textArea;
        }

        public void focusGained(FocusEvent e) {
            this.textArea.selectAll();
        }

        public void focusLost(FocusEvent e) {

        }
    }

    public void addTaskAddedActionListener(ActionListener l) {
        this.taskAddedListeners.add(l);
        if (this.processDiagram != null) {
            this.processDiagram.addTaskAddedActionListener(l);
        }
    }

    public void removeTaskAddedActionListener(ActionListener l) {

        this.taskAddedListeners.remove(l);
        if (this.processDiagram != null) {
            this.processDiagram.removeTaskAddedActionListener(l);
        }
    }

    /**
     * @return Returns the process.
     */
    public I_EncodeBusinessProcess getProcess() {
        return process;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == "name") {

        } else if (evt.getPropertyName() == "originator") {

        } else if (evt.getPropertyName() == "priority") {

        } else if (evt.getPropertyName() == "subject") {

        } else if (evt.getPropertyName() == "destination") {

        } else if (evt.getPropertyName().startsWith("PropertyDescriptor:")) {

        } else if (evt.getPropertyName().startsWith("Externalize: ")) {

        } else {
            if (lastLayoutDoer != null) {
                lastLayoutDoer.setStopped(true);
            }
            lastLayoutDoer = new DoLayout();
            SwingUtilities.invokeLater(lastLayoutDoer);
        }
    }

    private DoLayout lastLayoutDoer;

    private class DoLayout implements Runnable {
        private boolean stopped = false;

        public void run() {
            if (stopped) {
                return;
            }
            try {
                layoutComponents();
            } catch (PropertyVetoException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        public boolean isStopped() {
            return stopped;
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

    }
}
