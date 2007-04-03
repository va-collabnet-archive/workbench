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
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.tapi.NoMappingException;

/**
 * @author kec
 * 
 */
public class ProcessPanel extends JPanel implements PropertyChangeListener {

    private static final String VIEW_MESSAGE = "message";

    private static final String VIEW_TASKS = "tasks";

    private static final String VIEW_HISTORY = "history";

    private static Logger logger = Logger.getLogger(ProcessPanel.class
            .getName());

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private I_EncodeBusinessProcess process;

    private ProcessDiagramPanel processDiagram;

    List<ActionListener> taskAddedListeners = new ArrayList<ActionListener>();

    private JComboBox viewCombo = new JComboBox(new Object[] { VIEW_HISTORY,
            VIEW_TASKS, VIEW_MESSAGE });

    private I_Work worker;

    private JCheckBox viewAttachments;

    private JPopupMenu attachmentPopup;

    private ProcessAttachmentTableModel attachmentTableModel;

    private JTable attachmentTable;

    private ActionListener popupListener = new AttachmentPopupActionListener();

    private JButton addAttachment;

    private TableSorter attachmentSortingTable;

    private TableSorter executionSortingTable;

    private JButton addEmptyAttachmentKey;
    private class AddAttachmentActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) getTopLevelAncestor(),
                    "Add Attachment", FileDialog.LOAD);
            f.setDirectory(System.getProperty("user.dir")
                    + System.getProperty("file.separator") + "beans");
            f.setFilenameFilter(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".bean") || name.endsWith(".xml") ||
                   	name.toLowerCase().endsWith(".png") ||
                   	name.toLowerCase().endsWith(".jpg") ||
                   	name.toLowerCase().endsWith(".jpeg") ||
                   	name.toLowerCase().endsWith(".gif");
                }
            });
            f.setVisible(true); // Display dialog and wait for response
            try {
                if (f.getFile() != null) {
                    File processFile = new File(f.getDirectory(), f.getFile());
                    String attachmentName;
                    if (f.getFile().toLowerCase().endsWith(".xml")) {
                        XMLDecoder d = new XMLDecoder(new BufferedInputStream(
                                new FileInputStream(processFile)));
                        Object obj = d.readObject();
                        d.close();
                        attachmentName = f.getFile();
                        attachmentName = attachmentName.substring(0,
                                attachmentName.length() - 4);
                        addAttachment(obj, attachmentName);
                        layoutComponents();
                    } else if (f.getFile().toLowerCase().endsWith(".bean")) {
                        FileInputStream fis = new FileInputStream(processFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        Object obj = ois.readObject();
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Read object: "
                                    + obj.getClass().toString());
                        }
                        ois.close();
                        attachmentName = f.getFile();
                        attachmentName = attachmentName.substring(0,
                                attachmentName.length() - 5);
                        addAttachment(obj, attachmentName);
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
                logger.log(Level.WARNING, "Exception reading object: "
                        + f.getFile(), ex);
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
            String rootName = "rename";
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
                String key = (String) attachmentTableModel.getValueAt(
                        attachmentSortingTable.modelIndex(attachmentTable.getSelectedRow()),
                        ProcessAttachmentTableModel.NAME);
                Object object = process.readAttachement(key);
                open(object);
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
                String key = (String) attachmentTableModel.getValueAt(
                        attachmentSortingTable.modelIndex(attachmentTable.getSelectedRow()),
                        ProcessAttachmentTableModel.NAME);
                Object obj = process.readAttachement(key);
                // Create a file dialog box to prompt for a new file to display
                FileDialog f = new FileDialog((Frame) getTopLevelAncestor(),
                        "Save Attachment", FileDialog.SAVE);
                f.setDirectory(System.getProperty("user.dir"));
                f.setFile(key + ".bean");
                f.setVisible(true); // Display dialog and wait for response
                if (f.getFile() != null) {
                    File processBinaryFile = new File(f.getDirectory(), f
                            .getFile());
                    FileOutputStream fos = new FileOutputStream(
                            processBinaryFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(obj);
                    oos.close();
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
            String key = (String) attachmentTableModel
                    .getValueAt(attachmentSortingTable.modelIndex(attachmentTable.getSelectedRow()),
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
                } else if (I_EncodeBusinessProcess.class
                        .isAssignableFrom(object.getClass())) {
                    I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) object;
                    ProcessPanel panel = new ProcessPanel(process, null);
                    new FrameWithOpenFramesListener("Attached Process: "
                            + process.getName(), "Attachment", new JScrollPane(
                            panel));
                } else if (byte[].class.isAssignableFrom(object.getClass())) {
                	byte[] imageBytes = (byte[]) object;
                     new FrameWithOpenFramesListener("Attached Image: "
                            + process.getName(), "Attachment", new JLabel(new ImageIcon(imageBytes)));
               	
                } else {
                    JEditorPane textPane = new JEditorPane();
                    textPane.setContentType("text/html");
                    textPane.setText(object.toString());
                    textPane.setEditable(false);
                    new FrameWithOpenFramesListener("Attached Object: "
                            + process.getName(), "Attachment", new JScrollPane(
                            textPane));

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
        public UpdateFieldDocumentListener(Method setMethod, I_DefineTask task,
                JTextArea textField) {
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
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws QueryException
     * @throws PropertyVetoException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws NoMappingException
     * @throws RemoteException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * 
     */
    public ProcessPanel(I_EncodeBusinessProcess process, I_Work worker)
            throws PropertyVetoException, Exception {
        super(new GridBagLayout());

        viewCombo.setSelectedIndex(1);
        viewCombo.addActionListener(new SetPanelActionListener());

        this.process = process;
        this.worker = worker;
        this.viewAttachments = new JCheckBox("attachments: ");
        this.viewAttachments
                .addActionListener(new ShowAttachmentListActionListener());
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
        addEmptyAttachmentKey = new JButton("Add Key");
        addEmptyAttachmentKey.addActionListener(new AddEmptyAttachmentActionListener());
        addAttachment = new JButton("Add Attachment");
        addAttachment.addActionListener(new AddAttachmentActionListener());
        process.addPropertyChangeListener(this);
        layoutComponents();

    }

    /**
     * @throws PropertyVetoException
     * @throws Exception
     */
    private void layoutComponents() throws PropertyVetoException, Exception {
        this.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("priority: ", JLabel.RIGHT), c);
        c.gridx++;

        JComboBox priorityComboBox = new JComboBox(Priority.values);
        priorityComboBox.setSelectedItem(process.getPriority());
        priorityComboBox.addActionListener(new UpdatePriorityActionListener());
        this.add(priorityComboBox, c);
        c.gridx++;
        this.add(new JLabel("     process id: ", JLabel.RIGHT), c);
        c.gridx++;
        JLabel pidLabel = new JLabel(process.getProcessID().toString());
        c.gridwidth = 2;
        this.add(pidLabel, c);
        
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        this.add(new JLabel("process name: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        JTextArea textField = new JTextArea(1, 45);
        textField.setLineWrap(true);
        textField.setText(process.getName());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(process.getClass().getMethod(
                        "setName", new Class[] { String.class }), process,
                        textField));
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        c.gridwidth = 4;
        this.add(textField, c);
        c.gridwidth = 1;
        c.gridy++;

        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("subject: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;

        c.gridx++;
        textField = new JTextArea(1, 45);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(true);
        textField.setText(process.getSubject());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(process.getClass().getMethod(
                        "setSubject", new Class[] { String.class }), process,
                        textField));
        c.gridwidth = 4;
        this.add(textField, c);
        c.gridwidth = 1;
        c.gridy++;
        // ------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("originator: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        textField = new JTextArea(1, 45);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(true);
        textField.setText(process.getOriginator());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(process.getClass().getMethod(
                        "setOriginator", new Class[] { String.class }),
                        process, textField));
        c.gridwidth = 4;
        this.add(textField, c);
        c.gridwidth = 1;
        c.gridy++;
        // ------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("destination: ", JLabel.RIGHT), c);
        c.fill = GridBagConstraints.NONE;
        c.gridx++;
        // c.weightx = 0.5;
        textField = new JTextArea(1, 45);
        textField.addKeyListener(new TextfieldKeyAdaptor(textField));
        textField.addFocusListener(new SelectAllFocusAdapter(textField));
        textField.setLineWrap(true);
        textField.setText(process.getDestination());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(process.getClass().getMethod(
                        "setDestination", new Class[] { String.class }),
                        process, textField));
        c.gridwidth = 4;
        this.add(textField, c);
        c.gridwidth = 1;
        c.gridy++;

        // --------------
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        //this.add(new JLabel("attachments: ", JLabel.RIGHT), c);
        this.add(viewAttachments, c);
        
        c.gridx++;
        this.add(new JLabel(new Integer(process.getAttachmentKeys().size())
                .toString(), JLabel.LEFT), c);
        c.gridx++;
        this.add(new JLabel(), c);
        c.gridx++;
        this.add(addAttachment, c);

        c.gridx++;
        this.add(addEmptyAttachmentKey, c);
        c.gridy++;
        if (viewAttachments.isSelected()) {
            attachmentTableModel = new ProcessAttachmentTableModel(this.process);
            attachmentSortingTable = new TableSorter(attachmentTableModel);
            attachmentTable = new JTable(attachmentSortingTable);
            attachmentTable
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            attachmentSortingTable.setTableHeader(attachmentTable.getTableHeader());
            attachmentTableModel.setWidths(attachmentTable);

            // Set up tool tips for column headers.
            attachmentSortingTable
                    .getTableHeader()
                    .setToolTipText(
                            "Click to specify sorting; Control-Click to specify secondary sorting");
            attachmentTable.addMouseListener(new AttachmentPopupListener());
            JScrollPane scroller = new JScrollPane(attachmentTable);
            scroller.setPreferredSize(new Dimension(500, 100));
            c.gridx = 0;
            c.gridwidth = 5;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            this.add(scroller, c);
            c.gridy++;
        }
        // ---------- view line
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("view: ", JLabel.RIGHT), c);
        c.gridx++;
        this.add(viewCombo, c);

        JLabel label = new JLabel("            current task id: ", JLabel.RIGHT);
        c.gridx++;

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        this.add(label, c);

        TaskIdPanel idLabel = new TaskIdPanel(process.getCurrentTaskId(),
                process);
        idLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        idLabel.addPropertyChangeListener("id", new SetCurrentTaskId(process));
        this.process.addPropertyChangeListener("currentTaskId", idLabel);
        c.gridx = c.gridx + c.gridwidth;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        this.add(idLabel, c);
        c.gridy++;

        // ------------ Process/History/Message pane...
        c.weightx = 1;
        c.weighty = 1;
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 6;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        String viewString = (String) this.viewCombo.getSelectedItem();
        if (viewString.equals(VIEW_HISTORY)) {
            ExecutionRecordTableModel tableModel = new ExecutionRecordTableModel(
                    process);
            executionSortingTable = new TableSorter(tableModel);
            JTable executionTable = new JTable(executionSortingTable);
            executionSortingTable.setTableHeader(executionTable.getTableHeader());

            // Set up tool tips for column headers.
            executionSortingTable
                    .getTableHeader()
                    .setToolTipText(
                            "Click to specify sorting; Control-Click to specify secondary sorting");

            JScrollPane sp = new JScrollPane(executionTable);
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary.setBorder(BorderFactory
                    .createTitledBorder("Execution History:"));
            this.add(intermediary, c);
        } else if (viewString.equals(VIEW_TASKS)) {
            ProcessDiagramPanel processDiagram = new ProcessDiagramPanel(
                    this.process, worker);
            JScrollPane sp = new JScrollPane(processDiagram);
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary.setBorder(BorderFactory
                    .createTitledBorder("Process Diagram:"));
            this.add(intermediary, c);
            Iterator<ActionListener> listenerItr = this.taskAddedListeners
                    .iterator();
            while (listenerItr.hasNext()) {
                ActionListener l = listenerItr.next();
                this.processDiagram.addTaskAddedActionListener(l);
            }

        } else if (viewString.equals(VIEW_MESSAGE)) {
        	   I_RenderMessage renderer = this.process.getMessageRenderer();
        	   String message;
        	   if (renderer != null) {
        		   message = renderer.getMessage();
        	   } else {
        		   message = "<html>" + this.process.getName() + " has no message renderer.";
        	   }
            JScrollPane sp = new JScrollPane(new JLabel(message));
            JPanel intermediary = new JPanel(new GridLayout(1, 1));
            intermediary.add(sp);
            intermediary
                    .setBorder(BorderFactory.createTitledBorder("Message:"));
            this.add(intermediary, c);

        }
        this.revalidate();
        this.repaint();

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
            if (event.getKeyCode() == KeyEvent.VK_TAB
                    || event.getKeyChar() == '\t') {

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

        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        layoutComponents();
                    } catch (PropertyVetoException ex) {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        }
    }
}