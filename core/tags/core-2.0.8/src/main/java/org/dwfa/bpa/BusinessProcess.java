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

package org.dwfa.bpa;

import java.awt.Image;
import java.awt.Rectangle;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.bpa.gui.SimpleMessageRenderer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_ManageVetoableProperties;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.PropertySpec;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.PropertySpec.SourceType;
import org.dwfa.bpa.tasks.deadline.SetDeadlineRelative;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.ObjectEditor;
import org.dwfa.bpa.tasks.util.ChangeProcessInstanceId;

/**
 * @author kec
 * 
 */
public class BusinessProcess implements I_EncodeBusinessProcess,
        VetoableChangeListener {

    public class AttachmentGlue {
        private String attachmentKey;

        /**
         * @param key
         */
        public AttachmentGlue(String key) {
            super();
            attachmentKey = key;
        }

        public Object read() {
            return BusinessProcess.this.readAttachement(attachmentKey);
        }

        public void write(Object attachment) {
            BusinessProcess.this.writeAttachment(attachmentKey, attachment);
        }

        public Method getReadMethod() throws SecurityException,
                NoSuchMethodException {
            return this.getClass().getMethod("read", new Class[] {});
        }

        public Method getWriteMethod() throws SecurityException,
                NoSuchMethodException {
            return this.getClass().getMethod("write",
                    new Class[] { Object.class });
        }
    }

    public class ProcessBeanInfo extends ProcessAllPropertyBeanInfo {

        public ProcessBeanInfo() {
            super();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
         */
        public PropertyDescriptor[] getPropertyDescriptors() {
            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            for (PropertyDescriptor d : super.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                PropertySpec propSpec;
                if (I_DefineTask.class.isAssignableFrom(dwt.getTarget()
                        .getClass())) {
                    I_DefineTask t = (I_DefineTask) dwt.getTarget();
                    propSpec = new PropertySpec(PropertySpec.SourceType.TASK, t
                            .getId(), d.getName());

                } else if (I_ContainData.class.isAssignableFrom(dwt.getTarget()
                        .getClass())) {
                    I_ContainData dc = (I_ContainData) dwt.getTarget();
                    propSpec = new PropertySpec(
                            PropertySpec.SourceType.DATA_CONTAINER, dc.getId(),
                            dc.getDescription());

                } else {
                    propSpec = new PropertySpec(
                            PropertySpec.SourceType.ATTACHMENT, -1, d.getName());
                }

                if (isPropertyExternal(propSpec)) {
                    propertyDescriptorList.add(d);
                }
            }
            return propertyDescriptorList
                    .toArray(new PropertyDescriptor[propertyDescriptorList
                            .size()]);
        }

    }

    public class ProcessAllPropertyBeanInfo extends SimpleBeanInfo {

        public ProcessAllPropertyBeanInfo() {
            super();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getAdditionalBeanInfo()
         */
        public BeanInfo[] getAdditionalBeanInfo() {
            return super.getAdditionalBeanInfo();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
         */
        public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor bd = new BeanDescriptor(BusinessProcess.class);
            bd.setDisplayName("<html><font color='green'><u><center>" + name);
            return bd;
        }

        /**
         * @see java.beans.SimpleBeanInfo#getDefaultEventIndex()
         */
        public int getDefaultEventIndex() {
            return super.getDefaultEventIndex();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getDefaultPropertyIndex()
         */
        public int getDefaultPropertyIndex() {
            return super.getDefaultPropertyIndex();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getEventSetDescriptors()
         */
        public EventSetDescriptor[] getEventSetDescriptors() {
            return super.getEventSetDescriptors();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getIcon(int)
         */
        public Image getIcon(int arg0) {
            return super.getIcon(arg0);
        }

        /**
         * @see java.beans.SimpleBeanInfo#getMethodDescriptors()
         */
        public MethodDescriptor[] getMethodDescriptors() {

            return super.getMethodDescriptors();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
         */
        public PropertyDescriptor[] getPropertyDescriptors() {
            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            Iterator<I_DefineTask> taskItr = getTasks().iterator();
            while (taskItr.hasNext()) {
                I_DefineTask task = taskItr.next();
                try {
                    PropertyDescriptor[] taskPropDescs = task.getBeanInfo()
                            .getPropertyDescriptors();
                    if (taskPropDescs != null) {
                        for (int i = 0; i < taskPropDescs.length; i++) {
                            PropertyDescriptor processPropDesc = new PropertyDescriptorWithTarget(
                                    taskPropDescs[i].getName(), task);
                            processPropDesc
                                    .setBound(taskPropDescs[i].isBound());
                            processPropDesc.setConstrained(taskPropDescs[i]
                                    .isConstrained());
                            processPropDesc.setDisplayName(taskPropDescs[i]
                                    .getDisplayName());
                            processPropDesc.setExpert(taskPropDescs[i]
                                    .isExpert());
                            processPropDesc.setHidden(taskPropDescs[i]
                                    .isHidden());
                            processPropDesc.setName(taskPropDescs[i].getName());
                            processPropDesc.setPreferred(taskPropDescs[i]
                                    .isPreferred());
                            processPropDesc
                                    .setPropertyEditorClass(taskPropDescs[i]
                                            .getPropertyEditorClass());
                            processPropDesc.setReadMethod(taskPropDescs[i]
                                    .getReadMethod());
                            processPropDesc.setWriteMethod(taskPropDescs[i]
                                    .getWriteMethod());
                            processPropDesc
                                    .setShortDescription(taskPropDescs[i]
                                            .getShortDescription());
                            propertyDescriptorList.add(processPropDesc);

                        }
                    }
                } catch (IntrospectionException e) {
                    logger.log(Level.SEVERE, "Processing " + task.getName(), e);
                }
            }
            for (I_ContainData dc : getDataContainers()) {
                try {
                    PropertyDescriptor processPropDesc = new PropertyDescriptorWithTarget(
                            dc.getDescription(), dc, dc.getReadMethod()
                                    .getName(), dc.getWriteMethod().getName());
                    processPropDesc.setBound(false);
                    processPropDesc.setConstrained(false);
                    processPropDesc.setDisplayName(dc.getDescription());
                    processPropDesc.setExpert(false);
                    processPropDesc.setHidden(false);
                    processPropDesc.setName(dc.getDescription());
                    processPropDesc.setPreferred(true);
                    processPropDesc.setPropertyEditorClass(dc.getEditorClass());
                    processPropDesc.setReadMethod(dc.getReadMethod());
                    processPropDesc.setWriteMethod(dc.getWriteMethod());
                    processPropDesc.setShortDescription("Data container: "
                            + dc.getDescription());
                    propertyDescriptorList.add(processPropDesc);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
            }
            for (Map.Entry<String, ?> entry : attachments.entrySet()) {
                try {
                    String desc = entry.getKey();
                    AttachmentGlue glue = new AttachmentGlue(desc);
                    PropertyDescriptor processPropDesc = new PropertyDescriptorWithTarget(
                            desc, glue, glue.getReadMethod().getName(), glue
                                    .getWriteMethod().getName());
                    processPropDesc.setBound(false);
                    processPropDesc.setConstrained(false);
                    processPropDesc.setDisplayName(desc);
                    processPropDesc.setExpert(false);
                    processPropDesc.setHidden(false);
                    processPropDesc.setName(desc);
                    processPropDesc.setPreferred(true);
                    Object value = readAttachement(desc);
                    if (value == null) {
                        processPropDesc.setPropertyEditorClass(null);
                    } else {
                        Class<?> valueClass = value.getClass();
                        if (Boolean.class.isAssignableFrom(valueClass)) {
                            processPropDesc
                                    .setPropertyEditorClass(CheckboxEditor.class);
                        } else if (String.class.isAssignableFrom(valueClass)) {
                            processPropDesc
                                    .setPropertyEditorClass(JTextFieldEditorOneLine.class);
                        } else {
                            processPropDesc
                                    .setPropertyEditorClass(ObjectEditor.class);
                        }

                    }

                    processPropDesc.setReadMethod(glue.getReadMethod());
                    processPropDesc.setWriteMethod(glue.getWriteMethod());
                    processPropDesc.setShortDescription("Attachment: " + desc);
                    propertyDescriptorList.add(processPropDesc);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
            }
            return propertyDescriptorList
                    .toArray(new PropertyDescriptor[propertyDescriptorList
                            .size()]);
        }

        /**
         * @see java.beans.SimpleBeanInfo#loadImage(java.lang.String)
         */
        public Image loadImage(String arg0) {
            return super.loadImage(arg0);
        }

    }

    private static Logger logger = Logger
            .getLogger(I_EncodeBusinessProcess.class.getName());

    protected List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();

    private List<I_ContainData> dataContainerList = new ArrayList<I_ContainData>();

    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(
            this);

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(
            this);

    private Condition exitCondition;

    private transient Condition[] values;

    private transient List<Condition> conditions;

    private ProcessID processID;

    private int id = -1;

    private String name;

    private int currentTaskId = 0;

    // version 2
    private Date deadline = new Date(Long.MAX_VALUE);

    private Priority priority = Priority.NORMAL;

    private String destination;

    // version 3
    private int lastTaskId = -1;

    // version 4

    private String originator;

    private String subject;

    // version 5
    private Map<String, Object> attachments = new HashMap<String, Object>();

    // version 6
    private HashSet<PropertySpec> externalProperties = new HashSet<PropertySpec>();

    // version 7
    private int firstTaskId = 0;
    
    // version 8
    private boolean continueUntilComplete = false;

    // transient and static
    private transient I_DefineTask lastTaskAdded;

    private transient I_DefineTask lastTaskRemoved;

    private transient boolean addDefaultTasks;

	private I_RenderMessage renderer;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 9;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(BusinessProcess.dataVersion);
        out.writeObject(this.taskInfoList);
        out.writeObject(this.dataContainerList);
        out.writeObject(this.exitCondition);
        out.writeObject(this.processID);
        out.writeInt(this.id);
        out.writeObject(this.name);
        out.writeInt(this.getCurrentTaskId());
        out.writeObject(this.deadline);
        out.writeObject(this.priority);
        out.writeObject(this.destination);
        out.writeInt(this.lastTaskId);
        out.writeObject(this.originator);
        out.writeObject(this.subject);
        out.writeObject(this.attachments);
        out.writeObject(this.externalProperties);
        out.writeInt(this.firstTaskId);
        out.writeBoolean(this.continueUntilComplete);
        out.writeObject(this.renderer);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if ((objDataVersion > 0) && (objDataVersion <= dataVersion)) {
            this.taskInfoList = (List<TaskInfo>) in.readObject();
            this.dataContainerList = (List<I_ContainData>) in.readObject();
            this.changeSupport = new PropertyChangeSupport(this);
            this.vetoSupport = new VetoableChangeSupport(this);
            this.exitCondition = (Condition) in.readObject();
            this.values = new Condition[] { exitCondition };
            this.conditions = Collections.unmodifiableList(Arrays
                    .asList(values));
            if (objDataVersion < 7) {
                this.processID = new ProcessID((UUID) in.readObject());
            } else {
                this.processID = (ProcessID) in.readObject();
            }
            this.id = in.readInt();
            this.name = (String) in.readObject();
            this.setCurrentTaskId(in.readInt());
            if ((objDataVersion > 0) && (objDataVersion <= dataVersion)) {
                this.deadline = (Date) in.readObject();
                this.priority = (Priority) in.readObject();
                this.destination = (String) in.readObject();
            } else {
                this.deadline = new Date(Long.MAX_VALUE);
                this.priority = Priority.NORMAL;
                this.destination = (String) in.readObject();
            }
            if ((objDataVersion > 1) && (objDataVersion <= dataVersion)) {
                this.lastTaskId = in.readInt();
            } else {
                this.lastTaskId = -1;
            }
            if ((objDataVersion > 3) && (objDataVersion <= dataVersion)) {
                this.originator = (String) in.readObject();
                this.subject = (String) in.readObject();
            } else {
                this.originator = null;
                this.subject = null;
            }
            if ((objDataVersion > 4) && (objDataVersion <= dataVersion)) {
                this.attachments = (Map<String, Object>) in.readObject();
            } else {
                this.attachments = new HashMap<String, Object>();
            }
            if ((objDataVersion > 5) && (objDataVersion <= dataVersion)) {
                this.externalProperties = (HashSet<PropertySpec>) in
                        .readObject();
            } else {
                this.externalProperties = new HashSet<PropertySpec>();
            }
            if ((objDataVersion > 6) && (objDataVersion <= dataVersion)) {
                this.firstTaskId = in.readInt();
            } else {
                this.firstTaskId = 0;
            }
            if ((objDataVersion > 7) && (objDataVersion <= dataVersion)) {
                this.continueUntilComplete = in.readBoolean();
            } else {
                this.continueUntilComplete = false;
            }
            if ((objDataVersion > 8) && (objDataVersion <= dataVersion)) {
                this.renderer = (I_RenderMessage) in.readObject();
            } else {
                this.renderer = null;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * /**
     * 
     * @param processID
     *            The processID to set.
     */
    public void setProcessID(ProcessID processID) {
        ProcessID oldValue = this.processID;
        this.processID = processID;
        this.changeSupport.firePropertyChange(PROCESS_ID, oldValue,
                this.processID);

    }

    /**
     * @param processID
     * @param id
     * @param name
     */
    public BusinessProcess(String name, Condition exitCondition) {
        this(name, exitCondition, true);
    }

    public BusinessProcess() {
        this(null, Condition.CONTINUE, false);
    }

    public BusinessProcess(String name, Condition exitCondition,
            boolean addDefaultTasks) {
        super();
        this.processID = new ProcessID(UUID.randomUUID());
        this.name = name;
        this.exitCondition = exitCondition;
        this.values = new Condition[] { exitCondition };
        this.conditions = Collections.unmodifiableList(Arrays.asList(values));
        this.addDefaultTasks = addDefaultTasks;
        if (addDefaultTasks) {
            try {
                ChangeProcessInstanceId changeId = new ChangeProcessInstanceId();
                this.addTask(changeId);
                SetDeadlineRelative deadline = new SetDeadlineRelative();
                this.addTask(deadline);
                this.addBranch(changeId, deadline, Condition.CONTINUE);
            } catch (PropertyVetoException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public String getProcessIdStr() {
        return this.processID.toString();
    }

    public void setProcessIdStr(String idStr) {
        this.setProcessID(new ProcessID(UUID.fromString(idStr)));
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getPrerequisites()
     */
    public Set<?> getPrerequisites() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return conditions;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#execute(org.dwfa.bpa.process.I_Work)
     */
    public Condition execute(I_Work worker) throws TaskFailedException {
        @SuppressWarnings("unused")
        Condition condition = this.evaluate(this, worker);
        this.complete(this, worker);
        return condition;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        if (worker.isExecutionStopFlagged()) {
            return Condition.ITEM_CANCELED;
        }
        worker.getProcessStack().push(this);
        I_DefineTask currentTask = this.getTask(this.getCurrentTaskId());
        Condition condition = Condition.CONTINUE;
        I_DefineTask branchTask = null;
        while ((currentTask != null)
                && (condition.equals(Condition.STOP) == false)
                && (condition.equals(Condition.PROCESS_COMPLETE) == false)
                && (condition.equals(Condition.WAIT_FOR_WEB_FORM) == false)) {
            if (worker.isExecutionStopFlagged()) {
                return Condition.ITEM_CANCELED;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(process.getProcessID() + ": " + process.getCurrentTaskId()
                		+ "/" + currentTask.getId()
                        + " " + currentTask.getName() + " start");

            }
            try {
                condition = currentTask.evaluate(this, worker);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(process.getProcessID() + ": " 
                            + process.getCurrentTaskId()
                    		+ "/" + currentTask.getId() + " " + currentTask.getName()
                            + " evaluate: " + condition);

                }
                if (continueUntilComplete) {
                	 if (condition.equals(Condition.STOP_THEN_REPEAT)) {
                         if (logger.isLoggable(Level.FINE)) {
                             logger.fine(process.getProcessID() + ": " 
                                     + process.getCurrentTaskId()
                             		+ "/" + currentTask.getId() + " " + currentTask.getName()
                                     + " Continue for Stop then Repeat ");
                         }
                		 condition = Condition.CONTINUE;
                	 }
                }
                if (condition.isBranchCondition()) {
                    branchTask = getTaskForCondition(this.getCurrentTaskId(),
                            condition);
                    if (branchTask == null) {
                        TaskFailedException ex = new TaskFailedException(
                                worker.getWorkerDesc()
                                        + "Malformed process. Branch missing for condition: "
                                        + condition + " on task "
                                        + currentTask.getName() + " ("
                                        + currentTask.getId() + ")");
                        logger.throwing(this.getName(), "evaluate", ex);
                        throw ex;
                    }
                    this.setCurrentTaskId(branchTask.getId());
                }
                ExecutionRecord er = new ExecutionRecord(new Date(),
                        currentTask.getId(), worker.getId(), worker
                                .getWorkerDesc(), condition);
                TaskInfo info = this.taskInfoList.get(currentTask.getId());
                info.addExecutionRecord(er);
                currentTask.complete(this, worker);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(worker.getWorkerDesc() + "; process: "
                            + process.getName() + " (" + process.getProcessID()
                            + ") " + " Current task: " + currentTask.getId() 
                            + " " + currentTask.getName() + " : " + condition
                            + " Next task: " + process.getCurrentTaskId());

                }
                currentTask = branchTask;
                branchTask = null;
                if (continueUntilComplete) {
                		if (condition.equals(Condition.STOP)) {
                			condition = Condition.CONTINUE;
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(process.getProcessID() + ": " 
                                        + process.getCurrentTaskId()
                                		+ "/" + currentTask.getId() + " " + currentTask.getName()
                                        + " Continue for Stop");
                            }
                		}
                }
            } catch (TaskFailedException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new TaskFailedException(ex);
            }
        }
        if (condition.equals(Condition.PROCESS_COMPLETE)) {
            // Check to see if this process is embedded in another
            if (worker.getProcessStack().size() > 1) {
                // If so, reset the process so subsequent executions restart
                this.setCurrentTaskId(this.getFirstTaskId());
            }
            return this.exitCondition;
        }
        
        if (condition.equals(Condition.WAIT_FOR_WEB_FORM)) {
            // Check to see if this process is embedded in another
            if (worker.getProcessStack().size() > 1) {
                // If so, reset the process so subsequent executions restart
                this.setCurrentTaskId(this.getFirstTaskId());
            }
            return Condition.WAIT_FOR_WEB_FORM;
        }
        return Condition.STOP_THEN_REPEAT;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    	if (worker.getProcessStack().empty() == false) {
            worker.getProcessStack().pop();
    	}
    }

    /**
     * @throws NoBranchForConditionException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTaskForCondition(int,
     *      org.dwfa.bpa.process.Condition)
     */
    public I_DefineTask getTaskForCondition(int id, Condition condition)
            throws NoBranchForConditionException {
        TaskInfo info = this.taskInfoList.get(id);
        I_DefineTask task = this.getTask(info.getTaskIdForCondition(condition));
        addThisToVetoListeners(task);
        return task;
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public I_DefineTask addTask(I_DefineTask task) throws PropertyVetoException {
        task.setId(taskInfoList.size());
        this.taskInfoList.add(new TaskInfo(task, null, null, null));
        addThisToVetoListeners(task);
        setLastTaskAdded(task);
        return task;
    }

    public void removeTask(I_DefineTask task) throws PropertyVetoException {
        TaskInfo info = this.taskInfoList.get(task.getId());
        if ((info.getExecutionRecords() != null)
                && (info.getExecutionRecords().size() > 0)) {
            throw new PropertyVetoException("This task has execution records",
                    null);
        }
        // Remove references to this task;
        Iterator<TaskInfo> taskInfoItr = this.taskInfoList.iterator();
        while (taskInfoItr.hasNext()) {
            info = taskInfoItr.next();
            if (info != null) {
                ListIterator<Branch> branchItr = info.branches.listIterator();
                while (branchItr.hasNext()) {
                    Branch branch = branchItr.next();
                    if (branch.getDestinationId() == task.getId()) {
                        branchItr.remove();
                    }
                }
            }
        }
        this.taskInfoList.set(task.getId(), null);
        this.setLastTaskRemoved(task);
        if (this.currentTaskId == task.getId()) {
            this.setCurrentTaskId(-1);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTasks()
     */
    public Collection<I_DefineTask> getTasks() {
        List<I_DefineTask> taskList = new ArrayList<I_DefineTask>(
                this.taskInfoList.size());
        for (TaskInfo ti : this.taskInfoList) {
            if (ti != null) {
                addThisToVetoListeners(ti.getTask());
                taskList.add(ti.getTask());
            }
        }
        return Collections.unmodifiableCollection(taskList);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTask(int)
     */
    public I_DefineTask getTask(int taskId) {
        TaskInfo ti = this.taskInfoList.get(taskId);
        addThisToVetoListeners(ti.getTask());
        return ti.getTask();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getProcessID()
     */
    public ProcessID getProcessID() {
        return this.processID;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getRootTask()
     */
    public I_DefineTask getRootTask() {
        return this.getTask(0);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addBranch(org.dwfa.bpa.process.I_DefineTask,
     *      org.dwfa.bpa.process.I_DefineTask, org.dwfa.bpa.process.Condition)
     */
    public synchronized void addBranch(I_DefineTask origin,
            I_DefineTask destination, Condition condition) {
        TaskInfo ti = this.taskInfoList.get(origin.getId());
        ti.addBranch(destination, condition);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Added branch. \nOrigin:" + origin + "\ndestination: "
                    + destination + "\ncondition: " + condition
                    + "\nTaskInfo: " + ti);

        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Resulting process:\n" + this);

        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#removeBranch(org.dwfa.bpa.process.I_DefineTask,
     *      org.dwfa.bpa.process.I_DefineTask, org.dwfa.bpa.process.Condition)
     */
    public synchronized void removeBranch(I_DefineTask origin,
            I_DefineTask destination, Condition condition) {
        TaskInfo ti = this.taskInfoList.get(origin.getId());
        ti.removeBranch(destination, condition);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getBranches(org.dwfa.bpa.process.I_DefineTask)
     */
    public Collection<Branch> getBranches(I_DefineTask from) {
        TaskInfo ti = this.taskInfoList.get(from.getId());
        if (ti != null) {
            return ti.getBranches();
        }
        return new ArrayList<Branch>();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getExecutionRecords()
     */
    public Collection<ExecutionRecord> getExecutionRecords() {
        List<ExecutionRecord> records = new ArrayList<ExecutionRecord>();
        for (Iterator<TaskInfo> allInfoItr = this.taskInfoList.iterator(); allInfoItr
                .hasNext();) {
            TaskInfo ti = allInfoItr.next();
            if (ti != null) {
                records.addAll(ti.getExecutionRecords());
            }
        }
        return records;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getExecutionRecords(int)
     */
    public Collection<ExecutionRecord> getExecutionRecords(int taskId) {
        TaskInfo ti = this.taskInfoList.get(taskId);
        return ti.getExecutionRecords();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getId()
     */
    public int getId() {
        return this.id;
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.changeSupport.getPropertyChangeListeners();
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(propertyName, listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(propertyName, listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        return this.changeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {

        // TODO Auto-generated method stub

    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_DefineTask#setId(int)
     */
    public void setId(int id) throws PropertyVetoException {
        int oldId = this.id;
        this.vetoSupport.fireVetoableChange("id", oldId, id);
        this.id = id;
        this.changeSupport.firePropertyChange("id", oldId, this.id);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setTaskBounds(int,
     *      java.awt.Rectangle)
     */
    public void setTaskBounds(int id, Rectangle bounds) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Changing task " + id + " bounds to: " + bounds);
        }
        TaskInfo ti = this.taskInfoList.get(id);
        ti.setBounds(bounds);

    }

    public void setTaskInfo(TaskInfo ti) throws PropertyVetoException {
        try {
            if (ti == null) {
                this.taskInfoList.add(null);
            } else if (ti.getTask().getId() == -1) {
                ti.getTask().setId(this.taskInfoList.size());
                this.taskInfoList.add(ti);
            } else {
                if (this.taskInfoList.size() == ti.getTask().getId()) {
                    this.taskInfoList.add(ti);
                } else {
                    this.taskInfoList.set(ti.getTask().getId(), ti);
                }
            }
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            this.taskInfoList.add(null);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTaskBounds(int)
     */
    public Rectangle getTaskBounds(int id) {
        TaskInfo ti = this.taskInfoList.get(id);
        return ti.getBounds();
    }

    /**
     * @param currentTaskId
     *            The currentTaskId to set.
     */
    public void setCurrentTaskId(int currentTaskId) {
        this.lastTaskId = this.currentTaskId;
        this.currentTaskId = currentTaskId;
        this.changeSupport.firePropertyChange("currentTaskId", this.lastTaskId,
                this.currentTaskId);
    }

    /**
     * @return Returns the currentTaskId.
     */
    public int getCurrentTaskId() {
        return currentTaskId;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getNumberOfTasks()
     */
    public int getNumberOfTasks() {
        return this.taskInfoList.size();
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addDataContainer(org.dwfa.bpa.process.I_ContainData)
     */
    public synchronized I_ContainData addDataContainer(I_ContainData data)
            throws PropertyVetoException {
        this.vetoSupport.fireVetoableChange("description", data
                .getDescription(), data.getDescription());
        data.setId(this.dataContainerList.size());
        this.dataContainerList.add(data);
        addThisToVetoListeners(data);
        return data;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDataContainer(int)
     */
    public I_ContainData getDataContainer(int id) {
        I_ContainData dataContainer = this.dataContainerList.get(id);
        addThisToVetoListeners(dataContainer);
        return dataContainer;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDataContainerBounds(int,
     *      java.awt.Rectangle)
     */
    public void setDataContainerBounds(int id, Rectangle bounds) {
        I_ContainData dataContainer = this.getDataContainer(id);
        dataContainer.setDataContainerBounds(bounds);

    }

    /**
     * @param objToListenTo
     */
    private void addThisToVetoListeners(I_ManageVetoableProperties objToListenTo) {
        objToListenTo.addVetoableChangeListener("id", this);
        objToListenTo.addVetoableChangeListener("description", this);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDataContainers()
     */
    public List<I_ContainData> getDataContainers() {
        Iterator<I_ContainData> dataItr = this.dataContainerList.iterator();
        while (dataItr.hasNext()) {
            I_ContainData dataContainer = dataItr.next();
            addThisToVetoListeners(dataContainer);
        }
        return Collections.unmodifiableList(this.dataContainerList);
    }

    /**
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
     */
    public void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {
        if (evt.getPropertyName().equals("id")) {
            throw new PropertyVetoException(
                    "Component id cannot be changed once associated with a process.",
                    evt);
        } else if (evt.getPropertyName().equals("description")) {
            Iterator<I_ContainData> dataItr = this.dataContainerList.iterator();
            while (dataItr.hasNext()) {
                I_ContainData data = dataItr.next();
                if (data.getDescription().equals(evt.getNewValue())) {
                    throw new PropertyVetoException(
                            "Description is not unique compared to other data containers: ",
                            evt);
                }
            }
            for (String key : this.getAttachmentKeys()) {
                if (key.equals(evt.getNewValue())) {
                    throw new PropertyVetoException(
                            "Description is not unique compared to attachments: ",
                            evt);
                }
            }

        }

    }

    /**
     * @param listener
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    public void addVetoableChangeListener(String propertyName,
            VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(propertyName, listener);
    }

    /**
     * @return
     */
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vetoSupport.getVetoableChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     */
    public VetoableChangeListener[] getVetoableChangeListeners(
            String propertyName) {
        return vetoSupport.getVetoableChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     */
    public boolean hasListeners(String propertyName) {
        return vetoSupport.hasListeners(propertyName);
    }

    /**
     * @param listener
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    public void removeVetoableChangeListener(String propertyName,
            VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDeadline(java.util.Date)
     */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDeadline()
     */
    public Date getDeadline() {
        return this.deadline;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setPriority(org.dwfa.bpa.process.Priority)
     */
    public void setPriority(Priority priority) {
        Priority old = this.priority;
        this.priority = priority;
        this.changeSupport.firePropertyChange("priority", old, this.priority);

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getPriority()
     */
    public Priority getPriority() {
        return this.priority;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDestination(java.lang.String)
     */
    public void setDestination(String address) {
        String old = this.destination;
        this.destination = address;
        this.changeSupport.firePropertyChange("destination", old,
                this.destination);

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDestination()
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getLastTaskId()
     */
    public int getLastTaskId() {
        return this.lastTaskId;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#isLoggable(java.util.logging.Level)
     */
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getLogger()
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return Returns the originator.
     */
    public String getOriginator() {
        return originator;
    }

    /**
     * @param originator
     *            The originator to set.
     */
    public void setOriginator(String originator) {
        String old = this.originator;
        this.originator = originator;
        this.changeSupport.firePropertyChange("originator", old, this.name);
    }

    /**
     * @return Returns the subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            The subject to set.
     */
    public void setSubject(String subject) {
        String old = this.subject;
        this.subject = subject;
        this.changeSupport.firePropertyChange("subject", old, this.priority);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#readAttachement(java.lang.String)
     */
    public Object readAttachement(String key) {
        return this.attachments.get(key);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#writeAttachment(java.lang.String,
     *      java.lang.Object)
     */
    public void writeAttachment(String key, Object attachment) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("writing attachment: " + key + " value:" + attachment);
        }
        Object oldValue = this.readAttachement(key);
        this.attachments.put(key, attachment);
        this.changeSupport.firePropertyChange(key, oldValue, attachment);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#takeAttachment(java.lang.String)
     */
    public Object takeAttachment(String key) {
        return this.attachments.remove(key);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getAttachmentKeys()
     */
    public Collection<String> getAttachmentKeys() {
        return Collections.unmodifiableCollection(this.attachments.keySet());
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setName(java.lang.String)
     */
    public void setName(String name) throws PropertyVetoException {
        String oldName = this.name;
        this.vetoSupport.fireVetoableChange("name", oldName, name);
        this.name = name;
        this.changeSupport.firePropertyChange("name", oldName, this.name);

    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Name: ");
        buff.append(this.name);
        buff.append("\nId as task: ");
        buff.append(this.id);
        buff.append("\nInstance id: ");
        buff.append(this.processID);
        buff.append("\nSubject: ");
        buff.append(this.subject);
        buff.append("\nOriginator: ");
        buff.append(this.originator);
        buff.append("\nPriority: ");
        buff.append(this.priority);
        buff.append("\nCurrent task: ");
        buff.append(this.currentTaskId);
        buff.append("\nLast task: ");
        buff.append(this.lastTaskId);
        buff.append("\nDeadline: ");
        buff.append(this.deadline);
        buff.append("\nDestination: ");
        buff.append(this.destination);
        Iterator<TaskInfo> taskItr = this.taskInfoList.iterator();
        while (taskItr.hasNext()) {
            TaskInfo info = taskItr.next();
            buff.append("\nTask: ");
            buff.append(info);
        }
        Iterator<I_ContainData> dataItr = this.dataContainerList.iterator();
        while (dataItr.hasNext()) {
            I_ContainData data = dataItr.next();
            buff.append("\nData: ");
            buff.append(data);
        }

        return buff.toString();
    }

    /**
     * @param process
     */
    public void validateAddresses() throws TaskFailedException {
        validateAddresses(this);

    }

    /**
     * @param process
     */
    public void validateDestination() throws TaskFailedException {
        validateAddress(this.destination, this.getProcessID());

    }

    public static void validateAddresses(I_DescribeBusinessProcess processDesc)
            throws TaskFailedException {
        try {
            validateAddress(processDesc.getDestination(), processDesc
                    .getProcessID());
        } catch (TaskFailedException e) {
            throw new TaskFailedException("Invalid Destination: "
                    + processDesc.getDestination(), e);
        }
        try {
            validateAddress(processDesc.getOriginator(), processDesc
                    .getProcessID());
        } catch (TaskFailedException e) {
            throw new TaskFailedException("Invalid Originator: "
                    + processDesc.getOriginator(), e);
        }
    }

    public static void validateAddress(String address, ProcessID processID)
            throws TaskFailedException {
        if ((address == null) || address.length() < 6) {
            throw new TaskFailedException("malformed address: " + address
                    + " for process: " + processID);
        }
    }

    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new ProcessBeanInfo();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#isPropertyExternal(org.dwfa.bpa.process.PropertySpec)
     */
    public boolean isPropertyExternal(PropertySpec spec) {
        return this.externalProperties.contains(spec);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setPropertyExternal(org.dwfa.bpa.process.PropertySpec,
     *      boolean)
     */
    public void setPropertyExternal(PropertySpec spec, boolean external) {
        if (external) {
            this.externalProperties.add(spec);
        } else {
            this.externalProperties.remove(spec);
        }

    }

    public void setPropertyExternal(String propertyName, String sourceType,
            int id, boolean external) {
        PropertySpec spec = new PropertySpec(SourceType.valueOf(sourceType),
                id, propertyName);
        this.setPropertyExternal(spec, external);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getAllPropertiesBeanInfo()
     */
    public BeanInfo getAllPropertiesBeanInfo() throws IntrospectionException {
        return new ProcessAllPropertyBeanInfo();
    }

    /**
     * @return Returns the lastTaskAdded.
     */
    public I_DefineTask getLastTaskAdded() {
        return lastTaskAdded;
    }

    /**
     * @param lastTaskAdded
     *            The lastTaskAdded to set.
     */
    protected void setLastTaskAdded(I_DefineTask lastTaskAdded) {
        Object prevTask = this.lastTaskAdded;
        this.lastTaskAdded = lastTaskAdded;
        this.changeSupport.firePropertyChange("lastTaskAdded", prevTask,
                this.lastTaskAdded);
    }

    /**
     * @return Returns the lastTaskRemoved.
     */
    public I_DefineTask getLastTaskRemoved() {
        return lastTaskRemoved;
    }

    /**
     * @param lastTaskRemoved
     *            The lastTaskRemoved to set.
     */
    protected void setLastTaskRemoved(I_DefineTask lastTaskRemoved) {
        Object prevTask = this.lastTaskRemoved;
        this.lastTaskRemoved = lastTaskRemoved;
        this.changeSupport.firePropertyChange("lastTaskRemoved", prevTask,
                this.lastTaskRemoved);
    }

    /**
     * @return Returns the exitCondition.
     */
    public Condition getExitCondition() {
        return exitCondition;
    }

    /**
     * @return Returns the addDefaultTasks.
     */
    public boolean getAddDefaultTasks() {
        return addDefaultTasks;
    }

    /**
     * @param exitCondition
     *            The exitCondition to set.
     */
    public void setExitCondition(Condition exitCondition) {
        this.exitCondition = exitCondition;
    }

    /**
     * @return Returns the taskInfoList.
     */
    public List<TaskInfo> getTaskInfoList() {
        return taskInfoList;
    }

    /**
     * @return Returns the externalProperties.
     */
    public HashSet<PropertySpec> getExternalProperties() {
        return externalProperties;
    }

    /**
     * @return Returns the firstTaskId.
     */
    public int getFirstTaskId() {
        return firstTaskId;
    }

    /**
     * @param firstTaskId
     *            The firstTaskId to set.
     */
    public void setFirstTaskId(int firstTaskId) {
        this.firstTaskId = firstTaskId;
    }

    public InputStream getStreamFromURL(URL locator) throws IOException {
        String protocol = locator.getProtocol().toLowerCase();
        String host  = locator.getHost().toLowerCase();
        if (protocol.equals("file") && (host.equals("attachment.bp")
                || host.equals("property.bp"))) {
            Object obj = this.getObjectFromURL(locator);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return new ByteArrayInputStream(bos.toByteArray());
        } else if (protocol.equals("file")) {
            return new FileInputStream(locator.getFile());
        }
        return locator.openStream();
    }

    public Object getObjectFromURL(URL locator) throws IOException {
        String protocol = locator.getProtocol().toLowerCase();
        String host  = locator.getHost().toLowerCase();
        if (protocol.equals("file") && host.equals("attachment.bp")) {
        	String path = locator.getPath();
        	if (path.startsWith("/")) {
        		path = path.substring(1);
        	}
            return this.readAttachement(path);
        } else if (protocol.equals("file") && host.equals("property.bp")) {
            int taskId = new Integer(locator.getAuthority());
            I_DefineTask task = this.getTask(taskId);
        	String path = locator.getPath();
        	if (path.startsWith("/")) {
        		path = path.substring(1);
        	}
            try {
                BeanInfo taskInfo = task.getBeanInfo();

                for (PropertyDescriptor d : taskInfo.getPropertyDescriptors()) {
                    if (d.getName().equals(path)) {
                        return d.getReadMethod().invoke(task, new Object[] {});
                    }
                }
                throw new IOException("Can't locate object for: "
                        + path + " from URL: " + locator);
            } catch (Exception e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        } else if (protocol.equals("file") && host.equals("dataContainer.bp")) {
            String dcName = locator.getPath();
       	if (dcName.startsWith("/")) {
        		dcName = dcName.substring(1);
        	}
           for (I_ContainData dc : this.dataContainerList) {
                if (dc.getDescription().equals(dcName)) {
                    try {
                        return dc.getData();
                    } catch (Exception e) {
                        IOException ioe = new IOException(e.getMessage());
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }

        } else if (protocol.equals("file")) {
            BufferedInputStream bis = new BufferedInputStream(locator.openStream());
            ObjectInputStream ois = new ObjectInputStream(bis);
            try {
                return ois.readObject();
            } catch (Exception e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }

        }
        return locator.getContent();
    }

    public UUID getObjectID() {
        return this.getProcessID().getUuid();
    }

    public Object readProperty(String propertyLabel)
            throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        try {
            BeanInfo info = this.getAllPropertiesBeanInfo();
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) pd;
                if (pdwt.getLabel().equals(propertyLabel)) {
                    Method readMethod = pdwt.getReadMethod();
                    return readMethod.invoke(pdwt.getTarget(), new Object[] {});
                }
            }
            if (propertyLabel.startsWith("A: ")) {
            	String propertyName = propertyLabel.substring(3);
            	return readAttachement(propertyName);
            } else if (attachments.containsKey(propertyLabel)) {
            	return readAttachement(propertyLabel);
            }
            throw new IntrospectionException("Property not found: "
                    + propertyLabel);
        } catch (IllegalArgumentException ex) {
            IntrospectionException ie = new IntrospectionException(ex
                    .getMessage());
            ie.initCause(ex);
            throw ie;
        }
    }

    public void setProperty(String propertyLabel, Object value)
            throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        try {
            BeanInfo info = this.getAllPropertiesBeanInfo();
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) pd;
                if (pdwt.getLabel().equals(propertyLabel)) {
                    Method writeMethod = pdwt.getWriteMethod();
                    writeMethod.invoke(pdwt.getTarget(),
                            new Object[] { value });
                    return;
                }
            }
            if (propertyLabel.startsWith("A: ")) {
            	String propertyName = propertyLabel.substring(3);
            	this.writeAttachment(propertyName, value);
            	return;
            } 
            throw new IntrospectionException("Property not found: "
                    + propertyLabel);
        } catch (IllegalArgumentException ex) {
            IntrospectionException ie = new IntrospectionException(ex
                    .getMessage());
            ie.initCause(ex);
            throw ie;
        }
    }

	public boolean getContinueUntilComplete() {
		return continueUntilComplete;
	}

	public void setContinueUntilComplete(boolean continueUntilComplete) {
		this.continueUntilComplete = continueUntilComplete;
	}

	public void setMessageRenderer(I_RenderMessage renderer) {
		this.renderer = renderer;
		
	}

	public I_RenderMessage getMessageRenderer() {
      if (renderer == null) {
    	 String[] standardMessageAttachments = new String[] { "MESSAGE", "HTML_INSTRUCTION", "HTML_DETAIL", "SIGNPOST_HTML",
    			 "INSTRUCTION_STR", };
         for (String key: standardMessageAttachments) {
            if (attachments.containsKey(key) && attachments.get(key) != null) {
               return new SimpleMessageRenderer((String) attachments.get(key));
            }
         }
      }
		return this.renderer;
	}
}
