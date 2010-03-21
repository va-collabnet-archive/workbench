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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputListener;

import org.dwfa.bpa.I_Branch;
import org.dwfa.bpa.NoBranchForConditionException;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.dnd.TaskTransferable;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.tasks.editor.I_NeedPropertyDisplayName;
import org.dwfa.bpa.tasks.editor.I_OnlyWantOneLine;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;
import org.dwfa.bpa.util.PlatformWebBrowser;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 * 
 */
public class TaskPanel extends JPanel implements I_DoDragAndDrop, MouseInputListener, PropertyChangeListener,
        FocusListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(TaskPanel.class.getName());

    private class TaskComponentListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("resized: " + e);
            }

        }

        public void componentMoved(ComponentEvent e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("moved: " + e);
            }
        }

        public void componentShown(ComponentEvent e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("shown: " + e);
            }
        }

        public void componentHidden(ComponentEvent e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("hidden: " + e);
            }
        }

    }

    public static class EditorGlue implements PropertyChangeListener {
        private Method writeMethod;

        private PropertyEditor editor;

        private Object target;

        public EditorGlue(PropertyEditor editor, Method writeMethod, Object target) {
            this.editor = editor;
            this.writeMethod = writeMethod;
            this.target = target;
        }

        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @SuppressWarnings("unchecked")
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                Object newValue = editor.getValue();
                if (newValue != null) {
                    if (Set.class.isAssignableFrom(newValue.getClass())) {
                        newValue = new HashSet((Collection) newValue);
                    } else if (List.class.isAssignableFrom(newValue.getClass())) {
                        newValue = new ArrayList((Collection) newValue);
                    }
                }
                writeMethod.invoke(target, new Object[] { newValue });
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }

    }

    public class UpAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param arg0
         */
        public UpAction() {
            super("moveUp");
            putValue(SHORT_DESCRIPTION, "move the task panel up");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
        }

        public void actionPerformed(ActionEvent evt) {
            if (TaskPanel.this.process == null) {
                return;
            }
            TaskPanel.this.setLocation(TaskPanel.this.getLocation().x, TaskPanel.this.getLocation().y - 1);
        }

    }

    public class DeleteAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param arg0
         */
        public DeleteAction() {
            super("deleteUp");
            putValue(SHORT_DESCRIPTION, "Delete the task from the process");
        }

        public void actionPerformed(ActionEvent evt) {
            if (TaskPanel.this.process == null) {
                return;
            }

            int option = JOptionPane.showConfirmDialog(TaskPanel.this.getTopLevelAncestor(),
                "Are you sure you want to delete this task?", "Delete?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try {
                    if (TaskPanel.this.process != null) {
                        TaskPanel.this.process.removeTask(TaskPanel.this.task);
                    }
                } catch (PropertyVetoException e) {
                    ProcessBuilderPanel.getLogWithAlerts().alertAndLogException(e);
                }
            }
        }

    }

    public class RightAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param arg0
         */
        public RightAction() {
            super("moveRight");
            putValue(SHORT_DESCRIPTION, "move the task panel right");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        }

        public void actionPerformed(ActionEvent evt) {
            if (TaskPanel.this.process == null) {
                return;
            }
            TaskPanel.this.setLocation(TaskPanel.this.getLocation().x + 1, TaskPanel.this.getLocation().y);
        }

    }

    public class LeftAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param arg0
         */
        public LeftAction() {
            super("moveLeft");
            putValue(SHORT_DESCRIPTION, "move the task panel left");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }

        public void actionPerformed(ActionEvent evt) {
            if (TaskPanel.this.process == null) {
                return;
            }
            TaskPanel.this.setLocation(TaskPanel.this.getLocation().x - 1, TaskPanel.this.getLocation().y);
        }

    }

    public class DownAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param arg0
         */
        public DownAction() {
            super("moveDown");
            putValue(SHORT_DESCRIPTION, "move the task panel down");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }

        public void actionPerformed(ActionEvent evt) {
            if (TaskPanel.this.process == null) {
                return;
            }
            TaskPanel.this.setLocation(TaskPanel.this.getLocation().x, TaskPanel.this.getLocation().y + 1);
        }

    }

    private class TaskPanelContainsProcessMouseListener implements MouseListener {
        I_EncodeBusinessProcess process;

        /**
         * @param process
         */
        public TaskPanelContainsProcessMouseListener(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        /**
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent ev) {
            if (ev.getClickCount() == 2) {
                try {
                    handleTaskDoubleClickInTaskProcess();
                } catch (Exception e) {
                    ProcessBuilderPanel.getLogWithAlerts().alertAndLogException(e);
                }
            }

        }

        /**
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent ev) {
        }

        /**
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent ev) {
        }

        /**
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent ev) {
        }

        /**
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent ev) {
        }

    }

    private I_SupportDragAndDrop dndBean;

    private I_DefineTask task;

    private JLabel[] conditions;

    boolean newInstanceOnDrop;

    private boolean movingComponent = false;

    private Point offset;

    private I_EncodeBusinessProcess process;

    private JLabel nameLabel;

    private File source;

    private I_Work worker;

    private JLabel idLabel;

    private boolean taskIsProcess = false;

    private I_HandleDoubleClickInTaskProcess doubleClickHandler;

    /**
     * @throws ClassNotFoundException
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * 
     */
    public TaskPanel(I_DefineTask task, boolean allowDrag, boolean newInstanceOnDrop, I_EncodeBusinessProcess process,
            File source, I_Work worker, I_HandleDoubleClickInTaskProcess doubleClickHandler)
            throws ClassNotFoundException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        super(new GridBagLayout());
        this.doubleClickHandler = doubleClickHandler;
        this.addComponentListener(new TaskComponentListener());
        this.setBackground(Color.WHITE);
        this.setFocusable(true);
        this.addFocusListener(this);
        this.task = task;
        this.worker = worker;
        this.newInstanceOnDrop = newInstanceOnDrop;
        this.process = process;
        this.dndBean = new BpaDragAndDropBean("Task", this, false, allowDrag);
        this.conditions = new JLabel[task.getConditions().size()];
        this.source = source;
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        BeanInfo taskInfo = task.getBeanInfo();
        String nameForLabel = taskInfo.getBeanDescriptor().getDisplayName();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Processing task " + taskInfo.getBeanDescriptor().getName() + " (" + this.getId() + ")");
        }
        if (I_DescribeBusinessProcess.class.isAssignableFrom(task.getClass())) {
            taskIsProcess = true;
        }
        Image iconImage = taskInfo.getIcon(BeanInfo.ICON_COLOR_32x32);
        Icon theIcon = null;
        if (iconImage != null) {
            theIcon = new ImageIcon(iconImage);
        }
        this.nameLabel = new JLabel(nameForLabel, theIcon, JLabel.CENTER);
        this.add(this.nameLabel, c);

        c.anchor = GridBagConstraints.NORTHWEST;
        if (task.getId() != -1) {

            c.gridy++;
            c.gridx = 0;
            idLabel = new JLabel("id: " + task.getId());
            this.add(idLabel, c);
            if (this.process != null) {
                this.setCurrentTask(task.getId() == this.process.getCurrentTaskId());
            }

            if (taskIsProcess) {
                c.gridx = 1;
                this.add(new JLabel("<html><font color='blue'>process", JLabel.RIGHT), c);
                this.addMouseListener(new TaskPanelContainsProcessMouseListener((I_EncodeBusinessProcess) task));
            }

            addProperties(task, taskInfo, process, c);

            addConditions(task, taskInfo, process, c);

        } else {
            this.setCurrentTask(false);
        }
        if (this.process != null) {
            if (this.getId() != -1) {
                if (this.process.getTaskBounds(this.getId()) != null) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("reading bounds for task " + this.getId() + " from process: "
                            + this.process.getTaskBounds(this.getId()));
                    }
                    this.setBounds(this.process.getTaskBounds(this.getId()));
                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Process has no bounds for task id: " + this.getId());
                    }
                }
            }
        }
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "moveUp");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        this.getActionMap().put("moveUp", new UpAction());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "moveDown");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        this.getActionMap().put("moveDown", new DownAction());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "moveRight");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        this.getActionMap().put("moveRight", new RightAction());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "moveLeft");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        this.getActionMap().put("moveLeft", new LeftAction());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        this.getActionMap().put("deleteTask", new DeleteAction());
    }

    private void handleTaskDoubleClickInTaskProcess() throws PropertyVetoException, Exception {
        if (doubleClickHandler == null) {
            ProcessPanel panel = new ProcessPanel((I_EncodeBusinessProcess) task, worker, null);
            new FrameWithOpenFramesListener("Embedded Process Viewer: " + ((I_EncodeBusinessProcess) task).getName(),
                "Workflow Bundle", new JScrollPane(panel));
        } else {
            doubleClickHandler.handle((I_EncodeBusinessProcess) task, worker, process);
        }
    }


    /**
     * @param task
     * @param taskInfo
     * @param process
     * @param c
     */
    private void addProperties(I_DefineTask task, BeanInfo taskInfo, I_EncodeBusinessProcess process, GridBagConstraints c) {
        
        PropertyDescriptor[] properties = taskInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            try {
                PropertyDescriptor prop = properties[i];
                Object taskToEdit = this.task;
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(prop.getClass())) {
                    PropertyDescriptorWithTarget descWithObj = (PropertyDescriptorWithTarget) prop;
                    taskToEdit = descWithObj.getTarget();
                }
                PropertyEditor editor = prop.createPropertyEditor(new TargetAndProcessForEditor(process, taskToEdit));
                if (editor != null) {
                    if (I_NeedPropertyDisplayName.class.isAssignableFrom(editor.getClass())) {
                        I_NeedPropertyDisplayName propNameEditor = (I_NeedPropertyDisplayName) editor;
                        propNameEditor.setPropertyDisplayName(prop.getDisplayName());
                    }
                    Method readMethod = prop.getReadMethod();
                    Object value = readMethod.invoke(taskToEdit, (Object[]) null);
                    editor.setValue(value);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Set editor value for property " + prop.getDisplayName() + " to: " + value);
                        logger.finer("Get editor value for property " + prop.getDisplayName() + " returns: "
                            + editor.getValue());
                        logger.finer("Editor: " + editor);
                    }

                    editor.addPropertyChangeListener(new EditorGlue(editor, prop.getWriteMethod(), taskToEdit));
                    editor.addPropertyChangeListener(this);
                    if (I_OnlyWantOneLine.class.isAssignableFrom(editor.getClass())) {
                        c.gridy++;
                        c.gridx = 0;
                        c.gridwidth = 1;
                        c.anchor = GridBagConstraints.NORTHWEST;
                        JLabel label = new JLabel(prop.getDisplayName());
                        label.setToolTipText(prop.getShortDescription());
                        this.add(label, c);
                        c.gridx = 1;
                        c.gridwidth = 1;
                        c.anchor = GridBagConstraints.NORTHWEST;
                        this.add(editor.getCustomEditor(), c);

                    } else {
                        c.gridy++;
                        c.gridx = 0;
                        c.gridwidth = 2;
                        c.anchor = GridBagConstraints.CENTER;
                        JLabel label = new JLabel(prop.getDisplayName());
                        label.setToolTipText(prop.getShortDescription());
                        this.add(label, c);
                        c.gridy++;
                        c.gridx = 0;
                        c.gridwidth = 2;
                        c.anchor = GridBagConstraints.NORTHWEST;
                        this.add(editor.getCustomEditor(), c);
                    }
                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Null editor for property " + prop.getDisplayName());
                    }
                }
            } catch (Throwable t) {
                // Need to also catch errors which can occur through the use of reflection (eg NoSuchMethodError)                
                String errMsg = "Error processing task '" + taskInfo.getBeanDescriptor().getName() + "' in process '" + process.getName() + "'.";
                new LogWithAlerts(this.getClass().getName()).alertAndLog(Level.SEVERE, errMsg, t);
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * @param task
     * @param process
     * @param c
     * @throws ClassNotFoundException
     */
    private void addConditions(I_DefineTask task, BeanInfo taskInfo, I_EncodeBusinessProcess process,
            GridBagConstraints c) throws ClassNotFoundException {
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        JLabel header = new JLabel("   Condition   ", JLabel.CENTER);
        header.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
        this.add(header, c);
        c.gridx = 1;
        header = new JLabel("   Task Id   ", JLabel.CENTER);
        header.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
        this.add(header, c);

        c.anchor = GridBagConstraints.WEST;
        Iterator<Condition> conditionItr = task.getConditions().iterator();
        int i = 0;
        while (conditionItr.hasNext()) {
            Condition condition = conditionItr.next();
            JLabel label = new JLabel(condition.toString() + ": ", JLabel.RIGHT);
            this.conditions[i++] = label;
            c.gridy++;
            c.weightx = 0;
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.anchor = GridBagConstraints.EAST;
            this.add(label, c);
            if (condition.isBranchCondition()) {

                try {
                    I_DefineTask taskForCondition = this.process.getTaskForCondition(this.getId(), condition);
                    label = new TaskIdPanel(taskForCondition.getId(), process);
                    label.addPropertyChangeListener("id", new SetBranchForCondition(this.getId(), condition, process));
                } catch (NoBranchForConditionException e) {
                    label = new TaskIdPanel(-1, process);
                    label.addPropertyChangeListener("id", new SetBranchForCondition(this.getId(), condition, process));
                }
                label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                label.addPropertyChangeListener("id", this);
            } else {
                label = new JLabel("<html>&Oslash");
            }
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.anchor = GridBagConstraints.WEST;
            this.add(label, c);
        }
    }

    /**
     * @return
     */
    public int getAcceptableActions() {
        return dndBean.getAcceptableActions();
    }

    /**
     * @param highlight
     */
    public void highlightForDrag(boolean highlight) {
        if (highlight) {
            this.setBackground(UIManager.getColor("Tree.selectionBackground"));
        } else {
            this.setBackground(Color.WHITE);
        }
    }

    /**
     * @param highlight
     */
    public void highlightForDrop(boolean highlight) {
        this.setBackground(Color.white);
    }

    /**
     * @return
     */
    public boolean isDragging() {
        return dndBean.isDragging();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#getImportDataFlavors()
     */
    public DataFlavor[] getImportDataFlavors() {
        return TaskTransferable.getImportFlavors();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForImport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForImport(DataFlavor flavor) {
        return TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getImportFlavors());
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForExport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        return TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getExportFlavors());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object task, DropTargetDropEvent ev) {
        throw new UnsupportedOperationException();

    }

    /**
     * @throws ClassNotFoundException
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getTaskToDrop()
     */
    public I_DefineTask getTaskToDrop() throws ClassNotFoundException {
        try {
            if (newInstanceOnDrop) {
                if (this.source != null) {
                    FileInputStream fis = new FileInputStream(this.source);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_DefineTask newTask = (I_DefineTask) ois.readObject();
                    ois.close();
                    return newTask;
                }
                Constructor<? extends I_DefineTask> c = this.task.getClass().getConstructor(new Class[] {});
                I_DefineTask newTask = c.newInstance(new Object[] {});
                return newTask;

            }
            return this.task;
        } catch (Exception ex) {
            throw new ClassNotFoundException("Unable to construct class secondary to:", ex);
        }
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getJComponent()
     */
    public JComponent getJComponent() {
        return this;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object task) {
        throw new UnsupportedOperationException();

    }

    /**
     * @return
     */
    public int getId() {
        return this.task.getId();
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent evt) {
        if (this.taskIsProcess == false) {
            if (evt.getClickCount() == 2) { // Double-click
                showTaskDoc();
            } else if (evt.getClickCount() == 3) { // Triple-click
                showTaskDoc();
            }
        }
    }

    private void showTaskDoc() {
        try {
            Class<?> taskClass = this.getTask().getClass();

            BeanList beanList = (BeanList) taskClass.getAnnotation(BeanList.class);
            for (Spec spec : beanList.specs()) {
                String userDir = System.getProperty("user.dir");
                String moduleLoc = "/doc/html/Task_Library/autogenerated/" + spec.directory() + "/"
                    + taskClass.getSimpleName() + ".html";
                File taskDocFile = new File(userDir + moduleLoc);
                if (taskDocFile.exists()) {
                    PlatformWebBrowser.openURL(taskDocFile.toURI().toURL());
                    logger.log(Level.INFO, "task doc file exists: " + taskDocFile);
                } else {
                    logger.log(Level.WARNING, "task doc file does NOT exist: " + taskDocFile);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        this.movingComponent = false;
        this.offset = null;
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        this.requestFocus();
        if (e.isShiftDown() && this.process != null) {
            this.dndBean.resetRecognizer();
            if (this.contains(e.getPoint())) {
                this.movingComponent = true;
                this.offset = e.getPoint();
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if (this.movingComponent) {
            Point oldLocation = this.getLocation();
            Point parentLocation = SwingUtilities.convertPoint(this, e.getPoint(), this.getParent());
            this.setLocation(parentLocation.x - offset.x, parentLocation.y - offset.y);
            this.process.setTaskBounds(this.getId(), this.getBounds());
            this.firePropertyChange("taskLocation", oldLocation, this.getLocation());
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {

    }

    /**
     * @return Returns the task.
     */
    public I_DefineTask getTask() {
        return task;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt != null) && (evt.getPropertyName() != null)) {
            if (evt.getPropertyName().equals("id")) {
                this.firePropertyChange("branch", null, null);
            }
            if (evt.getPropertyName().equals("value")) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Resetting size");
                        }
                        TaskPanel.this.setSize(TaskPanel.this.getPreferredSize());
                    }
                });
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Property changed: " + evt.getPropertyName() + " new value: " + evt.getNewValue());
            }
        } else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Property changed: " + evt);
            }
        }
    }

    public int getConditionIndex(Condition c) {
        for (int i = 0; i < this.conditions.length; i++) {
            if (this.conditions[i].getText().startsWith(c.toString())) {
                return i;
            }
        }
        return -1;

    }

    public Point getBranchExitPoint(I_Branch branch) {
        int index = this.getConditionIndex(branch.getCondition());
        if (index != -1) {
            Rectangle labelBounds = this.conditions[index].getBounds();
            int x = this.getX() + labelBounds.x + this.getWidth();
            int y = this.getY() + labelBounds.y + (labelBounds.height / 2);
            return new Point(x, y);
        }
        return new Point(0, 0);

    }

    public Point getBranchEntrancePoint() {
        Rectangle nameBounds = this.nameLabel.getBounds();
        int x = this.getX() + nameBounds.x + this.getWidth();
        int y = this.getY() + nameBounds.y + (nameBounds.height / 2);
        return new Point(x, y);
    }

    /**
     * @param b
     */
    public void setCurrentTask(boolean currentTask) {
        if (this.idLabel != null) {
            if (currentTask) {
                this.idLabel.setForeground(Color.RED);
            } else {
                this.idLabel.setForeground(Color.BLACK);
            }
            this.idLabel.revalidate();
            this.revalidate();
        }

    }

    public Transferable getTransferable() throws ClassNotFoundException {
        return new TaskTransferable(this.getTaskToDrop());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getLocalFlavors()
     */
    public Collection<DataFlavor> getLocalFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getLocalTaskFlavor());
        return flavorList;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getSerialFlavors()
     */
    public Collection<DataFlavor> getSerialFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getSerialTaskFlavor());
        return flavorList;
    }

    public void focusGained(FocusEvent arg0) {
        this.setBackground(UIManager.getColor("Tree.selectionBackground"));
        this.revalidate();
    }

    public void focusLost(FocusEvent arg0) {
        this.setBackground(Color.WHITE);
        this.revalidate();
    }

    public Logger getLogger() {
        return logger;
    }

}
